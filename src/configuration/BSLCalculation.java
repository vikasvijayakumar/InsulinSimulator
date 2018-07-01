package configuration;


public class BSLCalculation {
	private static BSLCalculation BSLCalculation = null;
	public static double k1 = 0.0453;
	public static double k2 = 0.0224;

	
	private double bloodsugar = StaticValues.CurrentBSL; 

	public BSLCalculation() {
	}

	public static synchronized BSLCalculation getInstance() {

		if (BSLCalculation == null) {
			BSLCalculation = new BSLCalculation();
		}
		return BSLCalculation;

	}
	
	public double checkBloodGlucose() {
		return getBloodSugar();
	}

	public double bslOnIdeal() {
		bloodsugar = StaticValues.CurrentBSL;
		bloodsugar -= (3 * (k1 / (k2 - k1)) * (Math.exp(-k1 * 5) - Math.exp(-k2 * 5)));
		return bloodsugar;
	}

	public double bslAfterActivity(double carbs, int t) {
		if (carbs != 0) {
			bloodsugar = (StaticValues.PreviousBSL) +  (2 * carbs * (k1 / (k2 - k1)) * (Math.exp(-k1 * 3 * t) - Math.exp(-k2 * 3 * t)));
		}
		return bloodsugar;
	}

	public double bslOnInsulinDosage(double insulin) {
		bloodsugar = StaticValues.CurrentBSL;
		if (insulin > 0) {
			bloodsugar -= (StaticValues.ISF * insulin);
		}
		
		return bloodsugar;
	}

	public double bslOnGlucagonDosage(double glucagon) {
		if(glucagon < 0) 
			return StaticValues.CurrentBSL;
			
		bloodsugar = StaticValues.CurrentBSL;
		bloodsugar += glucagon * 3;
		return bloodsugar;
	}

	private double getBloodSugar() {
		return bloodsugar;
	}
}
