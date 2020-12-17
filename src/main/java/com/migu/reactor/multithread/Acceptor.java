package com.migu.reactor.multithread;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

//只负责接收请求的组件
public class Acceptor implements Runnable {

    private int coreSize = Runtime.getRuntime().availableProcessors();

    private ServerSocketChannel serverSocketChannel;

    private Selector[] selectors = new Selector[coreSize];

    private SubReactor[] reactors = new SubReactor[coreSize];

    private Thread[] threads = new Thread[coreSize];

    private AtomicInteger index = new AtomicInteger();

    public Acceptor(ServerSocketChannel serverSocketChannel) throws IOException {

        this.serverSocketChannel = serverSocketChannel;
        for (int i = 0; i < coreSize; i++) {
            selectors[i] = Selector.open();
            reactors[i] = new SubReactor(selectors[i]);
            threads[i] = new Thread(reactors[i]);
            threads[i].start();
        }
    }

    @Override public void run() {
        SocketChannel socketChannel;
        try {
            socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println(String.format("收到%s的连接", socketChannel.getRemoteAddress()));
                socketChannel.configureBlocking(false);
                Selector selector = getSeletor();
                selector.wakeup();
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
                key.attach(new AsyncHandler(socketChannel, selector));

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Selector getSeletor() {
        return selectors[Math.abs(index.getAndIncrement() % selectors.length)];
    }
}
