package com.migu.reactor.singlethread.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
 
public class Reactor implements Runnable {
 
	private ServerSocketChannel serverSocketChannel = null;
 
	private Selector selector = null;
 
	public Reactor() {
		try {
			// Selector是非阻塞的IO的核心
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open(); //创建 serverSocketChannel
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(8888));
			// 将 ServerSocketChannel 注册到Selector上，得到 SelectionKey，
			// 调用 SelectionKey.attach() 将channel事件和处理类关联
			// ServerSocketChannel 可以监听多个客户端连接
			// 每个channel只对应一个SelectionKey，所以如果要改变感兴趣事件，
			// 只能改变 interestOps()，不能register()和cancel()。
			// 一个 Selector 对应多个 Channel（包括 ServerSocketChannel 和 SocketChannel）
			SelectionKey selectionKey = serverSocketChannel.register(selector,
					SelectionKey.OP_ACCEPT); // serverSocketChannel绑定监听端口，得到SelectionKey
			selectionKey.attach(new Acceptor(serverSocketChannel, selector));
			System.out.println("服务器启动正常!");
		} catch (IOException e) {
			System.out.println("启动服务器时出现异常!");
			e.printStackTrace();
		}
	}
 
	public void run() {
		while (true) {
			try {
				selector.select();
 
				Iterator<SelectionKey> iter = selector.selectedKeys()
						.iterator();
				while (iter.hasNext()) {
					SelectionKey selectionKey = iter.next();
					dispatch((Runnable) selectionKey.attachment());
					iter.remove();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	public void dispatch(Runnable runnable) {
		if (runnable != null) {
			runnable.run();
		}
	}
 

}