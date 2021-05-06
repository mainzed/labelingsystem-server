FROM yyz1989/rdf4j:2.5.1 AS builder

COPY conf/*.xml /usr/local/tomcat/conf/

COPY conf/importTTL.sh /tmp/

RUN apt-get update && apt-get install unattended-upgrades apt-listchanges -y

COPY owl/defaultDevServer.ttl /opt/defaultDevServer.ttl

RUN apt-get update && apt-get install unattended-upgrades -y

RUN dpkg-reconfigure -plow unattended-upgrades

RUN cat /tmp/importTTL.sh | /opt/eclipse-rdf4j-2.5.1/bin/console.sh

FROM builder AS production
COPY --from=builder /root/.RDF4J/console/repositories/* /opt/eclipse-rdf4j-2.5.1/data/server/repositories/textelsem/

COPY db/ls.sqlite /opt/db/

COPY api/target/*.war /usr/local/tomcat/webapps/

COPY item/target/*.war /usr/local/tomcat/webapps/

