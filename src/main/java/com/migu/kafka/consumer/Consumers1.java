package com.migu.kafka.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author whz
 * @create 2020-12-15 16:21
 * @desc TODO: add description here
 **/
public class Consumers1 {
  private ExecutorService executors;
  private final static int WORKERS_NUM = 10;
  private LinkedList<Consumer1> consumers = new LinkedList<>();

  public Consumers1(String groupId, String  brokerAddr, String topicName, int numOfConsumers) {
    ThreadFactory threadFactory = new ThreadFactory() {
      private final AtomicInteger integer = new AtomicInteger();
      @Override public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "consumer-" + integer.getAndIncrement());
        thread.setDaemon(false);
        return thread;
      }
    };

    executors = new ThreadPoolExecutor(WORKERS_NUM, WORKERS_NUM, 0L, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<Runnable>(1000), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    for(int i =0; i< numOfConsumers; i++) {
      Consumer1 consumer1 = new Consumer1(groupId, brokerAddr, topicName);
      consumers.add(consumer1);
      executors.submit(consumer1);
    }
  }
  public void shutdown() {
    for(Consumer1 consumer1: consumers) {
      consumer1.shutdown();
    }
  }
}