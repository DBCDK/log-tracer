/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import org.slf4j.event.Level;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.function.Predicate;

public class LogEventFilter implements Predicate<LogEvent> {
    private OffsetDateTime from, until;
    private Set<String> appIDs;
    private Set<String> hosts;
    private Level loglevel;
    private int numberOfExitEvents;

    public LogEventFilter setFrom(Date from) {
        this.from = OffsetDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault());
        return this;
    }

    public OffsetDateTime getFrom() {
        return from;
    }

    public LogEventFilter setUntil(Date until) {
        this.until = OffsetDateTime.ofInstant(until.toInstant(), ZoneId.systemDefault());
        return this;
    }

    public OffsetDateTime getUntil() {
        return until;
    }

    public LogEventFilter setAppIDs(Set<String> appIDs) {
        this.appIDs = appIDs;
        return this;
    }

    public LogEventFilter setHosts(Set<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    public LogEventFilter setLoglevel(Level loglevel) {
        this.loglevel = loglevel;
        return this;
    }

    public int getNumberOfExitEvents() {
        return numberOfExitEvents;
    }

    @Override
    public boolean test(LogEvent logEvent) {
        boolean allowed = true;

        if (from != null && (logEvent.getTimestamp() == null || logEvent.getTimestamp().isBefore(from))) {
            allowed = false;
        }

        if (until != null && (logEvent.getTimestamp() == null || logEvent.getTimestamp().isAfter(until))) {
            allowed = false;
            numberOfExitEvents++;
        }

        if (appIDs != null && !appIDs.contains(logEvent.getAppID())) {
            allowed = false;
        }

        if (hosts != null && !hosts.contains(logEvent.getHost())) {
            allowed = false;
        }

        if (loglevel != null && (logEvent.getLevel() == null || logEvent.getLevel().toInt() < loglevel.toInt())) {
            allowed = false;
        }

        return allowed;
    }

    @Override
    public String toString() {
        return "LogEventFilter{" +
                "from=" + from +
                ", until=" + until +
                ", appIDs=" + appIDs +
                ", hosts=" + hosts +
                ", loglevel=" + loglevel +
                '}';
    }
}
