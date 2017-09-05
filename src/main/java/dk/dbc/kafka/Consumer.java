package dk.dbc.kafka;

import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventFilter;
import dk.dbc.kafka.logformat.LogEventMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by andreas on 12/8/16. Inspired by javaworld 'Big data messaging with Kafka, Part 1'
 */
public class Consumer {
    private static final Logger LOGGER = Logger.getLogger("Consumer");

    private final LogEventMapper logEventMapper = new LogEventMapper();

    /**
     * Consume kafka topics
     */
    public List<LogEvent> readLogEventsFromTopic(String hostname, String port, String topicName, String groupId,
                                                 String offset, String clientID, LogEventFilter logEventFilter) {

        // setup consumer
        Properties consumerProps = createKafkaConsumerProperties(hostname, port, groupId, offset, clientID);

        KafkaConsumer<Integer, byte[]> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(topicName));
        List<LogEvent> output = new ArrayList<LogEvent>();

        // starting consumer
        ConsumerRecords<Integer, byte[]> records = consumer.poll(3000);
        Iterator<ConsumerRecord<Integer, byte[]>> recordIterator = records.iterator();
        if (records.count() == 0) {
            LOGGER.warning("No records found in topic");
            return output;
        } else {
            LOGGER.info("Topic: " + topicName + " Count: " + records.count());
            LogEvent logEvent = null;

            while (recordIterator.hasNext()) {
                ConsumerRecord<Integer, byte[]> record = recordIterator.next();
                try {
                    logEvent = logEventMapper.unmarshall(record.value());
                } catch (UncheckedIOException e) {
                    System.out.println(e.toString());
                    continue;
                }

                if (logEventFilter.test(logEvent)) {
                    System.out.println(logEvent);
                    output.add(logEvent);
                }
            }
        }

        return output;
    }

    private Properties createKafkaConsumerProperties(String hostname, String port, String groupId, String offset, String clientID) {
        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", hostname + ":" + port);
        consumerProps.setProperty("group.id", groupId);
        consumerProps.setProperty("client.id", clientID); // UUID.randomUUID().toString()
        consumerProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProps.put("auto.offset.reset", offset);  // The consumer can starts from the beginning of the topic or the end
        return consumerProps;
    }
}