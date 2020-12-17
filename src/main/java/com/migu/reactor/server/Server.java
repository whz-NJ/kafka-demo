package com.migu.reactor.server;

/**
 * @author whz
 * @create 2020-12-17 7:26
 * @desc TODO: add description here
 **/
public class Server {
  public static void main(String[] args) {
    new Thread(new Reactor()).start();
  }
}