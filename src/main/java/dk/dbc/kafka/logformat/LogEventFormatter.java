package dk.dbc.kafka.logformat;

abstract class LogEventFormatter {
    static <T> void appendField(StringBuilder buffer, T fieldValue) {
        if (fieldValue != null) {
            buffer.append(fieldValue.toString());
        } else {
            buffer.append('-');
        }
        buffer.append(' ');
    }

    static <T> void appendBoxedField(StringBuilder buffer, T fieldValue) {
        buffer.append("[");
        if (fieldValue != null) {
            buffer.append(fieldValue.toString());
        } else {
            buffer.append('-');
        }
        buffer.append("] ");
    }

    static void appendLogger(StringBuilder buffer, String logger) {
        if (logger != null) {
            appendField(buffer, logger.substring(logger.lastIndexOf('.') + 1));
        } else {
            buffer.append("- ");
        }
    }

}
