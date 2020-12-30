package com.mg.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @author whz
 * @create 2020-12-15 15:37
 * @desc TODO: add description here
 **/
public class Producer1 {
  //Kafka 的 Java Producer 是线程安全的，你可以放心地在多个线程中共享同一个实例；而 Java Consumer 不是线程安全的
  private KafkaProducer producer;
  private String topicName;

    public Producer1(String brokerAddr, String topicName) {
      Properties props = new Properties();
      props.put("bootstrap.servers", brokerAddr); // localhost:9092
      props.put("partitioner.class", "com.mg.kafka.producer.RandomPartitioner");
      props.put("linger.ms", 1000);     //批量发送配置
      props.put("batch.size", 1024*10); //批量发送配置
      props.put("max.request.size", 5000); // 请求的最大字节数
      props.put("acks", "all");
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transactional_id-0");
      // 开启GZIP压缩
      props.put("compression.type", "gzip");
      producer = new KafkaProducer<>(props);
      producer.initTransactions();
      this.topicName = topicName;
    }

    public void sendMsg(String key, String msg) {
      ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key,msg);//Topic Key Value
      try{
        producer.beginTransaction(); //消息队列事务操作
        Future future = producer.send(record);
        future.get();//不关心是否发送成功，则不需要这行。
        //TODO 加入DB数据库操作
        producer.commitTransaction();
      } catch(Exception e) {
        producer.abortTransaction();
        e.printStackTrace();//连接错误、No Leader错误都可以通过重试解决；消息太大这类错误kafkaProducer不会进行任何重试，直接抛出异常
      }
    }
}