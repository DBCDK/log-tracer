/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import java.util.Map;
import java.util.TreeMap;

public class LogEventSimpleFormatter {
    private LogEventSimpleFormatter() {}

    public static String of(LogEvent logEvent) {
        final StringBuilder buffer = new StringBuilder();
        if (logEvent.getTimestamp() != null) {
            appendBoxedField(buffer, logEvent.getTimestamp());
        } else {
            appendBoxedField(buffer, logEvent.getKafkaTimestamp());
        }
        appendBoxedField(buffer, logEvent.getLevel());
        appendBoxedField(buffer, logEvent.getAppID());
        if (logEvent.getMdc() != null) {
            appendMdc(buffer, logEvent.getMdc());
        }
        appendField(buffer, logEvent.getMessage());
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

    private static void appendMdc(StringBuilder buffer, Map<String, String> mdc) {
        new TreeMap<>(mdc).forEach((k,v) -> {
            buffer.append(k).append("=\"").append(v).append("\" ");
        });
    }
}
