package com.migu.kafka.consumer;

import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author whz
 * @create 2020-12-16 8:23
 * @desc TODO: add description here
 **/
public class Consumers3 {
  private ExecutorService executors;
  private final static int WORKERS_NUM = 10;
  private LinkedList<Consumer3> consumers = new LinkedList<>();

  public Consumers3(String groupId, String  brokerAddr, String topicName, int numOfConsumers) {
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
      Consumer3 consumer3 = new Consumer3(groupId, brokerAddr, topicName);
      consumers.add(consumer3);
      executors.submit(consumer3);
    }
  }
  public void shutdown() {
    for(Consumer3 consumer3: consumers) {
      consumer3.shutdown();
    }
  }
}