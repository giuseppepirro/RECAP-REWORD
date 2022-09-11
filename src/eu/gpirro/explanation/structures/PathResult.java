package eu.gpirro.explanation.structures;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays;
import java.util.StringTokenizer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.gpirro.utilities.Util;

/**
 * 
 * @author
 */
public class PathResult {
	private PathElement[] listURL;
	private boolean[] isReversed;

	public PathResult(PathElement[] newListURL, boolean[] newIsReversed) {
		setListURL(newListURL);
		setIsReversed(newIsReversed);
	}

	/**
	 * @return the listURL
	 */
	public PathElement[] getListURL() {
		return listURL;
	}

	/**
	 * @param listURL
	 *            the listURL to set
	 */
	public void setListURL(PathElement[] listURL) {
		this.listURL = Arrays.copyOf(listURL, listURL.length);
	}

	/**
	 * @return the isReversed
	 */
	public boolean[] getIsReversed() {
		return isReversed;
	}

	/**
	 * @param isReversed
	 *            the isReversed to set
	 */
	public void setIsReversed(boolean[] isReversed) {
		this.isReversed = Arrays.copyOf(isReversed, isReversed.length);
	}

	/**
	 * @param i
	 *            order number
	 * @param type
	 *            1 = the number of predicate in list of predicate, 2 = the
	 *            number of predicate in list of URI
	 */
	public boolean isReversed(int i, int type) {
		return type == 1 ? isReversed[i] : isReversed[(i - 1) / 2];
	}

	/**
	 * 
	 * 
	 * 
	 * CHECK THISSSSSS Return a Jena model associated to the current path
	 * (query)
	 * 
	 * @return
	 */
	public Model toRDFModel()

	{

		Model res_model = ModelFactory.createDefaultModel();

		StringBuilder up = new StringBuilder();
		String previous_node = "";

		for (int i = 0; i < listURL.length; i++) {

			// System.out.println("listURI[i] "+listURL[i].toString());

			// URL in a path element
			String name = listURL[i].getUrl();

			// System.out.println(name);

			// Check if it is a predicate
			if (isPred(i))
			{
				// check if it is reversed
				if (isReversed(i, 2)) {
					up.append(previous_node + "---@@<");

				}

				for (int j = 0; j < 2; j++) {
					up.append("");
				}

				if (!isReversed(i, 2)) {
					up.append(previous_node + " ---@@>");
				}

				up.append(name);

				// System.out.println(previous_node);

				// System.out.println(up);
			} else {
				up.append(name + "] ~~[");
				previous_node = name;
			}
			up.append(" ");
		}
		up.append("\n");
		up.append("\n");
		

		/**
		 *
		 * 
		 * 
		 * Create the TRIPLES by parsing the string; BE CAREFUL WITH THE SPECIAL
		 * CHARACTER USED TO DISTINGuISH THE TOKENS!!!
		 */

		StringTokenizer st = new StringTokenizer(up.toString(), "~~");

		String triple = "";
		String sub = "";
		String pred = "";
		String obj = "";

		String final_triple = "";

		String pred1 = "";

		while (st.hasMoreTokens()) {

			triple = st.nextToken();

			// System.out.println("triple " + triple);

			if (triple.startsWith("[") && triple.contains("http")) {
				sub = triple.substring(triple.indexOf("h"),
						triple.indexOf("--"));

				pred = triple.substring(triple.indexOf("@@"));

				//System.out.println("INITIAL: "+pred);
				
				obj = pred.substring(pred.indexOf(" "), pred.indexOf("]"))
						.trim();
				pred1 = pred;

				// System.out.println("PRED ERROR "+pred);
				pred = pred.substring(pred.indexOf("http://"),
						pred.indexOf(" "));
				
				//System.out.println("s="+sub+" p="+pred+" o="+obj);



				final_triple = "(" + sub + " " + pred + " " + obj + ")";

				// / System.out.println(final_triple);

				if (pred1.startsWith("@@>")) {

					// System.out.println("DIRECT");

					Statement statement = ResourceFactory.createStatement(
							res_model.createResource(Util
									.getObjectShortName(sub.trim())), res_model
									.createProperty(Util
											.getObjectShortName(pred.trim())),
							res_model.createResource(Util
									.getObjectShortName(obj.trim())));

					res_model.add(statement);

				} else if (pred1.startsWith("@@<")) {
					// System.out.println("INVERSE");
					Statement statement = ResourceFactory.createStatement(
							res_model.createResource(Util
									.getObjectShortName(obj.trim())), res_model
									.createProperty(Util
											.getObjectShortName(pred.trim())),
							res_model.createResource(Util
									.getObjectShortName(sub.trim())));
					res_model.add(statement);

				}

			}
		}
		// System.out.println();

		return res_model;
	}

	@Override
	public String toString() {
		StringBuilder up = new StringBuilder();
		StringBuilder down = new StringBuilder();

		for (int i = 0; i < listURL.length; i++) {
			String shortName = listURL[i].getShortName();
			if (isPred(i)) {
				if (isReversed(i, 2))
					up.append("<");
				for (int j = 0; j < shortName.length() - 1; j++)
					up.append("-");
				if (!isReversed(i, 2))
					up.append(">");
				down.append(shortName);
			} else {
				up.append(shortName);
				for (int j = 0; j < shortName.length(); j++) {
					down.append(" ");
				}
			}
			up.append(" ");
			down.append(" ");
		}
		up.append("\n");
		up.append(down);
		up.append("\n");

		

		return up.toString();
	}

	private boolean isPred(int i) {
		return i % 2 == 1;
	}

}
