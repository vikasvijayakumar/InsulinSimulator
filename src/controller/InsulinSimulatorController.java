package controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


import com.sun.javafx.scene.control.skin.ProgressIndicatorSkin;

import configuration.StaticValues;
import configuration.BSLCalculation;
import configuration.Battery;
import configuration.InsulinGlucagonCalculation;
import configuration.InsulinGlucagonReservior;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;


public class InsulinSimulatorController {

	static int counter = 0;
	static int maxDataPoint = 0;
	
	// properties for insulin and glucagon level
	static float insulinLevel = 100;
	static float glucagonLevel = 100;
	
	// properties to check raise in BSL
	static int decreasingCarboCounter = 0;
	static int increasingCarboCounter = 1;
	
	protected static boolean checkForBsl = false;
	protected static boolean showHighBslMsg = true;
	protected static boolean showStableBslMsg = true;
	
	public static boolean isAutoMode = true;
	public static boolean autoInjectionStarted = false;
	
	public static boolean inManualInject = false;
	public static boolean inManualIdealDecrease = false;
	
	static XYChart.Series<String, Number> sugarLevelSeries;
	static XYChart.Series<String, Number> minSugarLevelSeries;
	static XYChart.Series<String, Number> maxSugarLevelSeries;
	static LineChart<String, Number> _bloodSugarLevelChart;
	
	private static ObservableList<Text> msgBoxItems = FXCollections.observableArrayList();
	
	private BSLCalculation bslCalculation;
	public InsulinGlucagonCalculation insulinGlucagonCalculation;
	public Battery batteryClass;
	public InsulinGlucagonReservior insulinGlucagonReservior;
	
	@FXML LineChart<String, Number> bloodSugarLevelChart;
	@FXML Group grpManualInj;
	
	@FXML ImageView imgBattery;
	@FXML ImageView imgInsulinRefill;
	@FXML ListView<Text> msgBox;
	
	@FXML TextField currentBSLTextBox;
	@FXML TextField previousBSLTextBox;
	@FXML TextField txtInsulinInj;
	@FXML TextField txtGlucagonInj;
	@FXML NumberAxis yAxis;
	@FXML ProgressIndicator InsulinPgIc;
	@FXML ProgressBar InsulinProgressBar;
	@FXML ProgressIndicator GlucagonPgIc;
	@FXML ProgressBar GlucagonProgressBar;
	@FXML Button btnAuto;
	
