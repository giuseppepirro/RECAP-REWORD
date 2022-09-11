package eu.gpirro.explanation.reword.multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.Variable;

public class FeatureThread implements Runnable {

	private ReWORDMultiThread manager;

	private String direction;
	private String entity;
	private int id;
	private static final long t0 = System.currentTimeMillis();
	
	private String endpointAddress;
	
	private String namedGraph;


	public FeatureThread(ReWORDMultiThread manager, String direction,
			String entity, int id, String endpointAddress,String namedGraph) {
		this.id = id;
		this.direction = direction;
		this.manager = manager;
		this.entity = entity;
		this.endpointAddress=endpointAddress;
		this.namedGraph=namedGraph;
	}

	@Override
	public void run() {
		ArrayList<String> features = new ArrayList<String>();
		String query = "";

		System.out.println("Feature Thread " + id);

		manager.incrNumActiveFeatureThreads();

		if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			query = Constants.PREFIXES + " SELECT DISTINCT ?p WHERE { "
					+ entity + " ?p " + " ?o }";
		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			query = Constants.PREFIXES + " SELECT DISTINCT ?p WHERE { ?o ?p "
					+ entity + " }";
		}

		// //
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
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URLConnection connection = null;

		try {
			connection = url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		reader = new BufferedReader(new InputStreamReader(input_stream));
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject jTotal = null;
			try {
				jTotal = new JSONObject(builder.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		JSONArray listVar = null;
		try {
			listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONArray listResults = null;
		try {
			listResults = jTotal.getJSONObject("results").getJSONArray(
					"bindings");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Variable[] vars = new Variable[listVar.length()];
		for (int i = 0; i < listVar.length(); i++) {
			try {
				vars[i] = new Variable(listVar.getString(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			;
		}

		Arrays.sort(vars);

		for (int i = 0; i < listResults.length(); i++) {
			JSONObject rowResult = null;
			try {
				rowResult = (JSONObject) listResults.get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(rowResult.toString());
			String pred = null;
			try {
				pred = rowResult.getJSONObject("p").getString("value");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			features.add("<" + pred + ">");
		}

		/**
		 * CLOSE THE CONNECTION
		 */
		try {
			reader.close();
			input_stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ///
		manager.addFeatures(entity, direction, features);

		manager.decrNumActiveFeatureThreads();

		if (manager.getActiveFeatureThreadCount() == 1
				&& manager.getNumFeatureQueriesToBeExecuted() <= 0) {

			// Total time in terms of Query processing
			System.out.println("\nTotal time to retrieve features="
					+ ((System.currentTimeMillis() - t0)) + " ms");
			// manager.decreaseNumFeatureQueriesToBeExecuted();

			manager.shutdownFeatureThreadExecutor();
		}
	}

}
