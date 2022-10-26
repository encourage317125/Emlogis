#!/bin/sh

java -server -Djava.net.preferIPv4Stack=true -cp lib/hazelcast-3.4.jar com.hazelcast.client.examples.ClientTestApp