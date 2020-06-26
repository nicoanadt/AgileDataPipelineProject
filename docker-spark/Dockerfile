# Dockerfile

# FROM directive instructing base image to build upon
FROM streamingenv-base:0.5

WORKDIR /usr/src/app

# COPY all folder content to docker
COPY . .

RUN wget -q https://www.mirrorservice.org/sites/ftp.apache.org/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz && \
	tar xzf spark-2.4.5-bin-hadoop2.7.tgz && \
	mv spark-2.4.5-bin-hadoop2.7 /usr/local/ && \
	ln -s /usr/local/spark-2.4.5-bin-hadoop2.7/ /usr/local/spark && \
	chmod +x deployment/*.sh
	
EXPOSE 8080 7077 8081 22

# HDFS CONNECTION SETUP
RUN wget -q https://archive.apache.org/dist/hadoop/core/hadoop-2.7.2/hadoop-2.7.2.tar.gz && \
	tar -xzf hadoop-2.7.2.tar.gz && \
	mv hadoop-2.7.2 /opt/hadoop && \
	apt-get -y install sshpass && \
	cp hdfs/core-site.xml /opt/hadoop/etc/hadoop && \
	cp hdfs/hdfs-site.xml /opt/hadoop/etc/hadoop && \
	cp hdfs/hadoop-env.sh /etc/profile.d/

# CMD enable django in container run
CMD ["deployment/start_cmd.sh"]
