package dk.dbc.kafka;

import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

public class Consumer implements Iterable<LogEvent> {
    private final Properties kafkaConsumerProperties;
    private final String topicName;
    private final LogEventMapper logEventMapper;

    public Consumer(String hostname, String port, String topicName, String groupId, String offset, String clientID) {
        this.topicName = topicName;
        this.kafkaConsumerProperties = createKafkaConsumerProperties(hostname, port, groupId, offset, clientID);
        this.logEventMapper = new LogEventMapper();
    }

    @Override
    public Iterator<LogEvent> iterator() {
        return new Iterator<LogEvent>() {
            final KafkaConsumer<Integer, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
            {
                kafkaConsumer.subscribe(Collections.singletonList(topicName));
            }
            ConsumerRecords<Integer, byte[]> records;
            Iterator<ConsumerRecord<Integer, byte[]>> recordsIterator;

            @Override
            public boolean hasNext() {
                if (recordsIterator == null || !recordsIterator.hasNext()) {
                    records = kafkaConsumer.poll(3000);
                    recordsIterator = records.iterator();
                }
                return recordsIterator.hasNext();
            }

            @Override
            public LogEvent next() {
                final ConsumerRecord<Integer, byte[]> record = recordsIterator.next();
                LogEvent logEvent;
                try {
                    logEvent = logEventMapper.unmarshall(record.value());
                } catch (UncheckedIOException e) {
                    // log exception??
                    logEvent = new LogEvent();
                }
                logEvent.setRaw(record.value());
                return logEvent;
            }
        };
    }

    private Properties createKafkaConsumerProperties(String hostname, String port, String groupId, String offset, String clientID) {
        final Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", hostname + ":" + port);
        consumerProps.setProperty("group.id", groupId);
        consumerProps.setProperty("client.id", clientID); // UUID.randomUUID().toString()
        consumerProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProps.put("auto.offset.reset", offset);  // The consumer can starts from the beginning of the topic or the end
        consumerProps.put("max.poll.records", "100");
        return consumerProps;
    }
}