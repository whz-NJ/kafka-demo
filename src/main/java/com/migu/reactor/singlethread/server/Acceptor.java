package com.migu.reactor.singlethread.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Acceptor implements Runnable {
	  private ServerSocketChannel serverSocketChannel;
	  private Selector selector;

	  public Acceptor(ServerSocketChannel serverSocketChannel, Selector selector) {
	  	this.serverSocketChannel = serverSocketChannel;
	  	this.selector = selector;
		}

		public void run() {
			try {
				//接入客户端连接请求，得到 SocketChannel
				SocketChannel socketChannel = serverSocketChannel.accept();
				if (socketChannel != null) {
					System.out.println("接收到来自客户端（"
							+ socketChannel.socket().getInetAddress()
									.getHostAddress() + "）的连接");
					new Handler(selector, socketChannel);
				}
 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}