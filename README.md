[![Build Status](https://travis-ci.org/DBCDK/log-tracer.svg?branch=master)](https://travis-ci.org/DBCDK/log-tracer)
# log-tracer
Log-tracer will be a commandline-tool for developers who wants to extract logs from one or more container, application or node for a given period, log-level, specific application. A prerequisite is that all applications logs in a unified format and at the lowest log-level possible (without decreasing performance significantly) and forwards to a [Apache Kafka](https://kafka.apache.org/) topic. The upside is the user a typical dev ops person
* can compare and monitor logs without having to open a ssh tunnel to every relevant container or host.
* can extract logs retroactive in a different level than the default. (Think about it. Bug shows up in production. Retrieve all logs from relevant containers at i.e. debug level)
* manage for how long the log messages should be stored via Kafkas Retention hours




# Log format
The JSON message format for all log-events
`{"timestamp":"2017-01-22T15:22:57.567824034+02:00","hostname":"mesos-node-7","app":"any-application-with-modern-logging","level":"DEBUG","env":"prod","msg":"the log message"}`

With no linebreaks.
