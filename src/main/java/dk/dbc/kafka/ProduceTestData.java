package dk.dbc.kafka;

/**
 * Created by andreas on 12/8/16.
 */

import dk.dbc.kafka.logformat.LogEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;


public class ProduceTestData {
    private static int counter = 0;
    private static String [] environment = {"dev", "test", "stage", "prod"} ;
    private static String [] hostname_examples = {"mesos-node-1", "mesos-node-2", "mesos-node-3", "oldfaithfull"} ;
    private static String [] appid_examples = {"smooth-sink", "wild-webapp", "terrific-transformer", "dashing-database"} ;


    /**
     * Generate random test data
     * @param hostname kafkahost
     * @param port kafkaport
     * @param topicName name of the topic
     * @throws Exception
     */
    public void produceTestData(String hostname, String port, String topicName )throws Exception {


        //Configure the ProduceTestData
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hostname + ":" + port);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

        org.apache.kafka.clients.producer.Producer producer = new KafkaProducer(configProperties);

        Thread thread = new Thread(){
            public void run(){

                while(true) {
                    try {
                        LogEvent logEvent = createDummyObject(counter++);
                        ProducerRecord<String, String> rec = new ProducerRecord<String, String>(topicName,logEvent.toJSON());
                        System.out.println("Generating log event. " + logEvent.toJSON());
                        producer.send(rec);
                        sleep(500);
                    } catch (InterruptedException e) {
                        producer.close();
                        System.out.println("### Generated " + counter + " message(s)");
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();

    }


    private static LogEvent createDummyObject(int id){
        LogEvent logEvent = new LogEvent();
        int rand = (int) (Math.random()*4);
        logEvent.setAppID(appid_examples[rand]);
        logEvent.setHost(hostname_examples[rand]);
        logEvent.setEnv(environment[rand]);
        logEvent.setLevel(Level.INFO.getName());
        logEvent.setMsg("This is an auto generated log message. Its number " + id);
        logEvent.setTimestamp(new Date());
        return logEvent;
    }
}