/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import java.time.ZoneId;
import java.util.Map;
import java.util.TreeMap;

public class LogEventFormatterJava {
    private LogEventFormatterJava() {}

    public static String of(LogEvent logEvent, ZoneId zoneId) {
        final StringBuilder buffer = new StringBuilder();
        appendBoxedField(buffer, TimeStampFormatter.of(logEvent.getTimestamp(), zoneId));
        appendBoxedField(buffer, logEvent.getLevel());
        appendBoxedField(buffer, logEvent.getAppID());
        appendBoxedField(buffer, logEvent.getThread());
        appendLogger(buffer, logEvent.getLogger());
        if (logEvent.getMdc() != null) {
            appendMdc(buffer, logEvent.getMdc());
        }
        final String message = logEvent.getMessage();
        final String stacktrace = logEvent.getStacktrace();
        if (message != null && !message.isEmpty()) {
            buffer.append(message);
            if (stacktrace != null && !stacktrace.isEmpty()) {
                buffer.append('\n').append(stacktrace);
            }
        } else if(stacktrace != null && !stacktrace.isEmpty()) {
            buffer.append(stacktrace);
        }
        return buffer.toString();
    }

    private static <T> void appendField(StringBuilder buffer, T fieldValue) {
        if (fieldValue != null) {
            buffer.append(fieldValue.toString());
        } else {
            buffer.append('-');
        }
        buffer.append(' ');
    }

    private static <T> void appendBoxedField(StringBuilder buffer, T fieldValue) {
        buffer.append("[");
        if (fieldValue != null) {
            buffer.append(fieldValue.toString());
        } else {
            buffer.append('-');
        }
        buffer.append("] ");
    }

    private static void appendLogger(StringBuilder buffer, String logger) {
        if (logger != null) {
            appendField(buffer, logger.substring(logger.lastIndexOf('.') + 1));
        } else {
            buffer.append("- ");
        }
    }

    private static void appendMdc(StringBuilder buffer, Map<String, String> mdc) {
        new TreeMap<>(mdc).forEach((k,v) ->
                buffer.append(k).append("=\"").append(v).append("\" "));
    }
}
