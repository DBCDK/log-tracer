/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.UUID;

public class Cli {
    public Namespace args;

    public Cli(String[] args) throws CliException {
        final ArgumentParser parser = ArgumentParsers.newFor("log-tracer").build();
        final MutuallyExclusiveGroup source = parser.addMutuallyExclusiveGroup()
                .required(true);
        source.addArgument("--from-file")
                .help("Input file containing either RAW or SORTABLE format");
        source.addArgument("-b", "--broker")
                .help("Kafka host");
        parser.addArgument("-p", "--port")
                .type(Integer.class)
                .setDefault(9092)
                .help("Kafka port");
        parser.addArgument("-t", "--topic")
                .help("Kafka topic to consume");
        parser.addArgument("-o", "--offset")
                .choices("earliest", "latest")
                .setDefault("latest")
                .help("Consume from the beginning or the end of the topic");
        parser.addArgument("-c", "--clientid")
                .setDefault(UUID.randomUUID().toString())
                .help("Provide a client ID to identify the client and make use of Kafkas built in offset");
        parser.addArgument("--log-host")
                .action(Arguments.append())
                .help("Log hostname filter, repeatable");
        parser.addArgument("--log-appid")
                .action(Arguments.append())
                .help("Log application ID filter, repeatable");
        parser.addArgument("--log-level")
                .choices("ERROR", "WARN", "INFO", "DEBUG", "TRACE")
                .help("Log level filter, get only level and above");
        parser.addArgument("--format")
                .setDefault("RAW")
                .help("Output format, {RAW, SORTABLE, JAVA, PYTHON, CUSTOM}\n" +
                    "CUSTOM is a user-defined format where keys to look for " +
                    "in the log json can be specified with %(key).\n" +
                    "ex: --format \"[%(level)] %(message)\")");
        parser.addArgument("-f", "--follow")
                .action(Arguments.storeTrue())
                .help("Consume log events continuously");
        parser.addArgument("--log-from")
                .action(new TimestampAction())
                .help("Log timestamp from filter in localtime format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T13:22");
        parser.addArgument("--log-until")
                .action(new TimestampAction())
                .help("Log timestamp until filter in localtime format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T13:22");
        source.addArgument("--time-zone")
                .setDefault("Europe/Copenhagen")
                .help("Time-zone ID");

        try {
            this.args = parser.parseArgs(args);
            if (this.args.get("broker") != null && this.args.get("topic") == null) {
                throw new ArgumentParserException("topic must be specified", parser);
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw new CliException(e);
        }
    }

    private static class TimestampAction implements ArgumentAction {
        private final static String PATTERN = "yyyy-MM-dd'T'HH:mm";

        private final SimpleDateFormat format = new SimpleDateFormat(PATTERN);

        @Override
        public void run(ArgumentParser parser, Argument arg,
                        Map<String, Object> attrs, String flag, Object value)
                throws ArgumentParserException {
            try {
                attrs.put(arg.getDest(), format.parse((String) value));
            } catch (ParseException e) {
                throw new ArgumentParserException(e.getMessage(), parser);
            }
        }

        @Override
        public void onAttach(Argument arg) {
        }

        @Override
        public boolean consumeArgument() {
            return true;
        }
    }
}
