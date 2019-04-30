package dk.dbc.kafka.logformat;

import org.apache.commons.text.lookup.StringLookup;

import java.util.Map;

/**
 * Variable resolver which defaults to an empty string if the key is not
 * found in the provided map.
 */
public class EmptyDefaultVariableResolver implements StringLookup {
    private final Map<String, String> map;
    EmptyDefaultVariableResolver(Map<String, String> map) {
        this.map = map;
    }
    public String lookup(String key) {
        if(map.containsKey(key)) {
            return map.get(key);
        }
        return "";
    }
}
