/**
 *
 *  @author Kondej Mariusz S18158
 *
 */

package zad1;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                new Server();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(5000);
        new Thread(() -> {
            try {
                new Client();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(1000);
        new Thread(() -> {
            try {
                new Client();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
