package eu.gpirro.reword;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import eu.gpirro.utilities.Constants;

public class QueryExecutor {

	Hashtable<String, ArrayList<String>> classes_to_types;
	Hashtable<String, ArrayList<String>> predicates_to_domains;
	Hashtable<String, ArrayList<String>> predicates_to_ranges;
	int total_number_queries;
	
	private String endpointAddress;

	public QueryExecutor(String endpointAddress) {
		total_number_queries = 0;
		classes_to_types = new Hashtable<String, ArrayList<String>>();
		predicates_to_domains = new Hashtable<String, ArrayList<String>>();
		predicates_to_ranges = new Hashtable<String, ArrayList<String>>();
		
		this.endpointAddress=endpointAddress;

	}

	/**
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public int getAllPredicatesQuery(String query) throws Exception {

		URLConnection connection = getQueryConnection(query);

		String line;
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject jTotal = new JSONObject(builder.toString());
		JSONArray listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");

		String var_count = listVar.get(0).toString();
		JSONObject res = (JSONObject) listResults.get(0);

		int count = Integer.parseInt(res.getJSONObject(var_count).getString(
				"value"));

		//
		total_number_queries++;

		return count;

	}

	/**
	 * Get IN/OUT predicate count wrt a given entity
	 * 
	 * @param entity
	 * @param predicate
	 * @param direction
	 * @return
	 * @throws Exception
	 */
	public int getEntitySpecificPredicateCount(String entity, String predicate,
			String direction) throws Exception {

		String query = "";
		if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			query = Constants.PREFIXES + " SELECT COUNT(*) WHERE { " + entity
					+ " " + predicate + " ?o }";
		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			query = Constants.PREFIXES + " SELECT COUNT(*) WHERE { ?o "
					+ predicate + " " + entity + " }";
		}

		URLConnection connection = getQueryConnection(query);

		String line;
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject jTotal = new JSONObject(builder.toString());
		JSONArray listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");
		String var_count = listVar.get(0).toString();
		JSONObject res = (JSONObject) listResults.get(0);

		int count = Integer.parseInt(res.getJSONObject(var_count).getString(
				"value"));

		//
		total_number_queries++;

