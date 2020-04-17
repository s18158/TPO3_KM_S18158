package zad1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class clientWindowController {

    @FXML
    private TextField inputField;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextField loginField;

    @FXML
    private Button loginButton;

    @FXML
    void initialize() {
        inputField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)){
                Client.write(inputField.getText());
                inputField.clear();
            }
        });
        outputArea.setEditable(false);
        inputField.setEditable(false);
        outputArea.setDisable(true);
        inputField.setDisable(true);
        loginButton.setOnMousePressed(event -> {
            if (loginButton.getText().equals("Log in")) {
                Client.setLogedInAs(loginField.getText());
                loginField.clear();
                loginField.setEditable(false);
                outputArea.setDisable(false);
                inputField.setDisable(false);
                inputField.setEditable(true);
                loginButton.setText("Log Out");
            } else if (loginButton.getText().equals("Log Out")){
                loginField.setEditable(true);
                outputArea.clear();
                outputArea.setDisable(true);
                inputField.clear();
                inputField.setDisable(true);
                loginButton.setText("Log in");
            }
        });
    }

    void displayMsg(String s){
        outputArea.setText(s);
    }
}
