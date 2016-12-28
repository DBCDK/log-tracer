package dk.dbc.kafka.logformat;

import kafka.utils.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by andreas on 12/22/16.
 */
public class LogEvent {
    private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ"); // "2017-01-22T15:22:57.567824034+02:00",
    private Time kafkaTimestamp;
    private Date timestamp;
    private String host;
    private String env;
    private String appID;
    //private Level level;
    String level;
    private  String msg;

    public boolean isInRelevantPeriod(Date start, Date end){
        return ( start.before(timestamp) && end.after(timestamp));
    }

    public boolean isRelevantAppID(String relevantAppID){
        return relevantAppID.equalsIgnoreCase(this.appID);
    }

    public boolean isRelevantHost(String relevantHost){
        return relevantHost.equalsIgnoreCase(this.host);
    }
    public boolean isRelevantEnvironment(String relevantEnv){
        return relevantEnv.equalsIgnoreCase(this.env);
    }


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

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "timestamp=" + timestamp +
                ", host='" + host + '\'' +
                ", env='" + env + '\'' +
                ", appID='" + appID + '\'' +
                ", level='" + level + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
