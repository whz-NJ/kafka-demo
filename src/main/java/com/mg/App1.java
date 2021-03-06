package com.mg;

import com.alibaba.fastjson.JSONObject;
import com.mg.kafka.consumer.Consumers1;
import com.mg.kafka.entity.Address;
import com.mg.kafka.entity.UserInfo;
import com.mg.kafka.producer.Producer1;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 *
 */
public class App1
{
    private final static int WORKERS_NUM = 2;
    private final static AtomicBoolean closed = new AtomicBoolean(false);

    public static void main( String[] args )
    {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger integer = new AtomicInteger();
            @Override public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "producer-" + integer.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        };
        ExecutorService executors = new ThreadPoolExecutor(WORKERS_NUM, WORKERS_NUM, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1000), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

        executors.submit(new Runnable() {
            @Override public void run() {
                Producer1 producer1 = new Producer1("10.148.159.200:8888,10.148.159.201:8889,10.148.159.204:8890", "whz_test");
                int cnt = 0;
                while (!closed.get()) {
                    String key = UUID.randomUUID().toString();
                    Address address = new Address();
                    address.setCity("city_"+cnt);
                    address.setCountry("CN");
                    address.setStreet("street_"+cnt);
                    address.setPostCode(String.valueOf(cnt));
                    UserInfo userInfo = new UserInfo();
                    userInfo.setAddress(address);
                    userInfo.setCredit(cnt);
                    userInfo.setName("name_" + cnt);
                    String value = JSONObject.toJSONString(userInfo);
                    producer1.sendMsg(key, value);
                    try {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName() + " exited.");
            }
        });
        Consumers1 consumers1 = new Consumers1("group1",
            "10.148.159.200:8888,10.148.159.201:8889,10.148.159.204:8890", "whz_test", 10);
        Scanner input = new Scanner(System.in);
        System.out.print("press any key to exit.\n");
        while (true) {
            String next = input.next();
            if(next != null && next.length() > 0) {
                closed.set(true);
                consumers1.shutdown();
                break;
            }
        }
    }
}
