#!/bin/sh

java -server -Dhazelcast.config=hazelcast-dev.xml -Djava.net.preferIPv4Stack=true -cp lib/hazelcast-3.5.jar:lib/scheduler-common-1.0.jar com.emlogis.util.hazelcast.StartServer

