package eu.gpirro.utilities;

import java.util.ArrayList;
import java.util.Hashtable;

public class SPARQLPathRetrievalTemplate {

	String query;

	ArrayList<ArrayList<TriplePattern>> BGP;

	public SPARQLPathRetrievalTemplate(String source_ent, String target_ent,
			int k) {
		this.BGP = generateCombinations(source_ent, target_ent, k);

	}

	public ArrayList<ArrayList<TriplePattern>> getAllCombinationsBGP() {
		return BGP;
	}

	public void setBGP(ArrayList<ArrayList<TriplePattern>> bGP) {
		BGP = bGP;
	}

	/**
	 * Given concrete path (from the result of a SPARQL query), replace the
	 * variables for PREDICATES with URIs from the path and the endpoints (now
	 * put variables) to see how many time it occurs
	 * 
	 * @TODO
	 * @return
	 */
	public String generatePathInstance(String query,
			Hashtable<String, String> bindings, String start_e, String end_e) {

		// 1) Replace all the bindings for variable ?p_i
		// with actual predicate values
		query = query.replaceAll(start_e, "?start");
		query = query.replaceAll(end_e, "?end");

		for (String s : bindings.keySet()) {

			// the ? symbol belongs to the REG_EXPR syntax!!!!
			// String base_s=s;
			// s="\\"+s;
			System.out.println(s);
			query.replaceAll(s, bindings.get(s));
		}

		// 2) Replace the endpoints (source and target entity) with 2 new
		// variables

		return query;
	}

	/**
	 * Create the BGP with joins given a value for k;
	 */
	public ArrayList<ArrayList<TriplePattern>> generateCombinations(
			String source_ent, String target_ent, int k) {
		/**
		 * The result is a LIST of ArrayList (each represent a set of Triple
		 * Patterns - Each list encodes e combination of the joins; there will
		 * be 2^k combinations)
		 */
		ArrayList<ArrayList<TriplePattern>> result = new ArrayList<ArrayList<TriplePattern>>();

		ArrayList<TriplePattern> initial_combination = generateInitialChain(
				source_ent, target_ent, k);

		result.add(initial_combination);

		for (int i = 0; i < Math.pow(2, k); i++) {
			StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));

			for (int j = binary.length(); j < k; j++) {
				binary.insert(0, '0');
			}

			/*
					 * 
					 */
			char combination[] = new char[k];
			combination = binary.toString().toCharArray();
			if (i != 0) {
				ArrayList<TriplePattern> current_com = generateCurrentCombination(
						combination, initial_combination);

				result.add(current_com);

			}

		}
		return result;

	}

	/**
	 * A signe query that represents a PathPattern
	 * 
	 * @param input
	 * @return
	 */

	public String returnSinglePathPatternQuery(ArrayList<TriplePattern> list) {
		query = "SELECT * WHERE {\n " + fromListTPsToString(list) + " \n }";

		return query;
	}

	/**
	 * Return each path separately (in terms of variable bindings)
	 * 
	 * @return
	 */
	public String returnUnionOfPathRetrievalQueries() {
		query = "SELECT * WHERE { \n " + generateBGPChain(BGP) + " }";

		return query;
	}

	/**
	 * Returns a graph containing all the paths between two entities
	 * 
	 * @return
	 */
	public String returnExplanationRetrievalQuery() {
		query = "CONSTRUCT { * } WHERE { \n " + generateBGPChain(BGP) + " }";

		return query;
	}

	/**
	 * Count the number of paths
	 * 
	 * @return
	 */
	public String returnPathCountQuery() {
		query = "SELECT COUNT(*) WHERE { \n " + generateBGPChain(BGP) + " }";

		return query;
	}

	/**
	 * Given a list of lists of patterns generate the query Each list represents
	 * a combination of the join of the variables
	 * 
	 * @param BGP
	 * @return
	 */
	private String generateBGPChain(ArrayList<ArrayList<TriplePattern>> BGP) {
		String res = "";
		/*
		 * The First element is without union
		 */
		int i = 0;
		for (ArrayList<TriplePattern> list : BGP) {
			if (i == 0) {
				res = res + "{ " + fromListTPsToString(list) + "} \n";
				i++;
			} else
				res = res + "UNION { " + fromListTPsToString(list) + "} \n";
		}
		return res;
	}

	/**
	 * Converts a list of TPs into string; this will serve to form the final BGP
	 * 
	 * @param input
	 * @return
	 */
	private String fromListTPsToString(ArrayList<TriplePattern> input) {
		String res = "";

		for (TriplePattern tp : input) {
			res = res + tp.toString() + " ";
		}

		return res;
	}

	// ///////////////////////////////////////////

	/**
	 * Generate the initial chain of TPs; the others will be modified according
	 * to the binary encoding
	 * 
	 * @param k
	 * @return
	 */
	private ArrayList<TriplePattern> generateInitialChain(String source_entity,
			String target_entity, int k) {

		ArrayList<TriplePattern> patterns = new ArrayList<TriplePattern>();

		int var_index = 1;
		String current_join_variable = "";
		String subj;
		String pred;
		String obj;
		TriplePattern initial_pattern = null;
		TriplePattern chain_pattern = null;

		if (k == 1) {

			initial_pattern = new TriplePattern(source_entity,
					"?p" + var_index, target_entity, Constants.DIRECTION_FW);
			patterns.add(initial_pattern);
		} else if (k > 1) {
			current_join_variable = "?o" + var_index;
			initial_pattern = new TriplePattern(source_entity,
					"?p" + var_index, current_join_variable,
					Constants.DIRECTION_FW);
			patterns.add(initial_pattern);

			for (int i = 2; i <= k; i++) {
				subj = "?o" + var_index;
				var_index++;
				pred = "?p" + var_index;
				if (i == k) {
					obj = target_entity;

				} else {
					obj = "?o" + var_index;
				}

				chain_pattern = new TriplePattern(subj, pred, obj,
						Constants.DIRECTION_FW);
				patterns.add(chain_pattern);

			}
		}
		return patterns;
	}

	/**
	 * Generate the current pattern according to its binary encoding
	 * 
	 * @param combination
	 * @param initial_combination
	 * @return
	 */
	private ArrayList<TriplePattern> generateCurrentCombination(
			char[] combination, ArrayList<TriplePattern> initial_combination)

	{
		ArrayList<TriplePattern> result = new ArrayList<TriplePattern>();

		for (int i = 0; i < combination.length; i++) {

			char current_value = combination[i];

			TriplePattern tp = initial_combination.get(i);

			if (current_value == '0') {
				if (tp.getDirection().equalsIgnoreCase(Constants.DIRECTION_FW)) {
					result.add(tp);

				} else {
					result.add(tp.invertTriplePattern());
				}
			} else if (current_value == '1') {
				if (tp.getDirection().equalsIgnoreCase(Constants.DIRECTION_FW)) {
					result.add(tp.invertTriplePattern());

				} else {
					result.add(tp);
				}

			}
		}

		// TODO Auto-generated method stub
		return result;
	}

}
