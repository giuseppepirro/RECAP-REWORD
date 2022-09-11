package eu.gpirro.explanation.multithread.informativeness;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import eu.gpirro.explanation.multithread.EntitySuggestion;
import eu.gpirro.explanation.reword.multithread.ReWORDMultiThread;
import eu.gpirro.explanation.structures.Path;
import eu.gpirro.explanation.structures.Pattern;
import eu.gpirro.utilities.Constants;

public class PatternInformativenessCalculator {
	private ThreadPoolExecutor executor;
	private ArrayList<String> patterns;
	private Model ref_graph;

	public PatternInformativenessCalculator(ArrayList<String> patterns,
			Model ref_graph) {
		this.ref_graph = ref_graph;
		executor = new ThreadPoolExecutor(Constants.NUM_PARALLEL_QUERIES,
				Constants.NUM_PARALLEL_QUERIES, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		this.patterns = patterns;

	}

	/**
	 * MultiThreadVersion to obtain the informativeness
	 * 
	 * @return
	 */

	public ArrayList<Pattern> getPatternInformativenessMultiThreads() {
		ArrayList<Pattern> results = new ArrayList<Pattern>();
		for (int i = 0; i < patterns.size(); i++) {
			String m1 = patterns.get(i);
			Future<Pattern> inf = executor.submit(getPatternInfObject(m1));
			try {
				results.add(inf.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		executor.shutdown();

		return results;

	}

	private PatternInf getPatternInfObject(String pattern) {
		return new PatternInf(pattern);
	}

	public class PatternInf implements Callable<Pattern> {
		private String pattern;

		public PatternInf(String pattern) {
			this.pattern = pattern;
		}

		@Override
		public Pattern call() throws Exception {
			return new Pattern(pattern, getPatternInformativeness(pattern));

		}
	}

	/**
	 * Execute a SPARQL count query to obtain the informativeness of the pattern
	 * @param query_pattern
	 * @return
	 */
	public double getPatternInformativeness(String query_pattern) {
		double total_num_patterns = patterns.size();
		double informativeness = 0.0;

		Query jena_query = null;
		QueryExecution qexec = null;
		ResultSet results = null;
		jena_query = QueryFactory.create(query_pattern);
		qexec = QueryExecutionFactory.create(jena_query, ref_graph);

		results = qexec.execSelect();
		// get the result
		double denom = Double.parseDouble(results.nextSolution().get("count")
				.asLiteral().getDouble()
				+ "");// get the result from above!
		qexec.close();

		if (denom != 0)
			informativeness = Math.log(total_num_patterns / denom);
		return informativeness;

	}

}
