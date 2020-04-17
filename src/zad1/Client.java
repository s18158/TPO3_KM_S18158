/**
 *
 *  @author Kondej Mariusz S18158
 *
 */

package zad1;


import javafx.application.Application;
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
    private static String logedInAs;
    private static String[] myArgs;

    Client() throws IOException{
        selector = Selector.open();
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 11000);
        client = SocketChannel.open(socketAddress);
        client.configureBlocking(false);
        int ops = client.validOps();
        client.register(selector, ops, null);
        history = new ArrayList<>();
        //new Thread(new Client()).start();
        launch(myArgs);

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
                            e.printStackTrace();
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
                            history.add(result);
                            StringBuilder sb = new StringBuilder();
                            history.forEach(s-> sb.append(logedInAs).append(": ").append(s).append("\n"));
                            myClientWindowController.displayMsg(sb.toString());
                            log(history.toString().trim());
                            buf.clear();
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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("./clientWindow.fxml"));
        Parent root = loader.load();
        myClientWindowController = loader.getController();
        Scene scene = new Scene(root,600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ChatNIO");
        primaryStage.show();

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

    static void setLogedInAs(String s){
        logedInAs = s;
    }

    public static void main(String[] args) throws IOException{
        myArgs=args;
        new Thread(new Client()).start();
    }
}