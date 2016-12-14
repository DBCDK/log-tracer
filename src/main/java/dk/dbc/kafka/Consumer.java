package dk.dbc.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

/**
 * Created by andreas on 12/8/16. Inspired by javaworld 'Big data messaging with Kafka, Part 1'
 */
public class Consumer {
    private static Scanner in;



    public boolean readKafkaTopics(String hostname, String port, String topicName, String groupId){

        // setup consumer
        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", hostname + ":" + port);
        consumerProps.setProperty("group.id", "GGGG" + UUID.randomUUID().toString());
        consumerProps.setProperty("client.id", "GGGG" + UUID.randomUUID().toString());
        consumerProps.setProperty("key.deserializer","org.apache.kafka.common.serialization.IntegerDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        //consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put("auto.offset.reset", "earliest");  // to make sure the consumer starts from the beginning of the topic


        KafkaConsumer<Integer, byte[]> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(topicName));



        // starting consumer
        ConsumerRecords<Integer, byte[]> records = consumer.poll(3000);
        Iterator<ConsumerRecord<Integer, byte[]>> recordIterator = records.iterator();
        if (records.count() == 0) {
            System.err.println("No records found in topic");
            return false;
        } else {
            System.out.println("Topic: " + topicName + " Count: " + records.count());
            while (recordIterator.hasNext()) {
                ConsumerRecord<Integer, byte[]> record = recordIterator.next();
                System.out.println("(" + record.offset() + ") ##### " + new String(record.value(), StandardCharsets.UTF_8));
            }
        }

        return true;
    }
    /**
     * Consume kafka topics
     * @param hostname
     * @param port
     * @param topicName
     * @param groupId
     * @throws Exception
     */
    public void consumeKafkaTopics(String hostname, String port, String topicName, String groupId) throws Exception {

        in = new Scanner(System.in);

        ConsumerThread consumerRunnable = new ConsumerThread(hostname, port, topicName, groupId);
        consumerRunnable.start();
        String line = "";
        while (!line.equals("exit") || line.equals("")) {
            line = in.next();
        }
        consumerRunnable.getKafkaConsumer().wakeup();
        System.out.println("Stopping consumer .....");
        consumerRunnable.join();
    }

    private static class ConsumerThread extends Thread {
        private String hostname;
        private String port;
        private String topicName;
        private String groupId;
        private KafkaConsumer<String, String> kafkaConsumer;

        public ConsumerThread(String hostname, String port, String topicName, String groupId) {
            this.topicName = topicName;
            this.groupId = groupId;
            this.hostname = hostname;
            this.port = port;
        }

        public void run() {
            Properties configProperties = new Properties();
            configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hostname + ":" + port);
            configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
            configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

            //Figure out where to start processing messages from
            kafkaConsumer = new KafkaConsumer<String, String>(configProperties);
            kafkaConsumer.subscribe(Arrays.asList(topicName)); //TODO take a list of topics
            //Start processing messages
            try {
                while (true) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                    for (ConsumerRecord<String, String> record : records)
                        System.out.println(record.value());
                }
            } catch (WakeupException ex) {
                System.out.println("Exception caught " + ex.getMessage());
            } finally {
                kafkaConsumer.close();
                System.out.println("After closing KafkaConsumer");
            }
        }

        public KafkaConsumer<String, String> getKafkaConsumer() {
            return this.kafkaConsumer;
        }
    }
}