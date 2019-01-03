/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class LogEventMapper {
    private final ObjectMapper objectMapper;

    public LogEventMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    public String marshall(Object object) {
        final StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter, object);
        } catch (IOException e) {
            throw new UncheckedIOException("Exception caught when trying to marshall to JSON", e);
        }
        return stringWriter.toString();
    }

    public LogEvent unmarshall(byte[] json) {
        try {
            final LogEvent logEvent = objectMapper.readValue(json, LogEvent.class);
            if (logEvent != null) {
                logEvent.setRaw(json);
            }
            return logEvent;
        } catch (IOException e) {
            final LogEvent logEvent = new LogEvent();
            logEvent.setMessage(new String(json, StandardCharsets.UTF_8));
            logEvent.setRaw(json);
            return logEvent;
        }
    }
}
