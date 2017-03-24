# Introduction

The Labeling System consists of several components: the frontend web-app, the server api and resource service, a RDF4J triplestore and a SQLite database. The [datamodel](https://github.com/labelingsystem-ontology) is represented in an ontology using linked data vocabularies.

# Getting Started

## Data Model

https://github.com/mainzed/labelingsystem-ontology

## Server Components

### war-files

#### api.war

RESTful API for Labeling System applications.

API Documentation (Version 1): https://github.com/mainzed/labelingsystem-server/tree/rdf4j/apidoc/v1

#### item.war

RESTful API to provide cool URIs for Labeling System items.

#### persondb.war

Servlet providing persons for linking.

#### datahub.war

RESTful API for the Labeling Explorer.

### databases

RDF4J triplestore and SQlite databases

### web server

nginx and Tomcat

## set up labeling system server

### required

* Java 8
* Tomcat
* RDF4J 2.0 (server, opt. workbench)
* Labeling System API / ITEM / FRONTEND

### recommended

* CentOS (Linux release 7.2.1511 (Core))
* nginx (nginx/1.6.3) (Port 80)
* Tomcat (Version 8.5.4) (Port 8080)
* Java (openjdk version "1.8.0_101") (OpenJDK Runtime Environment (build 1.8.0_101-b13)) (OpenJDK 64-Bit Server VM (build 25.101-b13, mixed mode))
* RDF4J (server and workbench 2.0)
* SQlite support

### folder structure

* recommended
    * `[...]` = `/opt`
    * `[tomcat]` = `/opt/tomcat`

#### required foldes and files with r/w rights for Tomcat

* /[...]/`db`/
* /[tomcat]/webapps/`share`/
* /[tomcat]/webapps/`dump`/
* /[tomcat]/webapps/`ROOT`

#### required files

* /[tomcat]/webapps/`api.war`
* /[tomcat]/webapps/`item.war`
* /[tomcat]/webapps/`persondb.war`
* /[tomcat]/webapps/`datahub.war`
* /[tomcat]/webapps/`rdf4j-server.war`
* /[tomcat]/webapps/`rdf4j-workbench.war` (optional)
* /[...]/db/`ls.sqlite` (db with r/w rights for Tomcat)
* /[...]/db/`persondb.sqlite` (db with r/w rights for Tomcat)

### How to set-up the server?

* install `CentOS`
* install `nginx`
* install `OpenJDK`
* install `Tomcat`
* configure nginx (Port 80) and Tomcat (Port 8080)
* create folders and set r/w rights for Tomcat role
* copy dump script to /opt folder (if data is not in opt folder, change paths in shell script)
* start dump cron job
* get hashed admin pwd via /hash and write it to sqlite database
* copy `SQLite` and `Person` database into db folder and set full r/w rights (chmod 777)
* deploy war files for triplestore (`rdf4j-server.war`, `rdf4j-workbench.war`)
* create `labelingsystem` and `datahub` repository in RDF4J
* modify `default triples`
* fill `labelingsystem` repository with default triples
* configure API and ITEM in their WAR packages using `config[XXX].properties`
* deploy war files for ls backend (`api.war`, `item.war`, `persondb.war`, `datahub.war`)
* configure frontend app and set host for API
* deploy Labeling System frontend app

## Development Hints

The code was developed in NetBeans IDE 8.1, using Java EE 7 Web, JDK 1.8 and Maven 4.0.

## Credits

Software Developers:

* Florian Thiery M.Sc.
    * Institut für Raumbezogene Informations- und Messtechnik (i3mainz)
    * Römisch-Germanisches Zentralmuseum, Leibniz-Forschungsinstitut für Archäologie (RGZM)
    * Mainzer Zentrum für Digitalität in den Geistes- und Kulturwissenschaften (mainzed)

## License

MIT License

Copyright (c) 2014-2017 Florian Thiery M.Sc., i3mainz, RGZM, mainzed

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
