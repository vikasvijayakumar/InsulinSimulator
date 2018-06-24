package controller;

import java.io.IOException;

import configuration.BSLCalculation;
import configuration.StaticValues;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConfigurationController {
	
	private String actEat = "Eat Food";
	private String actExercise = "Exercise";
	
	@FXML InsulinSimulatorController insulinSimulatorController;
	@FXML TextField currentBSLTextId;
	@FXML TextField carboId;
	@FXML ChoiceBox<String> cboxActivity;
	@FXML Label lblIPCarbo;
	
	@FXML
    void initialize() {	
		cboxActivity.getItems().clear();
		cboxActivity.getItems().add(actEat);
		cboxActivity.getItems().add(actExercise);
		cboxActivity.setValue(actEat);
		
		cboxActivity.setOnAction(e -> getChoice());
		currentBSLTextId.setText(Double.toString(StaticValues.CurrentBSL));
	}
	
	private void getChoice() {
		String selectedValue = cboxActivity.getValue();
		if(selectedValue == actExercise) {
			carboId.setVisible(false);
			lblIPCarbo.setVisible(false);
		}
		else {
			carboId.setVisible(true);
			lblIPCarbo.setVisible(true);
		}
		
	}

	public void configurationOkClick(ActionEvent event) throws IOException {
		double carbo = 0;
		String selectedValue = cboxActivity.getValue();
		
		if(selectedValue == actExercise) {
			carbo -= 30;
			InsulinSimulatorController.addMessage("Amount of Carbohydrates lost due to exercise : " + carbo + " g");
		}
		else {
			carbo = Double.parseDouble(carboId.getText());
			InsulinSimulatorController.addMessage("Amount of Carbohydrates taken : " + carbo + " g");
		}
		
		StaticValues.CarbohydrateIntake = carbo;
		
		double bsl = BSLCalculation.getInstance().bslAfterActivity(carbo, 0);
		System.out.println("Blood Sugar Level increased to " + bsl + " mg/dl");
		
		setInsulinControllerDefaultValue();
		
		StaticValues.PreviousBSL = StaticValues.CurrentBSL;
		StaticValues.CurrentBSL = bsl;
		StaticValues.TempBSL = bsl;
		
		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
		window.close();
	}
	
	private void setInsulinControllerDefaultValue() {
		InsulinSimulatorController.checkForBsl = true;
		InsulinSimulatorController.showHighBslMsg = true;
		InsulinSimulatorController.increasingCarboCounter = 1;
		InsulinSimulatorController.autoInjectionStarted = false;
		InsulinSimulatorController.inManualIdealDecrease = false;
		InsulinSimulatorController.inManualInject = false;
	}
	
	public void configurationCancelClick(ActionEvent event) throws IOException {
		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
		window.close();
	}
}
