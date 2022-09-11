package eu.gpirro.explanation.multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Hashtable;

import javafx.concurrent.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.gpirro.explanation.structures.PathElement;
import eu.gpirro.explanation.structures.PathResult;
import eu.gpirro.recap.controllers.MainPanelController;
import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.Util;
import eu.gpirro.utilities.Variable;

public class PathFindingThread extends Task {

	private PathResult[] partial_query_results;
	private RecapMultiThread manager;
	private String query;
	private String sourceEntity;
	private String targetEntity;
	private int id;

	private int distance;
	private Hashtable<String, String> res_to_write;
	private MainPanelController controller;
	private static final long t0 = System.currentTimeMillis();

	private String endpointAddress;
	private String namedGraph;

	/**
	 * Constructor
	 * 
	 * @param manager
	 * @param query
	 * @param sourceEntity
	 * @param targetEntity
	 * @param id
	 */
	public PathFindingThread(RecapMultiThread manager, String query,
			String sourceEntity, String targetEntity, int id,
			MainPanelController controller, int distance,
			String endpointAddress, String namedGraph) {
		this.controller = controller;
		this.manager = manager;
		this.query = query;
		this.sourceEntity = sourceEntity;
		this.targetEntity = targetEntity;
		this.id = id;
		this.distance = distance;
		this.endpointAddress = endpointAddress;
		this.namedGraph = namedGraph;

	}

	/**
	 * Job done by each Thread
	 */
	/*
	 * @Override public void run() {
	 * 
	 * try {
	 * 
	 * if (manager.getNumActiveQueryThreads() == 1) { long start =
	 * System.currentTimeMillis(); manager.setStartingTimePaths(start); }
	 * 
	 * //System.out.println(query);
	 * 
	 * manager.incrNumActiveQueryThreads();
	 * 
	 * partial_query_results = doExecuteQuery(query, Constants.TIMEOUT);
	 * 
	 * // turn into a jena model here!
	 * 
	 * // decrement the total number of queries to be executed
	 * manager.decrTotalNumberOfRemoteQueries();
	 * 
	 * if (partial_query_results != null) {
	 *//**
	 * 
	 * The following commented line gives the results for each partial query
	 *
	 *
	 */
	/*
	 * // manager.addPartialResults(query, partial_query_results);
	 * 
	 * // Atomic Result(s) for (int j = 0; j < partial_query_results.length;
	 * j++) {
	 * 
	 * // avoids duplicate results! manager.addAtomicResult(
	 * partial_query_results[j].toString(), partial_query_results[j]); } if
	 * (partial_query_results.length > 0)
	 * manager.incrTotalNumberOfRemoteQueriesWithResults();
	 * 
	 * }
	 * 
	 * } catch (Exception e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * manager.decrNumActiveQueryThreads();
	 *//**
	 * Termination condition The last Thread calls the method to shutdown the
	 * ThreadPool and start the building of the different types of explanations
	 */
	/*
	 * if (manager.getActiveThreadCount() == 1 &&
	 * manager.getNumActiveQueryThreads() <= 0 &&
	 * manager.getTotalNumberOfRemoteQueries() == 0)
	 * 
	 * {
	 * 
	 * long time = System.currentTimeMillis() - manager.getStartingTimePaths();
	 * 
	 * res_to_write = new Hashtable<String, String>(); int dist =
	 * (Constants.MAX_DIST) + 1;
	 *//**
	 * Pass to the method shutdown!
	 */
	/*
	 * 
	 * res_to_write.put(Constants.RES_TOTAL_NUM_QUERIES_PATHS,
	 * manager.getTOTALQueries() + "");
	 * res_to_write.put(Constants.RES_TOTAL_NUM_QUERIES_WITH_RES_PATHS,
	 * manager.getTotalNumberOfRemoteQueriesWithResults() + "");
	 * res_to_write.put(Constants.RES_TOTAL_TIME_PATHS, time + "");
	 * res_to_write.put(Constants.MAX_DIST + "", dist + "");
	 * res_to_write.put(Constants.RES_TOTAL_NUM_PATHS + "", manager
	 * .getQueryResultsNoDuplicates().size() + "");
	 * manager.setTotalTimePaths(time); manager.shutdown(res_to_write); }
	 * 
	 * }
	 */

	public MainPanelController getController() {
		return controller;
	}

	public void setController(MainPanelController controller) {
		this.controller = controller;
	}

