package dk.dbc.kafka;


import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventFilter;
import org.apache.commons.cli.CommandLine;
import org.slf4j.event.Level;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Log tracer
 */
public class LogTracerApp {
    private static Logger LOGGER = Logger.getLogger("LogTracerApp");
    private static String pattern = "yyyy-MM-dd'T'HH:mm";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    public static void main(String[] args) {
        try {
            runWith(args);
        } catch (ParseException | org.apache.commons.cli.ParseException | RuntimeException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }

    static void runWith(String[] args) throws ParseException, org.apache.commons.cli.ParseException {
        LOGGER.info("Log tracer has been started");

        Cli cliParser = null;
        CommandLine cmdLine = null;
        try {
            cliParser = new Cli(args);
            cmdLine = cliParser.parse();
            if(cmdLine != null) {
                String hostname = cmdLine.hasOption("hostname") ? ((String) cmdLine.getParsedOptionValue("hostname")) : "localhost";
                String port = cmdLine.hasOption("port") ? ((Number) cmdLine.getParsedOptionValue("port")).toString() : "2081";
                String topic = cmdLine.hasOption("topic") ? ((String) cmdLine.getParsedOptionValue("topic")) : "test";
                String offset = cmdLine.hasOption("offset") ? ((String) cmdLine.getParsedOptionValue("offset")) : "earliest";
                String clientID = cmdLine.hasOption("clientid") ? ((String) cmdLine.getParsedOptionValue("clientid")) : UUID.randomUUID().toString();

                final LogEventFilter logEventFilter = new LogEventFilter();
                if (cmdLine.hasOption("data-start")) {
                    final Date start = simpleDateFormat.parse((String) cmdLine.getParsedOptionValue("data-start"));
                    if (start != null) {
                        logEventFilter.setFrom(start);
                    }
                }

                if (cmdLine.hasOption("data-end")) {
                    final Date end = simpleDateFormat.parse((String) cmdLine.getParsedOptionValue("data-end"));
                    if (end != null) {
                        logEventFilter.setUntil(end);
                    }
                }

                if (cmdLine.hasOption("data-env")) {
                    logEventFilter.setEnv((String) cmdLine.getParsedOptionValue("data-env"));
                }

                if (cmdLine.hasOption("data-host")) {
                    logEventFilter.setHost((String) cmdLine.getParsedOptionValue("data-host"));
                }

                if (cmdLine.hasOption("data-appid")) {
                    logEventFilter.setAppID((String) cmdLine.getParsedOptionValue("data-appid"));
                }

                if (cmdLine.hasOption("data-loglevel")) {
                    logEventFilter.setLoglevel(Level.valueOf((String) cmdLine.getParsedOptionValue("data-loglevel")));
                }

                if (cmdLine.hasOption("generate-test-events")) {
                    ProduceTestData generateTestLogEvents = new ProduceTestData();
                    generateTestLogEvents.produceTestData(hostname, port, topic);
                } else {
                    final Consumer consumer =
                            new Consumer(hostname, port, topic, UUID.randomUUID().toString(), offset, clientID);

                    for (final LogEvent logEvent : consumer) {
                        if (logEvent != null && logEventFilter.test(logEvent)) {
                            System.out.println(logEvent);
                        }
                    }
                }
            }
        } catch (ParseException | org.apache.commons.cli.ParseException | RuntimeException e) {
            if (cliParser != null) {
                cliParser.showHelp();
            }
            throw e;
        }
    }
}
