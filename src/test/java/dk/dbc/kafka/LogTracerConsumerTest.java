package dk.dbc.kafka;

import dk.dbc.kafka.logformat.LogEvent;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.SystemTime$;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import kafka.zk.EmbeddedZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
public class LogTracerConsumerTest {

    private static Logger LOGGER = Logger.getLogger("LogTracerConsumerTest");
    private static final String ZKHOST = "127.0.0.1";
    private static final String BROKERHOST = "127.0.0.1";
    private static final String BROKERPORT = "9091";
    private static final String TOPIC = "test";
    private static final String TOPIC_JSON = "test_json";
    private static String pattern = "yyyy-MM-dd'T'HH:mm";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

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


    @Test
    public void testConsumerAllRecords(){

        sendTestLogEventsFromFile();

        Consumer consumer = new Consumer();
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("Prod Log events = (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size()>0);
    }

    @Test
    public void testConsumeProdLogEventsFromSpecificHost(){
        sendTestLogEventsFromFile();

        Consumer consumer = new Consumer();
        consumer.setEnv("prod");
        consumer.setHost("mesos-node-7");
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("Prod Log events = (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size()==2);
    }

    @Test
    public void testConsumeLogEventsFromAppID(){
        sendTestLogEventsFromFile();

        Consumer consumer = new Consumer();
        consumer.setAppID("superapp");
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("Prod Log events = (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size()==5);
    }
    @Test
    public void testConsumeLogEventsFromNonExistingAppID(){
        sendTestLogEventsFromFile();

        Consumer consumer = new Consumer();
        consumer.setAppID("None existing app");
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        assertTrue(output.size()==0);
    }

    @Test
    public void testConsumeLogEventsFromSpecificHost(){
        sendTestLogEventsFromFile();

        Consumer consumer = new Consumer();
        consumer.setHost("mesos-node-7");
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("ConsumeLogEventsFromSpecificHost = (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size()==2);
    }

    @Test
    public void testConsumeProdLogEvents(){
        sendTestLogEventsFromFile();

        Consumer consumer = new Consumer();
        consumer.setEnv("prod");
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("Prod Log events = (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size() == 4);
    }

/*
// TODO Test Log LEVEL
    @Test
    public void testConsumeWarnProdLogEvents(){
        sendTestLogEventsFromFile();

        Consumer consumer = new Consumer();
        consumer.setEnv("prod");
        consumer.setLevel("WARN");
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("Prod Log events = (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size()==1);
    }*/

    @Test
    public void testConsumeRelevantTimePeriodLogEvents() {
        sendTestLogEventsFromFile();
        Consumer consumer = new Consumer();
        try {
            Date start = simpleDateFormat.parse("2017-01-06T00:00");
            Date end = simpleDateFormat.parse("2017-01-08T23:59");
            consumer.setRelevantPeriod(start, end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("testConsumeRelevantTimePeriodLogEvents Relevant logevents= (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size() == 3);
    }


    @Test
    public void testConsumeRelevantTimePeriodeLogEvents() {
        sendTestLogEventsFromFile();
        Consumer consumer = new Consumer();
        try {
            Date start = simpleDateFormat.parse("2017-01-16T00:00");
            Date end = simpleDateFormat.parse("2017-01-25T00:00");
            consumer.setRelevantPeriod(start, end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("testConsumeRelevantTimePeriodLogEvents Relevant logevents= (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size() == 4);
    }


    @Test
    public void testConsumeRelevantTimePeriodeLogEventsNoHits() {
        sendTestLogEventsFromFile();
        Consumer consumer = new Consumer();
        try {
            Date start = simpleDateFormat.parse("2016-01-01T00:00");
            Date end = simpleDateFormat.parse("2016-01-08T23:59");
            consumer.setRelevantPeriod(start, end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<LogEvent> output = consumer.readLogEventsFromTopic(BROKERHOST, BROKERPORT, "logevents", UUID.randomUUID().toString(), "earliest", "new-test", 0);
        LOGGER.info("testConsumeRelevantTimePeriodLogEvents Relevant logevents= (" + output.size() + ")");

        for (LogEvent event : output) {
            LOGGER.info("event = " + event);
        }
        assertTrue(output.size() == 0);
    }


    @Test
    public void testLogTracer() throws ParseException {
        KafkaProducer<Integer, byte[]> producer = new KafkaProducer<>(producerProps);

        // send pre-formatted messages
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("json-formatted-sample-data.txt").getFile());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                ProducerRecord<Integer, byte[]> rec = new ProducerRecord<>(TOPIC_JSON + "_temp_topic", line.getBytes(StandardCharsets.UTF_8));
                producer.send(rec);
            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        producer.close();

        String[] args = new String[3];
        args[0] = "--hostname=" + BROKERHOST;
        args[1] = "--port=" + BROKERPORT;
        args[2] = "--topic=" + TOPIC_JSON + "xx";
        LogTracerApp.runWith(args);
    }


    @Test
    public void testFileOutput() throws ParseException {

        String[] args = new String[4];
        args[0] = "--hostname=" + BROKERHOST;
        args[1] = "--port=" + BROKERPORT;
        args[2] = "--topic=" + TOPIC_JSON;
        args[3] = "--store=" + "fileoutput.json";

        LogTracerApp.runWith(args);

    }

    @Test
    public void testEmptyTopicLogTracer() throws ParseException {
        String[] args = new String[3];
        args[0] = "--hostname=" + BROKERHOST;
        args[1] = "--port=" + BROKERPORT;
        args[2] = "--topic=" + "not_a_topic";
        LogTracerApp.runWith(args);
    }

    @Test
    public void testNoHostnameLogTracer() throws ParseException {
        String[] args = new String[3];
        args[0] = "--port=" + BROKERPORT;
        args[1] = "--topic=" + "not_a_topic";
        LogTracerApp.runWith(args);
    }



    /**
     * Test that tests that the Kafka Test-Utils is okay. Doesn't use LogTracer
     * @throws InterruptedException
     * @throws IOException
     */
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

        KafkaProducer<Integer, byte[]> producer = new KafkaProducer<>(producerProps);

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



    @After
    public void closeDown(){
        kafkaServer.shutdown();
        zkClient.close();
        zkServer.shutdown();
    }

    private static void getAllFiles(File curDir){

        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory())
                getAllFiles(f);
            if (f.isFile()) {
                System.out.println(f.getName());
            }
        }
    }

    private void sendTestLogEventsFromFile() {
        KafkaProducer<Integer, byte[]> producer = new KafkaProducer<>(producerProps);

        // send many messages
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("json-formatted-sample-data.txt").getFile());

        int lines = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lines++;
                String line = scanner.nextLine();
                ProducerRecord<Integer, byte[]> rec = new ProducerRecord<>("logevents", line.getBytes(StandardCharsets.UTF_8));
                producer.send(rec);

            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        producer.close();
    }
}