# Introduction

The Labeling System server ...

# Getting Started

## Components

### api

RESTful API for Labeling System applications

### item

RESTful API to provide cool URIs for Labeling System items

## Set-Up Server

### required

* Java 8
* Tomcat
* RDF4J 2.0
* labeling system api / item / workbench

### recommended

* CentOS
 * Linux release 7.2.1511 (Core)
* nginx (Port 80)
 * nginx/1.6.3
* Tomcat (Port 8080)
 * Version 8.5.4
* Java
 * openjdk version "1.8.0_101"
 * OpenJDK Runtime Environment (build 1.8.0_101-b13)
 * OpenJDK 64-Bit Server VM (build 25.101-b13, mixed mode)
* SQlite support
* RDF4J
 * server/workbench 2.0

### folder structure

* /[tomcat]/webapps/api.war
* /[tomcat]/webapps/item.war
* /[tomcat]/webapps/rdf4j-server.war
* /[tomcat]/webapps/rdf4j-workbench.war (optional)
* /[tomcat]/webapps/{nameOfWebApp}

* /[..]/db/ls.sqlite
* /[tomcat]/webapps/share/
* /[tomcat]/webapps/dump/

## how to set-up the server

- Tomcat
- RDF4J Triplestore
- LS API
- LS Item API
- SQlite Database
- create folders and rights

## Configure API

- config.properties

## Wiki

[Labeling System Wiki](../../wiki)

## Ontologie

- https://github.com/labelingsystem-showcase/ls-core
- https://github.com/labelingsystem-showcase/ls-reference

## API usage examples

- https://github.com/labelingsystem-showcase/js-examples
- https://github.com/labelingsystem-showcase/sparqlsearch

## Development Hints

The code was developed in NetBeans IDE 8.1, using Java EE 7 Web, JDK 1.8 and Maven 4.0.

# FAQ

## How to add a retcat item?

## Credits

Software Developers:

- Florian Thiery M.Sc.
 - Institut für Raumbezogene Informations- und Messtechnik (i3mainz)
 - Römisch-Germanisches Zentralmuseum, Leibniz-Forschungsinstitut für Archäologie (RGZM)

## License

MIT License

Copyright (c) 2016 Florian Thiery M.Sc., i3mainz, RGZM

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
