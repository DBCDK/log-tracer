/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.consumer;

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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class KafkaConsumerTest {
    private static final String ZKHOST = "127.0.0.1";
    private static final String BROKERHOST = "127.0.0.1";
    private static final Integer BROKERPORT = 9091;
    private static final String TOPIC = "test";

    private EmbeddedZookeeper zkServer;
    private ZkClient zkClient;
    private KafkaServer broker;
    private Properties producerProps;

    @Before
    public void prepare() throws IOException {
        // setup Zookeeper
        zkServer = new EmbeddedZookeeper();
        final String zkConnect = ZKHOST + ":" + zkServer.port();
        zkClient = new ZkClient(zkConnect, 30000, 30000, ZKStringSerializer$.MODULE$);
        final ZkUtils zkUtils = ZkUtils.apply(zkClient, false);

        // setup Broker
        final Properties brokerProps = new Properties();
        brokerProps.setProperty("zookeeper.connect", zkConnect);
        brokerProps.setProperty("broker.id", "0");
        brokerProps.setProperty("log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString());
        brokerProps.setProperty("listeners", "PLAINTEXT://" + BROKERHOST +":" + BROKERPORT);
        broker = TestUtils.createServer(new KafkaConfig(brokerProps), SystemTime$.MODULE$);

        AdminUtils.createTopic(zkUtils, TOPIC, 1, 1, new Properties(), RackAwareMode.Disabled$.MODULE$);

        producerProps = new Properties();
        producerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        producerProps.setProperty("key.serializer","org.apache.kafka.common.serialization.IntegerSerializer");
        producerProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
    }

    @After
    public void teardown() {
        try {
            broker.shutdown();
            zkClient.close();
            zkServer.shutdown();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void consume() {
        produceLogEvents();

        final KafkaConsumer consumer = new KafkaConsumer(BROKERHOST, BROKERPORT, TOPIC, UUID.randomUUID().toString(),
                "earliest", "new-test");
        int eventCount = 0;
        for (LogEvent event : consumer) {
            eventCount++;
        }
        assertThat(eventCount, is(9));
    }

    private void produceLogEvents() {
        try (KafkaProducer<Integer, byte[]> producer = new KafkaProducer<>(producerProps)) {
            final ClassLoader classLoader = getClass().getClassLoader();
            final File messages = new File(classLoader.getResource("json-formatted-sample-data.txt").getFile());

            try (Scanner scanner = new Scanner(messages)) {
                while (scanner.hasNextLine()) {
                    final String message = scanner.nextLine();
                    final ProducerRecord<Integer, byte[]> rec =
                            new ProducerRecord<>(TOPIC, message.getBytes(StandardCharsets.UTF_8));
                    producer.send(rec);
                }
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}