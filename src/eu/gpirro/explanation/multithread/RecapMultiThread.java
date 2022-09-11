package eu.gpirro.explanation.multithread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

import com.hp.hpl.jena.rdf.model.Model;

import eu.gpirro.explanation.structures.PathFinderException;
import eu.gpirro.explanation.structures.PathResult;
import eu.gpirro.recap.controllers.MainPanelController;
import eu.gpirro.utilities.Combination;
import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.Util;

public class RecapMultiThread {

	/**
	 * 
	 */
	private String source;
	private String target;
	private int numActiveQueryThreads = 0;
	private int totalNumberOfRemoteQueries = 0;
	private int totalNumberOfRemoteQueriesWithResults = 0;
	private long totalTimePaths;
	// map that keeps results associated to each query
	private final Map<String, PathResult[]> query_results = Collections
			.synchronizedMap(new HashMap<String, PathResult[]>());
	// atomic results (no duplicates)
	private final Map<String, PathResult> query_results_no_dupl = Collections
			.synchronizedMap(new HashMap<String, PathResult>());
	private ExecutorService execService;
	private ThreadPoolExecutor threadGroup;
	private int totalQueries;
	private ArrayList<Model> paths;
	private long startTimePaths;
	private int numQueriesGenerated;
	private MainPanelController controller;
	private ExplanationBuilder expl_builder;
	private int distance;
	private String endpointAddress;
	private String namedGraph;


	
	

	public RecapMultiThread( ) {
		
	
	}
	
	
	

	public String getEndpointAddress() {
		return endpointAddress;
	}




	public void setEndpointAddress(String endpointAddress) {
		this.endpointAddress = endpointAddress;
	}




	public String getNamedGraph() {
		return namedGraph;
	}




	public void setNamedGraph(String namedGraph) {
		this.namedGraph = namedGraph;
	}




	/**
	 * 
	 * @param source
	 * @param target
	 */
	public RecapMultiThread(String source, String target,
			MainPanelController controller, int distance, String endpointAddress, String namedGraph) {
		this.source = source;
		this.target = target;
		this.totalTimePaths = 0;
		this.startTimePaths = 0;
		this.numQueriesGenerated = 0;
		this.controller = controller;
		this.distance = distance;
		this.endpointAddress=endpointAddress;
		this.namedGraph=namedGraph;
	}

