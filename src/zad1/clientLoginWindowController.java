package zad1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class clientLoginWindowController {

    @FXML
    Label promptLabel;

    @FXML
    Button logButton;

    @FXML
    Button registerButton;

    @FXML
    TextField logField;

    @FXML
    PasswordField passField;

    @FXML
    void initialize(){
        promptLabel.setText("Please log in or register new account");
        registerButton.setOnMouseClicked(event -> {
            Client.sendCredentials(logField.getText(), passField.getText().hashCode(), "new");
            logField.clear();
            passField.clear();
        });
        logButton.setOnMouseClicked(event -> {
            Client.sendCredentials(logField.getText(), passField.getText().hashCode(), "old");
            logField.clear();
            passField.clear();
        });
    }
}
