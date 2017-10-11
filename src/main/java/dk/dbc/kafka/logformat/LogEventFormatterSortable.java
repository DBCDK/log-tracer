/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import java.nio.charset.StandardCharsets;

public class LogEventFormatterSortable {
    private LogEventFormatterSortable() {}

    public static String of(LogEvent logEvent) {
        return logEvent.getTimestamp().toInstant().toEpochMilli()
                + " " + new String(logEvent.getRaw(), StandardCharsets.UTF_8);
    }
}
