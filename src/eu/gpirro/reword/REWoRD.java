package eu.gpirro.reword;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.xml.ws.Endpoint;

import eu.gpirro.utilities.Constants;

public class REWoRD {

	private String source_entity;
	private String target_entity;

	private double total_in_pred_count;
	private double total_out_pred_count;

	private Hashtable<String, Double> pf_source_IN;
	private Hashtable<String, Double> pf_source_OUT;

	private Hashtable<String, Double> pf_target_IN;
	private Hashtable<String, Double> pf_target_OUT;

	private Hashtable<String, Double> itf_values;

	// %
	ArrayList<String> features_source_IN;
	ArrayList<String> features_source_OUT;
	ArrayList<String> features_target_IN;

	ArrayList<String> features_target_OUT;

	private QueryExecutor query_executor;
	
	private String endpointAddress;

	public REWoRD(String source_entity, String target_entity, String endpointAddress) {
		this.source_entity = source_entity;
		this.target_entity = target_entity;

		pf_source_IN = new Hashtable<String, Double>();
		pf_source_OUT = new Hashtable<String, Double>();
		total_in_pred_count = -1;

		pf_target_IN = new Hashtable<String, Double>();
		pf_target_OUT = new Hashtable<String, Double>();
		total_out_pred_count = -1;

		// %
		features_source_IN = new ArrayList<String>();
		features_source_OUT = new ArrayList<String>();
		features_target_IN = new ArrayList<String>();
		features_target_OUT = new ArrayList<String>();

		itf_values = new Hashtable<String, Double>();
		
		this.endpointAddress=endpointAddress;

		query_executor = new QueryExecutor(endpointAddress);
	}

	public REWoRD(String endpointAddress) {
		this.endpointAddress=endpointAddress;
		// TODO Auto-generated constructor stub
		query_executor = new QueryExecutor(endpointAddress);

	}

	public QueryExecutor getQueryExecutor() {
		return query_executor;
	}

	public double getTotal_in_pred_count() {
		return total_in_pred_count;
	}

	public void setTotal_in_pred_count(double total_in_pred_count) {
		this.total_in_pred_count = total_in_pred_count;
	}

	public double getTotal_out_pred_count() {
		return total_out_pred_count;
	}

	public void setTotal_out_pred_count(double total_out_pred_count) {
		this.total_out_pred_count = total_out_pred_count;
	}

	/**
	 * Collects the features of the entities
	 */
	public void initializeEntitiesFeatures() {

		// 4 threads AD HOC FIXED NUMBER
		features_source_OUT = query_executor.getFeatures(source_entity,
				Constants.DIRECTION_OUT);

		features_source_IN = query_executor.getFeatures(source_entity,
				Constants.DIRECTION_IN);

		features_target_OUT = query_executor.getFeatures(target_entity,
				Constants.DIRECTION_OUT);

		features_target_IN = query_executor.getFeatures(target_entity,
				Constants.DIRECTION_IN);

	}

