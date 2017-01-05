package dk.dbc.kafka;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Created by andreas on 12/28/16.
 */
public class CliTest {

    String pattern = "yyyy-MM-dd'T'HH:mm";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    @Test
    public void testCli() {
        assertTrue(true);
    }

    @Test
    public void testCliArgs() throws ParseException {

        String[] args = new String[4];
        args[0] = "--hostname=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--store=" + "fileoutput.json";

        Cli cli = new Cli(args);
        CommandLine parsedCli = cli.parse();

        assertTrue(parsedCli.hasOption("store") && parsedCli.hasOption("topic") && parsedCli.hasOption("port") && parsedCli.hasOption("hostname"));
    }

    @Test
    public void testCliArgsWithDate() throws ParseException, org.apache.commons.cli.ParseException {

        String[] args = new String[6];
        args[0] = "--hostname=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--store=" + "fileoutput.json";
        args[4] = "--data-start=" + "2016-12-24T11:00";
        args[5] = "--data-end=" + "2016-12-25T19:00";

        Cli cli = new Cli(args);
        CommandLine parsedCli = cli.parse();


        Date start = simpleDateFormat.parse((String)parsedCli.getParsedOptionValue("data-start"));
        Date end = simpleDateFormat.parse((String)parsedCli.getParsedOptionValue("data-end"));

        assertTrue(parsedCli.hasOption("store") && parsedCli.hasOption("topic") && parsedCli.hasOption("port") && parsedCli.hasOption("hostname") &&
                parsedCli.hasOption("data-start") && parsedCli.hasOption("data-end"));
        assertTrue( (start.getTime() > 0) && (end.getTime() > 0) && (end.getTime()> start.getTime()) );
    }

    @Test
    public void testCliArgsWithEnvHostAppID() throws ParseException, org.apache.commons.cli.ParseException {

        String[] args = new String[6];
        args[0] = "--hostname=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--data-host=" + "Mesos-node-7";
        args[4] = "--data-appid=" + "superapp";
        args[5] = "--data-env=" + "test";

        Cli cli = new Cli(args);
        CommandLine parsedCli = cli.parse();


        assertTrue( parsedCli.hasOption("data-appid") );
        assertTrue(  parsedCli.hasOption("data-env"));
        assertTrue( parsedCli.hasOption("data-host"));

    }

    @Test
    public void testCliArgsWithEnv() throws ParseException, org.apache.commons.cli.ParseException {

        String[] args = new String[4];
        args[0] = "--hostname=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--data-env=" + "test";

        Cli cli = new Cli(args);
        CommandLine parsedCli = cli.parse();

        assertTrue(  parsedCli.hasOption("data-env"));
    }


    @Test
    public void testCliArgsWithLogHost() throws ParseException, org.apache.commons.cli.ParseException {

        String[] args = new String[4];
        args[0] = "--hostname=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--data-host=" + "mesos-1";

        Cli cli = new Cli(args);
        CommandLine parsedCli = cli.parse();

        assertTrue(  parsedCli.hasOption("data-host"));
    }

    @Test(expected = ParseException.class)
    public void testCliArgsWithWrongDate() throws ParseException {

        String[] args = new String[6];
        args[0] = "--hostname=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--store=" + "fileoutput.json";
        args[4] = "--data-start=" + "201-12-24T11:00";
        args[5] = "--data-end=" + "2016-12-25";

        Cli cli = new Cli(args);
        CommandLine parsedCli = cli.parse();

        assertTrue(parsedCli.hasOption("store") && parsedCli.hasOption("topic") && parsedCli.hasOption("port") && parsedCli.hasOption("hostname") &&
                parsedCli.hasOption("date-start") && parsedCli.hasOption("date-end"));
    }

    @Test(expected=NullPointerException.class)
    public void testCliArgsNotAnOption() throws ParseException {
        String[] args = new String[6];
        args[0] = "--hostname=" + "localhost";
        args[1] = "--port=" + "9092";
        args[2] = "--topic=" + "test-topic";
        args[3] = "--Not-An-Option=" + "error!";

        Cli cli = new Cli(args);
        CommandLine parsedCli = cli.parse();
        assertTrue(parsedCli.hasOption("topic") && parsedCli.hasOption("port") && parsedCli.hasOption("hostname"));
    }

}
