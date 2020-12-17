package com.migu.reactor.multithread;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class SubReactor  extends  AbstractReactor   implements Runnable {
    private Selector selector;

    public SubReactor(Selector selector) {
        this.selector = selector;
    }
    private boolean register = false;
 
    @Override
    public void run() {
 
        while (!Thread.interrupted()){
            System.out.println("等待注册中。。。。。。");
            //接收事件
            while(!Thread.interrupted()){
                try {
                    //如果没有事件准备好，继续监听，
                    if (selector.select() == 0){
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        //分发任务
                        dispatch(key);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public Selector getSelector() {
        return selector;
    }
}