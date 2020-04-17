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

public class Server{

  private static Selector selector;
  private static boolean running = true;
  private static final int BUFFER_SIZE = 2048;
  private static ByteBuffer newMsg = ByteBuffer.allocate(BUFFER_SIZE);
  private static ArrayList<String> history = new ArrayList<>();
  //private static Map<Integer, ByteBuffer>  pending = new HashMap<>();
  private static Set<msgHolder> pending = new HashSet<>();


  @SuppressWarnings("unused")
  public static void main(String[] args) throws IOException {

    selector = Selector.open();
    ServerSocketChannel socketChannel = ServerSocketChannel.open();
    InetSocketAddress socketAddress = new InetSocketAddress("localhost", 11000);
    socketChannel.bind(socketAddress);
    socketChannel.configureBlocking(false);
    int ops = socketChannel.validOps();
    SelectionKey selectionKey = socketChannel.register(selector,ops, null);
    while(running){

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
          if (client.read(byteBuffer) == -1) {
            client.close();
          } else {
            byteBuffer.flip();
            Iterator<SelectionKey> it = selector.keys().iterator();
            SelectionKey thisKey = null;
            while (it.hasNext()){
              thisKey = it.next();
              ByteBuffer buf = (ByteBuffer)thisKey.attachment();
              if (buf!=null){
                buf.put(byteBuffer);

                thisKey.interestOps(SelectionKey.OP_WRITE|SelectionKey.OP_READ);
                byteBuffer.rewind();
              }
            }
            //pending.add(new msgHolder(keys.size(), byteBuffer));
            byteBuffer.clear();
          }


        } else if (myKey.isWritable()){
          client = (SocketChannel) myKey.channel();
          ByteBuffer buf = (ByteBuffer)myKey.attachment();
          buf.flip();
          buf.rewind();
            log(new String(buf.array()).trim());
          client.write(buf);
          if (buf.hasRemaining()){
            buf.compact();
          } else {
            buf.clear();
            myKey.interestOps(SelectionKey.OP_READ);
          }

          /*pending.forEach(holder ->{
            if (holder.getHowManyClients()==0){
              pending.remove(holder);
            } else if (holder.getHowManyClients()>0){
              try {
                client.write(holder.getMsg());
                holder.decreaseClients();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          });*/
        }
      }

    }
  }

  private static void log(String s){
    System.out.println(s);
  }

  private static boolean isEmpty(ByteBuffer buf) {
    return buf == null || buf.remaining() == 0;
  }

}

class msgHolder{

  private static int howManyClients;
  private static ByteBuffer msg;

  msgHolder(int clients, ByteBuffer message){
    howManyClients = clients;
    msg = message;
  }

  ByteBuffer getMsg(){
    log("getMsg: "+howManyClients);
    return msg;
  }

  void decreaseClients(){howManyClients--;}

  int getHowManyClients(){return howManyClients;}

  private static void log(String s){
    System.out.println(s);
  }
}