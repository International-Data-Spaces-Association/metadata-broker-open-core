# IDS Metadata Broker

This is an implementation of an International Data Spaces (IDS) Metadata Broker, which is a registry for IDS Connector self-description documents. It is currently under development and intends to act as a reference for members of the International Data Spaces Association (IDSA) to help with the implementation of custom Broker solutions. Work on this repository closely aligns with the IDS Handshake Document, which describes the concepts of communication on the IDS in textual form.

## 1. Purpose

The goal of this implementation is to show how the concepts introduced in the [Handshake Document](https://industrialdataspace.jiveon.com/docs/DOC-1817#jive_content_id_Standard_Protocols_HTTPS_MQTT__TLS) (currently restricted to IDSA members) can be turned into an actual application. It, therefore, demonstrates the usage of the [IDS Information Model]() for core communication tasks. More specifically, it shows:

* Implementation of the messaging interfaces for IDS infrastructure-level communication,
* Information flow of typical interactions with the Broker

Security is currently supported in terms of TLS via a reverse proxy.

## 2. Repository Structure

[broker-core](./broker-core): The main Maven Artifact to start with.

[open-broker-common](./open-broker-common): Shared code which [broker-core](./broker-core) requires.

[open-index-common](./open-index-common): Shared functionalities not only for [open-broker-common](./open-broker-common) but also for further IDS index services (for instance ParIS).

[docker](./docker): Docker ([installation guide](https://docs.docker.com/engine/install/)) and DockerCompose ([installation guide](https://docs.docker.com/compose/install/)) files to deploy the IDS Metadata Broker.

## 3. Prerequisites

In this section, we will provide some guidance as to recommendations for the number of resources that should be available to smoothly operate the IDS Metadata Broker. The actual number of resources required heavily depends on the load. In case of very little traffic, fewer resources than listed below might be required.


### 3.1 Hardware

2GB of disk space is required for operating the IDS Metadata Broker, though we recommend providing at least 20GB of free disk storage to avoid running out of disk space with increasing number of registered items. To provide enough processing power for all Docker containers, we recommend using a 64bit quad core processor or better.

### 3.2 Software

- **OS**: We recommend using a Linux based operating system. However, any operating system with a Docker installation can be used (tested on Ubuntu 20.04 and Windows 10). More strict hardware requirements than listed above might apply if a non-Linux operating system is used.
- **Docker**: version 20.10.7 or later
- **Docker Compose**: version 1.29.1 or later
- **OpenSSL**: Version 1.1.1k or later. A valid X.509 certificate, signed by a trusted certification authority, is strongly recommended to avoid warnings about insecure HTTPS connections. Docker must be installed on the target machine.
- **Identity certificate**: A DAPS certificate which acts like a Keystore of the Broker from [Fraunhofer AISEC](https://www.dataspaces.fraunhofer.de/en/software/identity_provider.html) in **JKS** format.
- **Java**: Java 11 or later should be installed in your local environment to build the docker image.
- **Maven**: Maven 3.6.3 or later should be installed in your local environment to build the docker image (execute `mvn -version` to check the successful installation).

## 4  Installation Guide
This part aims to aid IT administrators or developers in the installation of the IDS Metadata Broker. Metadata Broker is still actively maintained by Fraunhofer IAIS. If any problem arises while following the installation guide, please get in touch with the email provided at the end of this file.

The IDS Metadata Broker consists of several Docker containers as shown in the image below, which need to be orchestrated by docker-compose.
![index](https://user-images.githubusercontent.com/38520537/125645191-55307611-1262-421a-97b6-c3b626246556.png)
**Note:** A video tutorial of the Broker's installation guide is available [here](https://www.youtube.com/watch?v=0zojF4ubEMI&t=864s).

### 4.1 Prepare The SSL Certificate
For the SSL certificate, you need to have these two files:
-  **server.crt:** an x509 certificate, either self-signed or from an official CA
- **server.key:** the private key for the certificate.

The certificate needs to be of *.crt* format and must have the name *server.crt* and the file for the private key should have the name *server.key*. Here is a sample command to create a self-signed certificate, which requires OpenSSL to be installed:
```sh
openssl req -newkey rsa:4096 \
            -x509 \
            -sha256 \
            -days 3650 \
            -nodes \
            -out server.crt \
            -keyout server.key
```
In case your certificate is in *.pem* format, it can be converted with the following commands:

			openssl x509 -in mycert.pem -out server.crt
			openssl rsa -in mykey.pem -out server.key
			mkdir cert
			mv server.crt cert/
			mv server.key cert/

### 4.2 Running The Broker
To run the broker you can either make use of docker images provided by us as shown in **Section 4.2.1** or build your customized docker as shown in **Section 4.2.2**.

#### 4.2.1 Running The Broker With Provided Image
If you want to run the broker with the provided image please follow the following steps:

**Step 1: Clone the repository**

	git clone https://github.com/International-Data-Spaces-Association/metadata-broker-open-core.git

**Step 2: Configure the docker-compose file**

Once the repository is cloned, the docker-compose file will be found in this path:

	`./docker/composefiles/broker-localhost/docker-compose.yml`

**Step 2.1** Please put the SSL certificate (server.key and server.crt) and the DAPS certificate, for example "isst-broker.jks", under the same folder which will be mounted in container. This repository provides a [sample DAPS certificate](https://github.com/International-Data-Spaces-Association/metadata-broker-open-core/blob/master/broker-core/src/main/resources/isstbroker-keystore.jks) to test the interaction with the Broker via Postman collection provided in **Section 4.3**.

**Step 2.2** Provide the correct location of the X.509 certificate created above in the "broker-reverseproxy" and "broker-core" services.

**For Linux users:**  if the location of the certificate is *“/home/ids/cert”*, the corresponding configuration in the yml file is:

	services: broker-reverseproxy:
		image: registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker/reverseproxy
		volumes:
		- /home/ids/cert:/etc/cert/
		[…]



**For Windows users:**  if the location of the certificate is *“c:/etc/ids/cert”*, the corresponding configuration in the yml file is:  


	services: broker-reverseproxy:
		image: registry.gitlab.cc-asp.fraunhofer.de:4567/eis-ids/broker/reverseproxy
		volumes:
		- c:/etc/ids/cert:/etc/cert/
		[…]
Do the same for "broker-core" services.

**Step 2.3** After successful completion of the previous steps, the location of the certificates is mounted to "/etc/cert" of the container. If the name of your DAPS certificate is "isstbroker-keystore.jks", please change the following line in the docker-compose file:
 


	environment:
		[…]
		- IDENTITY_JAVAKEYSTORE=/etc/cert/isstbroker-keystore.jks
	
Please note: only adapt the name of your certificate in the line.

**Step 3: Download the docker images**

All the IDS Metadata Broker Docker images are hosted at the GitLab of Fraunhofer IAIS. No credentials needed to download the images. The following command is for pulling all docker images:

		docker-compose pull

Note that this command should be executed in the same path of docker-compose.yml file.


**Step 4: Start up the IDS Metadata Broker**

To start up the IDS Metadata Broker, run the following command inside the directory of the docker-compose.yml file:

		docker-compose up –d



This process can take several minutes to complete. You can test whether the IDS Metadata Broker has successfully started by opening [https://localhost](https://localhost/). The result should be a JSON document, providing some general metadata about the IDS Metadata Broker.

Furthermore, the docker-compose logs command can be used to access the logs for a docker-compose.yml file, see [here](https://docs.docker.com/compose/reference/logs/).


**Step 5: Stop the IDS Metadata Broker**

To stop the Broker, run the following in the terminal in the same path as the docker-compose.yml file:

		docker-compose down

**Step 6: Update the IDS Metadata Broker**

To update an existing installation of the IDS Metadata Broker, first repeat the steps explained in **Step 3**. Containers can be either hot updated or restarted to apply the changes. To hot update a container, run the following command:

		docker-compose up -d --no-deps --build <container name>

Alternatively, one can restart the entire service by running:

		docker-compose down
		docker-compose up –d

#### 4.2.2 Running The Broker With Locally Built Image
You can also use a docker-compose file that uses locally built images. Please note that you need to have Maven installed for executing the script.  You can find a build script for the images in the docker directory: docker/buildImages.sh .
Once you have docker-compose file please follow **Step 4 - 5** in **Section 4.2.1** to run and stop the IDS Metadata Broker.

### 4.3 Interacting With The IDS Metadata Broker
The IDS Metadata Broker accepts and sends messages according to the IDS information model. This model uses the Resource Description Framework (RDF) to leverage the power of linked data. Many examples about representations of IDS concepts can be found at [https://github.com/International-Data-Spaces-Association/InformationModel/tree/develop/examples](https://github.com/International-Data-Spaces-Association/InformationModel/tree/develop/examples).

The multipart endpoint of IDS Metadata Broker is “/infrastructure”. If the IDS Metadata Broker is running using docker-compose as mentioned earlier, an HTTP POST or GET request can be sent to interact with it. We provide some example messages, illustrating all core functions of the IDS Metadata Broker in this  [postman collection](https://www.getpostman.com/collections/0a8f223c9141de195795).

In addition to the multipart endpoint, the IDS Metadata Broker also serves a prototypical [IDS-REST](https://www.getpostman.com/collections/01d6bf596f67303c08ce) endpoint at “/catalog”. This endpoint will reach a non-prototype state soon after the final specification of the IDS-REST protocol.

## 5. Building and deploying the MetaData-Broker

1. Execute to build the jar files:
    - `mvn clean package -U -Drevision=<new revision number here>`
2. To deploy, make sure that the particular credentials for the repositories are provided in the `settings.xml` or via the maven property `-s <path to the settings.xml>`.
   To deploy the artifacts execute:
    - to eis-ids-public repository `mvn deploy -DaltDeploymentRepository=eis-public-repo::default::https://maven.iais.fraunhofer.de/artifactory/eis-ids-public -Drevision=<new revision number here>`
    - to eis-ids-snapshot repository `mvn deploy -DaltDeploymentRepository=eis-snapshot-repo::default::https://maven.iais.fraunhofer.de/artifactory/eis-ids-snapshot  -Drevision=<new revision number here>`
    - to eis-ids-release repository `mvn deploy -DaltDeploymentRepository=eis-release-repo::default::http://maven.iais.fraunhofer.de/artifactory/eis-ids-release  -Drevision=<new revision number here>`

## API Description

see [Broker API in SwaggerHub](https://app.swaggerhub.com/apis/idsa/IDS-Broker/)

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - Application Framework
* [Apache Jena](https://jena.apache.org/documentation/) - Parsing and serializing RDF and Fuseki as triple store for meta data

## Contributions
IDS Broker has been part of:
* PLATOON H2020 project funded by the EU commission.
