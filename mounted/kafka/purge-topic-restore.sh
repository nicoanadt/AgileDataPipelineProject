/usr/local/kafka/bin/kafka-configs.sh --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name $1 --delete-config retention.ms
