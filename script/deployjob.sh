#!/bin/bash

/usr/local/flink/bin/flink run -d -c com.hska.cep.FlinkCEPMonitor /home/flink-cep-hska-1.0-SNAPSHOT.jar --topicIn atrium --topicOutTemp temp-warning --topicOutErr error-warning --bootstrap.servers $KAFKA_PORT_9092_TCP_ADDR:9092 --zookeeper.connect $ZOOKEEPER_PORT_2181_TCP_ADDR:2181 --group.id hska
