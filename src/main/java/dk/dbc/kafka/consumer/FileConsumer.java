/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.consumer;

import dk.dbc.kafka.logformat.LogEvent;
import dk.dbc.kafka.logformat.LogEventMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class FileConsumer implements Consumer {
    private final Path file;
    private final LogEventMapper logEventMapper;

    public FileConsumer(String file) {
        this.file = Paths.get(file);
        if (!Files.exists(this.file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        this.logEventMapper = new LogEventMapper();
    }

    @Override
    public Iterator<LogEvent> iterator() {
        return new Iterator<LogEvent>() {
            final BufferedReader reader = createBufferedReader(file);

            @Override
            public boolean hasNext() {
                try {
                    reader.mark(2);
                    final int readResult = reader.read();
                    reader.reset();
                    if (readResult == -1) {
                        reader.close();
                        return false;
                    }
                    return true;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public LogEvent next() {
                try {
                    final String line = reader.readLine();
                    try {
                        if (line.startsWith("{")) { // assume RAW format
                            return logEventMapper.unmarshall(line.getBytes(StandardCharsets.UTF_8));
                        }
                        final String[] parts = line.split("\\s", 2);
                        if (parts.length == 2) { // assume SORTABLE format
                            return logEventMapper.unmarshall(parts[1].getBytes(StandardCharsets.UTF_8));
                        }
                        // This is probably doomed to fail...
                        return logEventMapper.unmarshall(line.getBytes(StandardCharsets.UTF_8));
                    } catch (UncheckedIOException e) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                return null;
            }

            private BufferedReader createBufferedReader(Path file) {
                try {
                    return new BufferedReader(
                            new InputStreamReader(
                                    Files.newInputStream(file), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }
}
