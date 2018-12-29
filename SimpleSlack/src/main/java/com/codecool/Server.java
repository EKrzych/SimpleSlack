package com.codecool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {
    private Selector selector = Selector.open();
    private ServerSocketChannel serverSocketChannel;

    public Server(ServerSocketChannel serverSocketChannel, InetSocketAddress inetSocketAddress) throws IOException {

        this.serverSocketChannel = serverSocketChannel;
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(inetSocketAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    protected void handleConnection() throws IOException {
        while(true) {
            System.out.println("Waiting for connection");
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while(keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if(key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel channel = server.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println("Connection accepted");
                } else if(key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    key.interestOps(SelectionKey.OP_WRITE);
                } else if(key.isWritable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    buffer.flip();
                    channel.write(buffer);

                    if(buffer.hasRemaining()) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                    }
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }
}
