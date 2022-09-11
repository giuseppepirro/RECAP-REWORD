package eu.gpirro.explanation.structures;

public class Pattern {

	double informativenenss = 0.0;

	String prototypical_pattern;

	public Pattern(String prototypical_pattern, double informativenenss) {
		this.informativenenss = informativenenss;
		this.prototypical_pattern = prototypical_pattern;
	}

	public String getPrototypical_pattern() {
		return prototypical_pattern;
	}

	public void setPrototypical_pattern(String prototypical_pattern) {
		this.prototypical_pattern = prototypical_pattern;
	}

	public double getInformativenenss() {
		return informativenenss;
	}

	public void setInformativenenss(double informativenenss) {
		this.informativenenss = informativenenss;
	}

}
