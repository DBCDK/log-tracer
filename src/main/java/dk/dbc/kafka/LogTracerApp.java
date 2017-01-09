package dk.dbc.kafka;


import dk.dbc.kafka.logformat.LogEvent;
import org.apache.commons.cli.CommandLine;
import org.slf4j.event.Level;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Log tracer
 */
public class LogTracerApp
{
    private static Logger LOGGER = Logger.getLogger("LogTracerApp");
    private static String pattern = "yyyy-MM-dd'T'HH:mm";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

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

                // Relevant periode
                if(cmdLine.hasOption("data-start") && cmdLine.hasOption("data-end")) {
                    Date start = simpleDateFormat.parse((String) cmdLine.getParsedOptionValue("data-start"));
                    Date end = simpleDateFormat.parse((String) cmdLine.getParsedOptionValue("data-end"));

                    if (start != null && end != null) {
                        consumer.setRelevantPeriod(start, end);
                    }
                }

                // relevant env,host or app-id
                if(cmdLine.hasOption("data-env")){
                  consumer.setEnv(((String) cmdLine.getParsedOptionValue("data-env")));
                    LOGGER.info("Filtering on environment " + consumer.getEnv());

                }

                if(cmdLine.hasOption("data-host")){
                    consumer.setHost((String) cmdLine.getParsedOptionValue("data-host"));
                    LOGGER.info("Filtering on host " + consumer.getHost());

                }

                if(cmdLine.hasOption("data-appid")){
                    consumer.setAppID((String) cmdLine.getParsedOptionValue("data-appid"));
                    LOGGER.info("Filtering on app-id " + consumer.getAppID());

                }
                if(cmdLine.hasOption("data-loglevel")){
                    Level lvl = Level.valueOf((String) cmdLine.getParsedOptionValue("data-loglevel"));
                    consumer.setLogLevel(lvl);
                    LOGGER.info("Filtering on log level " + lvl.toString() + " or above");
                }

                if (cmdLine.hasOption("generate-test-events")){
                    ProduceTestData generateTestLogEvents = new ProduceTestData();
                    generateTestLogEvents.produceTestData(hostname, port, topic);
                    //System.out.println("End generated test events");
                }else {
                    List<LogEvent> x = consumer.readLogEventsFromTopic(hostname, port, topic, UUID.randomUUID().toString(), offset, clientID, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.severe("Log Tracer could not retrieve records from Kafka topic.");
            }
        }
    }
}
