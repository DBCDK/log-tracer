/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import java.time.ZoneId;
import java.util.Map;
import java.util.TreeMap;

public class LogEventFormatterJava extends LogEventFormatter {
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

    private static void appendMdc(StringBuilder buffer, Map<String, String> mdc) {
        new TreeMap<>(mdc).forEach((k,v) ->
                buffer.append(k).append("=\"").append(v).append("\" "));
    }
}
