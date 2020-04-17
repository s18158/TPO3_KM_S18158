/**
 *
 *  @author Kondej Mariusz S18158
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server {

  private static final int BUFFER_SIZE = 2048;


  @SuppressWarnings("unused")
  Server() throws IOException {

    Selector selector = Selector.open();
    ServerSocketChannel socketChannel = ServerSocketChannel.open();
    InetSocketAddress socketAddress = new InetSocketAddress("localhost", 11000);
    socketChannel.bind(socketAddress);
    socketChannel.configureBlocking(false);
    int ops = socketChannel.validOps();
    SelectionKey selectionKey = socketChannel.register(selector,ops, null);
    while(true){

      selector.select();
      Set<SelectionKey> keys = selector.selectedKeys();
      Iterator<SelectionKey> iterator = keys.iterator();

      while (iterator.hasNext()) {
        SelectionKey myKey = iterator.next();
        iterator.remove();
        SocketChannel client;
        if (!myKey.isValid()) {
          return;
        }
        if (myKey.isAcceptable()) {
          client = socketChannel.accept();
          client.configureBlocking(false);
          ByteBuffer channelBuffer = ByteBuffer.allocate(BUFFER_SIZE);
          client.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, channelBuffer);
        } else if (myKey.isReadable()) {

          client = (SocketChannel) myKey.channel();
          ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
          if (client.read(byteBuffer) == -1) {
            client.close();
          } else {
            byteBuffer.flip();
            Iterator<SelectionKey> it = selector.keys().iterator();
            SelectionKey thisKey;
            while (it.hasNext()){
              thisKey = it.next();
              ByteBuffer buf = (ByteBuffer)thisKey.attachment();
              if (buf!=null){
                buf.put(byteBuffer);

                thisKey.interestOps(SelectionKey.OP_WRITE|SelectionKey.OP_READ);
                byteBuffer.rewind();
              }
            }
            byteBuffer.clear();
          }


        } else if (myKey.isWritable()){
          client = (SocketChannel) myKey.channel();
          ByteBuffer buf = (ByteBuffer)myKey.attachment();
          buf.flip();
          buf.rewind();
          client.write(buf);
          if (buf.hasRemaining()){
            buf.compact();
          } else {
            buf.clear();
            myKey.interestOps(SelectionKey.OP_READ);
          }
        }
      }
    }
  }

  public static void main(String[] args) throws IOException{
    new Server();
  }
}