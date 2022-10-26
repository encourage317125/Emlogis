SET mypath=%~dp0
echo %mypath:~0,-1%
chdir /D %mypath:~0,-1%
java -server -Dhazelcast.config=hazelcast-dev.xml -Djava.net.preferIPv4Stack=true -cp lib/hazelcast-3.5.jar;lib/scheduler-common-1.0.jar com.emlogis.util.hazelcast.StartServer