package controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
	@FXML TextField txtUserName;
	@FXML TextField txtPassword;
	@FXML Label lblErrorMsg;
	@FXML InsulinSimulatorController insulinSimulatorController;

	@FXML
    void initialize() {	
	}
	
	public void loginClick(ActionEvent event) throws IOException {
		String userName = txtUserName.getText();
		String password = txtPassword.getText();
		
		if(userName.equals("Doctor") && password.equals("123")) {
			InsulinSimulatorController.isAutoMode = false;
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.close();
		}
		else {
			lblErrorMsg.setText("Login Failed.");
		}
	}
	
	public void btnCancelClick(ActionEvent event) {
		InsulinSimulatorController.isAutoMode = true;
		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
		window.close();
	}
}
