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
        logEvent.setLevel(Level.INFO.getName());
        logEvent.setMsg("This is auto generated log message number " + id);
        logEvent.setTimestamp(new Date());
        return logEvent;
    }
}
