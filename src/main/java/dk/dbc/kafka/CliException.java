/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka;

public class CliException extends RuntimeException {
    public CliException(Throwable cause) {
        super(cause);
    }
}
