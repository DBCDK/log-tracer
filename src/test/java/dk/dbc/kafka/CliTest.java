/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CliTest {
    private final static String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm";

    @Test
    public void cli() throws ParseException {
        final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);

        final String[] args = new String[14];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--log-from=" + "2016-12-24T11:00";
        args[4] = "--log-until=" + "2016-12-24T12:00";
        args[5] = "--log-env=" + "env";
        args[6] = "--log-host=" + "host1";
        args[7] = "--log-host=" + "host2";
        args[8] = "--log-appid=" + "appId1";
        args[9] = "--log-appid=" + "appId2";
        args[10] = "--clientid=" + "myId";
        args[11] = "--offset=" + "earliest";
        args[12] = "--format=" + "JAVA";
        args[13] = "--follow";

        final Cli cli = new Cli(args);

        assertThat("broker", cli.args.getString("broker"), is("localhost"));
        assertThat("port", cli.args.getInt("port"), is(9092));
        assertThat("topic", cli.args.getString("topic"), is("test-topic"));
        assertThat("log-from", cli.args.get("log_from"), is(notNullValue()));
        assertThat("log-from", cli.args.get("log_from"),
                is(timestampFormat.parse("2016-12-24T11:00")));
        assertThat("log-until", cli.args.get("log_until"), is(notNullValue()));
        assertThat("log-until", cli.args.get("log_until"),
                is(timestampFormat.parse("2016-12-24T12:00")));
        assertThat("log-env", cli.args.getString("log_env"), is("env"));
        assertThat("log-host", cli.args.getList("log_host"), is(Arrays.asList("host1", "host2")));
        assertThat("log-appid", cli.args.getList("log_appid"), is(Arrays.asList("appId1", "appId2")));
        assertThat("clientid", cli.args.getString("clientid"), is("myId"));
        assertThat("offset", cli.args.getString("offset"), is("earliest"));
        assertThat("format", cli.args.getString("format"), is("JAVA"));
        assertThat("follow", cli.args.getBoolean("follow"), is(true));
    }

    @Test
    public void defaults() {
        final String[] args = new String[3];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";

        final Cli cli = new Cli(args);
        assertThat("clientid", cli.args.getString("clientid"), is(notNullValue()));
        assertThat("offset", cli.args.getString("offset"), is("latest"));
        assertThat("format", cli.args.getString("format"), is("RAW"));
        assertThat("follow", cli.args.getBoolean("follow"), is(false));
    }

    @Test(expected = CliException.class)
    public void missingRequiredOption() {
        final String[] args = new String[1];
        args[0] = "--broker=" + "localhost";

        new Cli(args);
    }

    @Test(expected = CliException.class)
    public void unknownOption() {
        final String[] args = new String[4];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--unknown";

        new Cli(args);
    }

    @Test(expected = CliException.class)
    public void badlyFormattedTimestamp() {
        final String[] args = new String[4];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--log-from=" + "yesterday";

        new Cli(args);
    }

    @Test(expected = CliException.class)
    public void nonIntegerPort() {
        final String[] args = new String[3];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "ninezeroninetwo";
        args[2] = "--topic=" + "test-topic";

        new Cli(args);
    }

    @Test(expected = CliException.class)
    public void badFormatChoice() {
        final String[] args = new String[4];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--format=" + "XML";

        new Cli(args);
    }

    @Test(expected = CliException.class)
    public void badOffsetChoice() {
        final String[] args = new String[4];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--offset=" + "now";

        new Cli(args);
    }

    @Test(expected = CliException.class)
    public void badLogLevelChoice() {
        final String[] args = new String[4];
        args[0] = "--broker=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--log-level=" + "DANGER";

        new Cli(args);
    }
}
