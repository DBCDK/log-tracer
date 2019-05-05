/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LogEventMapperTest {
    private final LogEventMapper logEventMapper = new LogEventMapper();

    @Test
    public void marshallUnmarshall() {
        final LogEvent logEvent = createLogEvent(42);
        final String marshalled = logEventMapper.marshall(logEvent);
        final LogEvent unmarshalled = logEventMapper.unmarshall(marshalled.getBytes(StandardCharsets.UTF_8));
        assertThat(unmarshalled, is(logEvent));
    }

    @Test
    public void unmarshallTimestamp() {
        final String json = "{\"timestamp\":\"2017-01-22T15:22:57.567824034+02:00\"}";
        logEventMapper.unmarshall(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void unmarshallUnknownField() {
        final String json = "{\"host\":\"localhost\", \"hostess\":\"localhostess\"}";
        logEventMapper.unmarshall(json.getBytes(StandardCharsets.UTF_8));
    }

    private static LogEvent createLogEvent(int id) {
        final LogEvent logEvent = new LogEvent();
        logEvent.setAppID("UNIT-TEST");
        logEvent.setHost("localhost");
        logEvent.setLevel(org.slf4j.event.Level.INFO);
        logEvent.setMessage("This is auto generated log message number " + id);
        logEvent.setTimestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        logEvent.setJson(false);
        return logEvent;
    }
}