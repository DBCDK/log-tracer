package dk.dbc.kafka.logformat;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogEventFormaterCustomTest {
    @Test
    public void test_format() {
        final LogEvent mockedLogEvent = mock(LogEvent.class);
        final String json = "{\"key\": \"value\"}";
        when(mockedLogEvent.getRaw()).thenReturn(json.getBytes(
            Charset.defaultCharset()));
        final String result = LogEventFormaterCustom.of("%(key)",
            mockedLogEvent);
        assertThat(result, is("value"));
    }

    @Test
    public void test_formatNonExistingKey() {
        final LogEvent mockedLogEvent = mock(LogEvent.class);
        final String json = "{\"key\": \"value\"}";
        when(mockedLogEvent.getRaw()).thenReturn(json.getBytes(
            Charset.defaultCharset()));
        final String result = LogEventFormaterCustom.of("%(key) %(key2) END",
            mockedLogEvent);
        assertThat(result, is("value  END"));
    }
}
