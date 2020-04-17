package zad1;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class clientWindowController {

    @FXML
    private TextField inputField;

    @FXML
    private TextArea outputArea;

    @FXML
    private ScrollBar textScroll;

    @FXML
    void initialize(){
        inputField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)){
                Client.write(inputField.getText());
                inputField.clear();
            }
        });
        //outputArea.setWrapText(true);
        //outputArea.setEditable(false);


    }

    void displayMsg(String s){
        outputArea.setText(s);
    }
}
