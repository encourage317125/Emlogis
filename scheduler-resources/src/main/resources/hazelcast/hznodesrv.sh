#!/bin/sh
DIR="$(dirname "$0")"
cd $DIR

options=$1
java -server ${options} -Djava.net.preferIPv4Stack=true -cp lib/hazelcast-3.5.jar:lib/scheduler-common-1.0.jar com.emlogis.util.hazelcast.StartServer &
echo $! > /var/run/hazelcast.pid
