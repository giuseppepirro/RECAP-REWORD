package eu.gpirro.explanation.multithread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.gpirro.explanation.multithread.diversity.DiversityCalculator;
import eu.gpirro.explanation.multithread.informativeness.PathInformativenessCalculator;
import eu.gpirro.explanation.reword.multithread.ReWORDMultiThread;
import eu.gpirro.explanation.structures.Path;
import eu.gpirro.explanation.structures.Pattern;
import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.Variable;

public class ExplanationBuilder {

	private ReWORDMultiThread reword;
	private ArrayList<Path> ranked_paths;

	// for each pattern keep its instances
	private Hashtable<String, ArrayList<Model>> reference_patterns_graph;
	private String source_e;
	private String target_e;
	private ArrayList<Model> paths;
	// contains statistics about the execution of path finding
	private Hashtable<String, String> res_to_write;
	private Model reference_paths_graph;
	private boolean reference_paths_computed;
	private boolean reference_patterns_computed;
	private EntitySuggestion ent_suggestion;
	
	private ArrayList<Pattern> patterns_with_informativeness;

	private String endpointAddress;
	private String namedGraph;

	private int distance;

	/**
	 * Constructor
	 * 
	 * @param source_e
	 * @param target_e
	 * @param paths
	 *            : computed by the multithread approach
	 * @param res_to_write
	 */
	public ExplanationBuilder(String source_e, String target_e,
			ArrayList<Model> paths, Hashtable<String, String> res_to_write,
			int distance, String endpointAddress, String namedGraph) {

		this.res_to_write = res_to_write;
		this.source_e = source_e;
		this.target_e = target_e;
		this.paths = paths;
		this.ranked_paths = new ArrayList<Path>();
		patterns_with_informativeness = new ArrayList<Pattern>();
		ent_suggestion = new EntitySuggestion();
		reference_patterns_graph = new Hashtable<String, ArrayList<Model>>();
		this.distance = distance;
		this.endpointAddress = endpointAddress;
		this.namedGraph = namedGraph;

		/*
		 * NEEDED for PR-ITF values
		 */
		reword = new ReWORDMultiThread(source_e, target_e,
				Constants.REMOTE_ITF_VALUES, endpointAddress, namedGraph);

	}

	public String getNamedGraph() {
		return namedGraph;
	}

	public void setNamedGraph(String namedGraph) {
		this.namedGraph = namedGraph;
	}

	/**
	 * Returns the instance of entity_suggestion
	 * 
	 * @return
	 */
	public EntitySuggestion getEntitySuggestion() {
		return ent_suggestion;
	}

	/**
	 * BASIC UNION EXPLANATION
	 * 
	 * @param intial_first
	 * @param initial_second
	 */
	public Vector<Object> BASICMERGE(String intial_first, String initial_second) {

		Vector eMERGE_computation = getBasicMergeExplanation(intial_first,
				initial_second);

		reference_paths_graph = (Model) eMERGE_computation.get(0);
		setReferencePathsComputed(true);
		return eMERGE_computation;
	}

