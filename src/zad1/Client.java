/**
 *
 *  @author Kondej Mariusz S18158
 *
 */

package zad1;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Client extends Application implements Runnable{

  private static final int BUFFER_SIZE = 2048;
  private static SocketChannel client;
  private static clientWindowController myClientWindowController;
  private static ArrayList<String> history;
  private static Selector selector;
  private static Stage thisStage;

  @SuppressWarnings("unused")
  public static void main(String[] args) throws IOException{
    selector = Selector.open();
    InetSocketAddress socketAddress = new InetSocketAddress("localhost", 11000);
    client = SocketChannel.open(socketAddress);
    client.configureBlocking(false);
    int ops = client.validOps();
    SelectionKey selectionKey = client.register(selector, ops, null);
    history = new ArrayList<>();
    Thread thisThread = new Thread(new Client());
    thisThread.start();
    launch(args);

  }

  @Override
  //Listener
  public void run() {
      try{
          while (true) {
              selector.select();
              Set<SelectionKey> keys = selector.selectedKeys();
              Iterator<SelectionKey> iterator = keys.iterator();
              while (iterator.hasNext()) {
                  SelectionKey myKey = iterator.next();
                  iterator.remove();
                  if (!myKey.isValid()) {
                      continue;
                  }
                  if (myKey.isConnectable()) {
                      client = (SocketChannel) myKey.channel();
                      try {
                          client.finishConnect();
                      } catch (IOException e) {
                          myKey.cancel();
                          return;
                      }
                  } else if (myKey.isReadable()) {
                      client = (SocketChannel) myKey.channel();
                      ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                      if (client.read(buf) == -1) {
                          client.close();
                      } else {
                          client.read(buf);
                          String result = new String(buf.array()).trim();
                          log(result);
                          if (result.equals("Access_granted!")){

                            sceneChange(thisStage);
                          } else {
                              history.add(result);
                              StringBuilder sb = new StringBuilder();
                              history.forEach(s -> sb.append(s).append("\n"));
                              myClientWindowController.displayMsg(sb.toString());
                              log(history.toString().trim());
                              buf.clear();
                          }
                      }
                  }
              }
          }
      } catch (IOException e){
          e.printStackTrace();
      }
  }

  private void log(String s){
    System.out.println(s);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {

      thisStage = primaryStage;

      FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("./clientLoginWindow.fxml"));

      Parent loginRoot = loginLoader.load();


      Scene loginScene = new Scene(loginRoot, 332, 185);
      thisStage.setOnCloseRequest(e -> Platform.exit());
      thisStage.setScene(loginScene);
      thisStage.setTitle("Login");
      thisStage.show();

  }

  static void write (String s){

      byte[] msg = s.getBytes();
      ByteBuffer buffer = ByteBuffer.wrap(msg);
      try {
          client.write(buffer);
      } catch (IOException e) {
          e.printStackTrace();
      }
      buffer.clear();

  }

    static void sendCredentials (String login, int hashedPass, String ifNew){

        String message = ifNew+" "+login+" "+hashedPass;
        byte[] msg = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(msg);
        try {
            client.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.clear();

    }

    private void sceneChange(Stage stage) throws IOException{
        stage.hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("./clientWindow.fxml"));
        Parent root = loader.load();
        myClientWindowController = loader.getController();
        Scene newScene = new Scene(root,600, 400);
        stage.setScene(newScene);
        stage.setTitle("ChatNIO");
        stage.show();
    }

}