#!/bin/bash

for pid in $( ps -ef | grep java | grep GDAX | awk '{print $2}')
do
   echo "Kill application with PID ${pid}"
   kill -9 ${pid}
done
