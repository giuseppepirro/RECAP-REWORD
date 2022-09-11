package eu.gpirro.explanation.structures;

import com.hp.hpl.jena.rdf.model.Model;

public class Path {

	Model model;
	double informativenenss = 0.0;

	String prototypical_pattern;

	public Path(Model model, double informativenenss,
			String prototypical_pattern) 
	{
		super();
		this.model = model;
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

	public Model getModel() {
		return model;
	}

	public void printInfo() {
		System.out.println("Informativeness=" + this.getInformativenenss());
		model.write(System.out, "N-TRIPLE");

	}

	public void setInformativenenss(double informativenenss) {
		this.informativenenss = informativenenss;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return model.toString();

	}

}
