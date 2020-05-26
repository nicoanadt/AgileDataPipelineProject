/usr/local/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor $2 --partitions $3 --topic $1