	@FXML
    void initialize() {		
		grpManualInj.setVisible(false);
		msgBox.setItems(msgBoxItems);
		
		currentBSLTextBox.setText(Integer.toString(StaticValues.InitialBSL));
		previousBSLTextBox.setText(Integer.toString(StaticValues.InitialBSL));
		
		setInsulinAndGlucagonIndicator(InsulinPgIc, InsulinProgressBar);
		setInsulinAndGlucagonIndicator(GlucagonPgIc, GlucagonProgressBar);
		
		InitialiseBSLLineChart();
		
		// Battery TimeLine
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(30000), x -> changeBatteryIcon()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play(); 
        
        // BSL TimeLine
        Timeline bslTimeline = new Timeline();
        bslTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), (ActionEvent actionEvent) -> InitializeBSLSimulator()));
        bslTimeline.setCycleCount(Animation.INDEFINITE);
        bslTimeline.play(); 
        
        addMessage("Auto Mode turned on ", Color.GREY);
    }

	// The method to add messages to list item
	public static void addMessage(String message) {
		addMessage(message, Color.GREY);
	}
	
	// The method to add messages to list item
	public static void addMessage(String message, Color color) {
		Text msg = new Text(message);
		msg.setStroke(color);
		msg.setFont(new Font(15));
		msgBoxItems.add(msg);
		
		if (msgBoxItems.size() > 9) {
			msgBoxItems.remove(0);
		}

	}
	
	// Initializes the Insulin and Glucagon progress bar and indicator
	private void setInsulinAndGlucagonIndicator(ProgressIndicator pgIndicator, ProgressBar pgBar) {
		try {
			pgIndicator.progressProperty().bind(pgBar.progressProperty());
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
		setProgressIndicator(pgIndicator);
		pgBar.setProgress(1);	
	}
	
	// The method will change the 'Done' text of progress indicator to 100%
	public void setProgressIndicator(ProgressIndicator pgIndicator) {
		ProgressIndicatorSkin pis = new ProgressIndicatorSkin(pgIndicator);
		pgIndicator.skinProperty().set(pis);
		pgIndicator.progressProperty().addListener(new ChangeListener<Number>( ) {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if(newValue.doubleValue() >= 1) {
					((Text) pgIndicator.lookup(".text.percentage")).setText("100%");
				}			
			}		
		});
	}
	
	// The method will initialize the Blood sugar Level simulator.
	public void InitialiseBSLLineChart() {
		addMessage("Initializing Blood Sugar Level Simulator........");
		sugarLevelSeries = new XYChart.Series<String, Number>();
		sugarLevelSeries.setName("Current Blood Sugar Level");
		
		maxSugarLevelSeries = new XYChart.Series<String, Number>();
		maxSugarLevelSeries.setName("Maximum Blood Sugar Level (" + StaticValues.MaximumBloodSugarLevel + ")");
		
		minSugarLevelSeries = new XYChart.Series<String, Number>();
		minSugarLevelSeries.setName("Minimum Blood Sugar Level (" + StaticValues.MinimumBloodSugarLevel + ")");
		
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(250);
		yAxis.setTickUnit(50);
		yAxis.setLabel("Blood Sugar Level");
		
		
		if(bloodSugarLevelChart != null) {
			addMessage("The Blood Sugar Level is Stable :  " +  StaticValues.CurrentBSL + "mg/dl", Color.GREEN);
			
			_bloodSugarLevelChart = bloodSugarLevelChart;	
			DateTimeFormatter dtformat = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime currentTime = LocalDateTime.now();
			String time1 = dtformat.format(currentTime).toString();
			minSugarLevelSeries.getData().add(new Data<String, Number>(time1, StaticValues.MinimumBloodSugarLevel));
			sugarLevelSeries.getData().add(new Data<String, Number>(time1, StaticValues.CurrentBSL));
			maxSugarLevelSeries.getData().add(new Data<String, Number>(time1, StaticValues.MaximumBloodSugarLevel));
			
			_bloodSugarLevelChart.getData().add(sugarLevelSeries);
			_bloodSugarLevelChart.getData().add(maxSugarLevelSeries);
			_bloodSugarLevelChart.getData().add(minSugarLevelSeries);	
		}
	}
	
	// The mrthod will add BSL data as time changes
	public void InitializeBSLSimulator() {
		maxDataPoint++;
		
		if(_bloodSugarLevelChart != null) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String time = dtf.format(now).toString();
			
			
			sugarLevelSeries.getData().add(new Data<String, Number>(time, getBloodSugarLevel()));
			maxSugarLevelSeries.getData().add(new Data<String, Number>(time, StaticValues.MaximumBloodSugarLevel));
			minSugarLevelSeries.getData().add(new Data<String, Number>(time, StaticValues.MinimumBloodSugarLevel));
			
			if(maxDataPoint > 40) {
				minSugarLevelSeries.getData().remove(0);
				sugarLevelSeries.getData().remove(0);
				maxSugarLevelSeries.getData().remove(0);
			}
		}
	}
	
	// The method will get the raise in BSL value
	public double getBloodSugarLevel(){	
		double bsl = 0;
		
		// will be true when user take food or exercise
		if(checkForBsl) {	
			if(isAutoMode) {

				if(StaticValues.TempBSL > 120 && StaticValues.CurrentBSL > 120) {
					showChangeinBSLMessages(StaticValues.CurrentBSL);
					autoInjectionStarted = true;
	
					bsl = bslCalculation.getInstance().bslOnInsulinDosage(CheckForInsulinLevel(true));
					StaticValues.CurrentBSL = bsl;
				}
				else if(StaticValues.TempBSL < 70 && StaticValues.CurrentBSL < 70) {
					showChangeinBSLMessages(StaticValues.CurrentBSL);
					autoInjectionStarted = true;
	
					bsl = BSLCalculation.getInstance().bslOnGlucagonDosage(CheckForGlucagonLevel(true));
					StaticValues.CurrentBSL = bsl;
					
				}
				else if(StaticValues.CurrentBSL < 120 && autoInjectionStarted){
					bsl = BSLCalculation.getInstance().bslOnIdeal();
				}
				else {
					bsl = BSLCalculation.getInstance().bslAfterActivity(StaticValues.CarbohydrateIntake , 1 * increasingCarboCounter);
				}
	
				bsl = Double.parseDouble(new DecimalFormat("###.##").format(bsl));
				
				if(bsl != StaticValues.CurrentBSL) {
					StaticValues.TempBSL = StaticValues.CurrentBSL;
				}
				StaticValues.CurrentBSL = bsl;		
			}
			else if(!isAutoMode){
				if(!inManualInject) {
					bsl = BSLCalculation.getInstance().bslAfterActivity(StaticValues.CarbohydrateIntake , 1 * increasingCarboCounter);
				}
				else if (inManualIdealDecrease || (StaticValues.CurrentBSL >= 70 && StaticValues.CurrentBSL <= 120)) {
					bsl = BSLCalculation.getInstance().bslOnIdeal();
				}
				else {
					if(StaticValues.CurrentBSL > 120) {
						bsl = BSLCalculation.getInstance().bslOnInsulinDosage(CheckForInsulinLevel(false));
					}
					else if(StaticValues.CurrentBSL < 70) {
						bsl = BSLCalculation.getInstance().bslOnGlucagonDosage(CheckForGlucagonLevel(false));
					}
					inManualInject = true;
					inManualIdealDecrease = true;
				}			
				
				bsl = Double.parseDouble(new DecimalFormat("###.##").format(bsl));		
				if(bsl != StaticValues.CurrentBSL) {
					StaticValues.TempBSL = StaticValues.CurrentBSL;
				}
				StaticValues.CurrentBSL = bsl;
			}
			increasingCarboCounter++;
		}
		
		currentBSLTextBox.setText(Double.toString(StaticValues.CurrentBSL));
		previousBSLTextBox.setText(Double.toString(StaticValues.TempBSL));
	    return  StaticValues.CurrentBSL;     
	}

	// The method will show messages if BSL goes above or below the band
	private void showChangeinBSLMessages(double bsl) {
		if(bsl > StaticValues.MaximumBloodSugarLevel && showHighBslMsg) {
			addMessage("Blood Sugar Level exceeded the threshold level. Insulin needs to be injected!", Color.RED);
			showHighBslMsg = false;
		}
		else if(bsl < StaticValues.MinimumBloodSugarLevel && showHighBslMsg) {
			addMessage("Blood Sugar Level is very Low. Glucagon needs to be injected!", Color.RED);
			showHighBslMsg = false;
		}
		
		if((bsl >= StaticValues.MinimumBloodSugarLevel && bsl <= StaticValues.MaximumBloodSugarLevel) && showStableBslMsg) {
			addMessage("Blood Sugar Level is back to stable!", Color.GREEN);
			showStableBslMsg = false;
		}	
	}
	
	// The method will check if insul needs to be injected or not
	private double CheckForInsulinLevel(boolean isAuto) {
		double insulin = 0;
		if(isAuto) {
			insulin = InsulinGlucagonCalculation.getInsulinDosageValue(StaticValues.CurrentBSL);
		}
		else {
			String insulinInjected = txtInsulinInj.getText();					
			if(insulinInjected.isEmpty()) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Insulin Injection");
		        alert.setContentText("Insulin cannot be empty");
		        alert.show();
			}
			insulin = Double.parseDouble(insulinInjected);
			if(insulin > 1) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Insulin");
		        alert.setContentText("Cannot inject more than 1 unit of Insulin");
		        alert.show();
			}
		}
		if(insulin > 0) {
			insulinLevel -= insulin;
			float insulinLevelIndicator = insulinLevel/100;
				
			if(insulinLevelIndicator <= 0.5) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Low Insulin");
		        alert.setContentText("Insulin Reservior is very low. Please Refill the reservior!");
		        alert.show();
				addMessage("Insulin Reservior is very low. Please Refill the reservior!", Color.RED);
			}
			double bslAfterInsulin = BSLCalculation.getInstance().bslOnInsulinDosage(insulin);
			addMessage("After " + insulin + " of insulin injection, BSL decreased from " + StaticValues.CurrentBSL + " to " + bslAfterInsulin + "mg/dl");
			InsulinProgressBar.setProgress(insulinLevelIndicator);
		}
		return insulin;
	}
	
	// The method will check if glucagon needs to be injected or not
	private double CheckForGlucagonLevel(boolean isAuto) {
		double glucagon = 0;
		
		if(isAuto) {
			glucagon =	InsulinGlucagonCalculation.getGlucagonDosageValue(StaticValues.CurrentBSL);
		}
		else {
			String glucagonInjected = txtGlucagonInj.getText();					
			if(glucagonInjected.isEmpty()) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Glucagon Injection");
		        alert.setContentText("Glucagon cannot be empty");
		        alert.show();
			}
			glucagon = Double.parseDouble(glucagonInjected);
			if(glucagon > 1) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Glucagon");
		        alert.setContentText("Cannot inject more than 2 unit of Glucagon");
		        alert.show();
			}
		}
		if(glucagon > 0) {
			glucagonLevel -= glucagon;
			StaticValues.CurrentBSL = BSLCalculation.getInstance().bslOnGlucagonDosage(glucagon);
			
			float glucagonLevelIndicator = glucagonLevel/100;
			if(glucagonLevelIndicator <= 0.5) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Low Glucagon");
		        alert.setContentText("Glucagon Reservior is very low. Please Refill the reservior!");
		        alert.show();
				addMessage("Glucagon Reservior is very low. Please Refill the reservior!", Color.RED);
			}
			double bslAfterGlucagon = BSLCalculation.getInstance().bslOnGlucagonDosage(glucagon);
			addMessage("After " + glucagon + " of glucagon injection, BSL increased from " + StaticValues.CurrentBSL + " to " + bslAfterGlucagon + "mg/dl");
			GlucagonProgressBar.setProgress(glucagonLevelIndicator);
		}	
		return glucagon;
	}
	
	// Method handles all the input for the simulator
	public void settingsClick(MouseEvent event) throws IOException {				
		Parent configurationForm = FXMLLoader.load(getClass().getResource("/application/ConfigurationForm.fxml"));
		Scene scene = new Scene(configurationForm);	
		Stage window = new Stage();
		window.setScene(scene);
		window.setTitle("Settings");
		window.showAndWait();
	}
	
	// The Method will handle Auto/Manual Event
	public void autoModeClick(ActionEvent event) throws IOException {	
		// If the mode is Auto, the show the login page so that it can be changed to Manual
		if(isAutoMode) {
			Parent loginForm = FXMLLoader.load(getClass().getResource("/application/LoginForm.fxml"));
			Scene scene = new Scene(loginForm);	
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Login");
			window.showAndWait();
			
			String modeName = isAutoMode ? "Auto" : "Manual";
			
			if(!isAutoMode) {
				addMessage("The Login was Successful!");
				addMessage("The Mode will be changed to " + modeName);
				grpManualInj.setVisible(true);
			}
		}
		else {
			grpManualInj.setVisible(false);
			isAutoMode = true;
			addMessage("The Mode will be changed to Auto");
		}
		btnAuto.setStyle(isAutoMode ? "-fx-background-color:  #90EE90" : "-fx-background-color: grey;");
	}
	
	// The event will handle the insulin refill click
	public void insulinRefillClick(MouseEvent event) {
		insulinLevel = 100;
		InsulinProgressBar.setProgress(1);
		addMessage("Insulin Bank is Refilled! ", Color.GREEN);
		
	}
	
	// The event will handle the glucagon refill click
	public void glucagonRefillClick(MouseEvent event) {
		glucagonLevel = 100;
		GlucagonProgressBar.setProgress(1);
		addMessage("Glucagon Bank is Refilled! ", Color.GREEN);
	}
	
	// The event will handle recharge of battery
	public void batteryChargeClick(MouseEvent event) {
		counter = 0;
		imgBattery.setImage(new Image("img/fullCharge.PNG"));
	}
	
	// the Method closes the application
	public void closeApplication(MouseEvent event) {
		if(buildAlertMessage(AlertType.CONFIRMATION, "Confirmation Dialog", "System Shutdown", "The system will shutdown. Do you want to continue?")) {
			System.exit(0);
		}
	}

	// Generic method to build alert messages
	public Boolean buildAlertMessage(AlertType alertType, String title, String headerText, String contextText) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
        	return true;
        }
        return false;
	}
	
	// The method will handle change of battery icon
	public void changeBatteryIcon() {
		counter++;
		if(imgBattery != null) {
			switch(counter) {
			case 2: 
				imgBattery.setImage(new Image("img/80Charge.PNG"));
				break;
				
			case 3:
				imgBattery.setImage(new Image("img/halfCharge.PNG"));
				break;
				
			case 4:
				imgBattery.setImage(new Image("img/lowCharge.PNG"));
				break;
						
			case 5: 
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Low Battery");
		        alert.setContentText("Please plug in to charger, else the system will shut down");
		        alert.show();
		        addMessage("Battery is very low. Please plug to charger.", Color.RED);
		        
			default:	
				if(counter == 15) {
					addMessage("System shut down due to low battery", Color.RED);
					System.exit(0);
				}
				break;
			}	
		}
	}
	
	// The event will handle injection of insulin in manual mode
	public void btnInsulinInj(ActionEvent event) {
		if (StaticValues.CurrentBSL >= 70 && StaticValues.CurrentBSL <= 120) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Alert Dialog");
	        alert.setHeaderText("Insulin Injection");
	        alert.setContentText("Blood Sugar Level is stable. No need to Inject Insulin");
	        alert.show();
	        return;
		}
		
		inManualInject = true;
		inManualIdealDecrease = false;
	}
	
	// The event will handle injection of glucagon in manual mode
	public void btnGlucagonInj(ActionEvent event) {
		if (StaticValues.CurrentBSL >= 80 && StaticValues.CurrentBSL <= 120) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Alert Dialog");
	        alert.setHeaderText("Glucagon Injection");
	        alert.setContentText("Blood Sugar Level is stable. No need to Inject Glucagon");
	        alert.show();
	        return;
		}

		inManualInject = true;
		inManualIdealDecrease = false;
	}
}


