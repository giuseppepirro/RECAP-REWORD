package eu.gpirro.explanation.reword.multithread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.Util;

public class ReWORDMultiThread {

	/**
	 * THIS IS THE MAIN
	 * 
	 * @param argv
	 */
	public static void main(String argv[]) {
		String source = "<http://dbpedia.org/resource/Fritz_Lang>";
		String target = "<http://dbpedia.org/resource/Thea_von_Harbou>";

		String endpointAddress="";
		String namedGraph="";

		ReWORDMultiThread man = new ReWORDMultiThread(source, target,
				Constants.REMOTE_ITF_VALUES,endpointAddress, namedGraph);

		man.startComputingFeatures();

		// man.startComputingPFValues();

	}

	private String source_entity;

	private String target_entity;

	private final Map<String, Double> pf_source_IN = Collections
			.synchronizedMap(new HashMap<String, Double>());

	private final Map<String, Double> pf_source_OUT = Collections
			.synchronizedMap(new HashMap<String, Double>());
	private final Map<String, Double> pf_target_IN = Collections
			.synchronizedMap(new HashMap<String, Double>());

	private final Map<String, Double> pf_target_OUT = Collections
			.synchronizedMap(new HashMap<String, Double>());

	private final Map<String, Double> itf_values = Collections
			.synchronizedMap(new HashMap<String, Double>());

	private final Set<String> features_source_IN = Collections
			.synchronizedSet(new HashSet<String>());

	private final Set<String> features_source_OUT = Collections
			.synchronizedSet(new HashSet<String>());
	private final Set<String> features_target_IN = Collections
			.synchronizedSet(new HashSet<String>());
	private final Set<String> features_target_OUT = Collections
			.synchronizedSet(new HashSet<String>());

	private int featureThreadsActive = 0;
	private int PFThreadsActive = 0;

	private int ITFThreadsActive = 0;
	private double totalInPredicateCont = -1;

	private double totalOutPredicateCont = -1;

	private double numberOfPFConections = 0;
	// FEATURES
	private ExecutorService execFeatureService;

	private ThreadPoolExecutor threadFeatureGroup;
	// PF
	private ExecutorService execPFService;

	private ThreadPoolExecutor threadPFGroup;
	// ITF
	private ExecutorService execITFService;

	private ThreadPoolExecutor threadITFGroup;
	// ITF AND PF values computed on a local graph
	private Map<String, Double> local_itf_cache;
	private Map<String, Double> local_PF_cache;
	private Map<String, Double> local_PF_no_direction_cache;

	private double getLocalINPredicateCount = -1;
	private double getLocalOUTPredicateCount = -1;

	private String endpointAddress;
	private String namedGraph;

	
	
	private boolean remote_itf_values;

	public ReWORDMultiThread(String source_entity, String target_entity,
			boolean remote_itf_values, String endpointAddress, String namedGraph) {
		super();
		this.source_entity = source_entity;
		this.target_entity = target_entity;
		local_itf_cache = new HashMap<String, Double>();
		local_PF_cache = new HashMap<String, Double>();
		local_PF_no_direction_cache = new HashMap<String, Double>();
		this.remote_itf_values = remote_itf_values;
		this.endpointAddress=endpointAddress;
		this.namedGraph=namedGraph;
	}