	/**
	 * This method receive paths computed by using a parallel (MultiThreaded)
	 * technique and compute the different types of explanations
	 * 
	 * @param res_to_write
	 */
	public void computeExplanations(Hashtable<String, String> res_to_write) {
		String intial_first = source_e.substring(source_e.lastIndexOf("/") + 1,
				source_e.length() - 1);
		String initial_second = target_e.substring(
				target_e.lastIndexOf("/") + 1, target_e.length() - 1);

		/**
		 * PATH RETRIEVAL INFO
		 */

		System.out.println(source_e + "\t" + target_e);
		System.out.println(res_to_write.get(Constants.RES_TOTAL_NUM_PATHS)
				+ "\t"
				+ ""
				+ res_to_write.get(Constants.RES_TOTAL_TIME_PATHS)
				+ "\t"
				+ ""
				+ res_to_write.get(Constants.RES_TOTAL_NUM_QUERIES_PATHS)
				+ "\t"
				+ ""
				+ res_to_write
						.get(Constants.RES_TOTAL_NUM_QUERIES_WITH_RES_PATHS));

		// Types of explanations to be computed
		BASICMERGE(intial_first, initial_second);
		// DIVERSITY(intial_first, initial_second);

		try {

			MIP_LOCAL(intial_first, initial_second);
		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * System.out.println(); ArrayList<Model> paths=getPaths(); for(Model m:
		 * paths) { m.write(System.out); System.out.println(); }
		 */

		// MIP_REMOTE(intial_first, initial_second);
		// TOPK_PATH_INSTANCES_REMOTE(intial_first, initial_second);

		int k = 0;
		Model top_k_expl = (Model) TOPK_PATH_INSTANCES_LOCAL(intial_first,
				initial_second, k).get(0);

		// System.out.println(getExplanationPattern(top_k_expl));

		// TODO: PathPattern based explanations
		// double getPathPatternInformativeness(Model path)
		// store in a hashtable: |path pattern| # of path instances sharing the
		// same pattern|

		// Model eTOPKPattern =
		// getUnionOfTopKPathPatternExplanation(intial_first, initial_second);
		// computeEntitySuggestions(MIP.getModel(), source_e, target_e);
		// Combine informativeness and Diversification
		// consider owl:sameAs

	}

	/**
	 * DIVERSITY BASED EXPLANATION
	 * 
	 * @param intial_first
	 * @param initial_second
	 */

	public Vector DIVERSITY(String intial_first, String initial_second,
			double percentage) {

		Vector eDELTA = getDiversyfiedExplanation(intial_first, initial_second,
				Constants.WRITE_EXPLANATIONS_ON_DISK, percentage);
		Model expl_delta = (Model) eDELTA.get(0);

		// System.out.println("Time_EDelta=" + time_delta + "ms \t"
		// + " size_EDelta=" + expl_delta.size());

		return eDELTA;

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

		}

		// System.out.println(queryBuilder.toString());

		URL url = null;
		try {
			url = new URL(queryBuilder.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block

			// System.out.println("MALFORMED URL ");

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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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

	/**
	 * 
	 * @param pahts_with_ranks
	 * @return
	 */
	private ArrayList<Path> filterTopKPathInstances(
			ArrayList<Path> pahts_with_ranks, int K) {

		ArrayList<Path> topk = new ArrayList<Path>();
		HashMap<String, String> temp_check = new HashMap<String, String>();
		Path best = null;
		Path temp_path = null;

		String pattern_as_string = "";
		boolean done = false;
		int k = 0;

		while (!done) {
			best = pahts_with_ranks.get(0);
			// Get the current best among those remaining
			for (int j = 0; j < pahts_with_ranks.size(); j++) {
				temp_path = pahts_with_ranks.get(j);
				if (temp_path.getInformativenenss() > best
						.getInformativenenss()) {
					best = temp_path;
				}
			}

			// Get the String representation of the best path
			pattern_as_string = best.getPrototypical_pattern().trim();
			// If it is the first time such a path is seen put it into a
			// Hashtable
			if (!temp_check.containsKey(pattern_as_string)) {
				// If it was the first time then such a path must be in the
				// topk
				topk.add(best);
				// increment k
				k++;

				// remove from those to be looked
				pahts_with_ranks.remove(best);

				// avoid to present the same pattern multiple times (should
				// be redundant since each explanation is unique)
				temp_check.put(pattern_as_string, pattern_as_string);

			} else if (temp_check.containsKey(pattern_as_string)) {

				pahts_with_ranks.remove(best);
			}

			if (k == K || pahts_with_ranks.size() == 0)
				done = true;

		}
		return topk;

	}

	/**
	 * GET BASIC merge of paths!
	 * 
	 * @param intial_first
	 * @param initial_second
	 */
	public Vector<Object> getBasicMergeExplanation(String intial_first,
			String initial_second) {

		// pos 0 there will be the Model
		// pos 1 there will be the time!
		Vector<Object> res = new Vector<Object>();
		long start = System.currentTimeMillis();
		Model union = null;
		try {
			union = getBasicPathUnion();
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		long end = System.currentTimeMillis();
		long time = end - start;
		if (union.size() == 0) {
			System.err.println("No connection found between - source_entity="
					+ source_e + " and target_entity=" + target_e);
		}
		res.add(union);
		res.add(time);

		return res;
	}

	/**
	 * The explanation contains the union of all paths!
	 * 
	 * @return
	 * @throws Exception
	 */
	public Model getBasicPathUnion() throws Exception {

		Model res = ModelFactory.createDefaultModel();

		if (paths.size() == 0) {
			paths = getPathsAsModels();
		}
		for (Model m : paths) {
			res = res.union(m);
		}
		return res;

	}

	/**
	 * Explanation with diversification
	 * 
	 * @param intial_first
	 * @param initial_second
	 */
	public Vector<Object> getDiversyfiedExplanation(String intial_first,
			String initial_second, boolean write_on_disk, double percentage) {

		// pos 0 there will be the Model
		// pos 1 there will be the time!
		Vector<Object> res = new Vector<Object>();
		// class to handle efficently the computation of distances
		DiversityCalculator div_calc = new DiversityCalculator(paths);
		long start = System.currentTimeMillis();
		Model delta = null;
		try {
			delta = div_calc.getRadiusExplanation(percentage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		long time = end - start;

		res.add(delta);
		res.add(time);
		if (write_on_disk) {
			String fileName_DELTA = "expl_" + intial_first + "-"
					+ initial_second + "_DELTA_" + Constants.RADIUS_DELTA
					+ "_DIST_" + distance + ".nt";
			try {
				writeModel(fileName_DELTA, delta);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return res;

	}

	/**
	 * Return a Path Pattern given a Model in a Path
	 * 
	 * @param path
	 * @return
	 */
	public String getExplanationPattern(Model path) {

		return "SELECT DISTINCT ?vsource ?vtarget WHERE { "
				+ ent_suggestion.getPrototypicalPattern(path, source_e,
						target_e) + " FILTER( ?vsource != ?vtarget) }";

	}

	/**
	 * Return the graph (of variables and preds) associated to a pattern
	 * 
	 * @return
	 */
	public String getGraphExplanationPattern(Model path) {

		return ent_suggestion.getPathPattern(path, source_e, target_e);
	}

	/**
	 * Compute an explanation as the Most Informative Path by usign a graph in
	 * input (e.g. the union of all paths) as reference
	 * 
	 * @param intial_first
	 * @param initial_second
	 * @param graph
	 * @param write_on_disk
	 * @return
	 */
	private Vector<Object> getLOCALMostInformativePathExplanation(
			String intial_first, String initial_second, Model graph,
			boolean write_on_disk) {

		// pos 0 there will be the Model
		// pos 1 there will be the time!

		Vector<Object> res = new Vector<Object>();

		// ///////////////////////////
		// MIP
		long start = System.currentTimeMillis();
		Path best = null;
		try {
			best = getMIPLocalData(graph);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		long end = System.currentTimeMillis();
		long time = end - start;

		res.add(best);
		res.add(time);

		if (write_on_disk) {

			String fileName_MIP = "MIP_LOCAL_DATA_" + intial_first + "-"
					+ initial_second + "_DIST_" + distance + ".nt";

			try {
				writeModel(fileName_MIP, best.getModel());
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

		}

		return res;
	}

	/**
	 * Get the most informative path of a set of paths wrt a graph of reference
	 * 
	 * @param graph
	 * @return
	 */
	public Path getMIPLocalData(Model graph) {

		if (paths.size() == 0) {
			try {
				paths = getPathsAsModels();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		double best = 0;
		double temp = 0;
		Path best_path = null;

		for (Model m : paths) {
			temp = reword.getLOCALPathInformativeness(m, graph);
			if (temp > best) {
				best = temp;

				best_path = new Path(m, temp,
						ent_suggestion.getPrototypicalPattern(m, source_e,
								target_e));
				ranked_paths.add(best_path);

			} else {

				best_path = new Path(m, temp,
						ent_suggestion.getPrototypicalPattern(m, source_e,
								target_e));

				ranked_paths.add(best_path);

			}

		}
		return best_path;

	}

	/**
	 * Returns the statistics for path finding
	 * 
	 * @return
	 */
	public Hashtable<String, String> getPathFindingStatistics() {
		return res_to_write;
	}

	/**
	 * Computes the informativeness of a path pattern
	 * 
	 * @param path
	 * @return
	 */
	public double getPathPatternInformativeness(String pattern) {

		double unique_patterns = reference_patterns_graph.size();
		double num_shared_instances = reference_patterns_graph.get(pattern)
				.size();

		double res = Math.log((unique_patterns / num_shared_instances));

		return res;
	}

	/**
	 * Return a pattern by substituting the entity passed with a variable
	 * 
	 * @param explanation
	 * @param entity
	 * @return
	 */

	public ArrayList<Model> getPaths() {
		return paths;
	}

	/**
	 * Each model in the ArrayList represents a path
	 * 
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Model> getPathsAsModels() throws Exception {

		return paths;
	}

	public Hashtable<String, ArrayList<Model>> getPatterns() {
		return reference_patterns_graph;
	}

	/**
	 * Union/merge of all patterns
	 * 
	 * The bindings of variables cannot in principle be shared
	 * 
	 * @return
	 */
	private Hashtable<String, ArrayList<Model>> getPatternsAndAssociatedInstances() {

		if (paths.size() == 0) {
			try {
				paths = getPathsAsModels();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String pattern = "";
		ArrayList<Model> path_instances;

		for (Model m : paths) {
			pattern = getGraphExplanationPattern(m);

			if (reference_patterns_graph.get(pattern) == null) {
				path_instances = new ArrayList<Model>();
				path_instances.add(m);
				reference_patterns_graph.put(pattern, path_instances);

			} else {
				path_instances = reference_patterns_graph.get(pattern);
				path_instances.add(m);
				reference_patterns_graph.put(pattern, path_instances);

			}
		}

		setReferencePatternsComputed(true);

		/*
		 * ArrayList<Model> re; for (String s :
		 * reference_patterns_graph.keySet()) { re =
		 * reference_patterns_graph.get(s);
		 * 
		 * for (Model m : re) { m.write(System.out); }
		 * 
		 * System.out.println("************"); System.out.println();
		 * 
		 * }
		 */

		// /System.out.println(pattern);

		return reference_patterns_graph;

	}

	/**
	 * Get basic merge of patterns
	 * 
	 * @param intial_first
	 * @param initial_second
	 * @return
	 */

	public Vector getPatternsAndInstances(String intial_first,
			String initial_second) {
		// pos 0 there will be the Model
		// pos 1 there will be the time!
		Vector res = new Vector();
		Hashtable<String, ArrayList<Model>> comp = null;
		long start = System.currentTimeMillis();

		try {
			comp = getPatternsAndAssociatedInstances();
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		long end = System.currentTimeMillis();
		long time = end - start;
		if (comp.size() == 0) {
			System.err.println("No connection found between - source_entity="
					+ source_e + " and target_entity=" + target_e);
		}
		res.add(comp);
		res.add(time);

		return res;
	}

	/**
	 * Returns the reference graph; merge of all paths
	 * 
	 * @return
	 */
	public Model getReferenceGraph() {

		if (isReferencePathsComputed())
			return reference_paths_graph;
		else
			try {
				return getBasicPathUnion();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}

	/**
	 * Most informative Model
	 * 
	 * @return
	 * @throws Exception
	 */
	public Path getREMOTEMostInformativePath() throws Exception {
		double best = 0;
		double temp = 0;

		Path best_path = null;

		if (paths.size() == 0) {
			paths = getPathsAsModels();
		}

		for (Model m : paths) {

			temp = getREMOTEPathInformativeness(m);

			if (temp > best) {
				best = temp;

				best_path = new Path(m, temp,
						ent_suggestion.getPrototypicalPattern(m, source_e,
								target_e));
				ranked_paths.add(best_path);

			} else {

				best_path = new Path(m, temp,
						ent_suggestion.getPrototypicalPattern(m, source_e,
								target_e));

				ranked_paths.add(best_path);

			}

		}

		return best_path;

	}

	/**
	 * Get the MOST informative (with Remote computations of PF-ITF) PATH
	 * EXPLANATION
	 * 
	 * @param intial_first
	 * @param initial_second
	 */
	public Vector<Object> getREMOTEMostInformativePathExplantion(
			String intial_first, String initial_second, boolean write_on_disk) {

		// pos 0 there will be the Model
		// pos 1 there will be the time!

		Vector<Object> res = new Vector<Object>();
		long start = System.currentTimeMillis();
		start = System.currentTimeMillis();
		Path best = null;
		try {
			best = getREMOTEMostInformativePath();
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		long end = System.currentTimeMillis();
		long time = end - start;
		res.add(best);
		res.add(time);

		if (write_on_disk) {
			String fileName_MIP = "MIP_REMOTE_" + intial_first + "-"
					+ initial_second + "_DIST_" + distance + ".nt";
			try {
				writeModel(fileName_MIP, best.getModel());
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		}

		return res;
	}

	/**
	 * Return the informativeness of a path
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	public double getREMOTEPathInformativeness(Model model) throws Exception {

		/***
		 * CAHNGE THIS TO use the multithread version of reword!
		 */

		System.out.println("ExplBuilder line 969 TO BE COMPLETED!");

		return -1.0;

	}

	/**
	 * Compute the informativeness of all patterns
	 */
	private void computeAllPatternsInformativeness() {
		Set<String> unique_patterns = reference_patterns_graph.keySet();
		for (String pat : unique_patterns) {
			double inf = getPathPatternInformativeness(pat);
			Pattern p = new Pattern(pat, inf);
			patterns_with_informativeness.add(p);
		}
	}

	/**
	 * Implements equation (6)
	 * 
	 * @param k
	 * @param
	 * @return
	 */
	private ArrayList<Pattern> getTopKPathPatterns(int K) {
		ArrayList<Pattern> top_k_patterns = new ArrayList<Pattern>();

		if (!reference_patterns_computed)
			getPatternsAndAssociatedInstances();

		computeAllPatternsInformativeness();
		//

		if (K >= patterns_with_informativeness.size())
			return patterns_with_informativeness;

		ArrayList<Pattern> copy_pattern_inf = new ArrayList<Pattern>();

		for (Pattern p : patterns_with_informativeness)
			copy_pattern_inf.add(p);

		boolean done = false;
		Pattern best = null;
		int k = 0;

		while (!done) {
			best = copy_pattern_inf.get(0);
			for (int j = 0; j < copy_pattern_inf.size(); j++) {
				Pattern temp_pattern = copy_pattern_inf.get(j);
				if (temp_pattern.getInformativenenss() > best
						.getInformativenenss()) {
					best = temp_pattern;
				}
			}
			top_k_patterns.add(best);
			k++;

			copy_pattern_inf.remove(best);

			if (k == K || copy_pattern_inf.size() == 0)
				done = true;
		}

		return top_k_patterns;

	}

	/**
	 * Return the top-k best explanations
	 * 
	 * @param K
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Path> getTopKPaths(int K, boolean remote_pf_itf)
			throws Exception {
		ArrayList<Path> topk = new ArrayList<Path>();
		HashMap<String, String> temp_check = new HashMap<String, String>();
		Path best = null;
		Path temp_path = null;
		String pattern_as_string = "";
		boolean done = false;
		int k = 0;

		if (paths.size() == 0)
			paths = getPathsAsModels();

		if (K > paths.size()) {

			System.out.println("There are " + paths.size()
					+ " explanations. Value of K automatically reduced");
			K = paths.size();
		} else {

			PathInformativenessCalculator inf_calc = new PathInformativenessCalculator(
					source_e, target_e, paths, getBasicPathUnion(),
					Constants.REMOTE_ITF_VALUES, endpointAddress, namedGraph);

			ranked_paths = inf_calc.getInformativenessMultiThreads();

			/*
			 * // System.out.println("# of paths " + paths.size()); for (Model
			 * path : paths) {
			 * 
			 * 
			 * 
			 * 
			 * RANKING DONE LOCALLY
			 * 
			 * 
			 * if (!remote_pf_itf) { ranked_paths.add(new Path(path, reword
			 * .getLOCALPathInformativeness(path, getBasicPathUnion()),
			 * ent_suggestion .getPrototypicalPattern(path, source_e,
			 * target_e))); }
			 * 
			 * 
			 * 
			 * 
			 * 
			 * RANKING DONE REMOTELY!
			 * 
			 * else {
			 *//**
			 * 
			 * 
			 * 
			 * TODO TODOTODOTODOTODOTODOTODOTODOTODO
			 * 
			 * 
			 * 
			 * 
			 * 
			 */
			/*
			 * // MAYBE IT IS THE CASE TO CREATE A NEW THREAD PER //
			 * PATH!!!!!!!!
			 * 
			 * // PUT THE CODE TO GET THE VALUES REMOTELY }
			 * 
			 * }
			 */

			while (!done) {

				best = ranked_paths.get(0);
				// Get the current best among those remaining
				for (int j = 0; j < ranked_paths.size(); j++) {
					temp_path = ranked_paths.get(j);
					if (temp_path.getInformativenenss() > best
							.getInformativenenss()) {
						best = temp_path;

					}

				}

				// Get the String representation of the best path
				pattern_as_string = best.getPrototypical_pattern().trim();

				// If it is the first time such a path is seen put it into a
				// Hashtable
				if (!temp_check.containsKey(pattern_as_string)) {

					// If it was the first time then such a path must be in the
					// topk
					topk.add(best);

					// increment k
					k++;
					// remove from those to be looked
					ranked_paths.remove(best);

					// avoid to present the same pattern multiple times (should
					// be redundant since each explanation is unique)
					temp_check.put(pattern_as_string, pattern_as_string);

				} else if (temp_check.containsKey(pattern_as_string)) {

					ranked_paths.remove(best);
				}

				if (k == K || ranked_paths.size() == 0)
					done = true;

			}

		}

		return topk;

	}

	/**
	 * Return the top-k best explanations
	 * 
	 * @param K
	 * @return
	 * @throws Exception
	 */
	/*
	 * public ArrayList<Path> getTopKPaths(int K, boolean remote_pf_itf) throws
	 * Exception {
	 * 
	 * ArrayList<Path> result = new ArrayList<Path>();
	 * 
	 * if (paths.size() == 0) paths = getPathsAsModels();
	 * 
	 * if (K > paths.size()) {
	 * 
	 * System.out.println("There are " + paths.size() +
	 * " explanations. Value of K automatically reduced"); K = paths.size(); }
	 * else {
	 * 
	 * if (ranked_paths.size() > 0) { int index = K - 1; ArrayList<Path> res =
	 * new ArrayList<Path>(); for (int i = 0; i < index; i++) {
	 * 
	 * res.add(ranked_paths.get(i));
	 * 
	 * }
	 * 
	 * return res; } else // build the ranked path arraylist {
	 * 
	 * for (Model path : paths) {
	 * 
	 * if (!remote_pf_itf) {
	 * 
	 * ranked_paths.add(new Path(path, reword .getLOCALPathInformativeness(path,
	 * getBasicPathUnion()), ent_suggestion .getPrototypicalPattern(path,
	 * source_e, target_e)));
	 * 
	 * } } // /end ranked paths
	 * 
	 * // pick the top-k!
	 * 
	 * ArrayList<Path> copy = new ArrayList<Path>();
	 * 
	 * for (Path p : ranked_paths) copy.add(p);
	 * 
	 * result = filterTopKPathInstances(copy, K);
	 * 
	 * }
	 * 
	 * } return result;
	 * 
	 * }
	 */

	/**
	 * Explanation as the union of the Top-k paths
	 * 
	 * @param intial_first
	 * @param initial_second
	 */
	public Vector<Object> getUnionOfTopKPathInstanceExplanation(
			String intial_first, String initial_second, boolean remote_pf_itf,
			boolean write_on_disk, int k) {

		// pos 0 there will be the Model
		// pos 1 there will be the time!

		Vector<Object> res = new Vector<Object>();
		long start = System.currentTimeMillis();

		Model top_k = null;
		try {
			top_k = getUnionTopKPaths(k, remote_pf_itf);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		long time = end - start;

		res.add(top_k);
		res.add(time);

		if (write_on_disk) {
			String fileName_topK = "";

			if (remote_pf_itf)
				fileName_topK = "expl_" + intial_first + "-" + initial_second
						+ "_TOP_REMOTE_" + Constants.TOP_K + "_DIST_"
						+ distance + ".nt";
			else

				fileName_topK = "expl_" + intial_first + "-" + initial_second
						+ "_TOP_LOCAL_" + Constants.TOP_K + "_DIST_" + distance
						+ ".nt";
			try {
				writeModel(fileName_topK, top_k);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return res;

	}

	/**
	 * Returns a Model, which is the union of the top-k paths
	 * 
	 * @param k
	 * @return
	 * @throws Exception
	 */
	public Model getUnionTopKPaths(int k, boolean remote_pf_itf)
			throws Exception {

		ArrayList<Path> topk = getTopKPaths(k, remote_pf_itf);

		Model res = ModelFactory.createDefaultModel();
		for (Path p : topk) {
			res = res.union(p.getModel());
		}
		return res;

	}

	public boolean isReferencePathsComputed() {
		return reference_paths_computed;
	}

	public boolean isReferencePatternsComputed() {
		return reference_patterns_computed;
	}

	/**
	 * Most Informative Path taking a graph (the union of all paths) as
	 * reference
	 * 
	 * @param intial_first
	 * @param initial_second
	 * @param reference_paths_graph
	 */
	public Vector MIP_LOCAL(String intial_first, String initial_second) {

		Vector eMIP_LOC = getLOCALMostInformativePathExplanation(intial_first,
				initial_second, getReferenceGraph(),
				Constants.WRITE_EXPLANATIONS_ON_DISK);

		return eMIP_LOC;
	}

	/**
	 * Most Informative Path taking a REMOTE GRAPH as reference
	 * 
	 * @param intial_first
	 * @param initial_second
	 * @param reference_paths_graph
	 */
	public void MIP_REMOTE(String intial_first, String initial_second) {

		Vector eMIP_REM = getREMOTEMostInformativePathExplantion(intial_first,
				initial_second, Constants.WRITE_EXPLANATIONS_ON_DISK);

		Path MIP = (Path) eMIP_REM.get(0);
		long time = (Long) eMIP_REM.get(1);

		System.out.println("Time_MostInformPathREMOTE=" + time + "ms"
				+ " size_MIP_REMOTE=" + MIP.getModel().size());
		// System.out.println("MIP pattern =" + MIP.getPrototypical_pattern());

	}

	public void setReferencePathsComputed(boolean reference_computed) {
		this.reference_paths_computed = reference_computed;
	}

	public void setReferencePatternsComputed(boolean reference_computed) {
		this.reference_patterns_computed = reference_computed;
	}

	/**
	 * TOP-K Most Informative paths using a graph (i.e. union of all paths) as
	 * reference
	 * 
	 * @param intial_first
	 * @param initial_second
	 */
	public Vector TOPK_PATH_INSTANCES_LOCAL(String intial_first,
			String initial_second, int k) {

		Vector eTOPk_LOC = getUnionOfTopKPathInstanceExplanation(intial_first,
				initial_second, false, Constants.WRITE_EXPLANATIONS_ON_DISK, k);

		Model eTOPK = (Model) eTOPk_LOC.get(0);
		long time = (Long) eTOPk_LOC.get(1);

		// System.out.println("TimeTOP-" + Constants.TOP_K + "_LOCAL=" + time
		// + " size_TOP-" + Constants.TOP_K + "_LOCAL=" + eTOPK.size());

		return eTOPk_LOC;
	}

	/**
	 * TOP-K Most Informative paths using a remote graph as reference
	 * 
	 * @param intial_first
	 * @param initial_second
	 */
	public void TOPK_PATH_INSTANCES_REMOTE(String intial_first,
			String initial_second, int k) {

		Vector eTOPk_REM = getUnionOfTopKPathInstanceExplanation(intial_first,
				initial_second, true, Constants.WRITE_EXPLANATIONS_ON_DISK, k);

		Model eTOPK = (Model) eTOPk_REM.get(0);
		long time = (Long) eTOPk_REM.get(1);

		System.out.println("TimeTOP-" + Constants.TOP_K + "_REMOTE=" + time
				+ " size_TOP-" + Constants.TOP_K + "_REMOTE=" + eTOPK.size());

	}

	/**
	 * GET top-K PATH patterns by replacing source and target entities
	 * 
	 * @param K
	 * @return
	 * @throws Exception
	 */

	public Vector TOPK_PATH_PATTERNS_LOCAL(int K) throws Exception {
		// pos 0 there will be the Model
		// pos 1 there will be the time!
		Vector<Object> res = new Vector<Object>();

		long start = System.currentTimeMillis();

		ArrayList<Pattern> topk = getTopKPathPatterns(K);

		Model merge_models = ModelFactory.createDefaultModel();

		for (Pattern pattern : topk) {
			ArrayList<Model> models = reference_patterns_graph.get(pattern
					.getPrototypical_pattern());

			for (Model temp : models)
				merge_models = merge_models.union(temp);
		}

		long end = System.currentTimeMillis();
		long time = end - start;

		res.add(merge_models);
		res.add(time);

		return res;
	}

	/**
	 * Write a model on the disk
	 * 
	 * @param fileName
	 * @param model
	 * @throws IOException
	 */
	public void writeModel(String fileName, Model model) throws IOException {
		FileWriter out = new FileWriter(Constants.RESULTS_DIRECTORY + fileName);
		try {
			model.write(out, "N-TRIPLE");// RDF/XML
		} finally {
			try {
				out.close();
			} catch (Exception closeException) {
				// ignore
			}
		}

	}

	/**
	 * Convert a model into N-TRIPLE
	 * 
	 * @param model
	 * @return
	 */
	public String convertModelToString(Model model) {
		StringWriter stw = new StringWriter();

		model.write(stw, "N-TRIPLE");

		return stw.toString();

	}

}
