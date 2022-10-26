sudo kill -9 `ps -aux | grep com.emlogis.schedule.engine.SchedulingEngine |  grep -v grep  | awk '/[0-9]*/ {print $2 }'`
