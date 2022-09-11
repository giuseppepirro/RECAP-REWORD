package eu.gpirro.explanation.multithread;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.log4j.lf5.util.Resource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelFactoryBase;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.Util;

public class EntitySuggestion {

	/**
	 * Given an explanation suggests PAIRS OF related entities
	 * 
	 * 
	 * @param explanation
	 * @param source
	 * @param target
	 * @throws JSONException
	 */
	public ArrayList<String> computeEntitySuggestions(String query, String endpointAddress, String namedGraph)
			throws JSONException {

		// pairs are separated by "\t";
		ArrayList<String> results = new ArrayList<String>();

		/*
		 * Take whatever explanation and replace with variables!
		 */

		// String query = "SELECT DISTINCT ?vsource ?vtarget WHERE { "
		// + getPrototypicalPattern(explanation, source, target)
		// + " FILTER( ?vsource != ?vtarget) }";

		System.out.println("Pair " + query);

		System.out.println("Query for entity suggestions " + query);
		/*
		 * MAKE the queries online and get the results!!!
		 */

		StringBuilder queryBuilder = new StringBuilder(endpointAddress
				+ "?default-graph-uri=");
		try {
			queryBuilder
					.append(URLEncoder.encode(namedGraph, "UTF-8"))
					.append("&query=")
					.append(URLEncoder.encode(query, "UTF-8"))
					.append("%0D%0A&debug=on&timeout=" + Constants.TIMEOUT
							+ "&format=")
					.append(URLEncoder.encode(
							"application/sparql-results+json", "UTF-8"))
					.append("&save=display&fname=");
		} catch (Exception ex) {
		}

		URL url = null;
		try {
			url = new URL(queryBuilder.toString());
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		URLConnection connection = null;

		try {
			connection = url.openConnection();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String line;
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = null;

		InputStream input_stream = null;
		try {
			input_stream = connection.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// System.out.println(builder.toString());

		JSONObject jTotal = null;

		jTotal = new JSONObject(builder.toString());

		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");

		for (int i = 0; i < listResults.length(); i++) {
			JSONObject rowResult = (JSONObject) listResults.get(i);

			// System.out.println(rowResult.toString());
			String vsource = rowResult.getJSONObject("vsource").getString(
					"value");
			String vtarget = rowResult.getJSONObject("vtarget").getString(
					"value");

			results.add(Util.getObjectShortName(vsource) + "\t"
					+ Util.getObjectShortName(vtarget));

			// System.out.println("Computed entity suggestions....");

			// System.out.println(vsource + "\t" + vtarget);

		}
		try {
			reader.close();
			input_stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;

	}

	public Model getPatternAsModel(String pattern) {
		Model res = ModelFactory.createDefaultModel();

		// System.out.println(pattern);
		StringTokenizer st = new StringTokenizer(pattern, " ");

		int i = 0;
		String string_final = null;
		String current_element;

		while (st.hasMoreTokens()) {

			current_element = st.nextToken();

			if (!current_element.equalsIgnoreCase(".")) 
			{
				string_final = string_final
						+ Util.getObjectFullName(current_element) + " ";
				i++;

				if (i == 3) {
					string_final = string_final + ".";
					i = 0;
				}
			}

		}
		string_final = string_final.replace("<<", "<");
		string_final = string_final.replace(">>", ">");
		string_final = string_final.replace("<.>", " ");
		string_final = string_final.replace("null", " ");
		string_final = string_final.replace("<.", " ");
		string_final = string_final.replace(" >", " ");
		string_final = string_final.substring(0, string_final.lastIndexOf(">"));

		//string_final = "PREFIX : \n"+string_final;
		
		
		string_final = string_final.trim();

	//	System.out.println(string_final);
		//res.read(new ByteArrayInputStream(string_final.getBytes()), null);

		res.read(string_final,"N-TRIPLE");

		return res;
	}

	/**
	 * Return a pattern by only leaving predicates
	 * 
	 * Useful for entity suggestion
	 * 
	 * 
	 * @param explanation_inst
	 * @return
	 */
	public String getPrototypicalPattern(Model explanation_inst, String source,
			String target) {

		String final_explanation_pattern = "";

		StringWriter model_to_string = new StringWriter();

		StmtIterator statement_iterator = explanation_inst.listStatements();
		String current_st = "";

		while (statement_iterator.hasNext()) {

			current_st = statement_iterator.nextStatement().toString();

			current_st = current_st.replaceAll(",", "");

			current_st = current_st.substring(current_st.indexOf("[") + 1,
					current_st.indexOf("]"));

			model_to_string.write(current_st + "\t");

		}

		/**
		 * 
		 * need to separate the parts of an n-triple!
		 */

		String temp_var = "";

		int variable_index = 1;
		// URI variable already used
		Hashtable<String, String> map_replacements = new Hashtable<String, String>();

		StringTokenizer st = new StringTokenizer(model_to_string.toString(),
				"\t");

		String triple_element = "";
		String triple = "";

		try {

			while (st.hasMoreTokens()) {
				triple = st.nextToken();

				if (triple.length() > 3) {
					StringTokenizer st1 = new StringTokenizer(triple, " ");

					int triple_index = 0;

					while (st1.hasMoreTokens()) {
						// each element of the triple encoded in st1
						triple_element = st1.nextToken().trim();
						triple_index++;
						String temp1 = triple_element;
						String temp2 = triple_element;
						String temp3 = triple_element;

						// System.out.println("triple element=" + triple_element
						// + " triple_index=" + triple_index);

						// if it is a predicate (index 2) DO NOTHING
						// otherwise replace
						if (triple_index != 2) {
							// if an element has not already been replaced by a
							// variable
							if (map_replacements.get(temp1) == null) {

								temp3 = Util.getObjectFullName(temp3);

								temp3 = temp3.substring(temp3.indexOf("<"),
										temp3.indexOf(">") + 1);

								// System.out.println("TEMP3=" + temp3);

								if (temp3.equalsIgnoreCase(source)) {

									// System.out.println("TEMP3 found to be source..");

									temp_var = "?vsource";

									final_explanation_pattern = final_explanation_pattern
											+ " "
											+ temp1.replace(temp1, temp_var);

									map_replacements.put(temp1, temp_var);

								} else if (temp3.equalsIgnoreCase(target)) {

									temp_var = "?vtarget";
									final_explanation_pattern = final_explanation_pattern
											+ " "
											+ temp1.replace(temp1, temp_var);

									map_replacements.put(temp1, temp_var);
								} else {

									temp_var = "?v" + variable_index;

									final_explanation_pattern = final_explanation_pattern
											+ " "
											+ temp1.replace(temp1, temp_var);

									map_replacements.put(temp1, temp_var);

									variable_index++;
								}

							} else {
								// get from the cache
								temp_var = map_replacements.get(temp1);
								// replace in the current result (i.e. path
								// pattern)
								final_explanation_pattern = final_explanation_pattern
										+ " " + temp1.replace(temp1, temp_var);

							}

						} else {

							// THIS MEANS THAT WE ARE IN THE PREDICATE CASE!
							// System.out.println("TEMP2=" + temp2);
							temp2 = Util.getObjectFullName(temp2);
							// System.out.println("TEMP2=" + temp2);

							final_explanation_pattern = final_explanation_pattern
									+ " " + temp2;

						}

						// reset the counter for the next triple encoded by in
						// st1
						if (triple_index == 3)
							triple_index = 0;

					}

					final_explanation_pattern = final_explanation_pattern
							+ ". ";
				}

			}
		} catch (Exception e) {
			System.err.println("ERROR analyzing " + triple_element);
			e.printStackTrace();
		}

		return final_explanation_pattern;

	}

	/**
	 * Returns a path pattern
	 * 
	 * @param path
	 * @param source
	 * @param target
	 * @return
	 */
	public String getPathPattern(Model path, String source, String target) {
		String final_explanation_pattern = "";
		StringWriter model_to_string = new StringWriter();
		StmtIterator statement_iterator = path.listStatements();
		String current_st = "";

		while (statement_iterator.hasNext()) {
			current_st = statement_iterator.nextStatement().toString();
			current_st = current_st.replaceAll(",", "");
			current_st = current_st.substring(current_st.indexOf("[") + 1,
					current_st.indexOf("]"));
			model_to_string.write(current_st + "\t");
		}

		// System.out.println(model_to_string);

		String temp_var = "";

		int variable_index = 1;
		// URI variable already used
		Hashtable<String, String> map_replacements = new Hashtable<String, String>();

		StringTokenizer st = new StringTokenizer(model_to_string.toString(),
				"\t");

		String triple_element = "";
		String triple = "";

		try {

			while (st.hasMoreTokens()) {
				triple = st.nextToken();

				// System.out.println(triple);

				if (triple.length() > 3) {
					StringTokenizer st1 = new StringTokenizer(triple, " ");

					int triple_index = 0;

					while (st1.hasMoreTokens()) {
						// each element of the triple encoded in st1
						triple_element = st1.nextToken().trim();
						triple_index++;
						String temp1 = triple_element;
						String temp2 = triple_element;
						String temp3 = triple_element;

						// System.out.println("triple element=" + triple_element
						// + " triple_index=" + triple_index);

						// if it is a predicate (index 2) DO NOTHING
						// otherwise replace
						if (triple_index != 2) {
							// if an element has not already been replaced by a
							// variable
							if (map_replacements.get(temp1) == null) {
								temp3 = Util.getObjectFullName(temp3);

								temp3 = temp3.substring(temp3.indexOf("<"),
										temp3.indexOf(">") + 1);

								if (temp3.equalsIgnoreCase(source)) {

									/**
									 * BEFORE OR AFTER?????/?/ it seems that it
									 * does not matter
									 */
									final_explanation_pattern = final_explanation_pattern
											+ " " + source + " ";

								} else if (temp3.equalsIgnoreCase(target)) {
									/**
									 * BEFORE OR AFTER?????/?/ it seems that it
									 * does not matter
									 */

									final_explanation_pattern = final_explanation_pattern
											+ " " + target + " ";

								} else {
									temp_var = "?v" + variable_index + "";

									final_explanation_pattern = final_explanation_pattern
											+ " "
											+ temp1.replace(temp1, temp_var);

									map_replacements.put(temp1, temp_var);

									variable_index++;
								}

							} else {
								// get from the cache
								temp_var = map_replacements.get(temp1);
								// replace in the current result (i.e. path
								// pattern)
								final_explanation_pattern = final_explanation_pattern
										+ " " + temp1.replace(temp1, temp_var);

							}

						} else {

							// THIS MEANS THAT WE ARE IN THE PREDICATE CASE!
							// System.out.println("TEMP2=" + temp2);
							temp2 = Util.getObjectFullName(temp2);
							// System.out.println("TEMP2=" + temp2);

							final_explanation_pattern = final_explanation_pattern
									+ " " + temp2;

						}

						// reset the counter for the next triple encoded by in
						// st1
						if (triple_index == 3)
							triple_index = 0;
					}

					final_explanation_pattern = final_explanation_pattern
							+ ". ";
				}

			}
		} catch (Exception e) {
			System.err.println("ERROR analyzing " + triple_element);
			e.printStackTrace();
		}

		return final_explanation_pattern;

	}

}