	public void setMainPanelController(MainPanelController mainPanelController) {
		this.controller = mainPanelController;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public synchronized void setTotalTimePaths(long time) {
		this.totalTimePaths = time;
	}

	public synchronized void setStartingTimePaths(long time) {
		this.startTimePaths = time;
	}

	public synchronized long getStartingTimePaths() {
		return startTimePaths;
	}

	public long getTotalTimePaths() {
		return totalTimePaths;
	}

	public ExplanationBuilder getExplanationBuilder() {
		return expl_builder;
	}

	/**
	 * Add an object of this Results; this avoids duplicates
	 * 
	 * @param s
	 * @param r
	 */
	public synchronized void addAtomicResult(String s, PathResult r) {
		query_results_no_dupl.put(s, r);
	}

	public Map<String, PathResult> getQueryResultsNoDuplicates() {
		return query_results_no_dupl;
	}

	/**
	 * Add the Result []associated to a particular query
	 * 
	 * @param query
	 * @param partial_res
	 */
	public synchronized void addPartialResults(String query,
			PathResult[] partial_res) {
		query_results.put(query, partial_res);
	}

	/**
	 * the main calls this method
	 */
	public synchronized void computeExplanations(int distance) {

		startComputingExplanations(distance);
	}

	/**
	 * 
	 * @param nbOfConnection
	 * @return
	 * @throws PathFinderException
	 */
	private String[] createCombination(int nbOfConnection)
			throws PathFinderException {
		if (nbOfConnection == 0)
			return new String[] { "to", "from" };
		else if (nbOfConnection > 0) {
			String[] ansTemp = createCombination(nbOfConnection - 1);
			int len = ansTemp.length;
			String[] ans = Arrays.copyOf(ansTemp, len * 2 + 2);
			// String[] ans = new String[ansTemp.length*2];
			for (int i = (len - 2) / 2, j = 0; i < len; i++, j++) {
				ans[len + j * 2] = ansTemp[i] + "_to";
				ans[len + j * 2 + 1] = ansTemp[i] + "_from";
			}
			ansTemp = null;

			return ans;
		} else {
			throw new PathFinderException(
					"The combination must be at least using 0 connection");
		}
	}

	/**
	 * Creates the queries to find the paths
	 * 
	 * @return
	 * @throws PathFinderException
	 */
	private String[] createSetOfCombinationQueries(int distance) throws PathFinderException {
		String[] patterns = createCombination(distance);		
		Combination[] combination = generateCombination(patterns);
		String[] combinationQuerySet = new String[combination.length];
		int idx = 0;
		for (Combination comb : combination) {

			StringBuilder queryBuilder = new StringBuilder(Constants.PREFIXES);
			queryBuilder.append("SELECT DISTINCT * ");
			queryBuilder.append("WHERE {");
			queryBuilder.append(comb.getAll());
			queryBuilder.append("FILTER (");
			for (int i = 0; i < comb.getNbPredicate(); i++) {
				String adder = comb.getIsReversed(i) ? "Fr" : "To";
				queryBuilder.append("!sameTerm(?pred" + adder + i
						+ ",rdf:type) && ");
				queryBuilder.append("!sameTerm(?pred" + adder + i
						+ ",rdfs:subClassOf) && ");
				queryBuilder.append("!regex(str(?pred" + adder + i
						+ "),'^http://www.w3.org/2002/07/owl#sameAs')");
				if (i < comb.getNbPredicate() - 1) {
					queryBuilder.append(" && ");
				}
			}
			for (int i = 0; i < comb.getNbPredicate() - 1; i++) {
				queryBuilder
						.append(" && ")
						.append("!isLiteral(?obj" + i + ") && ")
						.append("!regex(str(?obj" + i
								+ "),'^http://dbpedia.org/resource/List') && ")
						.append("!regex(str(?obj"
								+ i
								+ "),'^http://dbpedia.org/resource/Category:') && ")
						.append("!regex(str(?obj"
								+ i
								+ "),'^http://dbpedia.org/resource/Template:') && ")
						.append("!regex(str(?obj" + i
								+ "),'^http://sw.opencyc.org/')");
			}

			queryBuilder.append(Util.allDiff(comb.getNbPredicate() - 1, source,
					target));
			queryBuilder.append(" ) .");
			queryBuilder.append("}");
			//queryBuilder.append(" LIMIT 1000");
			combinationQuerySet[idx] = queryBuilder.toString();
			idx++;
		}

		return combinationQuerySet;
	}

	/**
	 * Decreases the number of active Threads. It serves to check the
	 * termination
	 */
	public synchronized void decrNumActiveQueryThreads() {
		numActiveQueryThreads = numActiveQueryThreads - 1;
	}

	/**
	 * Generate all the possible BGP combinations to reflect paths of length <=k
	 * 
	 * @param combination
	 * @return
	 */
	private Combination[] generateCombination(String[] combination) {
		Combination[] ans = new Combination[combination.length];
		int idx = 0;
		for (String s : combination) {
			String[] pattern = s.split("_");
			String[] object = new String[pattern.length + 1];
			object[0] = source;
			object[object.length - 1] = target;
			for (int i = 0; i < object.length - 2; i++) {
				object[i + 1] = "?obj" + i;
			}

			String all = "";
			boolean[] isReversed = new boolean[object.length - 1];
			for (int i = 0; i <= object.length - 2; i++) {
				if (pattern[i].equals("to")) {
					all += object[i] + " ?predTo" + i + " " + object[i + 1]
							+ " . ";
					isReversed[i] = false;
				} else {
					all += object[i + 1] + " ?predFr" + i + " " + object[i]
							+ " . ";
					isReversed[i] = true;
				}
			}
			ans[idx] = new Combination(all, object.length - 1, isReversed);

			idx++;
		}

		return ans;
	}

	/**
	 * Returns the number of thread still active It serves to check the
	 * termination
	 * 
	 * @return
	 */
	public synchronized int getActiveThreadCount() {
		return threadGroup.getActiveCount();
	}

	public synchronized int getNumActiveQueryThreads() {
		return numActiveQueryThreads;
	}

	public synchronized int getTotalNumberOfRemoteQueries() {
		return totalNumberOfRemoteQueries;
	}

	public synchronized int getTOTALQueries() {
		return totalQueries;
	}

	public synchronized int getTotalNumberOfRemoteQueriesWithResults() {
		return totalNumberOfRemoteQueriesWithResults;
	}

	/**
	 * Increases the number of active Threads.
	 */
	public synchronized void incrNumActiveQueryThreads() {
		numActiveQueryThreads = numActiveQueryThreads + 1;
	}

	public synchronized void incrTotalNumberOfRemoteQueries() {
		totalNumberOfRemoteQueries = totalNumberOfRemoteQueries + 1;
	}

	public synchronized void decrTotalNumberOfRemoteQueries() {
		/**
		 * Print here the total number of query resolved over the total number!
		 */
		double progress = ((numQueriesGenerated - totalNumberOfRemoteQueries) + 1);

		if (progress > 0.15 && progress < 0.27) {
			Platform.runLater(() -> controller.updateProgressBar((progress
					/ numQueriesGenerated + "")));

		//	System.out.println("done " + progress / numQueriesGenerated);

		} else if (progress > 0.25 && progress < 0.57) {
			Platform.runLater(() -> controller.updateProgressBar((progress
					/ numQueriesGenerated + "")));

		//	System.out.println("done " + progress / numQueriesGenerated);

		} else if (progress > 0.57 && progress < 0.77) {
			Platform.runLater(() -> controller.updateProgressBar((progress
					/ numQueriesGenerated + "")));

		} else if (progress > 0.9) {
			Platform.runLater(() -> controller.updateProgressBar((progress
					/ numQueriesGenerated + "")));

		}
		totalNumberOfRemoteQueries = totalNumberOfRemoteQueries - 1;
	}

	public synchronized void incrTotalNumberOfRemoteQueriesWithResults() {
		totalNumberOfRemoteQueriesWithResults = totalNumberOfRemoteQueriesWithResults + 1;
	}

	/**
	 * Initialize the Query executor One thread per query
	 * 
	 * @param maxThreads
	 */
	public void initializeExecutor(int maxThreads) {

		threadGroup = new ThreadPoolExecutor(maxThreads, maxThreads, 3000,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		execService = threadGroup;
	}

	public ArrayList<Model> getPaths() {
		return paths;
	}

	/**
	 * THIS IS THE END OF THE querying part; must call from here the other steps
	 * of the program
	 */
	public synchronized void shutdown(Hashtable<String, String> res_to_write) {

		execService.shutdownNow();
		
		ArrayList<PathResult> res = new ArrayList<PathResult>();
		res.addAll(query_results_no_dupl.values());

		paths = new ArrayList<Model>();
		for (PathResult r : res)
			paths.add(r.toRDFModel());

		expl_builder = new ExplanationBuilder(source, target, paths,
				res_to_write,distance,endpointAddress,namedGraph);

		// expl_builder.computeExplanations(res_to_write);

	}

	/**
	 * THIS STARTS THE COMPUTATION
	 */
	public void startComputingExplanations(int distance) {
		try {

			//System.out.println("Starting.......");
			String[] listQuery = createSetOfCombinationQueries(distance);

			numQueriesGenerated = listQuery.length;

			if (numQueriesGenerated > 0) 
			{
				totalNumberOfRemoteQueries = numQueriesGenerated;
				// System.out.println("TOTAL # of queries " + listQuery.length);
				
				// initializes the Thread pool
				totalQueries = numQueriesGenerated;
				initializeExecutor(Constants.NUM_PARALLEL_QUERIES);

				// start a new thread for each query
				for (int i = 0; i < numQueriesGenerated; i++) {
					try {
						startNewThread(source, target, listQuery[i], i);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} else {
				System.out.println("No queries have been generated ");
				System.exit(0);
			}

		} catch (PathFinderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */

	/**
	 * Start a new thread that will execute the query and add the partial
	 * results
	 * 
	 * @param source
	 * @param target
	 * @param query
	 * @throws Exception
	 */
	private void startNewThread(String source, String target, String query,
			int id) throws Exception {

		execService.execute(new PathFindingThread(this, query, source, target,
				id, controller,distance,endpointAddress,namedGraph));
	}

}
