/usr/local/kafka/bin/kafka-configs.sh --zookeeper zookeeper:2181 --entity-type topics --entity-name $1 --alter --add-config retention.ms=1000
