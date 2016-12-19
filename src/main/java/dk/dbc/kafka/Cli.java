package dk.dbc.kafka;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.logging.Logger;

/**
 * Created by andreas on 12/9/16.
 */
public class Cli {

    private static final Logger log = Logger.getLogger(Cli.class.getName());
    private String[] args = null;
    private Options options = new Options();


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

        // TODO listen functionality, keep consuming
        // TODO write consumed records output to file option
        // TODO write consumed output to stdout
        // TODO Consume a list of topics

        Option data_timeperiod = Option.builder("dt")
                .longOpt("time")
                .numberOfArgs(1)
                .required(false)
                .desc("Relevant time period you want data from")
                .build();


        // add t option
        options.addOption(helpOption);

        options.addOption(kafkaHostname);
        options.addOption(portOption);
        options.addOption(kafkaTopic);
        options.addOption(data_timeperiod);

    }
    public CommandLine parse() {
        CommandLineParser parser = new DefaultParser();         // create the parser

        try {
            // parse the command line arguments
            CommandLine cmdLine = parser.parse( options, args );

            for (Option option: cmdLine.getOptions()) {
                System.out.println(option.getLongOpt() + " = " + option.getValue());

            }
            if (cmdLine.hasOption("help")) {
                showHelp();
                return null;
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
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            showHelp();
            return null;
        }
    }

    public void showHelp(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Log Tracer", options);
    }

}