	/**
	 * Execute the query for this thread on the address specified by
	 * Constants.ENTPOINT_HTTP and Constants.WEBSITE_ADDRESS
	 * 
	 * @param query
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	private PathResult[] doExecuteQuery(String query, long timeout)
			throws Exception {
		// String[] listQuery = createAllCombinationQuery(nbOfConnection);

		System.out.println(query);
		
		StringBuilder queryBuilder = new StringBuilder(endpointAddress
				+ "?default-graph-uri=");
		try {
			queryBuilder
					.append(URLEncoder.encode(namedGraph, "UTF-8"))
					.append("&query=")
					.append(URLEncoder.encode(query, "UTF-8"))
					.append("%0D%0A&debug=on&timeout=" + timeout + "&format=")
					.append(URLEncoder.encode(
							"application/sparql-results+json", "UTF-8"))
					.append("&save=display&fname=");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//System.out.println(queryBuilder.toString());
		URL url = new URL(queryBuilder.toString());
		URLConnection connection = null;

		connection = url.openConnection();

		String line;
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = null;

		InputStream input_stream = null;
		try {

			input_stream = connection.getInputStream();

		} catch (Exception e1) {

			/**
			 * 
			 * CAREFUL HERE ERROR 503 may be Thrown!!!!!!
			 * 
			 */

			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			reader = new BufferedReader(new InputStreamReader(input_stream));
		} catch (Exception e) {
			// e.printStackTrace();
			System.err.println("No INPUT STREAM for pair source="
					+ sourceEntity + " target=" + targetEntity);
		}

		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject jTotal = new JSONObject(builder.toString());
		JSONArray listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");

		Variable[] vars = new Variable[listVar.length()];
		for (int i = 0; i < listVar.length(); i++) {
			vars[i] = new Variable(listVar.getString(i));
			;
		}

		Arrays.sort(vars);

		/**
		 * Build each atomic result
		 */
		PathResult[] result = new PathResult[listResults.length()];

		for (int i = 0; i < listResults.length(); i++) {

			JSONObject rowResult = (JSONObject) listResults.get(i);
			PathElement[] cols = new PathElement[vars.length + 2];
			boolean[] isReversedDirs = new boolean[(vars.length + 1) / 2];
			int idxRD = 0;
			{
				String fullURL0 = sourceEntity.substring(1,
						sourceEntity.length() - 1);

				String fullURL1 = targetEntity.substring(1,
						targetEntity.length() - 1);

				cols[0] = new PathElement(fullURL0,
						Util.getObjectShortName(fullURL0));

				cols[vars.length + 1] = new PathElement(fullURL1,
						Util.getObjectShortName(fullURL1));
			}

			for (int j = 0; j < vars.length; j++) {
				String pred = vars[j].getVarName();

				String fullURL = rowResult.getJSONObject(pred).getString(
						"value");

				cols[j + 1] = new PathElement(fullURL,
						Util.getObjectShortName(URLDecoder.decode(fullURL,
								"UTF-8")));

				if (pred.contains("pred")) {
					isReversedDirs[idxRD++] = pred.contains("To") ? false
							: true;
				}
			}

			result[i] = new PathResult(cols, isReversedDirs);
		}

		/**
		 */
		try {
			reader.close();
			input_stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	@Override
	protected PathResult[] call() throws Exception {
		// TODO Auto-generated method stub
		try {

			if (manager.getNumActiveQueryThreads() == 1) {
				long start = System.currentTimeMillis();
				manager.setStartingTimePaths(start);
			}

			// System.out.println(query);

			manager.incrNumActiveQueryThreads();
			partial_query_results = doExecuteQuery(query, Constants.TIMEOUT);
			manager.decrTotalNumberOfRemoteQueries();

			if (partial_query_results != null) {
				/**
				 * 
				 * The following commented line gives the results for each
				 * partial query
				 *
				 *
				 */
				// manager.addPartialResults(query, partial_query_results);

				// Atomic Result(s)
				for (int j = 0; j < partial_query_results.length; j++) {

					// avoids duplicate results!
					manager.addAtomicResult(
							partial_query_results[j].toString(),
							partial_query_results[j]);

				}
				if (partial_query_results.length > 0)
					manager.incrTotalNumberOfRemoteQueriesWithResults();

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		manager.decrNumActiveQueryThreads();

		/**
		 * Termination condition The last Thread calls the method to shutdown
		 * the ThreadPool and start the building of the different types of
		 * explanations
		 */
		if (manager.getActiveThreadCount() == 1
				&& manager.getNumActiveQueryThreads() <= 0
				&& manager.getTotalNumberOfRemoteQueries() == 0)

		{

			long time = System.currentTimeMillis()
					- manager.getStartingTimePaths();

			res_to_write = new Hashtable<String, String>();
			int dist = (distance) + 1;

			/**
			 * Pass to the method shutdown!
			 */

			res_to_write.put(Constants.RES_TOTAL_NUM_QUERIES_PATHS,
					manager.getTOTALQueries() + "");
			res_to_write.put(Constants.RES_TOTAL_NUM_QUERIES_WITH_RES_PATHS,
					manager.getTotalNumberOfRemoteQueriesWithResults() + "");
			res_to_write.put(Constants.RES_TOTAL_TIME_PATHS, time + "");
			res_to_write.put(distance + "", dist + "");
			res_to_write.put(Constants.RES_TOTAL_NUM_PATHS + "", manager
					.getQueryResultsNoDuplicates().size() + "");
			manager.setTotalTimePaths(time);

			manager.shutdown(res_to_write);

			/**
			 * Inside the call method, you can use the updateProgress,
			 * updateMessage, updateTitle methods, which update the values of
			 * the corresponding properties on the JavaFX Application thread
			 */
		}
		return null;

	}

	@Override
	protected void succeeded() {
		super.succeeded();
		updateMessage("Done!");
	}

}
