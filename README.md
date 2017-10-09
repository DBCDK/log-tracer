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

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --broker localhost --port 9092 --topic test

or via maven the build tool.

$ mvn exec:java -Dexec.mainClass='dk.dbc.kafka.LogTracerApp' -Dexec.arguments="--broker=localhost,--port=9092,--topic=test"
```

# usage 
Log-tracer is a commandline tool and needs parameters related to the Kafka instance and optional parameters if filtering is needed. 
```bash

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar -h
usage: log-tracer [-h] -b BROKER -p PORT -t TOPIC [-o {earliest,latest}] [-c CLIENTID] [--log-env LOG_ENV] [--log-host LOG_HOST] [--log-appid LOG_APPID] [--log-level {ERROR,WARN,INFO,DEBUG,TRACE}] [--format {RAW,JAVA}] [-f] [--log-from LOG_FROM] [--log-until LOG_UNTIL]

optional arguments:
  -h, --help             show this help message and exit
  -b BROKER, --broker BROKER
                         Kafka host
  -p PORT, --port PORT   Kafka port
  -t TOPIC, --topic TOPIC
                         Kafka topic to consume
  -o {earliest,latest}, --offset {earliest,latest}
                         Consume from the beginning or the end of the topic
  -c CLIENTID, --clientid CLIENTID
                         Provide a client ID to identify the client and make use of Kafkas built in offset
  --log-env LOG_ENV      Log environment filter, ex. prod or staging
  --log-host LOG_HOST    Log hostname filter, repeatable
  --log-appid LOG_APPID  Log application ID filter, repeatable
  --log-level {ERROR,WARN,INFO,DEBUG,TRACE}
                         Log level filter, get only level and above
  --format {RAW,JAVA}    Output format
  -f, --follow           Consume log events continuously
  --log-from LOG_FROM    Log timestamp from filter in the format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T13:22
  --log-until LOG_UNTIL  Log timestamp until filter in the format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T13:22

```

# filtering
Note that you are able to filter the log-events
```bash
$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --broker localhost --port 9092 --topic testtopic --log-env dev

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --broker localhost --port 9092 --topic testtopic --log-host mesos-node-1 --log-host mesos-node-2

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --broker localhost --port 9092 --topic testtopic --log-appid dashing-database --log-appid dashing-webapp

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --broker localhost --port 9092 --topic testtopic --log-from 2017-01-06T15:05 --log-until 2017-01-06T15:06

$ java -jar target/log-tracer-0.1-SNAPSHOT-jar-with-dependencies.jar --broker localhost --port 9092 --topic testtopic --log-level ERROR


```

# log format
The optimal JSON message format for all log-events
`{"timestamp":"2017-01-22T15:22:57.567824034+02:00","host":"mesos-node-7","sys_appid":"any-application-with-modern-logging","level":"DEBUG","sys_env":"prod","message":"the log message", "stack_trace": ""}`
