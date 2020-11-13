# IDS Metadata Broker

This is an implementation of an International Data Spaces (IDS) Metadata Broker, which is a registry for IDS Connector self-description documents. It is currently under development and intends to act as a reference for members of the International Data Spaces Association (IDSA) to help with the implementation of custom Broker solutions. Work on this repository closely aligns with the IDS Handshake Document, which describes the concepts of communication on the IDS in textual form. 

## Purpose

The goal of this implementation is to show how the concepts introduced in the Handshake Document can be turned into an actual application. It, therefore, demonstrates the usage of the IDS Information Model for core communication tasks. More specifically, it shows:

* Implementation of the messaging interfaces for IDS infrastructure-level communication,
* Information flow of typical interactions with the Broker

Security is currently supported in terms of TLS via a reverse proxy.

## Repository Structure

[broker-core](./broker-core): The main Maven Artifact to start with.

[open-broker-commen](./open-broker-commen): Shared code which [broker-core](./broker-core) requires.

[open-index-commen](./open-index-commen): Shared functionalities not only for [open-broker-commen](./open-broker-commen) but also for furhter IDS index services (for instance ParIS).

[docker](./docker): Docker and DockerCompose files to deploy the IDS Metadata Broker. 


## Running the Broker

The steps for bringing up a Broker instance depend on the host where the Broker should be deployed. The easiest option is to run the instance on localhost, which is described in the following. We assume that the Docker command-line tools are installed on your system.

1. Prepare the SSL certificate: On your host system, create a directory /etc/idscert/localhost (Linux), C:\etc\idscert\localhost (Windows) and put two files into this directory:
    * server.crt: an x509 certificate, either self-signed or from an official CA
    * server.key: the private key for the certificate

    In the case that you received Keys from our partner Institutes or us, that are stored in a .pem format a conversion to .crt and .key is required for the usage in the reverse proxy.

    openssl x509 -in example_cert.pem -out server.crt openssl rsa -in example_key.pem -out server.key mkdir cert mv server.crt cert/ mv server.key cert/


2. **Prepare and Check the Docker Compose File**

     In some cases no changes are required, but the docker images in the compose files need to be present in the IDS docker registry. Hence,  you need to login to the docker registry first using your credentials:

     `docker login app-store.ids.isst.fraunhofer.de:5000`

    Please also check, that the for Volumes in Reverseproxy contain your cert folder, and change it accordingly.

3. __Run the services__: We provide a [docker-compose file for a localhost setup](docker/composefiles/localhost/docker-compose.yml). Download the file, change
    to the directory where it is located and run ```docker-compose up```.   

## API description

see [Broker API in SwaggerHub](https://app.swaggerhub.com/apis/idsa/IDS-Broker/1.3.1#)

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - Application Framework
* [Apache Fuseki](https://jena.apache.org/documentation/fuseki2/) - RDF triple store for Connector Metadata
* [RDF4J](http://rdf4j.org/) - Java Framework for RDF handling


## Contact (Fraunhofer IAIS)

* [Sebastian Bader](https://gitlab.truzzt.com/sebbader/) [email](mailto:sebastian.bader@iais.fraunhofer.de)
