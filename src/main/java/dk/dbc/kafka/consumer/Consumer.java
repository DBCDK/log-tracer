/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.consumer;

import dk.dbc.kafka.logformat.LogEvent;

public interface Consumer extends Iterable<LogEvent> {}
