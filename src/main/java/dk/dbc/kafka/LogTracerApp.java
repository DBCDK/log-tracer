package dk.dbc.kafka;


import org.apache.commons.cli.CommandLine;


import java.util.UUID;
import java.util.logging.Logger;

/**
 * Log tracer
 */
public class LogTracerApp
{
    private static Logger LOGGER = Logger.getLogger("LogTracerApp");

    public static void main(String[] args)
    {
        LOGGER.info("Log tracer has been started");

        Cli cliParser = null;
        CommandLine cmdLine = null;
        try {
            cliParser = new Cli(args);
            cmdLine = cliParser.parse();
        }catch (Exception e){
            e.printStackTrace();
            if (cliParser != null) {
                cliParser.showHelp();
              //  System.exit(0);
            }
        }
        if(cmdLine != null) {
            Consumer consumer = new Consumer();
            try {
                String hostname = cmdLine.hasOption("hostname") ? ((String) cmdLine.getParsedOptionValue("hostname")) : "localhost";
                String port = cmdLine.hasOption("port") ? ((Number) cmdLine.getParsedOptionValue("port")).toString() : "2081";
                String topic = cmdLine.hasOption("topic") ? ((String) cmdLine.getParsedOptionValue("topic")) : "test";
                String offset = cmdLine.hasOption("offset") ? ((String) cmdLine.getParsedOptionValue("offset")) : "earliest";
                String clientID = cmdLine.hasOption("clientid") ? ((String) cmdLine.getParsedOptionValue("clientid")) : UUID.randomUUID().toString();
                consumer.readLogEventsFromTopic(hostname, port, topic, UUID.randomUUID().toString(), offset, clientID, 0);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.severe("Log Tracer could not retrieve records from Kafka topic. ");
            }
        }
    }
}