		return count;

	}

	/**
	 * Get IN/OUT predicate count wrt a given entity
	 * 
	 * @param entity
	 * @param predicate
	 * @param direction
	 * @return
	 * @throws Exception
	 */
	public int getEntityTotalPredicateCount(String entity, String direction)
			throws Exception {

		String query = "";
		if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			query = Constants.PREFIXES + " SELECT COUNT(*) WHERE { " + entity
					+ " ?p " + " ?o }";
		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			query = Constants.PREFIXES + " SELECT COUNT(*) WHERE { ?o ?p "
					+ entity + " }";
		}

		URLConnection connection = getQueryConnection(query);

		String line;
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));

		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject jTotal = new JSONObject(builder.toString());
		JSONArray listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");
		String var_count = listVar.get(0).toString();
		JSONObject res = (JSONObject) listResults.get(0);

		int count = Integer.parseInt(res.getJSONObject(var_count).getString(
				"value"));

		//
		total_number_queries++;

		return count;

	}

	/**
	 * Returns the features associated to an entity
	 * 
	 * @param entity
	 * @param direction
	 * @return
	 */
	public ArrayList<String> getFeatures(String entity, String direction) {
		ArrayList<String> features = new ArrayList<String>();

		String query = "";

		if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
			query = Constants.PREFIXES + " SELECT DISTINCT ?p WHERE { "
					+ entity + " ?p " + " ?o }";
		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			query = Constants.PREFIXES + " SELECT DISTINCT ?p WHERE { ?o ?p "
					+ entity + " }";
		}

		System.out.println("Query " + query);
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				endpointAddress, query);
		ResultSet result = qe.execSelect();

		while (result.hasNext()) {
			QuerySolution sol = result.nextSolution();
			features.add("<" + sol.get("?p").toString() + ">");
		}

		qe.close();

		total_number_queries++;
		return features;

	}

	/**
	 * Get IN/OUT predicate count wrt a given entity
	 * 
	 * @param entity
	 * @param predicate
	 * @param direction
	 * @return
	 */
	public int getGlobalPredicateCount(String predicate) throws Exception {

		// System.out.println(predicate);
		String query = Constants.PREFIXES + " SELECT count(*) as ?count WHERE "
				+ "{ " + "?s " + predicate + " ?o " + "}";

		URLConnection connection = getQueryConnection(query);

		String line;
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject jTotal = new JSONObject(builder.toString());
		JSONArray listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");
		String var_count = listVar.get(0).toString();
		JSONObject res = (JSONObject) listResults.get(0);

		int count = Integer.parseInt(res.getJSONObject(var_count).getString(
				"value"));

		//
		total_number_queries++;

		return count;

	}

	/**
	 * Get the rdfs:domain of a property
	 * 
	 * @param rdf_property
	 * @return
	 */
	public ArrayList<String> getPropertyDomains(String rdf_property) {
		ArrayList<String> rdfs_domain = null;

		if (predicates_to_domains.get(rdf_property) != null) {
			return predicates_to_domains.get(rdf_property);
		} else {

			rdfs_domain = new ArrayList<String>();
			String query = Constants.PREFIXES + " SELECT DISTINCT ?o WHERE { "
					+ rdf_property
					+ " <http://www.w3.org/2000/01/rdf-schema#domain> "
					+ " ?o }";

			QueryExecution qe = QueryExecutionFactory.sparqlService(
					endpointAddress, query);
			ResultSet result = qe.execSelect();

			while (result.hasNext()) {
				QuerySolution sol = result.nextSolution();

				rdfs_domain.add(sol.get("?o").toString());

			}

			qe.close();

			predicates_to_domains.put(rdf_property, rdfs_domain);
		}

		return rdfs_domain;

	}

	/**
	 * Get the rdfs:range of a property
	 * 
	 * @param rdf_property
	 * @return
	 */
	public ArrayList<String> getPropertyRanges(String rdf_property) {

		ArrayList<String> rdfs_range = null;

		if (predicates_to_ranges.get(rdf_property) != null) {
			return predicates_to_ranges.get(rdf_property);
		} else {

			rdfs_range = new ArrayList<String>();

			String query = Constants.PREFIXES + " SELECT DISTINCT ?o WHERE { "
					+ rdf_property
					+ " <http://www.w3.org/2000/01/rdf-schema#range> "
					+ " ?o }";

			QueryExecution qe = QueryExecutionFactory.sparqlService(
					endpointAddress, query);
			ResultSet result = qe.execSelect();

			while (result.hasNext()) {
				QuerySolution sol = result.nextSolution();

				rdfs_range.add(sol.get("?o").toString());

			}

			qe.close();
			predicates_to_ranges.put(rdf_property, rdfs_range);

		}

		return rdfs_range;
	}

	/**
	 * Returns a connection for qa givnen query
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public URLConnection getQueryConnection(String query) throws Exception {

		StringBuilder queryBuilder = new StringBuilder(
				"http://dbpedia.org/sparql?default-graph-uri=");
		try {
			queryBuilder
					.append(URLEncoder.encode("http://dbpedia.org", "UTF-8"))
					.append("&query=")
					.append(URLEncoder.encode(query, "UTF-8"))
					.append("%0D%0A&debug=on&timeout=" + Constants.TIMEOUT
							+ "&format=")
					.append(URLEncoder.encode(
							"application/sparql-results+json", "UTF-8"))
					.append("&save=display&fname=");
		} catch (Exception ex) {
		}
		URL url = new URL(queryBuilder.toString());
		URLConnection connection = null;

		Thread.sleep(Constants.CONNECTION_DELAY);

		connection = url.openConnection();

		return connection;
	}

	/**
	 * Given an entity returns its rdf:type
	 * 
	 * @param entity
	 * @return
	 */
	public ArrayList<String> getRDFTypes(String entity) {

		ArrayList<String> rdf_types = null;
		if (classes_to_types.get(entity) != null) {
			return classes_to_types.get(entity);
		} else {

			rdf_types = new ArrayList<String>();

			String query = Constants.PREFIXES + " SELECT DISTINCT ?o WHERE { "
					+ entity
					+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
					+ " ?o }";

			System.out.println(query);

			QueryExecution qe = QueryExecutionFactory.sparqlService(
					endpointAddress, query);
			ResultSet result = qe.execSelect();

			while (result.hasNext()) {
				QuerySolution sol = result.nextSolution();

				rdf_types.add("<" + sol.get("?o").toString() + ">");

			}

			qe.close();
			classes_to_types.put(entity, rdf_types);
		}

		return rdf_types;

	}

	public int getTotal_number_queries() {
		return total_number_queries;
	}

	public void setTotal_number_queries(int total_number_queries) {
		this.total_number_queries = total_number_queries;
	}

}
