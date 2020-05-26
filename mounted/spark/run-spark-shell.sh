#!/bin/bash

/usr/local/spark/bin/spark-shell --packages "org.apache.spark:spark-streaming-kafka-0-10_2.11:2.4.5,org.apache.kafka:kafka_2.11:2.4.1" --master spark://sparkmst:7077