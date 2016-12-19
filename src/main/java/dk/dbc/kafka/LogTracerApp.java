package dk.dbc.kafka;


import org.apache.commons.cli.CommandLine;

import java.util.Arrays;
import java.util.UUID;

/**
 * Log tracer
 */
public class LogTracerApp
{

    public static void main(String[] args )
    {
        System.out.println("Hello from Log tracer");
        System.out.println(Arrays.toString(args));

        Cli cli = null;
        CommandLine cmdLine = null;
        try {
            cli = new Cli(args);
            cmdLine = cli.parse();
        }catch (Exception e){
            e.printStackTrace();
            if (cli != null) {
                cli.showHelp();
               // System.exit(0);
            }
        }
        Consumer consumer = new Consumer();
            try {
                String hostname = cmdLine.hasOption("hostname") ? ((String)cmdLine.getParsedOptionValue("hostname")) : "localhost";
                String port = cmdLine.hasOption("port") ? ((Number)cmdLine.getParsedOptionValue("port")).toString() : "2081";
                String topic = cmdLine.hasOption("topic") ? ((String)cmdLine.getParsedOptionValue("topic")) : "test";
                consumer.readKafkaTopics(hostname, port, topic, UUID.randomUUID().toString());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Log Tracer could not retrieve records from Kafka topic. ");
              //  System.exit(0);

            }
/*        System.out.println("Goodbye from Log tracer");
        return false;*/
    }
}
