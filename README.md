[![Build Status](https://travis-ci.org/DBCDK/log-tracer.svg?branch=master)](https://travis-ci.org/DBCDK/log-tracer)
# log-tracer
Log-tracer is a commandline-tool for developers who wants to extract logs from one or more containers, applications or nodes for a given period, log-level, specific application. A prerequisite is that all applications log in a unified format and at the lowest log-level possible (without decreasing performance significantly) and forwards to an [Apache Kafka](https://kafka.apache.org/) topic. The upside is that the user - a typical devops person -
* can compare and monitor logs without having to open a ssh tunnel to every relevant container or host.
* can extract logs retroactive in a different level than the default. (Think about it. Bug shows up in production. Retrieve all logs from relevant containers at i.e. debug level)
* manage for how long the log messages should be stored via Kafkas retention hours


# installation
 You need to have Java JDK and the build tool Apache Maven
```bash
$ mvn install

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --hostname localhost --port 9092 --topic test

or via maven the build tool.

$ mvn exec:java -Dexec.mainClass='dk.dbc.kafka.LogTracerApp' -Dexec.arguments="--hostname=localhost,--port=9092,--topic=test"
```

# usage 
Log-tracer is a commandline tool and needs parameters related to the Kafka instance and optional parameters if filtering is needed. 
```bash

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar -?
usage: Log Tracer
 -?,--help                   Shows this usage message
 -dapp,--data-appid <arg>    Relevant data app name in logs
 -de,--data-end <arg>        Relevant time period you want data from in
                             the format yyyy-MM-dd'T'HH:mm i.e.
                             2017-01-22T17:22
 -denv,--data-env <arg>      Relevant environment
 -dhos,--data-host <arg>     Relevant hostname in logs
 -dl,--data-loglevel <arg>   Relevant log level i.e. ERROR, WARN, INFO,
                             DEBUG, TRACE. If you specify INFO you get
                             ERROR, WARN and INFO.
 -ds,--data-start <arg>      Relevant time period you want data from in
                             the format yyyy-MM-dd'T'HH:mm i.e.
                             2017-01-22T13:22
 -f,--follow                 Continuously consume log events
 -fmt,--format <arg>         Output format {RAW, SIMPLE}                            
 -g,--generate-test-events   Generate random log events. Simulating four
                             different environments, application ids and
                             hostnames.
 -h,--hostname <arg>         Kafka host you want to connect to
 -i,--clientid <arg>         Provide a client ID that can identify the
                             client and make use of Kafkas built in offset
 -o,--offset <arg>           The consumer can starts from the beginning or
                             the end of the topic [earliest, latest]
 -p,--port <arg>             Port of the kafka host
 -s,--store <arg>            Store consumed records to a file
 -t,--topic <arg>            Kafka topic you want to consume

```

# Filter the consumed log events
Note that you are able to filter the log-events on environment, host, env, period and
```bash
$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --hostname localhost --port 9092 --topic testtopic --data-env test

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --hostname localhost --port 9092 --topic testtopic --data-host mesos-node-2

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --hostname localhost --port 9092 --topic testtopic --data-app dashing-database

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --hostname localhost --port 9092 --topic testtopic --data-start 2017-01-06T15:05 --data-end 2017-01-06T15:06

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --hostname localhost --port 9092 --topic testtopic --data-loglevel ERROR  --data-env prod 

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --hostname localhost --port 9092 --topic testtopic --data-loglevel INFO



```

# Log format
The required  JSON message format for all log-events
`{"timestamp":"2017-01-22T15:22:57.567824034+02:00","hostname":"mesos-node-7","sys_appid":"any-application-with-modern-logging","level":"DEBUG","sys_env":"prod","message":"the log message"}`

With no linebreaks.
