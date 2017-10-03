package dk.dbc.kafka;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

/**
 * Created by andreas on 12/9/16.
 */
public class Cli {

    private String[] args = null;
    private Options options = new Options();
    private static Logger LOGGER = Logger.getLogger("Cli");

    String pattern = "yyyy-MM-dd'T'HH:mm";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);


    public Cli(String[] args) {

        this.args = args;

        Option helpOption = Option.builder("?")
                .longOpt("help")
                .required(false)
                .desc("Shows this usage message")
                .build();

        Option kafkaHostname = Option.builder("h")
                .longOpt("hostname")
                .numberOfArgs(1)
                .required(true)
                .desc("Kafka host you want to connect to")
                .build();


        Option portOption = Option.builder("p")
                .longOpt("port")
                .numberOfArgs(1)
                .required(true)
                .type(Number.class)
                .desc("Port of the kafka host")
                .build();

        Option kafkaTopic = Option.builder("t")
                .longOpt("topic")
                .numberOfArgs(1)
                .required(true)
                .desc("Kafka topic you want to consume")
                .build();

        Option storeTofile = Option.builder("s")
                .longOpt("store")
                .numberOfArgs(1)
                .required(false)
                .desc("Store consumed records to a file")
                .build();

        Option offset = Option.builder("o")
                .longOpt("offset")
                .numberOfArgs(1)
                .required(false)
                .desc("The consumer can starts from the beginning or the end of the topic [earliest, latest]")
                .build();
        Option clientID = Option.builder("i")
                .longOpt("clientid")
                .numberOfArgs(1)
                .required(false)
                .desc("Provide a client ID that can identify the client and make use of Kafkas built in offset")
                .build();
        // TODO Consume a list of topics

        Option data_timeperiod_start = Option.builder("ds")
                .longOpt("data-start")
                .numberOfArgs(1)
                .required(false)
                //.type(Date.class)
                .desc("Relevant time period you want data from in the format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T13:22")
                .build();

        Option data_timeperiod_end = Option.builder("de")
                .longOpt("data-end")
                .numberOfArgs(1)
                .required(false)
                //.type(Date.class)
                .desc("Relevant time period you want data from in the format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T17:22")
                .build();

        Option data_env = Option.builder("denv")
                .longOpt("data-env")
                .numberOfArgs(1)
                .required(false)
                .desc("Relevant environment")
                .build();

        Option data_host = Option.builder("dhos")
                .longOpt("data-host")
                .numberOfArgs(1)
                .required(false)
                .desc("Relevant hostname in logs")
                .build();

        Option data_appid = Option.builder("dapp")
                .longOpt("data-appid")
                .numberOfArgs(1)
                .required(false)
                .desc("Relevant data app name in logs")
                .build();

        Option data_loglevel = Option.builder("dl")
                .longOpt("data-loglevel")
                .numberOfArgs(1)
                .required(false)
                .desc("Relevant log level i.e. ERROR, WARN, INFO, DEBUG, TRACE. If you specify INFO you get ERROR, WARN and INFO. ")
                .build();

        Option format = Option.builder("fmt")
                .longOpt("format")
                .numberOfArgs(1)
                .required(false)
                .desc("Output format {RAW, SIMPLE}")
                .build();

        Option follow = Option.builder("f")
                .longOpt("follow")
                .required(false)
                .numberOfArgs(0)
                .desc("Continuously follow log events")
                .build();

        options.addOption(helpOption);
        options.addOption(kafkaHostname);
        options.addOption(portOption);
        options.addOption(kafkaTopic);
        options.addOption(storeTofile);
        options.addOption(offset);
        options.addOption(clientID);
        options.addOption(data_timeperiod_start);
        options.addOption(data_timeperiod_end);
        options.addOption(data_env);
        options.addOption(data_host);
        options.addOption(data_appid);
        options.addOption(data_loglevel);
        options.addOption(format);
        options.addOption(follow);

    }

    /**
     * Parse all arguments and do a simple validation. Do
     * @return
     */
    public CommandLine parse() throws java.text.ParseException {
        CommandLineParser parser = new DefaultParser();         // create the parser

        try {
            // parse the command line arguments
            CommandLine cmdLine = parser.parse( options, args );

            for (Option option: cmdLine.getOptions()) {
                LOGGER.fine(option.getLongOpt() + " = " + option.getValue());

            }
            if (cmdLine.hasOption("help")) {
                showHelp();
                return null;
            }

            if(cmdLine.hasOption("store")) {
                String outputFileName = cmdLine.hasOption("store") ? ((String)cmdLine.getParsedOptionValue("store")) : "output.json";
                PrintStream out = new PrintStream(new FileOutputStream(outputFileName));
                System.setOut(out);
            }

            if(cmdLine.hasOption("data-start")){
                try {
                    simpleDateFormat.parse((String)cmdLine.getParsedOptionValue("data-start"));
                } catch (java.text.ParseException e) {
                    showHelp();
                    LOGGER.severe("Could not parse [" + cmdLine.getParsedOptionValue("data-start") +  "] data startdate "  + e.getMessage() );
                    e.printStackTrace();
                    throw e;

                }
            }

            if(cmdLine.hasOption("data-end")){
                try {
                    simpleDateFormat.parse((String)cmdLine.getParsedOptionValue("data-end"));
                } catch (java.text.ParseException e) {
                    showHelp();
                    LOGGER.severe("Could not parse [" + cmdLine.getParsedOptionValue("data-end") +  "] data enddate "  + e.getMessage() );
                    e.printStackTrace();
                    throw e;
                }
            }

            if((cmdLine.hasOption("data-host") && ((String)cmdLine.getParsedOptionValue("data-host")).isEmpty() ) ){
                LOGGER.severe("Could not find log host but option was given" );
                throw new NullPointerException("Empty value for option");
            }
            if((cmdLine.hasOption("data-env") && ((String)cmdLine.getParsedOptionValue("data-env")).isEmpty() ) ){
                LOGGER.severe("Could not find log env but option was given" );
                throw new NullPointerException("Empty value for option");
            }
            if((cmdLine.hasOption("data-appid") && ((String)cmdLine.getParsedOptionValue("data-appid")).isEmpty() ) ){
                LOGGER.severe("Could not find log appid but option was given" );
                throw new NullPointerException("Empty value for option");
            }
            if(!cmdLine.hasOption("hostname") || !cmdLine.hasOption("topic") || !cmdLine.hasOption("port") ){
                showHelp();
                throw new MissingArgumentException("missing required arguments");
            } else {
                return cmdLine;
            }
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            LOGGER.severe("Encountered exception while parsing arguments:\n  Reason: " + exp.getMessage() );
            showHelp();
            return null;
        } catch (FileNotFoundException e) {
            LOGGER.severe("Encountered problems saving output to file");
            e.printStackTrace();
            return null;
        }
        catch (NullPointerException e){
            LOGGER.severe("Missing basic arguments to log-tracer" + e.getMessage() );
             e.printStackTrace();
            return null;
        }
    }

    public void showHelp(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Log Tracer", options);
    }

}
