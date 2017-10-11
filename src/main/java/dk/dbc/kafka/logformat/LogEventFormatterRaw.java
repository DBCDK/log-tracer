/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import java.nio.charset.StandardCharsets;

public class LogEventFormatterRaw {
    private LogEventFormatterRaw() {}

    public static String of(LogEvent logEvent) {
        return new String(logEvent.getRaw(), StandardCharsets.UTF_8);
    }
}
