package eu.gpirro.utilities;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.resultset.JSONOutput;

import eu.gpirro.explanation.structures.PathElement;
import eu.gpirro.explanation.structures.PathResult;
import eu.gpirro.reword.REWoRD;

public class SchemaPathFinder {

	private Model schema_model;
	private InfModel rdfs_model;

	private QueryUNIONGenerator query_gen;

	private String endpointAddress;

	public SchemaPathFinder(String endpointAddress) {

		query_gen = new QueryUNIONGenerator();
		this.endpointAddress = endpointAddress;

	}

	/**
	 * Read the schema from the file
	 * 
	 * @param filename
	 */
	public void readModel(String filename) {
		schema_model = ModelFactory.createDefaultModel();
		schema_model.read(filename);

	}

	/*
	 * Perform reasoning
	 */
	public void readRDFSModel(String filename) {

		schema_model = ModelFactory.createDefaultModel();

		schema_model.read(filename);

		rdfs_model = ModelFactory.createRDFSModel(schema_model);

	}

	/**
	 * Given two entities and two classes compute a special path of the types
	 * plus source and target entities
	 * 
	 * @param source_entity
	 * @param source_class
	 * @param target_entity
	 * @param target_class
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws ParseException
	 * @throws JSONException
	 */
	public ArrayList<PathResult> getSchemaPaths(String source_class,
			String target_class, int distance)
			throws UnsupportedEncodingException, ParseException, JSONException

	{

		ArrayList<PathResult> output = new ArrayList<PathResult>();

		// use method findPaths
		String query = query_gen.getUNIONQuery(source_class, target_class,
				distance);

		System.out.println(query);

		QueryExecution qe = QueryExecutionFactory.create(query, schema_model);

		ResultSet result_q = qe.execSelect();

		ByteArrayOutputStream sos = new ByteArrayOutputStream();

		JSONOutput jOut = new JSONOutput();
		jOut.format(sos, result_q);

		// ResultSetFormatter.outputAsJSON(result_q);

		// System.out.println(sos.toString());

		String output_results = sos.toString();

		//
		JSONObject jTotal = new JSONObject(output_results);
		JSONArray listVar = jTotal.getJSONObject("head").getJSONArray("vars");
		JSONArray listResults = jTotal.getJSONObject("results").getJSONArray(
				"bindings");

		Variable[] vars = new Variable[listVar.length()];
		for (int i = 0; i < listVar.length(); i++) {
			vars[i] = new Variable(listVar.getString(i));
			;
		}

		// Arrays.sort(vars);

		/**
		 * This prints the path graphically
		 * 
		 * */
		PathResult[] result_out = new PathResult[listResults.length()];

		for (int i = 0; i < listResults.length(); i++) {

			JSONObject rowResult = (JSONObject) listResults.get(i);
			PathElement[] cols = new PathElement[vars.length + 2];
			boolean[] isReversedDirs = new boolean[(vars.length + 1) / 2];
			int idxRD = 0;
			{
				String fullURL0 = source_class.substring(1,
						source_class.length() - 1);

				String fullURL1 = target_class.substring(1,
						target_class.length() - 1);

				cols[0] = new PathElement(fullURL0,
						Util.getObjectShortName(fullURL0));

				cols[vars.length + 1] = new PathElement(fullURL1,
						Util.getObjectShortName(fullURL1));
			}

			for (int j = 0; j < vars.length; j++) {
				String pred = vars[j].getVarName();

				// System.out.print(pred + " ");
				// System.out.print(rowResult.getJSONObject(pred).getString("value")
				// + " ");

				String fullURL = rowResult.getJSONObject(pred).getString(
						"value");
				cols[j + 1] = new PathElement(fullURL,
						Util.getObjectShortName(URLDecoder.decode(fullURL,
								"UTF-8")));

				// cols[j+1] = rowResult.getJSONObject(pred).getString("value");
				if (pred.contains("pred")) {
					isReversedDirs[idxRD++] = pred.contains("To") ? false
							: true;
				}
			}

			result_out[i] = new PathResult(cols, isReversedDirs);

			// System.out.println(result[i].toString());
		}

		//

		for (int j = 0; j < result_out.length; j++)

			output.add(result_out[j]);

		// schema_model
		return output;
	}

	/**
	 * Converts paths into models.
	 * 
	 * @param res
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Model> getPathsAsModels(ArrayList<PathResult> res)
			throws Exception {

		ArrayList<Model> paths = new ArrayList<Model>();

		for (PathResult r : res)
			paths.add(r.toRDFModel());

		return paths;
	}

	public static void main(String argv[]) {

		String source_e = "Person";
		String target_e = "Person";

		boolean rdfs = true;

		String schema_file = "dbpedia_2014.owl";

		String endpointAddres = "";
		int K = 3;

		System.out.println("Source Entity=" + source_e + "\t"
				+ "Target Entity=" + target_e);

		SchemaPathFinder sch = new SchemaPathFinder(endpointAddres);

		if (rdfs)
			sch.readRDFSModel(schema_file);
		else
			sch.readModel(schema_file);

		// ****

		// ** RUN A SPARQL query to find the paths
		// /

		// String query=" SELECT distinct * WHERE { "
		// +
		// "<http://dbpedia.org/ontology/director> <http://www.w3.org/2000/01/rdf-schema#domain> ?range ."
		// + "}";

		String source = "<http://dbpedia.org/ontology/Person> ";
		String target = "<http://dbpedia.org/ontology/Person> ";

		/*
		 * String query= "SELECT distinct * WHERE { " +
		 * "?p1 <http://www.w3.org/2000/01/rdf-schema#range> "+target+" . " +
		 * "?p1 <http://www.w3.org/2000/01/rdf-schema#domain> "+source+" ." +
		 * "}";
		 */

