/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.logformat;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeStampFormatter {
    public static String of(OffsetDateTime timestamp, ZoneId zoneId) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .format(timestamp.atZoneSameInstant(zoneId));
    }
}
