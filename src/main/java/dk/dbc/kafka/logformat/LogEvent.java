package dk.dbc.kafka.logformat;

import kafka.utils.Time;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.event.Level;

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
    private Level level; // ERROR int=40, WARN int=30, INFO int=20, DEBUG int=10, TRACE int=0
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

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Set the level from an int. ERROR int=40, WARN int=30, INFO int=20, DEBUG int=10, TRACE int=0
     * @param level 0,10,20,30,40
     */
    public void setLevel(int level){
        for (Level c : org.slf4j.event.Level.values()){
            if(c.toInt()== level){
                this.level = c;
            }
        }
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

    public String toJSON(){ // {"timestamp":"2017-01-12T08:00:00.000000000+02:00","host":"my-node","appID":"superapp","level":"WARN","env":"dev","msg":"the log message"}
        return "{\"timestamp\":\"" + sdf.format(timestamp) + "\",\"host\":\"" + host + "\",\"appID\":\"" + appID +"\",\"level\":\"" +level +"\",\"env\":\""+ env + "\",\"msg\":\""+msg + "\"}";
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "timestamp=" + sdf.format(timestamp) +
                ", host='" + host + '\'' +
                ", env='" + env + '\'' +
                ", appID='" + appID + '\'' +
                ", level='" + level.name() + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
