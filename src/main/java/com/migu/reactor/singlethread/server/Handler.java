package com.migu.reactor.singlethread.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

class Handler implements Runnable {
 
	private static final int READ_STATUS = 1;
 
	private static final int WRITE_STATUS = 2;
 
	private SocketChannel socketChannel;
 
	private SelectionKey selectionKey;
 
	private int status = READ_STATUS;
 
	public Handler(Selector selector, SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		try {
			socketChannel.configureBlocking(false);
			selectionKey = socketChannel.register(selector, 0);
			selectionKey.interestOps(SelectionKey.OP_READ);
			selectionKey.attach(this);
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	public void run() {
		System.out.println("enter Handler.run() ");
		try {
			if (status == READ_STATUS) {
				System.out.println("status=READ_STATUS\n");
				read();
				selectionKey.interestOps(SelectionKey.OP_WRITE); //设置感兴趣的 OP_WRITE 事件
				status = WRITE_STATUS;
			} else if (status == WRITE_STATUS) {
				System.out.println("status=WRITE_STATUS\n");
				process();
				selectionKey.cancel(); //取消 OP_WRITE 事件，否则会无限触发 OP_WRITE 事件
				System.out.println("服务器发送消息成功!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	public void read() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		socketChannel.read(buffer);
		System.out.println("接收到来自客户端（"
				+ socketChannel.socket().getInetAddress().getHostAddress()
				+ "）的消息：" + new String(buffer.array()));
	}
 
	public void process() throws IOException {
		String content = "Hello World!";
		ByteBuffer buffer = ByteBuffer.wrap(content.getBytes());
		socketChannel.write(buffer);
	}
}