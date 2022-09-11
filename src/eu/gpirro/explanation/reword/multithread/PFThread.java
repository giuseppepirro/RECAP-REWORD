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

public class PFThread implements Runnable {

	private ReWORDMultiThread manager;

	private String direction;
	private String entity;
	private int id;
	private String predicate;
	private static final long t0 = System.currentTimeMillis();
	private String endpointAddress;
	private String namedGraph;

	public PFThread(ReWORDMultiThread manager, String direction, String entity,
			String pred, int id, String endpointAddress, String namedGraph) {
		this.id = id;
		this.direction = direction;
		this.manager = manager;
		this.entity = entity;
		this.predicate = pred;
		this.endpointAddress = endpointAddress;
		this.namedGraph = namedGraph;

	}

	@Override
	public void run() {
		/**
		 * check if the value is already present in the monitor otherwise
		 * compute it!
		 */
		// ///
		double f_i_p = 0.0;
		double f_i = 0.0;
		double pf = 0.0;
		String query = "";
		double total_in_pred_count = -1;
		double total_out_pred_count = -1;

		// System.out.println("Thread PF id=" + id);

		// System.out.println("# Active Thread PF "+manager.getNumPFQueriesToBeExecuted());

		manager.incrNumActivePFThreads();

		if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			query = Constants.PREFIXES
					+ " SELECT COUNT(*) as ?count WHERE { ?o " + predicate
					+ " " + entity + " }";

			// execute query
			try {
				f_i_p = executeCountQuery(query);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (manager.getTotalInPredicateCount() == -1.0) {
				query = Constants.PREFIXES
						+ " SELECT COUNT(*) as ?count WHERE { ?o ?p " + entity
						+ " }";

				// execute query
				try {
					f_i = executeCountQuery(query);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				total_in_pred_count = f_i;
				manager.setTotalInPredicateCount(total_in_pred_count);

			} else {

				f_i = manager.getTotalInPredicateCount();

			}

		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			query = Constants.PREFIXES + " SELECT COUNT(*) as ?count WHERE { "
					+ entity + " " + predicate + " ?o }";

			// execute query
			try {
				f_i_p = executeCountQuery(query);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (manager.getTotalOutPredicateCount() == -1.0) {
				query = Constants.PREFIXES
						+ " SELECT COUNT(*) as ?count WHERE { " + entity
						+ " ?p " + " ?o }";

				// execute the second query
				// f_i = executeCountQuery(query);

				total_out_pred_count = f_i;
				manager.setTotalOutPredicateCount(total_out_pred_count);

			} else {
				f_i = manager.getTotalOutPredicateCount();// total_out_pred_count;
			}

		}

		pf = f_i_p / f_i;

		System.out.println("PF value for pred=" + predicate + " pf=" + pf);

		manager.addPFValue(entity, predicate, direction, pf);

		manager.decrNumActivePFThreads();

		if (manager.getActivePFThreadCount() == 1
				&& manager.getNumPFQueriesToBeExecuted() <= 0) {

			// Total time in terms computing PF values
			System.out.println("\nTotal time to retrieve PF values="
					+ ((System.currentTimeMillis() - t0)) + " ms");

			manager.shutdownPFThreadExecutor();
		}
	}

	/**
	 * Executes a specific COUNT query
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

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		URLConnection connection = null;

		try {

			connection = url.openConnection();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// System.out.println("Too many connections OPEN connection " + id);

			e.printStackTrace();
		}

		String line;
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = null;
		InputStream input_stream = null;
		try {
			input_stream = connection.getInputStream();

			manager.increasenumberOfPFConections();

			System.out.println("# active connections "
					+ manager.getNumberOfPFConnection());

		} catch (IOException e1) {
			// TODO Auto-generated catch block

			/**
			 * The problem is here!
			 */
			System.out.println(predicate + "********************** #Active="
					+ manager.getNumberOfPFConnection());
			e1.printStackTrace();

			System.exit(0);

			// System.out.println("Too many connections input stream " + id);

		}

		reader = new BufferedReader(new InputStreamReader(input_stream));

		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {

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
		 * CLOSE THE CONNECTION
		 */
		try {
			reader.close();
			input_stream.close();

			manager.decreasenumberOfPFConections();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// System.out.println("Too many connections closing " + id);

		}
		return count_r;

	}

}
