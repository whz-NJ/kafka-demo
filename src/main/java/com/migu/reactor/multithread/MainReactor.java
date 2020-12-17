package com.migu.reactor.multithread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

//用于接收的MainReactor
public class MainReactor extends AbstractReactor implements Runnable {
    //多路复用器
    private Selector selector;
 
    public MainReactor(ServerSocketChannel serverSocketChannel,int port) {
        try {
            this.selector =Selector.open();
            //绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            //配置非阻塞模式
            serverSocketChannel.configureBlocking(false);
 
            SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //依附处理Handler
            key.attach(new Acceptor(serverSocketChannel));
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    @Override
    public void run() {
 
        try {
            while(!Thread.interrupted()){
                selector.select();
                //遍历准备好的事件
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    //分发任务
                    dispatch(key);
                }
 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
 
}