/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

/**
 * Note that the Kafka source is expecting the following parameters to be set
 * - "bootstrap.servers" (comma separated list of kafka brokers)
 * - "group.id" the id of the consumer group
 * - "topic" the name of the topic to read data from.
 * <p>
 * <p>
 * This is a valid input example:
 * --topic test --bootstrap.servers localhost:9092 --group.id myGroup
 */
public class CountFromKafka {

    public static void main(String[] args) throws Exception {
        // create execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // parse user parameters
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        Properties properties = parameterTool.getProperties();
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DataStream<String> messageStream = env.addSource(new FlinkKafkaConsumer010<>(parameterTool.getRequired("topic"), new SimpleStringSchema(),
                                                                                     properties));

        messageStream.rebalance()
                     .map((MapFunction<String, Integer>) s -> 1)
                     .timeWindowAll(Time.seconds(10))
                     .reduce((ReduceFunction<Integer>) (t1, t2) -> t1 + t2).print();
        env.execute();
    }
}
