FROM tomcat

RUN apt-get update && apt-get install unattended-upgrades -y

RUN dpkg-reconfigure -plow unattended-upgrades

COPY /*/target/*.war /usr/local/tomcat/webapps/
