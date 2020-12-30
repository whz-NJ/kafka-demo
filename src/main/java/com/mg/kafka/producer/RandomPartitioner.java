package com.mg.kafka.producer;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author whz
 * @create 2020-12-15 15:43
 * @desc TODO: add description here
 **/
public class RandomPartitioner implements Partitioner {

  @Override public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
    List partitions = cluster.partitionsForTopic(topic);
    return ThreadLocalRandom.current().nextInt(partitions.size());
  }

  @Override public void close() {

  }

  @Override public void configure(Map<String, ?> map) {

  }
}