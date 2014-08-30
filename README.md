===============================================================================
    hq-console
===============================================================================

HornetQ CLI Management Tool.

Features : 
===============================================================================

Connects & interact through JMX with an HornetQ server instance. Allows retrieval of :

 - VM uptime, heap size, threads, file descriptors
 - Messaging Core queues
 - Diverts
 - Cluster status & nodes
 
Interactive commands allows to :

 - Destroy core queues
 - Kick a client session
 - Delete messages from a core queue

Building from sources :
===============================================================================

```
git clone git://github.com/mchaplin/hq-console.git
```

```
mvn package
```

Binary release is available under target/hq-console-<version>-jar-with-dependencies.jar

Usage :
===============================================================================

By default, tries to connect to localhost, at port 6001. Target host and port can be specified
with :

```
java -jar /usr/local/bin/hq-console/hq-console-0.6.1-SNAPSHOT-jar-with-dependencies.jar -h <host> -p <port>
```

To specify the RMI port to use for remote JMX add the following option to the variable JVMARGS or the java command line in the run.sh script :

```
-Dcom.sun.management.jmxremote.port=6001 
```

Once connected, use 'help' command
