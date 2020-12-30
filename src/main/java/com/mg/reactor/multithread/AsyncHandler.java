package com.mg.reactor.multithread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncHandler implements Runnable{
    /**
     * 被选中的selector
     */
    private Selector selector;
    /**
     * task socketChannel
     */
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    ExecutorService workers = Executors.newFixedThreadPool(5);
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer sendBuffer = ByteBuffer.allocate(2048);
 
    private final static int READ = 0;
    private final static int SEND = 1;
    private final static int PROCESSING = 2;
    private int status = READ;
 
    public AsyncHandler(SocketChannel socketChannel, Selector selector) {
        // socketChannel 为新接入的客户端连接， selector 为新的 Selector
        this.selector = selector;
        this.socketChannel = socketChannel;
        try {
            socketChannel.configureBlocking(false);
            // 将 socketChannel 注册到新的 Selector
            selectionKey = socketChannel.register(selector,0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 将 AsyncHandler 事务处理器和 Selector/SelectionKey 关联
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
 
    }
 
    // 由 Acceptor.reactors 线程池调用 SubReactor 调用的
    @Override
    public void run() {
        //对事件的处理
        switch (status){
            case READ:
                //读事件准备好
                read();
                break;
            case SEND:
                //发送事件准备好
                send();
                break;
            default:
        }
    }
 
    private void send() {
        if (selectionKey.isValid()){
            status = PROCESSING;
            workers.execute(this::sendWorker);
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }
 
    private void read() {
        if (selectionKey.isValid()){
            readBuffer.clear();
            try {
                int count = socketChannel.read(readBuffer);
                if (count > 0){
                    status = PROCESSING;
                    workers.execute(this::readWorker);
                }else{
                    selectionKey.cancel();
                    socketChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                selectionKey.cancel();
 
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
 
    //读入信息后的业务处理
    private void readWorker () {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("收到来自客户端的消息: %s",
                new String(readBuffer.array())));
        status = SEND;
        selectionKey.interestOps(SelectionKey.OP_WRITE); //注册写事件
        this.selector.wakeup(); //唤醒阻塞在select的线程，因为该interestOps写事件是放到子线程的，select在该channel还是对read事件感兴趣时又被调用，因此如果不主动唤醒，select可能并不会立刻select该读就绪事件（在该例中，可能永远不会被select到）
    }
 
    private void sendWorker() {
        try {
            sendBuffer.clear();
            sendBuffer.put(String.format("我收到来自%s的信息辣：%s,  200ok;",
                    socketChannel.getRemoteAddress(),
                    new String(readBuffer.array())).getBytes());
            sendBuffer.flip();
 
            int count = socketChannel.write(sendBuffer);
 
            if (count < 0) {
                selectionKey.cancel();
                socketChannel.close();
                System.out.println("send时-------连接关闭");
            } else {
                //再次切换到读
                status = READ;
            }
        } catch (IOException e) {
            System.err.println("异步处理send业务时发生异常！异常信息：" + e.getMessage());
            selectionKey.cancel();
            try {
                socketChannel.close();
            } catch (IOException e1) {
                System.err.println("异步处理send业务关闭通道时发生异常！异常信息：" + e.getMessage());
            }
        }
    }
}