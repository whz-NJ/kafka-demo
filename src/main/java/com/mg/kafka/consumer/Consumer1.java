package com.mg.kafka.consumer;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer1 implements Runnable {
  private final AtomicBoolean closed = new AtomicBoolean(false);

  //Kafka 的 Java Producer 是线程安全的，你可以放心地在多个线程中共享同一个实例；而 Java Consumer 不是线程安全的
  private final KafkaConsumer<String, String> consumer;

  public Consumer1(String groupId, String brokerAddr, String topicName) {
    Properties props = new Properties();
    props.put("bootstrap.servers", brokerAddr);
    props.put("group.id", groupId);
    props.put("max.poll.records", 100);
    props.put("max.poll.interval.ms", 100); //
    props.put("enable.auto.commit", false);//设置是否为自动提交
    props.setProperty("auto.offset.reset", "latest"); // earliest
    props.put("key.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    consumer = new KafkaConsumer<String, String>(props);
    consumer.subscribe(Arrays.asList(topicName));

    final Set<TopicPartition> assignment = consumer.assignment();
    for(TopicPartition tp : assignment) {
      System.out.println("consumer: " + this.toString() + " got topicPartition to consume are:" + tp.topic());
    }
  }

  public void run() {
    try {
      while (!closed.get()) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(100));                 //  执行消息处理逻辑
        for (ConsumerRecord<String, String> record : records) {
          JSONObject jsonObject = (JSONObject)JSONObject.parse(record.value());
          jsonObject.fluentPut("traceId", "xxx-yyyy-zzz");
          System.out.printf("[%s] offset = %d, key = %s, value = %s%n",
              Thread.currentThread().getName(), record.offset(), record.key(),
              jsonObject.toJSONString());
          Thread.sleep(800); // 模拟耗时业务处理（大于 max.poll.interval.ms），抛 CommitFailedException
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

  // Shutdown hook which can be called from a separate thread
  public void shutdown() {
    closed.set(true);
    consumer.wakeup();
  }
}