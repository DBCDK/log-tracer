package dk.dbc.kafka.logformat;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EmptyDefaultVariableResolverTest {
    @Test
    public void test_lookup() {
        final Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        final EmptyDefaultVariableResolver variableResolver =
            new EmptyDefaultVariableResolver(map);
        assertThat(variableResolver.lookup("key1"), is("value1"));
        assertThat(variableResolver.lookup("key2"), is("value2"));
        assertThat(variableResolver.lookup("key3"), is(""));
    }
}
