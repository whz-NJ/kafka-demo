package com.mg.kafka.consumer;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author whz
 * @create 2020-12-16 8:09
 * @desc TODO: add description here
 **/
public class Consumer3 implements Runnable{
  private final AtomicBoolean closed = new AtomicBoolean(false);

  //Kafka 的 Java Producer 是线程安全的，你可以放心地在多个线程中共享同一个实例；而 Java Consumer 不是线程安全的
  private final KafkaConsumer<String, String> consumer;

  public Consumer3(String groupId, String brokerAddr, String topicName) {

    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none");
    consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());
    consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getName());
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddr);

    consumer = new KafkaConsumer<String, String>(consumerProperties);
    consumer.subscribe(Collections.singleton(topicName));
    consumer.poll(0); //为了连接集群,获取元数据
    consumer.partitionsFor(topicName).stream() //调用 partitionsFor 方法获取给定主题的所有分区
        .map(info -> new TopicPartition(topicName, info.partition())).forEach(tp ->
    {
      long committedOffset = consumer.committed(tp).offset(); //依次获取对应分区上的已提交位移
      consumer.seek(tp, committedOffset); //通过 seek 方法重设位移到已提交位移处（重置消费者位移）
    });
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

  // Shutdown hook which can be called from a separate thread
  public void shutdown() {
    closed.set(true);
    consumer.wakeup();
  }
}