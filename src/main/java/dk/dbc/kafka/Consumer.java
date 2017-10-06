/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka;

import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.stream.Collectors;

public class Consumer implements Iterable<LogEvent> {
    private final Properties kafkaConsumerProperties;
    private final String topicName;
    private final LogEventMapper logEventMapper;
    private long timestamp;

    public Consumer(String hostname, Integer port, String topicName, String groupId, String offset, String clientID) {
        this.topicName = topicName;
        this.kafkaConsumerProperties = createKafkaConsumerProperties(hostname, port, groupId, offset, clientID);
        this.logEventMapper = new LogEventMapper();
    }

    public void setFromDateTime(OffsetDateTime dateTime) {
        this.timestamp = dateTime.toInstant().toEpochMilli();
    }

    @Override
    public Iterator<LogEvent> iterator() {
        return new Iterator<LogEvent>() {
            final KafkaConsumer<Integer, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
            {
                subscribe(kafkaConsumer, topicName, timestamp);
            }
            
            final PriorityQueue<ConsumedItem> consumedItems =
                    new PriorityQueue<>(3000, new ConsumedItemComparator());

            @Override
            public boolean hasNext() {
                if (consumedItems.isEmpty() || consumedItems.size() < 1000) {
                    kafkaConsumer.poll(3000).forEach(record
                            -> consumedItems.add(new ConsumedItem(record)));
                }
                return !consumedItems.isEmpty();
            }

            @Override
            public LogEvent next() {
                return consumedItems.poll().toLogEvent();
            }
        };
    }

    private Properties createKafkaConsumerProperties(String hostname, Integer port, String groupId, String offset, String clientID) {
        final Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", hostname + ":" + port);
        consumerProps.setProperty("group.id", groupId);
        consumerProps.setProperty("client.id", clientID); // UUID.randomUUID().toString()
        consumerProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProps.put("auto.offset.reset", offset);  // The consumer can starts from the beginning of the topic or the end
        consumerProps.put("max.poll.records", "2000");
        return consumerProps;
    }

    private void subscribe(KafkaConsumer<Integer, byte[]> kafkaConsumer, String topicName, long timestamp) {
        if (timestamp > 0) {
            seekToOffsetsForTimestamp(kafkaConsumer, topicName, timestamp);
        } else {
            kafkaConsumer.subscribe(Collections.singletonList(topicName));
        }
    }

    private void seekToOffsetsForTimestamp(KafkaConsumer<Integer, byte[]> kafkaConsumer, String topicName, long timestamp) {
        final List<TopicPartition> topicPartitions = kafkaConsumer.partitionsFor(topicName)
                .stream()
                .map(partitionInfo -> new TopicPartition(topicName, partitionInfo.partition()))
                .collect(Collectors.toList());
        kafkaConsumer.assign(topicPartitions);

        final Map<TopicPartition, Long> request = new HashMap<>();
        topicPartitions.forEach(partition -> request.put(partition, timestamp));
        kafkaConsumer.offsetsForTimes(request).entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> kafkaConsumer.seek(
                        entry.getKey(), entry.getValue().offset()));
    }

    private class ConsumedItem {
        private final ConsumerRecord<Integer, byte[]> consumerRecord;
        private final LogEvent logEvent;

        ConsumedItem(ConsumerRecord<Integer, byte[]> consumerRecord) {
            this.consumerRecord = consumerRecord;
            this.logEvent = toLogEvent(consumerRecord);
        }

        private LogEvent toLogEvent(ConsumerRecord<Integer, byte[]> consumerRecord) {
            LogEvent logEvent;
            try {
                logEvent = logEventMapper.unmarshall(consumerRecord.value());
            } catch (UncheckedIOException e) {
                // log exception??
                logEvent = new LogEvent();
            }
            logEvent.setRaw(consumerRecord.value());
            if (logEvent.getTimestamp() == null) {
                // No timestamp in record value, use kafka timestamp instead
                logEvent.setTimestamp(OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(consumerRecord.timestamp()), ZoneId.systemDefault()));
            }
            return logEvent;
        }

        LogEvent toLogEvent() {
            return logEvent;
        }
    }

    private static class ConsumedItemComparator implements Comparator<ConsumedItem> {
        @Override
        public int compare(ConsumedItem a, ConsumedItem b) {
            long byTimestamp = a.toLogEvent().getTimestamp().toInstant().toEpochMilli()
                    - b.toLogEvent().getTimestamp().toInstant().toEpochMilli();
            return Long.signum(byTimestamp);
        }
    }
}