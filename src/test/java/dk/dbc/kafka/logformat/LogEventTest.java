package dk.dbc.kafka.logformat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kafka.log.Log;
import kafka.utils.Time;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by andreas on 12/22/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogEventTest {

    @Test
    public void writeLogEvent()
    {
        LogEvent logEvent = createDummyObject(1);

        //1. Convert object to JSON string
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ").create();
        String json = gson.toJson(logEvent);
        System.out.println(json);

        //2. Convert object to JSON string and save into a file directly
        try (FileWriter writer = new FileWriter("json-formatted-sample-data-generated.log")) {

            gson.toJson(logEvent, writer);
            writer.write(System.lineSeparator());

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue( logEvent != null );
    }

    @Test
    public void toJson(){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ").create();

        LogEvent logEvent = createDummyObject(1);
        String json = logEvent.toJSON();
        LogEvent fromJson = gson.fromJson(json, LogEvent.class);
        System.out.println("logEvent = " + logEvent);
        System.out.println("json = " + json);
        System.out.println("logEvent fromJson = " + fromJson);

        assertTrue(json.contains("localhost"));
        assertEquals(fromJson.getTimestamp(),logEvent.getTimestamp());
        assertEquals(fromJson.getLevel(),logEvent.getLevel());
        assertEquals(fromJson.getHost(),logEvent.getHost());

    }

    @Test
    public void testLogLevel(){
        LogEvent dummyObject = createDummyObject(1);
        System.out.println("dummyObject = " + dummyObject);
        System.out.println("dummyObject.getLevel() = " + dummyObject.getLevel());
        System.out.println("dummyObject.getLevel().toInt() = " + dummyObject.getLevel().toInt());
        assertTrue(dummyObject.getLevel() != null);
        assertTrue(dummyObject.getLevel().toInt() == 20);
    }

    @Test
    public void testLevelToInt(){
    int oneHundredIsTheSum = 0;
        for (org.slf4j.event.Level c : org.slf4j.event.Level.values()) {
            System.out.print(c);
            System.out.println(" int=" + c.toInt());
            oneHundredIsTheSum+= c.toInt();
        }
        assertEquals(oneHundredIsTheSum, 100);
    }


    @Test
    public void readLogEvent()
    {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ").create();

        //2. Convert object to JSON string and save into a file directly
        try (FileWriter writer = new FileWriter("json-formatted-sample-data-generated.log")) {
            for(int i = 0; i < 100; i++) {
                LogEvent randomEvent = createDummyObject(i);
                gson.toJson(randomEvent, writer);
                writer.write(System.lineSeparator());
                System.out.println(randomEvent.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        LogEvent logEvent = null;
        ArrayList<LogEvent> myFinalList = new ArrayList<>();
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get("json-formatted-sample-data-generated.log"))) {

            stream.forEach(event->{
                LogEvent x = gson.fromJson(event, LogEvent.class);
                System.out.println(x);
                myFinalList.add(x);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue( myFinalList != null && myFinalList.size() == 100 );
    }

    private static LogEvent createDummyObject(int id){
     LogEvent logEvent = new LogEvent();
        logEvent.setAppID("UNIT-TEST");
        logEvent.setHost("localhost");
        logEvent.setEnv("test");
        logEvent.setLevel(org.slf4j.event.Level.INFO);
        logEvent.setMsg("This is auto generated log message number " + id);
        logEvent.setTimestamp(new Date());
        return logEvent;
    }
}
