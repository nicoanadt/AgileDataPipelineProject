#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
export SPARK_HOME=/usr/local/spark
PATH=$PATH:/usr/local/spark/bin

/usr/sbin/sshd
/usr/local/spark/sbin/start-master.sh 2>&1
sleep 5
/usr/local/spark/sbin/start-slave.sh spark://sparkmst:7077 2>&1
tail -f
