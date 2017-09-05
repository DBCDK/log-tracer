/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import org.junit.Test;
import org.slf4j.event.Level;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LogEventFilterTest {
    @Test
    public void fromFilter() {
        final Date now = new Date();

        final LogEventFilter logEventFilter = new LogEventFilter()
                .setFrom(now);

        final LogEvent logEvent = new LogEvent();
        assertThat("no timestamp", logEventFilter.test(logEvent), is(false));
        logEvent.setTimestamp(OffsetDateTime.ofInstant(new Date(now.getTime() - 1).toInstant(), ZoneId.systemDefault()));
        assertThat("timestamp before", logEventFilter.test(logEvent), is(false));
        logEvent.setTimestamp(OffsetDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()));
        assertThat("timestamp equals", logEventFilter.test(logEvent), is(true));
        logEvent.setTimestamp(OffsetDateTime.ofInstant(new Date(now.getTime() + 1).toInstant(), ZoneId.systemDefault()));
        assertThat("timestamp after", logEventFilter.test(logEvent), is(true));
    }

    @Test
    public void untilFilter() {
        final Date now = new Date();

        final LogEventFilter logEventFilter = new LogEventFilter()
                .setUntil(now);

        final LogEvent logEvent = new LogEvent();
        assertThat("no timestamp", logEventFilter.test(logEvent), is(false));
        logEvent.setTimestamp(OffsetDateTime.ofInstant(new Date(now.getTime() - 1).toInstant(), ZoneId.systemDefault()));
        assertThat("timestamp before", logEventFilter.test(logEvent), is(true));
        logEvent.setTimestamp(OffsetDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()));
        assertThat("timestamp equals", logEventFilter.test(logEvent), is(true));
        logEvent.setTimestamp(OffsetDateTime.ofInstant(new Date(now.getTime() + 1).toInstant(), ZoneId.systemDefault()));
        assertThat("timestamp after", logEventFilter.test(logEvent), is(false));
    }

    @Test
    public void appIdFilter() {
        final LogEventFilter logEventFilter = new LogEventFilter()
                .setAppID("testApp");
        final LogEvent logEvent = new LogEvent();
        assertThat("no appID", logEventFilter.test(logEvent), is(false));
        logEvent.setAppID("notTestApp");
        assertThat("no appID match", logEventFilter.test(logEvent), is(false));
        logEvent.setAppID("testApp");
        assertThat("appID match", logEventFilter.test(logEvent), is(true));
    }

    @Test
    public void envFilter() {
        final LogEventFilter logEventFilter = new LogEventFilter()
                .setEnv("testEnv");
        final LogEvent logEvent = new LogEvent();
        assertThat("no env", logEventFilter.test(logEvent), is(false));
        logEvent.setEnv("notTestEnv");
        assertThat("no env match", logEventFilter.test(logEvent), is(false));
        logEvent.setEnv("testEnv");
        assertThat("env match", logEventFilter.test(logEvent), is(true));
    }
    
    @Test
    public void hostFilter() {
        final LogEventFilter logEventFilter = new LogEventFilter()
                .setHost("testHost");
        final LogEvent logEvent = new LogEvent();
        assertThat("no host", logEventFilter.test(logEvent), is(false));
        logEvent.setHost("notTestHost");
        assertThat("no host match", logEventFilter.test(logEvent), is(false));
        logEvent.setHost("testHost");
        assertThat("host match", logEventFilter.test(logEvent), is(true));
    }

    @Test
    public void logLevelFilter() {
        final LogEventFilter logEventFilter = new LogEventFilter()
                .setLoglevel(Level.INFO);
        final LogEvent logEvent = new LogEvent();
        assertThat("no logLevel", logEventFilter.test(logEvent), is(false));
        logEvent.setLevel(Level.DEBUG);
        assertThat("log level below", logEventFilter.test(logEvent), is(false));
        logEvent.setLevel(Level.INFO);
        assertThat("log level equals", logEventFilter.test(logEvent), is(true));
        logEvent.setLevel(Level.ERROR);
        assertThat("log level above", logEventFilter.test(logEvent), is(true));
    }

    @Test
    public void noFilters() {
        final LogEventFilter logEventFilter = new LogEventFilter();
        final LogEvent logEvent = new LogEvent();
        logEvent.setAppID("testApp");
        assertThat(logEventFilter.test(logEvent), is(true));
    }
}