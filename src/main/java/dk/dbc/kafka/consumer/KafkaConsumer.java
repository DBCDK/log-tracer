/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.consumer;

import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.io.UncheckedIOException;
import java.time.Duration;
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

public class KafkaConsumer implements Consumer {
    final Properties kafkaProperties;
    final String topicName;
    final LogEventMapper logEventMapper;
    Long fromEpochMilli;

    public KafkaConsumer(String hostname, Integer port, String topicName, String groupId, String offset, String clientID) {
        this.topicName = topicName;
        this.kafkaProperties = createKafkaProperties(hostname, port, groupId, offset, clientID);
        this.logEventMapper = new LogEventMapper();
    }

    public void setFromDateTime(OffsetDateTime dateTime) {
        if (dateTime != null) {
            this.fromEpochMilli = dateTime.toInstant().toEpochMilli();
        }
    }

    @Override
    public Iterator<LogEvent> iterator() {
        return new Iterator<LogEvent>() {
            final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka =
                    new org.apache.kafka.clients.consumer.KafkaConsumer<>(kafkaProperties);
            {
                subscribe(kafka, topicName);
            }
            
            final PriorityQueue<ConsumedItem> consumedItems =
                    new PriorityQueue<>(1200, new ConsumedItemComparator());

            @Override
            public boolean hasNext() {
                if (consumedItems.isEmpty() || consumedItems.size() < 400) {
                    kafka.poll(Duration.ofMillis(3000)).forEach(record
                            -> consumedItems.add(new ConsumedItem(record)));
                }
                return !consumedItems.isEmpty();
            }

            @Override
            public LogEvent next() {
                final ConsumedItem consumedItem = consumedItems.poll();
                if (consumedItem != null) {
                    return consumedItem.toLogEvent();
                }
                return null;
            }
        };
    }

    private Properties createKafkaProperties(String hostname, Integer port, String groupId, String offset, String clientID) {
        final Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", hostname + ":" + port);
        properties.setProperty("group.id", groupId);
        properties.setProperty("client.id", clientID); // UUID.randomUUID().toString()
        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        properties.put("auto.offset.reset", offset);  // The consumer can starts from the beginning of the topic or the end
        properties.put("max.poll.records", "800");
        return properties;
    }

    private void subscribe(
            org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka,
            String topicName) {
        if (fromEpochMilli != null) {
            seekToOffsetsForTimestamp(kafka, topicName, fromEpochMilli);
        } else {
            kafka.subscribe(Collections.singletonList(topicName));
        }
    }

    private void seekToOffsetsForTimestamp(
            org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka,
            String topicName, Long timestamp) {

        // Get all partitions for topic.
        final List<PartitionInfo> topicPartitionInfo = kafka.partitionsFor(topicName);

        // Look up the offsets for all partitions such that the returned offset for
        // each partition is the earliest offset whose timestamp is greater than or
        // equal to the given fromEpochMilli.
        final Map<TopicPartition, Long> fromOffsets =
                getOffsetsForTimestamp(kafka, topicName, topicPartitionInfo, timestamp);

        seekToOffsets(kafka, fromOffsets);
    }

    Map<TopicPartition, Long> getOffsetsForTimestamp(
            final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka,
            final String topicName,
            final List<PartitionInfo> topicPartitionInfo,
            final Long timestamp) {

        final Map<TopicPartition, Long> partitionsTimestamp = new HashMap<>();
        for (PartitionInfo pi : topicPartitionInfo) {
            partitionsTimestamp.put(new TopicPartition(topicName, pi.partition()), timestamp);
        }

        final Map<TopicPartition, OffsetAndTimestamp> topicPartitionOffsetAndTimestampMap =
                kafka.offsetsForTimes(partitionsTimestamp);

        // Map <TopicPartition, OffsetAndTimestamp> to <TopicPartition, Long>.
        return topicPartitionOffsetAndTimestampMap.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().offset()));
    }

    private void seekToOffsets(final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka,
                               final Map<TopicPartition, Long> offsets) {
        System.err.println("partitions and offsets: " + offsets);
        kafka.assign(offsets.keySet());
        for (Map.Entry<TopicPartition, Long> entry : offsets.entrySet()) {
            if (entry.getValue() != null) {
                kafka.seek(entry.getKey(), entry.getValue());
            }
        }
    }

    class ConsumedItem {
        private final LogEvent logEvent;

        ConsumedItem(ConsumerRecord<String, byte[]> consumerRecord) {
            this.logEvent = toLogEvent(consumerRecord);
        }

        private LogEvent toLogEvent(ConsumerRecord<String, byte[]> consumerRecord) {
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

    static class ConsumedItemComparator implements Comparator<ConsumedItem> {
        @Override
        public int compare(ConsumedItem a, ConsumedItem b) {
            long byTimestamp = a.toLogEvent().getTimestamp().toInstant().toEpochMilli()
                    - b.toLogEvent().getTimestamp().toInstant().toEpochMilli();
            return Long.signum(byTimestamp);
        }
    }
}