#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
export SPARK_HOME=/usr/local/spark
PATH=$PATH:/usr/local/spark/bin
export HADOOP_HOME="/opt/hadoop"
export HADOOP_CONF_DIR="/opt/hadoop/etc/hadoop"
export PATH=$HADOOP_HOME/bin:$PATH

cat /dev/zero | ssh-keygen -q -t rsa -N ""
cat  ~/.ssh/id_rsa.pub | sshpass -proot ssh root@myhdfs 'cat >> .ssh/authorized_keys'

/usr/sbin/sshd
/usr/local/spark/sbin/start-master.sh 2>&1
sleep 5
/usr/local/spark/sbin/start-slave.sh spark://sparkmst:7077 2>&1
tail -f
