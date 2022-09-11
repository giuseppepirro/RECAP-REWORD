package eu.gpirro.explanation.multithread.informativeness;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.rdf.model.Model;

import eu.gpirro.explanation.multithread.EntitySuggestion;
import eu.gpirro.explanation.multithread.ExplanationBuilder;
import eu.gpirro.explanation.reword.multithread.ReWORDMultiThread;
import eu.gpirro.explanation.structures.Path;
import eu.gpirro.utilities.Constants;

public class PathInformativenessCalculator {
	private ThreadPoolExecutor executor;
	private ArrayList<Model> paths;
	private ReWORDMultiThread reword;
	private EntitySuggestion ent_suggestion;
	private Model ref_graph;
	private String source_entity;
	private String target_entity;

	public PathInformativenessCalculator(String source_entity,
			String target_entity, ArrayList<Model> paths, Model ref_graph,
			boolean remote_itf, String endpoint_address, String named_graph) {
		this.paths = paths;
		this.ref_graph = ref_graph;
		this.source_entity = source_entity;
		this.target_entity = target_entity;
		executor = new ThreadPoolExecutor(Constants.NUM_PARALLEL_QUERIES,
				Constants.NUM_PARALLEL_QUERIES, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		ent_suggestion = new EntitySuggestion();

		reword = new ReWORDMultiThread(source_entity, target_entity,
				remote_itf, endpoint_address, named_graph);

	}

	/**
	 * MultiThreadVersion to obtain the distance
	 * 
	 * @return
	 */

	public ArrayList<Path> getInformativenessMultiThreads() {
		ArrayList<Path> results = new ArrayList<Path>();

		for (int i = 0; i < paths.size(); i++) {
			Model m1 = paths.get(i);
			Future<Path> inf = executor.submit(getPathInfObject(m1));
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

	private PathInf getPathInfObject(Model m1) {
		return new PathInf(m1);
	}

	public class PathInf implements Callable<Path> {
		private Model m1;

		public PathInf(Model m1) {
			this.m1 = m1;
		}

		@Override
		public Path call() throws Exception {
			return new Path(m1, reword.getLOCALPathInformativeness(m1,
					ref_graph), ent_suggestion.getPrototypicalPattern(m1,
					source_entity, target_entity));

		}
	}

}
