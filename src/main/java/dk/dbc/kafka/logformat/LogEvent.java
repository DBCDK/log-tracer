package dk.dbc.kafka.logformat;

import kafka.utils.Time;

import java.util.Date;
import java.util.logging.Level;

/**
 * Created by andreas on 12/22/16.
 */
public class LogEvent {
    private Time kafkaTimestamp;
    private Date timestamp;
    private String host;
    private String appID;
    //private Level level;
    String level;
    private  String msg;


    public Time getKafkaTimestamp() {
        return kafkaTimestamp;
    }

    public void setKafkaTimestamp(Time kafkaTimestamp) {
        this.kafkaTimestamp = kafkaTimestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "kafkaTimestamp=" + kafkaTimestamp +
                ", timestamp=" + timestamp +
                ", host='" + host + '\'' +
                ", appID='" + appID + '\'' +
                ", level='" + level + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
