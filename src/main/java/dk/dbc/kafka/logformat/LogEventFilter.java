/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.kafka.logformat;

import org.slf4j.event.Level;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Predicate;

public class LogEventFilter implements Predicate<LogEvent> {
    private OffsetDateTime from, until;
    private String appID, host, env;
    private Level loglevel;

    public LogEventFilter setFrom(Date from) {
        this.from = OffsetDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault());
        return this;
    }

    public LogEventFilter setUntil(Date until) {
        this.until = OffsetDateTime.ofInstant(until.toInstant(), ZoneId.systemDefault());
        return this;
    }

    public LogEventFilter setAppID(String appID) {
        this.appID = appID;
        return this;
    }

    public LogEventFilter setHost(String host) {
        this.host = host;
        return this;
    }

    public LogEventFilter setEnv(String env) {
        this.env = env;
        return this;
    }

    public LogEventFilter setLoglevel(Level loglevel) {
        this.loglevel = loglevel;
        return this;
    }

    @Override
    public boolean test(LogEvent logEvent) {
        boolean allowed = true;

        if (from != null && (logEvent.getTimestamp() == null || logEvent.getTimestamp().isBefore(from))) {
            allowed = false;
        }

        if (until != null && (logEvent.getTimestamp() == null || logEvent.getTimestamp().isAfter(until))) {
            allowed = false;
        }

        if (appID != null && !appID.isEmpty() && !appID.equalsIgnoreCase(logEvent.getAppID())) {
            allowed = false;
        }

        if (env != null && !env.isEmpty() && !env.equalsIgnoreCase(logEvent.getEnv())) {
            allowed = false;
        }

        if (host != null && !host.isEmpty() && !host.equalsIgnoreCase(logEvent.getHost())) {
            allowed = false;
        }

        if (loglevel != null && (logEvent.getLevel() == null || logEvent.getLevel().toInt() < loglevel.toInt())) {
            allowed = false;
        }

        return allowed;
    }
}