	/**
	 * Initialize the ITF; (for both source and target) This could be stored on
	 * the disk for caching; It is independent from the pair considered!!!! A
	 * graph could be useful of the various predicates in DBpedia for instance
	 * 
	 * @throws Exception
	 */
	public void initializeITF() throws Exception {

		// SOME VALUES maybe cached!!!!
		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_source_IN) {
			if (!itf_values.containsKey(pred))
				itf_values.put(pred, getInverseRemoteTripleFrequency(pred));
		}

		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_source_OUT) {

			if (!itf_values.containsKey(pred))
				itf_values.put(pred, getInverseRemoteTripleFrequency(pred));

		}

		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_target_IN) {

			if (!itf_values.containsKey(pred))
				itf_values.put(pred, getInverseRemoteTripleFrequency(pred));

		}

		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_target_OUT) {

			if (!itf_values.containsKey(pred))
				itf_values.put(pred, getInverseRemoteTripleFrequency(pred));

		}

	}

	public void initializePF() throws Exception {

		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_source_IN) {
			pf_source_IN.put(
					pred,
					getPredicateFrequency(source_entity, pred,
							Constants.DIRECTION_IN));

		}

		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_source_OUT) {
			pf_source_OUT.put(
					pred,
					getPredicateFrequency(source_entity, pred,
							Constants.DIRECTION_OUT));

		}

		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_target_IN) {
			pf_target_IN.put(
					pred,
					getPredicateFrequency(target_entity, pred,
							Constants.DIRECTION_IN));

		}

		// HERE MULTIPLE THREADS ARE NEEDED!!!!!!
		for (String pred : features_target_OUT) {
			pf_target_OUT.put(
					pred,
					getPredicateFrequency(target_entity, pred,
							Constants.DIRECTION_OUT));

		}

	}

	/**
	 * Get the Predicate Frequency
	 * 
	 * @param entity
	 * @param RDF_predicate
	 * @return
	 * @throws Exception
	 */
	public double getPredicateFrequency(String entity, String predicate,
			String direction) throws Exception {
		double f_i_p = 0.0;
		double f_i = 0.0;
		double pf = 0.0;

		if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			f_i_p = query_executor.getEntitySpecificPredicateCount(entity,
					predicate, Constants.DIRECTION_IN);

			if (total_in_pred_count == -1) {
				f_i = query_executor.getEntityTotalPredicateCount(entity,
						Constants.DIRECTION_IN);
				total_in_pred_count = f_i;
			} else {
				f_i = total_in_pred_count;
			}

		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {

			f_i_p = query_executor.getEntitySpecificPredicateCount(entity,
					predicate, Constants.DIRECTION_OUT);

			if (total_out_pred_count == -1) {
				f_i = query_executor.getEntityTotalPredicateCount(entity,
						Constants.DIRECTION_OUT);
				total_out_pred_count = f_i;

			} else {
				f_i = total_out_pred_count;

			}

		}

		pf = f_i_p / f_i;

		// System.out.println("PF value for pred=" + predicate + " pf=" + pf);

		return pf;
	}

	public double getPathPredicateFrequency(String entity, String predicate,
			String direction) throws Exception {
		double f_i_p = 0.0;
		double f_i = 0.0;
		double pf = 0.0;

		if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {
			f_i_p = query_executor.getEntitySpecificPredicateCount(entity,
					predicate, Constants.DIRECTION_IN);

			f_i = query_executor.getEntityTotalPredicateCount(entity,
					Constants.DIRECTION_IN);

		} else if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {

			f_i_p = query_executor.getEntitySpecificPredicateCount(entity,
					predicate, Constants.DIRECTION_OUT);

			f_i = query_executor.getEntityTotalPredicateCount(entity,
					Constants.DIRECTION_OUT);

		}

		pf = f_i_p / f_i;

		return pf;
	}

	/**
	 * REUTRN THE INVERSE TRIPLE FREQUENCY given a Predicate
	 * 
	 * @param predicate
	 * @return
	 * @throws Exception
	 */
	public double getInverseRemoteTripleFrequency(String predicate)
			throws Exception {
		double itf = 0.0;
		double denom = -1;

		denom = query_executor.getGlobalPredicateCount(predicate);

		if (denom != 0)
			itf = Math.log(Constants.TOTAL_NUMER_OF_TRIPLES / denom);
		else
			itf = 0;

		return itf;

	}

	/**
	 * construct weighted vectors with PFITF values
	 * 
	 * @param entity
	 * @param direction
	 * @return
	 */
	public double[] getPFITFVector(String entity, String direction) {

		double pfitf[] = new double[itf_values.values().size()];
		double itf[] = new double[itf_values.values().size()];

		Set<String> predicates = itf_values.keySet();

		int i = 0;
		for (String pred : predicates) {
			itf[i] = itf_values.get(pred);
			i++;
		}

		if (entity.equalsIgnoreCase(source_entity)) {
			if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {

				int j = 0;

				for (String pred : predicates) {
					if (pf_source_IN.get(pred) != null) {
						pfitf[j] = pf_source_IN.get(pred) * itf[j];
						j++;
					} else {
						pfitf[j] = 0.0;
						j++;
					}

				}
			} else if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
				int j = 0;
				for (String pred : predicates) {
					if (pf_source_OUT.get(pred) != null) {
						pfitf[j] = pf_source_OUT.get(pred) * itf[j];
						j++;
					} else {
						pfitf[j] = 0.0;
						j++;
					}

				}

			}
		}

		else if (entity.equalsIgnoreCase(target_entity)) {
			if (direction.equalsIgnoreCase(Constants.DIRECTION_IN)) {

				int j = 0;
				for (String pred : predicates) {
					if (pf_target_IN.get(pred) != null) {
						pfitf[j] = pf_target_IN.get(pred) * itf[j];
						j++;
					} else {

						pfitf[j] = 0.0;
						j++;
					}

				}
			} else if (direction.equalsIgnoreCase(Constants.DIRECTION_OUT)) {
				int j = 0;
				for (String pred : predicates) {
					if (pf_target_OUT.get(pred) != null) {
						pfitf[j] = pf_target_OUT.get(pred) * itf[j];
						j++;
					} else {
						pfitf[j] = 0.0;
						j++;
					}

				}

			}
		}

		return pfitf;

	}

	/**
	 * Compute the cosine
	 * 
	 * @param vectorA
	 * @param vectorB
	 * @return
	 */
	public double cosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	public int getTotalNumberQueries() {
		return query_executor.getTotal_number_queries();
	}

	public String getSource_entity() {
		return source_entity;
	}

	public void setSource_entity(String source_entity) {
		this.source_entity = source_entity;
	}

	public String getTarget_entity() {
		return target_entity;
	}

	public void setTarget_entity(String target_entity) {
		this.target_entity = target_entity;
	}

	public Hashtable<String, Double> getPf_source_IN() {
		return pf_source_IN;
	}

	public void setPf_source_IN(Hashtable<String, Double> pf_source_IN) {
		this.pf_source_IN = pf_source_IN;
	}

	public Hashtable<String, Double> getPf_source_OUT() {
		return pf_source_OUT;
	}

	public void setPf_source_OUT(Hashtable<String, Double> pf_source_OUT) {
		this.pf_source_OUT = pf_source_OUT;
	}

	public Hashtable<String, Double> getPf_target_IN() {
		return pf_target_IN;
	}

	public void setPf_target_IN(Hashtable<String, Double> pf_target_IN) {
		this.pf_target_IN = pf_target_IN;
	}

	public Hashtable<String, Double> getPf_target_OUT() {
		return pf_target_OUT;
	}

	public void setPf_target_OUT(Hashtable<String, Double> pf_target_OUT) {
		this.pf_target_OUT = pf_target_OUT;
	}

	public Hashtable<String, Double> getItf_values() {
		return itf_values;
	}

	public void setItf_values(Hashtable<String, Double> itf_values) {
		this.itf_values = itf_values;
	}

	public ArrayList<String> getFeatures_source_IN() {
		return features_source_IN;
	}

	public void setFeatures_source_IN(ArrayList<String> features_source_IN) {
		this.features_source_IN = features_source_IN;
	}

	public ArrayList<String> getFeatures_source_OUT() {
		return features_source_OUT;
	}

	public void setFeatures_source_OUT(ArrayList<String> features_source_OUT) {
		this.features_source_OUT = features_source_OUT;
	}

	public ArrayList<String> getFeatures_target_IN() {
		return features_target_IN;
	}

	public void setFeatures_target_IN(ArrayList<String> features_target_IN) {
		this.features_target_IN = features_target_IN;
	}

	public ArrayList<String> getFeatures_target_OUT() {
		return features_target_OUT;
	}

	public void setFeatures_target_OUT(ArrayList<String> features_target_OUT) {
		this.features_target_OUT = features_target_OUT;
	}

	/**
	 * Read from a file the pairs
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Vector<String> readPairs(String file) throws IOException {
		Vector<String> pairs = new Vector<String>();

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				pairs.add(line);

				// sb.append(line);
				// sb.append(System.lineSeparator());
				line = br.readLine();
			}
			// String everything = sb.toString();
		} finally {
			br.close();
		}

		return pairs;

	}

	/**
	 * MAIN TEST
	 * 
	 * @throws Exception
	 */

	public static void main(String argv[]) throws Exception

	{
		String source_entity = ":Fritz_Lang";
		String target_entity = ":Thea_von_Harbou";
		boolean explain = false;

		String filename = "pairs.dat";
		
		String endpointAddress="";

		Vector<String> pairs = readPairs(filename);

		for (String pair : pairs) {
			System.out.println(pair);

			String source_e = pair.substring(0, pair.indexOf(","));
			String target_e = pair.substring(pair.indexOf(",") + 1,
					pair.length());

			System.out.println(source_e);
			System.out.println(target_e);

			// REWoRD rw = new REWoRD(source_entity, target_entity);
			REWoRD rw = new REWoRD(source_e, target_e,endpointAddress);

			rw.initializeEntitiesFeatures();

			System.out.println("REWORD computation..................");

			rw.initializePF();

			 rw.initializeITF();

			double reword_out = 0.0;
			double reword_in = 0.0;

			if (explain) {

				System.out.println("PF source IN values");
				for (String feat : rw.getPf_source_IN().keySet()) {

					System.out.println(feat + " PF_SOURCE_IN="
							+ rw.getPf_source_IN().get(feat));
				}
				System.out.println();
				System.out.println("PF source OUT values");
				for (String feat : rw.getPf_source_OUT().keySet()) {

					System.out.println(feat + " PF_SOURCE_OUT="
							+ rw.getPf_source_OUT().get(feat));
				}
				System.out.println();

				System.out.println("PF TARGET IN values");
				for (String feat : rw.getPf_target_IN().keySet()) {

					System.out.println(feat + " PF_TARGET_IN="
							+ rw.getPf_target_IN().get(feat));
				}
				System.out.println();
				System.out.println("PF TARGET OUT values");
				for (String feat : rw.getPf_target_OUT().keySet()) {

					System.out.println(feat + " PF_TARGET_OUT="
							+ rw.getPf_target_OUT().get(feat));
				}
				System.out.println();

				System.out.println();
				System.out.println("ITF values");
				int k = 0;
				for (String feat : rw.getItf_values().keySet()) {

					System.out.println("i=" + k + " " + feat + " ITF="
							+ rw.getItf_values().get(feat));
					k++;
				}
				System.out.println();

			}

			double in_s[] = rw.getPFITFVector(source_entity,
					Constants.DIRECTION_IN);
			double in_t[] = rw.getPFITFVector(target_entity,
					Constants.DIRECTION_IN);
			double out_s[] = rw.getPFITFVector(source_entity,
					Constants.DIRECTION_OUT);
			double out_t[] = rw.getPFITFVector(target_entity,
					Constants.DIRECTION_OUT);
			reword_in = rw.cosineSimilarity(in_s, in_t);
			reword_out = rw.cosineSimilarity(out_s, out_t);

			double reword_avg = (reword_in + reword_out) / 2;

			System.out.println();
			System.out.println(" Pair" + source_e + "," + target_e
					+ " REWORD IN=" + reword_in + "   REWORD OUT=" + reword_out
					+ " REWORD AVG=" + reword_avg + " #Queries="
					+ rw.getTotalNumberQueries());

			if (explain) {

				for (int i = 0; i < in_s.length; i++)
					System.out.print("(i=" + i + "," + in_s[i] + ")\t");

				System.out.println();
				for (int i = 0; i < in_t.length; i++)
					System.out.print("(i=" + i + "," + in_t[i] + ")\t");

				System.out.println();
				System.out.println("Shared dimensions for INCOMING");
				for (int i = 0; i < in_s.length; i++) {
					if (in_s[i] != 0 && in_t[i] != 0)
						System.out.print("(i=" + i + "," + in_s[i] + ")--"
								+ "(i=" + i + "," + in_t[i] + ")\t");
				}

				System.out.println();

				// %

				System.out.println("PFITF source out=");
				for (int i = 0; i < out_s.length; i++)
					System.out.print("[" + i + "] " + out_s[i]);

				System.out.println("PFITF target out=");
				for (int i = 0; i < out_t.length; i++)
					System.out.print("[" + i + "] " + out_t[i]);

				System.out.println();
				System.out.println("Shared dimensions for OUTGOING");
				for (int i = 0; i < out_s.length; i++) {
					if (out_s[i] != 0 && out_t[i] != 0)
						System.out.print("(i=" + i + "," + out_s[i] + ")--"
								+ "(i=" + i + "," + out_t[i] + ")\t");
				}

				System.out.println();

			}
		}

	}

}
