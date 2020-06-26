
FROM mdouchement/hdfs

EXPOSE 50010

CMD service ssh start \
  && start-dfs.sh \
  && hadoop-daemon.sh start portmap \
  && hdfs dfs -mkdir -p /tmp/checkpoint \
  && tail -f