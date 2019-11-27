package dk.dbc.kafka.logformat;

import java.time.ZoneId;

public class LogEventFormatterPython extends LogEventFormatter {
    private LogEventFormatterPython() {}

    public static String of(LogEvent logEvent, ZoneId zoneId) {
        final StringBuilder buffer = new StringBuilder();
        appendBoxedField(buffer, TimeStampFormatter.of(logEvent.getTimestamp(), zoneId));
        appendBoxedField(buffer, logEvent.getLevel());
        appendBoxedField(buffer, logEvent.getTaskId());
        appendLogger(buffer, logEvent.getLogger());
        final String message = logEvent.getMessage();
        final String exceptionInfo = logEvent.getExceptionInfo();
        if (message != null && !message.isEmpty()) {
            buffer.append(message);
            if (exceptionInfo != null && !exceptionInfo.isEmpty()) {
                buffer.append('\n').append(exceptionInfo);
            }
        } else if(exceptionInfo != null && !exceptionInfo.isEmpty()) {
            buffer.append(exceptionInfo);
        }
        return buffer.toString();
    }
}
