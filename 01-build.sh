#!/bin/bash
mvn clean package
mvn package #| tee ./build.log
cd ./streampipes-extensions/streampipes-extensions-all-jvm
docker build -t fouo/gft-custom-extensions .
docker push fouo/gft-custom-extensions
cd ../../installer/cli
./streampipes env -s pipeline-element-gft
