# AgileDataPipelineProject

A data pipeline in organizations is used to transform, store, and disseminate data from its data source to a data presentation layer. This project is part of my thesis to build an agile data pipeline for a dashboard. The data pipeline involved is real-time, scalable, and agile. The agile characteristic is supposed to provide flexibility and rapid development capability.

The data pipeline is composed of the following technology stack:
- Apache Kafka
- Apache Spark
- Apache Druid
- Apache Superset

And several supporting technology to support the overall architecture:
- MongoDB Database
- Hadoop Filesystem (HDFS)
- Python scripting environment

**More documentation is available in [gitbook](https://nico-anandito.gitbook.io/agile-data-pipeline-project/)**

*Disclaimer: This is an ongoing project for our personal use with dynamic changes and moving target.* 

## Environment setup

Environment setup for this project is deployed in Docker. 
The images are either available online or can be build from `Dockerfile` and `docker-compose` command.
 

### Apache Kafka

To run the kafka environment, start the docker container with the following command

```
cd docker-kafka
docker-compose up -d
```
Two docker containers will start in the background (with `-d`) 
- Zookeeper
- Kafka

The kafka cluster is available on:
- `zookeeper:2181`
- `kafka:9090` from inside docker network, `localhost:9092` from host machine

### Apache Spark
Build the image for spark application.
1. Build the base image (adjust the tag version based on your need)

    ```
    cd docker-base
    docker build --tag streamingenv-base:0.5 .
    ```
2. Build the spark image
    ```
    cd ..
    cd docker-spark
    docker build --tag streamingenv-spark:1.3 .
    ```

To run the spark environment, start the docker container with the following command:

```
cd docker-spark
docker-compose up -d
```

A docker container will start in the background. The Spark UI will be available in `localhost:8080`

NOTE: To integrate with HDFS, start HDFS Docker first before starting Spark.
### Apache Druid

To run the spark environment, start the docker container with the following command

```
cd docker-druid
docker-compose up -d
```

Docker containers will start in the background. The Druid UI will be available in `localhost:9888`

### Apache Superset

To run the superset environment, download and adjust the network configuration of the docker
```
git clone --branch release--0.68 https://github.com/apache/incubator-superset/
cd incubator-superset
```

Add network information in `docker-compose.yml` to connect Superset to other docker containers:
1. Add network info to ALL services
    ```
        ...
        networks:
          - docker-kafka_kfk-net
        ...
    ```
2. Join existing network by adding this information to the end of the file
    ```
        ...
        networks:
            docker-kafka_kfk-net:
                external: true
    ```
Then, start the docker container with the following command:
```
cd incubator-superset
docker-compose up -d
```

Docker containers will start in the background. The Superset UI will be available in `localhost:8088`

### Python edge node

This container is used to execute python scripts. 
Scripts are stored in host machine volume that is mounted as `/data` in the container (refer to `Dockerfile`) 

Start the docker container with the following command:

``` 
cd docker-edge
docker-compose run --rm python_edge python /bin/bash
``` 

### MongoDB database
This container is used to store workflow configuration metadata. The login credential is inside the `docker-compose` file

To run the MongoDB database, use the following command:
```
cd docker-mongodb
docker-compose up -d
```
The database will be available in `localhost:27017` from host, or `mongodb_container:27017` from Docker network

### HDFS filesystem
This container is used to store the Kafka checkpoint files. 
Not all workflow will require this filesystem.

NOTE: This docker must be running before starting Apache Spark docker.
```
cd docker-hdfs
docker-compose up -d
```
The HDFS will be available in:
- `localhost:8020` from host, `myhdfs:8020` from Docker network
- `http://localhost:20070/` for webUI

### 