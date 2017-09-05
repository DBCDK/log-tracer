/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

public class LogEventSimpleFormatter {
    private LogEventSimpleFormatter() {}

    public static String of(LogEvent logEvent) {
        final StringBuilder buffer = new StringBuilder();
        appendBoxedField(buffer, logEvent.getTimestamp());
        appendBoxedField(buffer, logEvent.getLevel());
        appendBoxedField(buffer, logEvent.getAppID());
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
}
