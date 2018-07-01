package configuration;

import java.text.DecimalFormat;


public class InsulinGlucagonCalculation {
	private static double calculatedinsulindose;
	private static double calculatedGlucagondose;
	
	public static Double getInsulinDosageValue(double currentBSL) {
		calculatedinsulindose = 0;
		if(currentBSL >= StaticValues.MaximumBloodSugarLevel) {
			double insulinCorrectionFactor = (getChangeInBSForInsulin(currentBSL)) / StaticValues.ISF;

			calculatedinsulindose = Double.parseDouble(new DecimalFormat("##.##").format(insulinCorrectionFactor));
		}
		return calculatedinsulindose;
	}
	
	private static double getChangeInBSForInsulin(double currentBSL) {
		if(currentBSL > 120 && currentBSL <= 130) {
			return 5;
		}
		else if(currentBSL > 130 && currentBSL <= 150) {
			return 10;
		}
		else if(currentBSL > 150 && currentBSL <= 180) {
			return 15;
		}
		else if(currentBSL > 180 && currentBSL <=220) {
			return 20;
		}
		return 25;
	}
	
	private static double getChangeInBSForGlucagon(double currentBSL) {
		if(currentBSL >= 65 && currentBSL < 80) {
			return 3;
		}
		return 6;
	}
	
	
	public static Double getGlucagonDosageValue(double currentBSL) {
		calculatedGlucagondose = 0;
		if (currentBSL < StaticValues.MinimumBloodSugarLevel) {
			calculatedGlucagondose = getChangeInBSForGlucagon(currentBSL) / 3;
			calculatedGlucagondose = Double.parseDouble(new DecimalFormat("##.##").format(calculatedGlucagondose));
		}
		return calculatedGlucagondose;
	}

}
