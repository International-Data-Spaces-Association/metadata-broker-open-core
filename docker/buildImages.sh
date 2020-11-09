#!/bin/bash

dos2unix broker-core/*

# GENERIC IMAGES

mvn -f ../ clean package
cp ../broker-core/target/broker-core-*.jar broker-core/
docker build broker-core/ -t ids.iais.fraunhofer.de:5000/ids/eis-broker

#cleanup
rm -rf ../index-common/target
rm -rf ../broker-common/target


# fuseki
docker build fuseki/ -t ids.iais.fraunhofer.de:5000/ids/eis-broker-fuseki

# reverseproxy
docker build reverseproxy/ -t ids.iais.fraunhofer.de:5000/ids/eis-broker-reverseproxy
