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

    // 由 MainReactor 构造方法生成 Acceptor 实例
    public Acceptor(ServerSocketChannel serverSocketChannel) throws IOException {

        this.serverSocketChannel = serverSocketChannel;
        for (int i = 0; i < coreSize; i++) {
            selectors[i] = Selector.open();
            reactors[i] = new SubReactor(selectors[i]);
            threads[i] = new Thread(reactors[i]);
            threads[i].start();
        }
    }

    // 由 MainReactor.run() 所在线程调用
    @Override public void run() {
        SocketChannel socketChannel;
        try {
            // 接受客户端连接请求，得到 socketChannel 对象
            socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println(String.format("收到%s的连接", socketChannel.getRemoteAddress()));
                socketChannel.configureBlocking(false);
                // 获取一个新的Selector
                Selector selector = getSeletor();
                selector.wakeup();
                // 将Channel注册到 新的 Selector 上，得到 SelectionKey
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
                // 调用 SelectionKey.attach() 将事件处理器和 Selector/SelectionKey 关联
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
