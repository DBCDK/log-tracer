package dk.dbc.kafka;

import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.event.Level;

import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private OffsetDateTime start, end;
    private String appID, host, env;
    private Level loglevel;

    public void setRelevantPeriod(Date start, Date end) {
        this.start = OffsetDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault());
        this.end = OffsetDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Consume kafka topics
     *
     * @param hostname
     * @param port
     * @param topicName
     * @param groupId
     * @param offset             The consumer can starts from the beginning of the topic or the end ["latest", "earliest"]
     * @param clientID           identify the consumer
     * @param maxNumberOfRecords
     * @return
     */
    public List<LogEvent> readLogEventsFromTopic(String hostname, String port, String topicName, String groupId, String offset, String clientID, int maxNumberOfRecords) {

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
            boolean relevant;

            while (recordIterator.hasNext()) {
                ConsumerRecord<Integer, byte[]> record = recordIterator.next();
                try {
                    logEvent = logEventMapper.unmarshall(record.value());
                } catch (UncheckedIOException e) {
                    System.out.println(e.toString());
                    continue;
                }
                relevant = true;

                //  filter events!
                if ((this.start != null && logEvent.getTimestamp().isBefore(this.start)) || (this.end != null && logEvent.getTimestamp().isAfter(this.end))) {
                    relevant = false;
                }

                if (this.appID != null && !this.appID.isEmpty() && !logEvent.getAppID().equalsIgnoreCase(this.appID)) {
                    relevant = false;
                }

                if (this.env != null && !this.env.isEmpty() && !logEvent.getEnv().equalsIgnoreCase(this.env)) {
                    relevant = false;
                }

                if (this.host != null && !this.host.isEmpty() && !logEvent.getHost().equalsIgnoreCase(this.host)) {
                    relevant = false;
                }

                if (this.loglevel != null && logEvent.getLevel().toInt() < this.loglevel.toInt()){ // 0, 10, 20, 30, 40
                    relevant = false;
                }

                if (relevant) {
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
        //consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put("auto.offset.reset", offset);  // The consumer can starts from the beginning of the topic or the end
        return consumerProps;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Level getLoglevel() {
        return loglevel;
    }

    public void setLogLevel(Level logLevel) {
        this.loglevel = logLevel;
    }
}