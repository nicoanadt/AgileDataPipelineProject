# AgileDataPipelineProject

A data pipeline in organizations is used to transform, store, and disseminate data from its data source to a data presentation layer. This project is part of my thesis to build an agile data pipeline for a dashboard. The data pipeline involved is real-time, scalable, and agile. The agile characteristic is supposed to provide flexibility and rapid development capability.

The objective is to minimize the effort required by data engineers to construct a streaming data pipeline. Instead of programming directly, a data pipeline can be constructed by simply deploying a custom configuration. 


The end-to-end architecture is composed of the following technology stack:
- Apache Kafka
- Apache Spark
- Apache Druid
- Apache Superset

And several supporting technology:
- MongoDB Database
- Hadoop Filesystem (HDFS)
- Python scripting environment

More information is available in the [Assets](https://gitlab.cs.man.ac.uk/u64588na/adaline/tree/master/Assets) page

<!---
**More documentation is available in [gitbook](https://nico-anandito.gitbook.io/agile-data-pipeline-project/)**
-->

## Environment setup

Environment setup for this project is deployed in Docker. 
The images are either available online (in Docker Hub) or can be build from `Dockerfile` and `docker-compose` command.
 

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

*NOTE: To integrate with HDFS, start HDFS Docker first before starting Spark.*
### Apache Druid

To run the spark environment, start the docker container with the following command

```
cd docker-druid
docker-compose up -d
```
*NOTE: This docker will start several containers which will require significant memory consumption in Docker. It it fails, increase memory allocation in your Docker settings.*

Docker containers will start in the background. The Druid UI will be available in `localhost:9888`

### Apache Superset

To run the superset environment, download and adjust the network configuration of the docker.
We used the `--branch release--0.68` release for our purpose, however you can try to get the latest release to get more features.
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
Configuration of python streamer scripts are stored in `/data/edge/cfg`.


### MongoDB database
This container is used to store workflow configuration metadata. The login credential is inside the `docker-compose` file

To run the MongoDB database, use the following command:
```
cd docker-mongodb
docker-compose up -d
```
The database will be available in `localhost:27017` from host, or `mongodb_container:27017` from Docker network


- Create database `AdalineFramework` 
- Create collection `config`
- Insert documents from `Assets/framework-wf-config` of this Git project
- Connect to MongoDB root console, and create a new user
    ```
  docker exec -it docker-mongodb_mongodb_container_1 mongo -u root
  > use AdalineFramework
  > db.createUser(
       {
         user: "AdalineFramework",
         pwd: "bigdata2020",
         roles: [ "readWrite", "dbAdmin" ]
       }
    )
    ```

First time MongoDB setup require the following credential configuration setup:


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

## Scala Project Build

I use IntelliJ IDE to develop and build this Scala program:

1. Import the project in `AgileDataPipeline` directory from IntelliJ
2. Setup the sbt in your environment. 
3. Refresh the sbt (scala build tool) in the project to obtain the dependencies. Dependencies are listed in the `build.sbt` file.
4. Build and compile the project from IDE.
5. Use `sbt assembly` to generate runnable JAR `AgilePipeline.jar`
6. Copy the JAR to the Spark container, using the mounted filesystem of the docker image. Mounted docker filesystem can be configured in the `docker-compose.yml` of the Spark container. The default mount point is `/data/spark`.
7. Bash into the Spark container, either using `ssh` or `docker exec` to run the application.


## Step by Step Data Pipeline Execution Procedure

1. Clone this Git project into your environment.
2. Start the Docker environments

    ```
    cd AgileDataPipelineProject
    docker-compose -f docker-kafka/docker-compose.yml up -d
    docker-compose -f docker-hdfs/docker-compose.yml up -d
    docker-compose -f docker-spark/docker-compose.yml up -d
    docker-compose -f docker-mongodb/docker-compose.yml up -d
    docker-compose -f docker-druid/docker-compose.yml up -d
    docker-compose -f incubator-superset/docker-compose.yml up -d
    ```

3. Ensure the MongoDB database already contain the workflow configuration
4. Start data pipeline framework
   
   - SSH or `docker exec` to the Spark container.\
       If using ssh from your local computer: `ssh -o UserKnownHostsFile=/dev/null thesis@localhost -p 9122` \
       Password of the user `thesis` is `BIGDATA2020`
   - Execute the Agile Data Pipeline Framework
   ```
   cd /data/spark
   ./run-spark-jar-silent.sh uk.ac.man.cs.agiledata.AdalineRunApp AgilePipeline.jar WFUC3
   ```
5. Start pushing data to the Kafka source topic

   ```
    # to run UC1
    docker-compose run --rm python_edge python /data/edge/kafka-streamer3.py cfg/cfg_rawpvr.yml
    
    # to run UC2 in parallel, 2 data source at the same time
    docker-compose run --rm python_edge /data/edge/kafkaFeedUC2.sh
   
    # to run UC3 in parallel, 4 data source at the same time
    docker-compose run --rm python_edge /data/edge/kafkaFeedUC3.sh
    ```
   
6. Monitor the Kafka topics and the output for results