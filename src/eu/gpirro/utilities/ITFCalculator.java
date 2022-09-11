package eu.gpirro.utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class ITFCalculator {

	private final Map<String, Double> itf_values = Collections
			.synchronizedMap(new HashMap<String, Double>());

	private final Set<String> predicates = Collections
			.synchronizedSet(new HashSet<String>());
	PrintWriter writer;
	
	private String endpointAddress;
	private String namedGraph;

	public ITFCalculator(String endpointAddress, String namedGraph) {
		try {
			writer = new PrintWriter("remote_ITF_values_third.dat", "UTF-8");
			
			this.endpointAddress=endpointAddress;
			this.namedGraph=namedGraph;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readFromFile(String filename) {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			StringBuilder sb = new StringBuilder();

			String line = null;
			try {
				line = br.readLine();
				predicates.add(line);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			while (line != null) {
				try {
					line = br.readLine();

					// System.out.println(line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (line != null)
					predicates.add(line);

			}

		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		doTheJod(predicates);

	}//

	public void doTheJod(Set<String> preds)

	{
		for (String s : preds)
			computeValue(s);

		writer.close();
		// write on a file;

	}

	public void computeValue(String predicate) {
		double itf = 0.0;
		double denom = -1;
		String query = "";

		// System.out.println("Thread ITF id=" + id);
		// System.out.println("# Active Thread ITF "
		// + manager.getNumITFQueriesToBeExecuted());

		query = Constants.PREFIXES + " SELECT count(*) as ?count WHERE " + "{ "
				+ "?s " + predicate + " ?o " + "}";

		try {
			denom = executeCountQuery(query);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (denom != 0)
			itf = Math.log(Constants.TOTAL_NUMER_OF_TRIPLES / denom);
		else
			itf = 0;

		writer.println(predicate + "\t " + itf);

		// System.out.println(predicate
		// + "\t " + itf);

		itf_values.put(predicate, itf);

		// else it is in the cache
	}

	/**
	 * Executes a specific query
	 * 
	 * @param query
	 * @throws JSONException 
	 */
	private double executeCountQuery(String query) throws JSONException {
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

			ex.printStackTrace();

			// System.out.println("Exception building query queryBuilder ");

		}

		// System.out.println(queryBuilder.toString());

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

			/**
			 * 
			 * CAREFUL HERE!!!!!!
			 * 
			 */

			// THE EXCEPTION ABOVE GENERATES HTTP 503 errors
			e1.printStackTrace();
		}

		try {
			reader = new BufferedReader(new InputStreamReader(input_stream));

			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// System.out.println("Too many connections  buff read" + id);

		}

		JSONObject jTotal = null;
		
			jTotal = new JSONObject(builder.toString());
		
		JSONArray listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");

		Variable[] vars = new Variable[listVar.length()];
		for (int i = 0; i < listVar.length(); i++) {
			vars[i] = new Variable(listVar.getString(i));
			;
		}

		Arrays.sort(vars);

		JSONObject rowResult = (JSONObject) listResults.get(0);

		// System.out.println(rowResult.toString());
		String count = rowResult.getJSONObject("count").getString("value");

		double count_r = Double.parseDouble(count);

		/**
		 * CLOSE THE CONNECTION ??? is this necessary ??
		 */
		try {
			reader.close();
			input_stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// System.out.println("Too many connections closing " + id);

		}
		return count_r;

	}

	//

	/**
	 * query DBPEDIA and take the PF value for all predicates!!! stream the
	 * resutls
	 */

	// read put in a shared structure;

	// each of the threads of the pool with get one pred and compute its itf
	// score!

	public static void main(String argv[]) {

		String endpointAddress="";
		String namedGraph="";

		ITFCalculator calc = new ITFCalculator(endpointAddress,namedGraph);

		calc.readFromFile("list_dbpedia_prop_first_5000.dat");

	}

}
