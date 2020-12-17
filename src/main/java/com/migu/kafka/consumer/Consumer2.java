package com.migu.kafka.consumer;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author whz
 * @create 2020-12-16 7:27
 * @desc TODO: add description here
 **/
public class Consumer2 implements Runnable {
  private final AtomicBoolean closed = new AtomicBoolean(false);

  //Kafka 的 Java Producer 是线程安全的，你可以放心地在多个线程中共享同一个实例；而 Java Consumer 不是线程安全的
  private final KafkaConsumer<String, String> consumer;

  public Consumer2(String groupId, String brokerAddr, String topicName) {

    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());
    consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddr);

    consumer = new KafkaConsumer<String, String>(consumerProperties);
    consumer.subscribe(Collections.singleton(topicName));
    consumer.poll(0); //为了连接集群,获取元数据
    consumer.seekToBeginning(consumer.partitionsFor(topicName).stream().map( // seekToBeginning重置消费者位移
        partitionInfo -> new TopicPartition(topicName,
            partitionInfo.partition())).collect(Collectors.toList()));

    final Set<TopicPartition> assignment = consumer.assignment();
    for(TopicPartition tp : assignment) {
      System.out.println("consumer: " + this.toString() + " got topicPartition to consume are:" + tp.topic());
    }
  }

  @Override public void run() {
    try {
      while (!closed.get()) {
        ConsumerRecords<String, String> records = consumer
            .poll(Duration.ofSeconds(100));                 //  执行消息处理逻辑
        for (ConsumerRecord<String, String> record : records) {
          JSONObject jsonObject = (JSONObject) JSONObject.parse(record.value());
          jsonObject.fluentPut("traceId", "xxx-yyyy-zzz");
          System.out.printf("[%s] offset = %d, key = %s, value = %s%n",
              Thread.currentThread().getName(), record.offset(), record.key(),
              jsonObject.toJSONString());
          Thread.sleep(
              800); // 模拟耗时业务处理（大于 max.poll.interval.ms），抛 CommitFailedException
        }
        consumer.commitAsync();
        consumer.assignment();
      }
    }
    catch (Exception e) {             // Ignore exception if closing
      System.out.println("exception occurs during processing kafka message.");
      e.printStackTrace();
      try {
        consumer.commitSync();
      }
      catch (Exception e2) {
        System.out.println("failed to call commitSync()");
        e2.printStackTrace();
      }
    }
    finally { consumer.close(); }

    System.out.println(Thread.currentThread().getName() + " exited.");
  }
}