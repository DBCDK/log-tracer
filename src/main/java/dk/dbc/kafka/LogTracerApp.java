package dk.dbc.kafka;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Log tracer
 *
 */
public class LogTracerApp
{

    public static void main( String[] args )
    {
        System.out.println("Hello from Log tracer");
        System.out.println(Arrays.toString(args));

        Cli cli = new Cli(args);
        CommandLine cmdLine = cli.parse();
        Consumer consumer = new Consumer();
            try {
                String hostname = cmdLine.hasOption("hostname") ? ((String)cmdLine.getParsedOptionValue("hostname")) : "localhost";
                String port = cmdLine.hasOption("port") ? ((Number)cmdLine.getParsedOptionValue("port")).toString() : "2081";
                String topic = cmdLine.hasOption("topic") ? ((String)cmdLine.getParsedOptionValue("topic")) : "test";
                consumer.consumeKafkaTopics(hostname, port, topic , "" + UUID.randomUUID().toString() );
            } catch (Exception e) {
                e.printStackTrace();
            }
        System.out.println("Goodbye from Log tracer");
    }
}
