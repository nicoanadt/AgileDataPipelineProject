# Dockerfile

# FROM directive instructing base image to build upon
FROM ubuntu:18.04

WORKDIR /usr/src/app

# COPY all folder content to docker
COPY . .

RUN apt-get update && \
	apt-get install -y wget && \
	apt-get install -y default-jdk && \
	apt-get install -y openssh-server && \
	apt-get install -y vim
	
RUN	wget -q http://www-us.apache.org/dist/kafka/2.4.0/kafka_2.13-2.4.0.tgz && \
	tar xzf kafka_2.13-2.4.0.tgz && \
	mv kafka_2.13-2.4.0 /usr/local/kafka && \
	cp kafka/*.service /etc/systemd/system && \
	chmod +x kafka/*.sh

# SSH SETUP BELOW	
RUN mkdir /var/run/sshd
# PASSWORD is BIGDATA2020
RUN useradd -m -s /bin/bash thesis
RUN echo 'thesis:BIGDATA2020' | chpasswd
RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# SSH login fix. Otherwise user is kicked off after login
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

ENV NOTVISIBLE "in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile
	
EXPOSE 22
CMD ["/usr/sbin/sshd"]
