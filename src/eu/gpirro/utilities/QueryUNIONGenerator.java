package eu.gpirro.utilities;

import java.util.ArrayList;

import javax.xml.ws.Endpoint;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryUNIONGenerator

{

	public static void main(String argv[]) {

		QueryUNIONGenerator gen = new QueryUNIONGenerator();

		String start_e = "<http://dbpedia.org/resource/Stanley_Kubrick>";
		String end_e = "?vtarget";
		
		String endpointAddress="";
		String namedGraph="";

		int K = 2;

		/**
		 * INVESTIGARE UN SOTTOINSIEME DEI PATH e scegliere un sottoinsieme
		 * delle queries nelle combinazioni per stabilire quali davvero servono!
		 */

		
		System.out.println(gen.getUNIONQuery(start_e, end_e, K));
		
		//gen.getPathBetweenEntitiesOneAtATime(start_e, end_e, K,endpointAddress,namedGraph);

		// gen.getRawExplanation(start_e, end_e, K);

		// System.out.println(query);
		// System.out.println(paths.returnPathCountQuery());

		/*
		 * Hashtable<String, String> bindings=new Hashtable<String, String>();
		 * bindings.put("?p1", "http://<predicate1>"); bindings.put("?p1",
		 * "http://<predicate2>");
		 * 
		 * // ? is a reserved character in REG_EXPR!!!!! System.out.println();
		 * String query_inst=paths.generatePathInstance(query,
		 * bindings,start_e,end_e);
		 * System.out.println("rewritten instance "+query_inst);
		 */

	}

	/**
	 * Return bindings of the representing a path
	 * 
	 * @param source
	 * @param target
	 * @param distance
	 * @return
	 */
	public void getPathBetweenEntitiesOneAtATime(String source, String target,
			int distance, String endpointAddress,String namedGraph) {
		ResultSet results = null;

		SPARQLPathRetrievalTemplate paths = new SPARQLPathRetrievalTemplate(
				source, target, distance);

		ArrayList<ArrayList<TriplePattern>> queries = paths
				.getAllCombinationsBGP();

		int index = 0;
		for (ArrayList<TriplePattern> pt : queries) {

			System.out.println("Executing query " + index++);

			String query = paths.returnSinglePathPatternQuery(pt);

			StringBuilder queryBuilder = new StringBuilder(
					"PREFIX : <http://dbpedia.org/resource/> ");

			queryBuilder.append(query);

			System.out.println(queryBuilder.toString());
			System.out.println();

			executeSELECTQuery(queryBuilder.toString(),
					endpointAddress, namedGraph);

		}

		// return executeSELECTQuery(queryBuilder.toString(), ENDPOINT_ADDRESS);

	}
	/**
	 * Return bindings of the varaibles representing the path
	 * 
	 * @param source
	 * @param target
	 * @param distance
	 * @return
	 */
	public ResultSet getALLPathsBetweenEntities(String source, String target,
			int distance, String endpointAddress, String namedGraph) {
		SPARQLPathRetrievalTemplate paths = new SPARQLPathRetrievalTemplate(
				source, target, distance);

		String query = paths.returnUnionOfPathRetrievalQueries();

		StringBuilder queryBuilder = new StringBuilder(
				"PREFIX : <http://dbpedia.org/resource/> ");

		queryBuilder.append(query);

		System.out.println(queryBuilder.toString());
		System.out.println();

		return executeSELECTQuery(queryBuilder.toString(),
				endpointAddress,namedGraph);

	}

	/**
	 * Returns a graph containing all the paths between entitites
	 * 
	 * @param source
	 * @param target
	 * @param distance
	 * @return
	 */

	public Model getRawExplanation(String source, String target, int distance, String endpointAddress, String namedGraph) {

		SPARQLPathRetrievalTemplate paths = new SPARQLPathRetrievalTemplate(
				source, target, distance);

		String query = paths.returnExplanationRetrievalQuery();

		StringBuilder queryBuilder = new StringBuilder(
				"PREFIX : <http://dbpedia.org/resource/> ");

		queryBuilder.append(query);

		return executeCONSTRUCTQuery(queryBuilder.toString(),endpointAddress,namedGraph				);

	}

	/**
	 * Execute the SPARQL query to retrieve the paths at a given distance.
	 * 
	 * @param query
	 * @param endpoint_address
	 * @return
	 */
	public ResultSet executeSELECTQuery(String query, String endpoint_address, String namedGraph) {

		QueryExecution qe = QueryExecutionFactory.sparqlService(
				endpoint_address, query);

		ResultSet result = qe.execSelect();

		while (result.hasNext()) {
			QuerySolution sol = result.nextSolution();

			System.out.println(sol.toString());

		}

		System.out.println();
		qe.close();

		return result;

	}

	/**
	 * Execute the SPARQL query to retrieve the paths at a given distance.
	 * 
	 * @param query
	 * @param endpoint_address
	 * @return
	 */
	public Model executeCONSTRUCTQuery(String query, String endpoint_address, String namedGraph) {

		QueryExecution qe = QueryExecutionFactory.sparqlService(
				endpoint_address, query);

		return qe.execConstruct();

	}

	public String getUNIONQuery(String source, String target, int distance) {
		SPARQLPathRetrievalTemplate paths = new SPARQLPathRetrievalTemplate(
				source, target, distance);

		String query = paths.returnUnionOfPathRetrievalQueries();

		// StringBuilder queryBuilder = new StringBuilder(
		// "PREFIX : <http://dbpedia.org/resource/> ");

		// queryBuilder.append(query);

		// System.out.println(queryBuilder.toString());
		// System.out.println();

		return query;

	}

}
