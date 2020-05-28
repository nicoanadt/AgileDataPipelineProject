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


# CMD enable django in container run
CMD ["deployment/start_cmd.sh"]
