/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka;

import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventFilter;
import dk.dbc.kafka.logformat.LogEventFormatterJava;
import org.slf4j.event.Level;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

public class LogTracerApp {
    private static final int EXIT_CUTOFF = 25000;

    public static void main(String[] args) {
        try {
            runWith(args);
        } catch (CliException e) {
            System.exit(1);
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static void runWith(String[] args) throws CliException {
        final Cli cli = new Cli(args);
        final LogEventFilter logEventFilter = createFilter(cli);
        final String format = cli.args.getString("format");
        final boolean follow = cli.args.getBoolean("follow");

        final Consumer consumer = new Consumer(
                cli.args.getString("broker"),
                cli.args.getInt("port"),
                cli.args.getString("topic"),
                UUID.randomUUID().toString(),
                cli.args.getString("offset"),
                cli.args.getString("clientid"));

        if (logEventFilter.getFrom() != null) {
            consumer.setFromDateTime(logEventFilter.getFrom());
        }

        final Iterator<LogEvent> iterator = consumer.iterator();
        while (true) {
            while (iterator.hasNext()) {
                if (logEventFilter.getNumberOfExitEvents() >= EXIT_CUTOFF) {
                    break;
                }
                final LogEvent logEvent = iterator.next();
                if (logEvent != null && logEventFilter.test(logEvent)) {
                    switch (format) {
                        case "JAVA":
                            System.out.println(
                                    LogEventFormatterJava.of(logEvent));
                            break;
                        default:
                            System.out.println(new String(
                                    logEvent.getRaw(), StandardCharsets.UTF_8));
                    }
                }
            }
            if (follow) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }

    private static LogEventFilter createFilter(Cli cli) {
        final LogEventFilter logEventFilter = new LogEventFilter();
        if (cli.args.get("log_from") != null) {
            logEventFilter.setFrom(cli.args.get("log_from"));
        }

        if (cli.args.get("log_until") != null) {
            logEventFilter.setUntil(cli.args.get("log_until"));
        }

        if (cli.args.get("log_env") != null) {
            logEventFilter.setEnv(cli.args.getString("log_env"));
        }

        if (cli.args.get("log_host") != null) {
            logEventFilter.setHosts(new HashSet<>(cli.args.getList("log_host")));
        }

        if (cli.args.get("log_appid") != null) {
            logEventFilter.setAppIDs(new HashSet<>(cli.args.getList("log_appid")));
        }

        if (cli.args.get("log_level") != null) {
            logEventFilter.setLoglevel(Level.valueOf(
                    cli.args.getString("log_level")));
        }

        return logEventFilter;
    }
}
