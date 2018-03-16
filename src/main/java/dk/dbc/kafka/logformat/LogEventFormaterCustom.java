package dk.dbc.kafka.logformat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.text.StrSubstitutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LogEventFormaterCustom {
    private static JsonFactory jsonFactory = new JsonFactory();

    public static String of(String format, LogEvent logEvent) {
        final Map<String, String> map = new HashMap<>();
        if(logEvent.getRaw() == null) return "";
        final String json = new String(logEvent.getRaw(),
            StandardCharsets.UTF_8);
        try {
            final JsonParser parser = jsonFactory.createParser(json);
            while(!parser.isClosed()) {
                final JsonToken token = parser.nextToken();
                if(JsonToken.FIELD_NAME.equals(token)) {
                    final String fieldName = parser.getCurrentName();
                    parser.nextToken();
                    map.put(fieldName, parser.getValueAsString());
                }
            }
        } catch(IOException e) {
            return "";
        }
        // change prefix and suffix from default values to avoid clashing
        // with shell variable expansion
        final StrSubstitutor substitutor = new StrSubstitutor(map, "%(", ")");
        return substitutor.replace(format);
    }
}