		// REASONING

		// DISTANCE 1
		/*
		 * String query = "SELECT distinct * WHERE { " +
		 * "?p1 <http://www.w3.org/2000/01/rdf-schema#domain> " + source +
		 * " . ?p1 <http://www.w3.org/2000/01/rdf-schema#range> " + target +
		 * ". }";
		 */

		// DISTANCE 2
		String query = "SELECT distinct * WHERE { "
				+ "?p1 <http://www.w3.org/2000/01/rdf-schema#domain> "
				+ source
				+ " . ?p1 <http://www.w3.org/2000/01/rdf-schema#range> ?c1."
				+ "?p2 <http://www.w3.org/2000/01/rdf-schema#domain> ?c1. ?p2 <http://www.w3.org/2000/01/rdf-schema#range> "
				+ target + ". }";

		Query query_s = QueryFactory.create(query);

		// DISTANCE 3????

		// DISTANCE ....K

		/**
		 * AUTOMATIZE THE CONSTRUCTION OF THE QUERIES
		 */

		// WE MUST COMPUTE THE CLOSURE!!!!!

		QueryExecution qexec = null;
		if (rdfs)
			qexec = QueryExecutionFactory.create(query_s, sch.rdfs_model);
		else
			qexec = QueryExecutionFactory.create(query_s, sch.schema_model);

		ResultSet results = qexec.execSelect();

		int i = 0;
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();

			System.out.println(source + " " + soln.toString() + " " + target);

			i++;

			/*
			 * RDFNode x = soln.get("varName") ; // Get a result variable by
			 * name. Resource r = soln.getResource("VarR") ; // Get a result
			 * variable - must be a resource Literal l = soln.getLiteral("VarL")
			 * ; // Get a result variable - must be a literal
			 */}

		System.out.println(query + i + " RESULTS");

		// GET the patterns; rank them and obtain directly the minimal
		// EXPLANATIONS by transforming them into queries!!!!!!!!!!!
		// each pattern can be materialized to find the isntances!!!

		/*
		 * ArrayList<Model> res = null;
		 * 
		 * try { res = sch.getPathsAsModels(sch .getSchemaPaths(source_c,
		 * target_c, K)); } catch (Exception e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 * 
		 * Model final_m = ModelFactory.createDefaultModel();
		 * 
		 * for (Model m : res) { // final_m=final_m.union(m);
		 * m.write(System.out, "N-TRIPLE");
		 * 
		 * System.out.println(); }
		 * 
		 * final_m.write(System.out, "N-TRIPLE");
		 */

	}

	/**
	 * Returns a path in terms of rdf:type connections
	 * 
	 * @param model_path
	 * @return
	 */
	public Model getPathOfTypes(Model model_path) {

		// recall to change it with the multi-thread version
		REWoRD reword = new REWoRD(endpointAddress);
		Model res = ModelFactory.createDefaultModel();
		StmtIterator it = model_path.listStatements();
		Statement temp_st;

		String sub;
		String obj;
		String pred;

		ArrayList<String> types_sub;
		ArrayList<String> pred_domains;
		ArrayList<String> pred_ranges;

		ArrayList<String> types_obj;

		while (it.hasNext()) {
			temp_st = it.next();
			sub = Util.getObjectFullName(temp_st.getSubject().toString());
			pred = Util.getObjectFullName(temp_st.getPredicate().toString());
			obj = Util.getObjectFullName(temp_st.getObject().toString());

			types_sub = reword.getQueryExecutor().getRDFTypes(sub);
			types_obj = reword.getQueryExecutor().getRDFTypes(obj);
			pred_domains = reword.getQueryExecutor().getPropertyDomains(pred);
			pred_ranges = reword.getQueryExecutor().getPropertyRanges(pred);

			// types_sub.retainAll(pred_domains);
			// types_obj.retainAll(pred_ranges);

			for (String type_s : types_sub) {

				Statement statement = ResourceFactory
						.createStatement(
								res.createResource(Util.getObjectShortName(pred
										.trim())),
								res.createProperty(Util
										.getObjectShortName("<http://www.w3.org/2000/01/rdf-schema#domain>")),
								res.createResource(Util
										.getObjectShortName(type_s.trim())));

				System.out.println("DOM " + statement.toString());

				res.add(statement);

			}
			for (String type_o : types_obj) {
				Statement statement = ResourceFactory
						.createStatement(
								res.createResource(Util.getObjectShortName(pred
										.trim())),
								res.createProperty(Util
										.getObjectShortName("<http://www.w3.org/2000/01/rdf-schema#range>")),
								res.createResource(Util
										.getObjectShortName(type_o.trim())));

				System.out.println("RANGE " + statement.toString());

				res.add(statement);
			}

		}

		return res;

	}
}
