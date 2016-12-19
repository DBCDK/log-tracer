package dk.dbc.kafka;


import org.apache.commons.cli.CommandLine;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Log tracer
 */
public class LogTracerApp
{
    private static Logger LOGGER = Logger.getLogger("LogTracerApp");

    public static void main(String[] args )
    {
        LOGGER.info("Log tracer has been started");

        Cli cli = null;
        CommandLine cmdLine = null;
        try {
            cli = new Cli(args);
            cmdLine = cli.parse();
        }catch (Exception e){
            e.printStackTrace();
            if (cli != null) {
                cli.showHelp();
              //  System.exit(0);
            }
        }
        if(cmdLine != null) {
            Consumer consumer = new Consumer();
            try {
                String hostname = cmdLine.hasOption("hostname") ? ((String) cmdLine.getParsedOptionValue("hostname")) : "localhost";
                String port = cmdLine.hasOption("port") ? ((Number) cmdLine.getParsedOptionValue("port")).toString() : "2081";
                String topic = cmdLine.hasOption("topic") ? ((String) cmdLine.getParsedOptionValue("topic")) : "test";
                consumer.readKafkaTopics(hostname, port, topic, UUID.randomUUID().toString());
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.severe("Log Tracer could not retrieve records from Kafka topic. ");
            }
        }
/*        System.out.println("Goodbye from Log tracer");
        return false;*/
    }
}
