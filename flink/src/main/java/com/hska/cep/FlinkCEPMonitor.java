package com.hska.cep;

import com.hska.cep.events.ErrorWarning;
import com.hska.cep.events.MonitoringEvent;
import com.hska.cep.events.TemperatureEvent;
import com.hska.cep.events.TemperatureWarning;
import com.hska.cep.timestamp.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.IngestionTimeExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer09;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

import java.util.Map;

/**
 * Note that the Kafka source is expecting the following parameters to be set
 *  - "bootstrap.servers" (comma separated list of kafka brokers)
 *  - "zookeeper.connect" (comma separated list of zookeeper servers)
 *  - "group.id" the id of the consumer group
 *  - "topic" the name of the topic to read data from.
 *
 * You can pass these required parameters using "--bootstrap.servers host:port,host1:port1 --zookeeper.connect host:port --topic testTopic"
 *
 * This is a valid input example:
 * 		--topic test --bootstrap.servers localhost:9092 --zookeeper.connect localhost:2181 --group.id myGroup
 *
 *
 */
public class FlinkCEPMonitor {

    private static final int ERROR_CODE = 999;
    private static final int MAX_TEMP = 40;

    public static void main (String args []) throws Exception {

        // parse user parameters
        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable event time processing
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // enable fault-tolerance
        env.enableCheckpointing(5000);

        //enable restarts
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(50, 500L));

        // enable filesystem state backend
        //env.setStateBackend(new FsStateBackend("file:///Users/patrickwiener/Development/tmp/flink-state-backend"));

        // run each operator separately
        env.disableOperatorChaining();

        // get data from kafka
        DataStream<MonitoringEvent> inputStream = env
                .addSource(new FlinkKafkaConsumer09<>(
                        parameterTool.getRequired("topicIn"),
                        new SimpleStringSchema(),
                        parameterTool.getProperties()))
                .name("Kafka 0.9 Source")
                .flatMap(new Splitter())
                .name("Parse messages")
                .assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());
//                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<MonitoringEvent>(Time.minutes(20L)) {
//
//                    @Override
//                    public long extractTimestamp(MonitoringEvent element) {
//                        return element.getTimestamp();
//                    }
//                }).name("Timestamp extractor");



        /**
         * Pattern definitions
         */
        // Patern #1: 3 consecutive measures exceeding MAX_TEMP and are not ERROR_CODE within 10 minutes
        Pattern<MonitoringEvent, ?> hotWarningPattern = Pattern.<MonitoringEvent>begin("first")
                .subtype(TemperatureEvent.class)
                .where(evt -> evt.getTemperature() >= MAX_TEMP && evt.getTemperature() != ERROR_CODE)
                .next("second")
                .subtype(TemperatureEvent.class)
                .where(evt -> evt.getTemperature() >= MAX_TEMP && evt.getTemperature() != ERROR_CODE)
                .next("third")
                .subtype(TemperatureEvent.class)
                .where(evt -> evt.getTemperature() >= MAX_TEMP && evt.getTemperature() != ERROR_CODE)
                .within(Time.minutes(10));

        // Pattern #2: whenever there is
        Pattern<MonitoringEvent, ?> errorPattern = Pattern.<MonitoringEvent>begin("first")
                .subtype(TemperatureEvent.class)
                .where(evt -> evt.getTemperature() == ERROR_CODE);

        /**
         * Pattern streams
         * Use defined patterns on input data stream
         */
        PatternStream<MonitoringEvent> hotTempPatternStream = CEP.pattern(
                inputStream.keyBy("sensorID"),
                hotWarningPattern
        );

        PatternStream<MonitoringEvent> errorPatternStream = CEP.pattern(
                inputStream.keyBy("sensorID"),
                errorPattern
        );

        /**
         * Derived data streams based on pattern
         */
        DataStream<TemperatureWarning> hotWarnings = hotTempPatternStream.select(
                (Map<String, MonitoringEvent> pattern) -> {
                    TemperatureEvent first = (TemperatureEvent) pattern.get("first");
                    TemperatureEvent second = (TemperatureEvent) pattern.get("second");
                    TemperatureEvent third = (TemperatureEvent) pattern.get("third");

                    return new TemperatureWarning(first.getSensorID(), (first.getTemperature() + second.getTemperature() + third.getTemperature()) / 3);
                }
        );

        DataStream<ErrorWarning> errorWarnings = errorPatternStream.select(
                (Map<String, MonitoringEvent> pattern) -> {
                    TemperatureEvent first = (TemperatureEvent) pattern.get("first");

                    return new ErrorWarning(first.getSensorID(), (int) first.getTemperature());
                }
        );

        // kafka data sinks
        hotWarnings.addSink(new FlinkKafkaProducer09<TemperatureWarning>(
                parameterTool.getRequired("bootstrap.servers"),
                parameterTool.getRequired("topicOutTemp"),
                new TempSerSchema())).name("Temperature warnings to Kafka");

        // kafka data sinks
        errorWarnings.addSink(new FlinkKafkaProducer09<ErrorWarning>(
                parameterTool.getRequired("bootstrap.servers"),
                parameterTool.getRequired("topicOutErr"),
                new ErrSerSchema())).name("Error warnings to Kafka");

        //inputStream.print();
        //hotWarnings.print();
        //errorWarnings.print();

        env.execute("CEP@HsKA");


    }

    public static class Splitter implements FlatMapFunction<String, MonitoringEvent> {

        public void flatMap(String input, Collector<MonitoringEvent> out) throws Exception {

            String [] token = input.split(";");
            int sensorId = Integer.parseInt(token[0]);
            long tstamp = Long.parseLong(token[1]);
            Double temperature = Double.parseDouble(token[2]);

            out.collect(new TemperatureEvent(sensorId, tstamp, temperature));
        }
    }

    public static class TempSerSchema implements SerializationSchema<TemperatureWarning> {

        @Override
        public byte[] serialize(TemperatureWarning tw) {
            return (tw.toString()).getBytes();
        }
    }

    public static class ErrSerSchema implements SerializationSchema<ErrorWarning> {

        @Override
        public byte[] serialize(ErrorWarning er) {
            return (er.toString()).getBytes();
        }
    }
}
