package dk.dbc.kafka;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

import kafka.utils.SystemTime$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.*;
import static org.junit.Assert.*;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import kafka.zk.EmbeddedZookeeper;
import org.junit.runners.MethodSorters;


/**
 * File created by andreas on 12/12/16 heavily inspired by the work done by https://github.com/asmaier/mini-kafka/
 * For online documentation see
 * https://github.com/apache/kafka/blob/0.10.0/core/src/test/scala/unit/kafka/utils/TestUtils.scala
 * https://github.com/apache/kafka/blob/0.10.0/core/src/main/scala/kafka/admin/AdminUtils.scala
 * https://github.com/apache/kafka/blob/0.10.0/core/src/main/scala/kafka/utils/ZkUtils.scala
 * http://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html
 * http://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html
 * http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecords.html
 * http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecord.html
 * http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaConsumerProducerTest {

    private static final String ZKHOST = "127.0.0.1";
    private static final String BROKERHOST = "127.0.0.1";
    private static final String BROKERPORT = "9092";
    private static final String TOPIC = "test";
    private static final String TOPIC_JSON = "test_json";
    EmbeddedZookeeper zkServer;
    ZkClient zkClient;
    ZkUtils zkUtils;
    Properties brokerProps;
    KafkaServer kafkaServer;
    Properties producerProps;
    Properties consumerProps;

    @Before
    public void prepare(){

        // setup Zookeeper
        zkServer = new EmbeddedZookeeper();
        String zkConnect = ZKHOST + ":" + zkServer.port();
        zkClient = new ZkClient(zkConnect, 30000, 30000, ZKStringSerializer$.MODULE$);
        zkUtils = ZkUtils.apply(zkClient, false);

        // setup Broker
        brokerProps = new Properties();
        brokerProps.setProperty("zookeeper.connect", zkConnect);
        brokerProps.setProperty("broker.id", "0");
        try {
            brokerProps.setProperty("log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        brokerProps.setProperty("listeners", "PLAINTEXT://" + BROKERHOST +":" + BROKERPORT);
        KafkaConfig config = new KafkaConfig(brokerProps);

        kafkaServer = TestUtils.createServer(config, SystemTime$.MODULE$);  //Time mock = new MockTime();

        // create topic
        AdminUtils.createTopic(zkUtils, TOPIC, 1, 1, new Properties(), RackAwareMode.Disabled$.MODULE$);

        // setup producer
        producerProps = new Properties();
        producerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        producerProps.setProperty("key.serializer","org.apache.kafka.common.serialization.IntegerSerializer");
        producerProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        // setup consumer
        consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        consumerProps.setProperty("group.id", "group0");
        consumerProps.setProperty("client.id", "consumer0");
        consumerProps.setProperty("key.deserializer","org.apache.kafka.common.serialization.IntegerDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProps.put("auto.offset.reset", "earliest");  // to make sure the consumer starts from the beginning of the topic

    }
    /**
     * Rigourous Test
     */
    @Test
    public void testKafkaProducerIT()
    {
        assertTrue( true );
    }

    @Test
    public void testProducer() throws InterruptedException, IOException {

        KafkaProducer<Integer, byte[]> producer = new KafkaProducer<Integer, byte[]>(producerProps);

        KafkaConsumer<Integer, byte[]> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(TOPIC));

        // send message
        ProducerRecord<Integer, byte[]> data = new ProducerRecord<>(TOPIC, 42, "test-message".getBytes(StandardCharsets.UTF_8));
        producer.send(data);
        producer.close();

        // starting consumer
        ConsumerRecords<Integer, byte[]> records = consumer.poll(3000);
        assertEquals(1, records.count());
        Iterator<ConsumerRecord<Integer, byte[]>> recordIterator = records.iterator();
        ConsumerRecord<Integer, byte[]> record = recordIterator.next();
        System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
        assertEquals(42, (int) record.key());
        assertEquals("test-message", new String(record.value(), StandardCharsets.UTF_8));

    }


    @Test
    public void testJsonProducer() throws InterruptedException, IOException {

        KafkaProducer<Integer, byte[]> producer = new KafkaProducer<Integer, byte[]>(producerProps);

        KafkaConsumer<Integer, byte[]> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(TOPIC_JSON));

        // send many messages
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("json-formatted-sample-data.txt").getFile());

        int lines = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lines++;
                String line = scanner.nextLine();
                ProducerRecord<Integer, byte[]> rec = new ProducerRecord<>(TOPIC_JSON, line.getBytes(StandardCharsets.UTF_8));
                producer.send(rec);

            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        producer.close();

        // starting consumer
        ConsumerRecords<Integer, byte[]> records = consumer.poll(3000);
        assertEquals(lines, records.count()); // number of lines in the file are the same as
        Iterator<ConsumerRecord<Integer, byte[]>> recordIterator = records.iterator();

        while(recordIterator.hasNext()){
            ConsumerRecord<Integer, byte[]> record = recordIterator.next();
            System.out.println("(" +record.offset() + ") ##### " + new String(record.value(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @Ignore
    public void testLogTracer(){
        String[] args = new String[3];
        args[0] = "--hostname=" + BROKERHOST;
        args[1] = "--port=" + BROKERPORT;
        args[2] = "--topic=" + TOPIC_JSON;
        LogTracerApp.main(args);
    }


    @After
    public void closeDown(){
        kafkaServer.shutdown();
        zkClient.close();
        zkServer.shutdown();
    }
}