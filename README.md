# log-tracer
Log-tracer is a commandline-tool for developers who wants to extract logs from
one or more containers, applications or nodes for a given period, log-level, 
specific application. A prerequisite is that all applications log in a unified
format and at the lowest log-level possible (without decreasing performance
significantly) and forwards to an [Apache Kafka](https://kafka.apache.org/)
topic. The upside is that the user - a typical devops person -

* can compare and monitor logs without having to open a ssh tunnel to every 
relevant container or host.
* can extract logs retroactive in a different level than the default. (Think
about it. Bug shows up in production. Retrieve all logs from relevant
containers at i.e. debug level)
* manage for how long the log messages should be stored via Kafkas retention
hours


# installation
 
```bash
curl -sL http://mavenrepo.dbc.dk/content/repositories/releases/dk/dbc/kafka/log-tracer/1.5/log-tracer-1.5.jar -o log-tracer-1.5.jar && unzip -op log-tracer-1.5.jar log-tracer | bash -s -- --install
```

Keep the installation up-to-date using the selfupdate action
```bash
log-tracer --selfupdate
```

Optionally define the LOG_TRACER_OPTS environment variable for frequently used
options
```bash
LOG_TRACER_OPTS="--broker localhost --port 9092"
```

# usage 
Log-tracer is a commandline tool and needs parameters related to the Kafka
instance and optional parameters if filtering is needed. 
```bash

$ log-tracer -h
usage: log-tracer --version
usage: log-tracer --selfupdate
usage: log-tracer [-h] [-p PORT] [-t TOPIC] [-o {earliest,latest}] [-c CLIENTID] [--log-host LOG_HOST] [--log-appid LOG_APPID] [--log-level {ERROR,WARN,INFO,DEBUG,TRACE}] [--format FORMAT] [-f] [--log-from LOG_FROM] [--log-until LOG_UNTIL] (--from-file FROM_FILE | -b BROKER |
                  --time-zone TIME_ZONE)

named arguments:
  -h, --help             show this help message and exit
  --from-file FROM_FILE  Input file containing either RAW or SORTABLE format
  -b BROKER, --broker BROKER
                         Kafka host
  -p PORT, --port PORT   Kafka port
  -t TOPIC, --topic TOPIC
                         Kafka topic to consume
  -o {earliest,latest}, --offset {earliest,latest}
                         Consume from the beginning or the end of the topic
  -c CLIENTID, --clientid CLIENTID
                         Provide a client ID to identify the client and make use of Kafkas built in offset
  --log-host LOG_HOST    Log hostname filter, repeatable
  --log-appid LOG_APPID  Log application ID filter, repeatable
  --log-level {ERROR,WARN,INFO,DEBUG,TRACE}
                         Log level filter, get only level and above
  --format FORMAT        Output format, {RAW, SORTABLE, JAVA, CUSTOM}
                         CUSTOM is a user-defined format where keys to look for in the log json can be specified with %(key).
                         ex: --format "[%(level)] %(message)")
  -f, --follow           Consume log events continuously
  --log-from LOG_FROM    Log timestamp from filter in localtime format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T13:22
  --log-until LOG_UNTIL  Log timestamp until filter in localtime format yyyy-MM-dd'T'HH:mm i.e. 2017-01-22T13:22
  --time-zone TIME_ZONE  Time-zone ID
```

# filtering
Note that you are able to filter the log-events
```bash
$ log-tracer --broker localhost --port 9092 --topic testtopic --log-host mesos-node-1 --log-host mesos-node-2

$ log-tracer --broker localhost --port 9092 --topic testtopic --log-appid dashing-database --log-appid dashing-webapp

$ log-tracer --broker localhost --port 9092 --topic testtopic --log-from 2017-01-06T15:05 --log-until 2017-01-06T15:06

$ log-tracer --broker localhost --port 9092 --topic testtopic --log-level ERROR
```

# a note on sorting

Since Kafka only provides a total order over records within a partition, not
between different partitions in a topic, and even though the log-tracer tool
does its best to sort on the fly, out-of-order log events must be expected,
especially when using --log-from and --log-until to select a large interval.

In these cases it is therefore recommended to first create an intermediate
file using the SORTABLE format:

```bash
$ log-tracer ... --format SORTABLE > out.log
```

The SORTABLE format generates output on the form 'sortkey raw_entry' where
sortkey is a numeric representation of the number of milliseconds since the
epoch of the timestamp of the log event in question. This is well suited for
subsequent processing by the GNU sort tool:

```bash
$ LC_ALL=C sort -n -k1 -t ' ' out.log -o sorted.log
```

Forcing the C locale avoids the overhead of having to parse UTF-8 and process
complex sort orders. For very large files also consider the sort --parallel
option, which controls the number of sorts run in parallel.

Finally the sorted file can then be traced using the --from-file option of the
log-tracer tool:

```bash
$ log-tracer ... --from-file sorted.log
```

# log format

### Fields
| Field | Required | Format | Examples | Comments |
| ----- | -------- | ------ | ------- | ----------- |
| timestamp | yes | ISO 8601 with rfc 3339 profile | `2016-07-25T17:22:40.835692521+02:00`, `2016-07-25T17:22:40.835Z` | Basically the same logformat as used by logstash. An arbitrary number of decimal digits is technically allowed for the fractions of a second, but the parser expects 0, 3, 6 or 9 decimal points. |
| message | yes | string | `Operation took 3 milliseconds` | Usually a human readable description of the event that is being logged. |
| level | yes | string | `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE` | Taken directly from SLF4J |
| stack_trace | no | string | | Raw stacktrace as a string |
| logger | no | string | `CleanUpBean` | Name of the logger that logged the event. |
| thread | no | string | `worker-thread-1` | Name of the thread that logged the event. |
| mdc | no | json | | Mapped Diagnostic Context (MDC) as nested JSON object. |
| sys_appid | no | string | | Field containing an ID of the application it came from, which might include information such as the namespace it's running in. Use this to find all events across multiple instances of the same application. |
| sys_taskid | no | string | | Field containing an ID of the _instance_ of the application it came from. Similar to `sys_appid`, but more specific since this can be used to identify events from a single instance of an application. |
| sys_host | no | string | | Hostname of the server the application is running on. |
