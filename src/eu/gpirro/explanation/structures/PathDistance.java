package eu.gpirro.explanation.structures;

import com.hp.hpl.jena.rdf.model.Model;

public class PathDistance {

	private Model path1;

	private Model path2;

	private double distance;

	public PathDistance(Model path1, Model path2, double distance) {
		super();
		this.path1 = path1;
		this.path2 = path2;
		this.distance = distance;
	}

	public Model getPath1() {
		return path1;
	}

	public void setPath1(Model path1) {
		this.path1 = path1;
	}

	public Model getPath2() {
		return path2;
	}

	public void setPath2(Model path2) {
		this.path2 = path2;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void printModels() {
		System.out.println("Path1");
		path1.write(System.out, "N-TRIPLE");
		System.out.println();
		System.out.println("Path2");
		path2.write(System.out, "N-TRIPLE");

	}

}