	/**
	 * Initialize the features of the source and target entity also according to
	 * the direction
	 * 
	 * @param entity
	 * @param direction
	 * @param features
	 */
	public synchronized void addFeatures(String entity, String direction,
			ArrayList<String> features) {
		if (entity.equalsIgnoreCase(source_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			features_source_OUT.addAll(features);
		} else if (entity.equalsIgnoreCase(source_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			features_source_IN.addAll(features);
		} else

		if (entity.equalsIgnoreCase(target_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			features_target_OUT.addAll(features);
		}

		if (entity.equalsIgnoreCase(target_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			features_target_IN.addAll(features);
		}

	}

	/**
	 * ITF value added by a thread
	 * 
	 * @param predicate
	 * @param value
	 */
	public synchronized void addITFValue(String predicate, double value) {

		itf_values.put(predicate, value);
	}

	/**
	 * PF value added by a thread
	 * 
	 * @param entity
	 * @param predicate
	 * @param direction
	 * @param value
	 */
	public synchronized void addPFValue(String entity, String predicate,
			String direction, double value) {
		if (entity.equalsIgnoreCase(source_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			pf_source_IN.put(predicate, value);
		} else if (entity.equalsIgnoreCase(source_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			pf_source_OUT.put(predicate, value);
		} else if (entity.equalsIgnoreCase(target_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			pf_target_IN.put(predicate, value);
		} else if (entity.equalsIgnoreCase(target_entity)
				&& direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			pf_target_OUT.put(predicate, value);
		}

	}

	/**
	 * check if an ITF value has been already computed
	 * 
	 * @param pred
	 * @return
	 */
	public synchronized boolean alreadyComputedITF(String pred) {
		return itf_values.containsKey(pred);
	}

	public synchronized void decreasenumberOfPFConections() {
		numberOfPFConections = numberOfPFConections - 1;
	}

	public synchronized void decrNumActiveFeatureThreads() {
		featureThreadsActive = featureThreadsActive - 1;
	}

	public synchronized void decrNumActiveITFThreads() {
		ITFThreadsActive = ITFThreadsActive - 1;
	}

	public synchronized void decrNumActivePFThreads() {
		PFThreadsActive = PFThreadsActive - 1;
	}

	public synchronized int getActiveFeatureThreadCount() {
		return threadFeatureGroup.getActiveCount();
	}

	public synchronized int getActiveITFThreadCount() {
		return threadITFGroup.getActiveCount();
	}

	public synchronized int getActivePFThreadCount() {
		return threadPFGroup.getActiveCount();
	}

	/**
	 * Compute ITF values given a graph in input
	 * 
	 * @param graph
	 * @param predicate
	 * @return
	 */

	/**
	 * RIDEFINIRE QUESTO METODO NON e' locale considerare TUTTE le triple che
	 * hanno p nel knowledge graph!!!
	 * 
	 * @param graph
	 * @param predicate
	 * @return
	 */
	public double getLocalITF(Model graph, String predicate) {
		double itf = 0.0;

		predicate = Util.getObjectShortName(predicate);

		/**
		 * Check prefixes and predicate
		 */

		if (!local_itf_cache.containsKey((predicate))) {

			double denom = -1;
			String query = "";

			query = " SELECT (COUNT(*) as ?count) WHERE " + "{ " + "?s "
					+ predicate + " ?o " + "}";

			Query jena_query = null;
			QueryExecution qexec = null;
			ResultSet results = null;
			jena_query = QueryFactory.create(query);
			qexec = QueryExecutionFactory.create(jena_query, graph);

			results = qexec.execSelect();
			// get the result
			denom = Double.parseDouble(results.nextSolution().get("count")
					.asLiteral().getDouble()
					+ "");// get the result from above!
			qexec.close();

			if (denom != 0)
				itf = Math.log(graph.size() / denom);
			else
				itf = 0;
			local_itf_cache.put(predicate, itf);

			return itf;

		}
		// else{
		// System.out.println("ALREADY in the LOCAL GRAPH cache ITF");
		// }

		return itf;

	}

	public double getLocalPFNoDirection(String predicate, Model graph) {
		double pf_no_direction = 0.0;

		predicate = Util.getObjectShortName(predicate);

		/**
		 * Check prefixes and predicate
		 */

		if (!local_PF_no_direction_cache.containsKey((predicate))) {

			double denom = -1;
			String query = "";

			query = " SELECT (COUNT(*) as ?count) WHERE " + "{ " + "?s "
					+ predicate + " ?o " + "}";

			Query jena_query = null;
			QueryExecution qexec = null;
			ResultSet results = null;
			jena_query = QueryFactory.create(query);
			qexec = QueryExecutionFactory.create(jena_query, graph);

			results = qexec.execSelect();
			// get the result
			denom = Double.parseDouble(results.nextSolution().get("count")
					.asLiteral().getDouble()
					+ "");// get the result from above!
			qexec.close();

			if (denom != 0) {
				pf_no_direction = denom / graph.size();
				local_PF_no_direction_cache.put(predicate, pf_no_direction);

			} else {
				pf_no_direction = 0;
				local_PF_no_direction_cache.put(predicate, pf_no_direction);
			}

			return pf_no_direction;
		} else {
			pf_no_direction = local_PF_no_direction_cache.get(predicate);

		}

		return pf_no_direction;

	}

	/**
	 * Compute PF values given a graph in input
	 * 
	 * @param entity
	 * @param graph
	 * @param predicate
	 * @param direction
	 * @return
	 */
	public double getLocalPF(String entity, Model graph, String predicate,
			String direction) {

		double f_i_p = 0.0;
		double f_i = 0.0;
		double pf = 0.0;
		String query = "";
		double total_in_pred_count = -1;
		double total_out_pred_count = -1;
		Query jena_query = null;
		QueryExecution qexec = null;
		ResultSet results = null;

		// necessary to rewrite with short names!
		predicate = Util.getObjectShortName(predicate);
		entity = Util.getObjectShortName(entity);

		if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {

			query = Constants.PREFIXES
					+ " SELECT (COUNT(*) as ?count) WHERE { ?o " + predicate
					+ " " + entity + " }";

			jena_query = QueryFactory.create(query);
			qexec = QueryExecutionFactory.create(jena_query, graph);

			results = qexec.execSelect();
			// get the result
			f_i_p = Double.parseDouble(results.nextSolution().get("count")
					.asLiteral().getDouble()
					+ "");// get the result from above!
			qexec.close();

			if (getLocalINPredicateCount == -1.0) {
				query = Constants.PREFIXES
						+ " SELECT (COUNT(*) as ?count) WHERE { ?o ?p "
						+ entity + " }";

				jena_query = QueryFactory.create(query);
				qexec = QueryExecutionFactory.create(jena_query, graph);

				results = qexec.execSelect();
				// get the result
				f_i = Double.parseDouble(results.nextSolution().get("count")
						.asLiteral().getDouble()
						+ "");// get the result from above!
				qexec.close();

				total_in_pred_count = f_i;
				getLocalINPredicateCount = total_in_pred_count;

			} else {

				f_i = getLocalINPredicateCount;

			}

		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {

			query = Constants.PREFIXES
					+ " SELECT (COUNT(*) as ?count) WHERE { " + entity + " "
					+ predicate + " ?o }";

			jena_query = QueryFactory.create(query);
			qexec = QueryExecutionFactory.create(jena_query, graph);

			results = qexec.execSelect();
			// get the result
			f_i_p = Double.parseDouble(results.nextSolution().get("count")
					.asLiteral().getDouble()
					+ "");
			qexec.close();

			if (getLocalOUTPredicateCount == -1.0) {
				query = Constants.PREFIXES
						+ " SELECT (COUNT(*) as ?count) WHERE { " + entity
						+ " ?p " + " ?o }";

				qexec = QueryExecutionFactory.create(jena_query, graph);

				results = qexec.execSelect();
				// get the result
				f_i = Double.parseDouble(results.nextSolution().get("count")
						.asLiteral().getDouble()
						+ "");
				qexec.close();

				total_out_pred_count = f_i;
				getLocalOUTPredicateCount = total_out_pred_count;

			} else {

				f_i = getLocalOUTPredicateCount;

			}

		}

		pf = f_i_p / f_i;
		return pf;

	}

	public synchronized double getNumberOfPFConnection() {
		return numberOfPFConections;
	}

	/**
	 * Total number of queries to retrieve features to be executed
	 * 
	 * @return
	 */
	public synchronized int getNumFeatureQueriesToBeExecuted() {
		return featureThreadsActive;
	}

	/**
	 * Total number of queries to retrieve ITF to be executed
	 * 
	 * @return
	 */
	public synchronized int getNumITFQueriesToBeExecuted() {
		return ITFThreadsActive;
	}

	/**
	 * Total number of queries to retrieve PF values to be executed
	 * 
	 * @return
	 */
	public synchronized int getNumPFQueriesToBeExecuted() {
		return PFThreadsActive;
	}

	/**
	 * Compute PF-ITF values on the REMOTE Knowledge Base
	 * 
	 * @param path
	 * @param graph
	 * @return
	 */
	public double getREMOTEPathInformativeness(Model path, Model graph) {

		/**
		 * 
		 * REMOTE PF-ITF VALUES!!!
		 * 
		 * MAYBE THIS METHOD GOES in the run of a Thread; Each thread computes
		 * the value of inf for a path!!!!!
		 * 
		 * The code of the original REWORD can be used!!!!!
		 * 
		 */

		StmtIterator it = path.listStatements();
		Statement temp_st;
		double itf = 0;
		double pf = 0;
		double inf = 0;

		String sub = "";
		String pred = "";
		String key = "";

		int lenght = 0;

		while (it.hasNext()) {
			lenght++;
			temp_st = it.next();

			sub = Util.getObjectFullName(temp_st.getSubject().toString());
			pred = Util.getObjectFullName(temp_st.getPredicate().toString());

			if (local_itf_cache.get(pred) == null) {

				// HERE one should fill the various data structures with the
				// things to be computed!
				// get the values when all threads terminates and compute the
				// informativeness

				itf = 0.0;

				/*
				 * 
				 * 
				 * 
				 * 
				 * getREMOTEITF(graph, pred);
				 */

				local_itf_cache.put(pred, itf);

			} else {

				itf = local_itf_cache.get(pred);
			}

			key = sub + pred + Constants.DIRECTION_OUT;

			if (local_PF_cache.get(key) == null) {
				pf = 0.0;

				/*
				 * 
				 * 
				 * 
				 * 
				 * getREMOTEPF(sub, graph, pred, Constants.DIRECTION_OUT);
				 */

				local_PF_cache.put(key, pf);

			} else {
				pf = local_PF_cache.get(key);
			}

			inf = inf + (itf * pf);

		}

		inf = inf / lenght;

		return inf;

	}

	/**
	 * Path is the current path; The Model graph is the Explanation obtained as
	 * union of all paths
	 * 
	 * @param path
	 * @param graph
	 */
	public double getLOCALPathInformativeness(Model path, Model graph) {
		StmtIterator it = path.listStatements();

		Statement temp_st;
		double itf = 0;
		double pf = 0;
		double inf = 0;

		String sub = "";
		String pred = "";
		String key = "";

		int lenght = 0;

		while (it.hasNext()) {
			lenght++;
			temp_st = it.next();

			sub = Util.getObjectFullName(temp_st.getSubject().toString());
			pred = Util.getObjectFullName(temp_st.getPredicate().toString());

			if (local_itf_cache.get(pred) == null) {
				itf = getLocalITF(graph, pred);

				local_itf_cache.put(pred, itf);

			} else {

				itf = local_itf_cache.get(pred);
			}

			key = sub + pred + Constants.DIRECTION_OUT;

		//	System.out.println("subj=" + sub);
			// the key also takes into account of the direction±
			if (local_PF_cache.get(key) == null) {
				pf = getLocalPF(sub, graph, pred, Constants.DIRECTION_OUT);

				local_PF_cache.put(key, pf);

			} else {
				pf = local_PF_cache.get(key);
			}

			// System.out.println(
			// "Pred="+pred+" PF="+pf+" ITF="+itf+" pfitf="+itf * pf);

			inf = inf + (itf * pf);

		}

		inf = inf / lenght;

		// System.out.println("Path in : " + +inf);
		// path.write(System.out);

		// System.out.println();

		return inf;

	}

	/**
	 * 
	 */
	public synchronized double getTotalInPredicateCount() {
		return totalInPredicateCont;
	}

	/**
	 * 
	 */
	public synchronized double getTotalOutPredicateCount() {

		return totalOutPredicateCont;
	}

	public synchronized void increasenumberOfPFConections() {
		numberOfPFConections = numberOfPFConections + 1;
	}

	public synchronized void incrNumActiveFeatureThreads() {
		featureThreadsActive = featureThreadsActive + 1;
	}

	public synchronized void incrNumActiveITFThreads() {
		ITFThreadsActive = ITFThreadsActive + 1;
	}

	public synchronized void incrNumActivePFThreads() {
		PFThreadsActive = PFThreadsActive + 1;
	}

	public void initializeFeatureExecutor(int maxThreads) {
		threadFeatureGroup = new ThreadPoolExecutor(maxThreads, maxThreads,
				5000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		execFeatureService = threadFeatureGroup;
		// featureQueriesToBeExecuted = maxThreads;
	}

	public void initializeITFExecutor(int maxThreads) {
		threadITFGroup = new ThreadPoolExecutor(maxThreads, maxThreads, 5000,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		execITFService = threadITFGroup;

		// ITFQueriesToBeExecuted = maxThreads;

	}

	public void initializePFExecutor(int maxThreads) {
		threadPFGroup = new ThreadPoolExecutor(maxThreads, maxThreads, 5000,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		execPFService = threadPFGroup;

		// PFQueriesToBeExecuted = maxThreads;

	}

	/**
	 * 
	 */
	public synchronized void setTotalInPredicateCount(
			double totalInPredicateCont) {
		this.totalInPredicateCont = totalInPredicateCont;
	}

	/**
	 * 
	 */
	public synchronized void setTotalOutPredicateCount(
			double totalOutPredicateCont) {
		this.totalOutPredicateCont = totalOutPredicateCont;

	}

	public synchronized void shutdownFeatureThreadExecutor() {

		execFeatureService.shutdownNow();

		// AT THIS POINT ALL THE FEATURES ARE AVAILABLE

		try {

			startComputingITFValues();
			System.out.println("DONE ITF VALUES");

			startComputingPFValues();

			System.out.println("DONE PF VALUES");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public synchronized void shutdownITFThreadExecutor() {

		execITFService.shutdownNow();

		startComputingPFValues();

		// DO SOMETHING AFTER having everything
		//
		//

	}

	public synchronized void shutdownPFThreadExecutor() {

		execPFService.shutdownNow();

		// AT THIS POINT ALL THE PF VALUES are available

		// DO SOMETHING e.g. start ITF

	}

	public void startComputingFeatures() {
		// There are 4 types of features
		// source_IN, source_OUT, target_IN, target_OUT

		initializeFeatureExecutor(4);
		try {
			startNewFeatureThread(Constants.DIRECTION_IN, source_entity, 0,endpointAddress);
			startNewFeatureThread(Constants.DIRECTION_OUT, source_entity, 1,endpointAddress);
			startNewFeatureThread(Constants.DIRECTION_IN, target_entity, 2,endpointAddress);
			startNewFeatureThread(Constants.DIRECTION_OUT, target_entity, 3,endpointAddress);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Compute ITF values
	 */

	public void startComputingITFValues() {

		if (remote_itf_values) {

			// get the number of queries needed
			int NUM_ITF_THREAD = Constants.NUM_ITF_THREADS;
			// change the number

			initializeITFExecutor(NUM_ITF_THREAD);

			int id = 0;

			// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
			for (String pred : features_source_IN) {

				try {
					startNewITFThread(pred, id,endpointAddress);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
			for (String pred : features_source_OUT) {

				try {
					startNewITFThread(pred, id,endpointAddress);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
			for (String pred : features_target_IN) {

				try {
					startNewITFThread(pred, id,endpointAddress);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
			for (String pred : features_target_OUT) {

				try {
					startNewITFThread(pred, id,endpointAddress);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} else {
			System.out.println("Getting ITF value from a file loaded....");

			// compute by reading value from a hash table
			// in a file pred itf value

		}

		// /

	}

	/**
	 * Compute PF values each predicate a thread; consider to reduce the number
	 * 
	 * 
	 * 
	 * 
	 * This is just useful for REWERD not for recap;
	 * 
	 * RECAP needs a PATH to compute its informativeness!!
	 * 
	 * An executorThread for each path must be created
	 */
	public void startComputingPFValues() {

		int size = features_source_IN.size() + features_target_IN.size()
				+ features_source_OUT.size() + features_target_OUT.size();

		// CHANGE THE SIZEEEEE below
		// initializePFExecutor(size)

		// DEPENDING ON THE SIZE CREATE MORE THAN ONE EXECUTOR!!!

		System.out.println("TOTAL NUMBER " + size);

		initializePFExecutor(size);

		int id = 0;
		/**
		 * 
		 * 
		 * TOO MANY QUERIES too MANY Threads the server returns 503!!
		 * 
		 */
		try {

			for (String pred : features_source_IN) {
				startNewPFThread(Constants.DIRECTION_IN, source_entity, pred,
						id,endpointAddress);
				id++;
			}

			for (String pred : features_source_OUT) {
				startNewPFThread(Constants.DIRECTION_OUT, source_entity, pred,
						id,endpointAddress);
				id++;

			}

			for (String pred : features_target_IN) {
				startNewPFThread(Constants.DIRECTION_IN, target_entity, pred,
						id,endpointAddress);
				id++;

			}

			for (String pred : features_target_OUT) {
				startNewPFThread(Constants.DIRECTION_OUT, target_entity, pred,
						id,endpointAddress);
				id++;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Start a new Feature Thread
	 * 
	 * @param direction
	 * @param entity
	 * @param id
	 * @throws Exception
	 */
	private void startNewFeatureThread(String direction, String entity, int id, String endpointAddress)
			throws Exception {

		execFeatureService.execute(new FeatureThread(this, direction, entity,
				id,endpointAddress,namedGraph));
	}

	/**
	 * Start a new ITF Threads
	 * 
	 * @param predicate
	 * @param id
	 * @throws Exception
	 */
	private void startNewITFThread(String predicate, int id, String endpointAddress) throws Exception {

		execITFService.execute(new ITFThread(this, predicate, id,endpointAddress,namedGraph));
	}

	private void startNewPFThread(String direction, String entity,
			String predicate, int id, String endpointAddress) throws Exception {

		execPFService.execute(new PFThread(this, direction, entity, predicate,
				id,endpointAddress,namedGraph));
	}

}
