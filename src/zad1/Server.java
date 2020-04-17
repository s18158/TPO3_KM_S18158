/**
 *
 *  @author Kondej Mariusz S18158
 *
 */

package zad1;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server{

  private static final int BUFFER_SIZE = 2048;
  private static String credentials = "data.txt";
  private static String pathToCredentials = "./"+ credentials;


  @SuppressWarnings("unused")
  public static void main(String[] args) throws IOException {

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
          log("Connection accepted: " + client.getLocalAddress() + "\n");
        } else if (myKey.isReadable()) {

          client = (SocketChannel) myKey.channel();
          ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
          log("what is in read: "+client.read(byteBuffer));
          if (client.read(byteBuffer) == -1) {
            client.close();
          } else {
            ByteBuffer bb = (ByteBuffer)myKey.attachment();
            String[] s = new String(bb.array()).trim().split("\\s+");
            for (String value : s) {
              log("s#="+value);
            }
            if (s[0].equals("new")){
              s[0] = "old";
              BufferedWriter writer = new BufferedWriter(new FileWriter(credentials));
              StringBuilder sb = new StringBuilder();
              for (String value : s) {
                sb.append(value).append(" ");
              }
              writer.write(sb.toString());
              writer.close();
            }else if (s[0].equals("old")) {
              log("jestem w old");
              BufferedReader reader = new BufferedReader(new FileReader(credentials));
              StringBuilder sb = new StringBuilder();
              for (String value : s) {
                sb.append(value).append(" ");
              }
              String foo = sb.toString();
              log("foo="+foo);
              String line = reader.readLine();
              while(line!=null){
                log("line="+line);
                if (foo.equals(line)){
                  log("foo equals");
                  String response = "Access_granted!";
                  ByteBuffer buf = (ByteBuffer) myKey.attachment();
                  buf.put(response.getBytes());
                  myKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                  buf.rewind();
                }
                line = reader.readLine();
              }
              reader.close();

            }else {
              byteBuffer.flip();
              Iterator<SelectionKey> it = selector.keys().iterator();
              SelectionKey thisKey;
              while (it.hasNext()) {
                thisKey = it.next();
                ByteBuffer buf = (ByteBuffer) thisKey.attachment();
                if (buf != null) {
                  buf.put(byteBuffer);

                  thisKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                  byteBuffer.rewind();
                }
              }
              byteBuffer.clear();
            }
          }


        } else if (myKey.isWritable()){
          client = (SocketChannel) myKey.channel();
          ByteBuffer buf = (ByteBuffer)myKey.attachment();
          buf.flip();
          buf.rewind();
            log("send: "+new String(buf.array()).trim());
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

  private static void log(String s){
    System.out.println(s);
  }

}