package eu.gpirro.explanation.reword.multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.Variable;

public class ITFThread implements Runnable {

	private ReWORDMultiThread manager;

	private int id;
	private String predicate;
	private static final long t0 = System.currentTimeMillis();
	private String endpointAddress;
	private String namedGraph;


	public ITFThread(ReWORDMultiThread manager, String predicate, int id, String endpointAddress,String namedGraph) {
		this.id = id;
		this.manager = manager;
		this.predicate = predicate;
		this.endpointAddress=endpointAddress;
		this.namedGraph=namedGraph;


	}

	@Override
	public void run() {

		if (!manager.alreadyComputedITF(predicate)) {

			double itf = 0.0;
			double denom = -1;
			String query = "";

			// System.out.println("Thread ITF id=" + id);

			// System.out.println("# Active Thread ITF "
			// + manager.getNumITFQueriesToBeExecuted());

			manager.incrNumActiveITFThreads();

			query = Constants.PREFIXES + " SELECT count(*) as ?count WHERE "
					+ "{ " + "?s " + predicate + " ?o " + "}";

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

			// System.out.println("ITF value for pred Thread=" + predicate
			// + " ITF=" + itf);

			manager.addITFValue(predicate, itf);

			manager.decrNumActiveITFThreads();

			if (manager.getActiveITFThreadCount() == 1
					&& manager.getNumITFQueriesToBeExecuted() <= 0) {

				// Total time in terms computing PF values
				System.out.println("\nTotal time to retrieve ITF values="
						+ ((System.currentTimeMillis() - t0)) + " ms");

				manager.shutdownITFThreadExecutor();
			}
		} else {
			// System.out.println("ALREADY in the cache ITF");
		}

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

}
