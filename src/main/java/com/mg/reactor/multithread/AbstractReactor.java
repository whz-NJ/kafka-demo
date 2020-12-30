package com.mg.reactor.multithread;

import java.nio.channels.SelectionKey;

//主从Reactor的抽象方法
public abstract class AbstractReactor implements Runnable {
    @Override
    public void run() {
    }
    protected void dispatch(SelectionKey key){
            try {
                Runnable task = (Runnable) key.attachment();
                if (task != null){
                    task.run();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
    }
}
