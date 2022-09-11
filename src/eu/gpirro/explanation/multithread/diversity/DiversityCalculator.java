package eu.gpirro.explanation.multithread.diversity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.gpirro.explanation.structures.PathDistance;
import eu.gpirro.explanation.structures.PathDistanceComparator;
import eu.gpirro.utilities.Constants;

public class DiversityCalculator {

	private Hashtable<String, Double> distances_cache;
	private ThreadPoolExecutor executor;
	private ArrayList<Model> paths;

	public DiversityCalculator(ArrayList<Model> paths) 
	{
		this.paths = paths;
		this.distances_cache = new Hashtable<String, Double>();
		executor = new ThreadPoolExecutor(Constants.NUM_PARALLEL_QUERIES,
				Constants.NUM_PARALLEL_QUERIES, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

	}

	/**
	 * Compute the distances pairwise! Optimize it with multithreading!
	 * 
	 * @param paths
	 * @return
	 */
	private ArrayList<PathDistance> getDistances() 
	{

		ArrayList<PathDistance> distances = new ArrayList<PathDistance>();

		for (int i = 0; i < paths.size(); i++) {
			for (int j = 0; j < paths.size(); j++) {

				Model m1 = paths.get(i);
				Model m2 = paths.get(j);

				PathDistance d = new PathDistance(m1, m2,
						getExplanationDistance(m1, m2));

				// System.out.println("Distance=" + d.getDistance());
				distances.add(d);
				// System.out.println("-----------------");

			}
		}

		return distances;
	}

	/**
	 * MultiThreadVersion to obtain the distance
	 * 
	 * @return
	 */

	private ArrayList<PathDistance> getDistancesMultiThreads() {

		ArrayList<PathDistance> results = new ArrayList<PathDistance>();

		for (int i = 0; i < paths.size(); i++) {
			for (int j = 0; j < paths.size(); j++) {
				Model m1 = paths.get(i);
				Model m2 = paths.get(j);

				Future<PathDistance> dist = executor.submit(getPathDiObject(m1,
						m2));

				try {
					results.add(dist.get());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// System.out.println(e.getMessage());
					//
					// e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		executor.shutdown();

		return results;

	}

	private PathDi getPathDiObject(Model m1, Model m2) {
		return new PathDi(m1, m2);
	}

	/**
	 * Given an ArrayList of path distances; select a subset of those that are
	 * in the range [AVG_DIST, AVG_DIST+percent_radius] Return the Jena model
	 * obtained as the merge of the model that satisfy the property above
	 * 
	 * @param distances
	 * @param percent_radius
	 * @return
	 */
	public Model getRadiusExplanation(double percent_radius) 
	{

		ArrayList<PathDistance> distances = getDistancesMultiThreads();
		Model res = ModelFactory.createDefaultModel();
		Stream<PathDistance> stream = distances.stream();
		Stream<PathDistance> maxstream = distances.stream();
		double max_dist = maxstream.max(new PathDistanceComparator()).get()
				.getDistance();
		maxstream.close();

		double min_dist = 0;
		double avg_dist = (max_dist + min_dist) / 2;

		double delta = avg_dist * percent_radius;

		System.out.println("MAX_DIST=" + max_dist + " AVG_DIST=" + avg_dist
				+ " DELTA=" + delta);

		/**
		 * left_radius AVG right_radius
		 */

		Predicate<PathDistance> isLeft = new Predicate<PathDistance>() {

			@Override
			public boolean test(PathDistance t) {
				if (t.getDistance() <= avg_dist
						&& t.getDistance() >= (avg_dist - delta))
					// TODO Auto-generated method stub
					return true;
				return false;
			}

		};

		Predicate<PathDistance> isRight = new Predicate<PathDistance>() {

			@Override
			public boolean test(PathDistance t) {
				if (t.getDistance() >= avg_dist
						&& t.getDistance() <= (avg_dist + delta))
					// TODO Auto-generated method stub
					return true;
				return false;
			}

		};

		
		Predicate<PathDistance> OR = new Predicate<PathDistance>() {
			@Override
			public boolean test(PathDistance t) {
				if (isLeft.test(t) || isRight.test(t))
					return true;
				return false;
			}

		};

		List<PathDistance> left_radius = stream.filter(OR).collect(
				Collectors.toList());

		stream.close();

		// List<PathDistance> right_radius = stream.filter(isRight).collect(
		// Collectors.toList());
		
	//	System.out.println("AVERAGE DISTANCE="+avg_dist);
		//System.out.println("MINIMAL DISTANCE="+min_dist);
		//System.out.println("MAXIMAL DISTANCE="+max_dist);



		for (PathDistance distf : left_radius) 
		{

			res = res.union(distf.getPath1());
			res = res.union(distf.getPath2());

		}

		return res;
	}

	/**
	 * Get the Jaccard distance between two Jena models; it uses a cache since
	 * the Jaccard measure is symmetric
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private double getExplanationDistance(Model first, Model second) {
		String model_pair_dir = first.hashCode() + "" + second.hashCode() + "";
		String model_pair_inv = second.hashCode() + "" + first.hashCode() + "";

		if (model_pair_dir.equalsIgnoreCase(model_pair_inv)) {
			return 0.0;
		}

		double res = -1.0;

		if (distances_cache.get(model_pair_dir) == null
				&& distances_cache.get(model_pair_inv) == null) {

			double intersection = first.intersection(second).size();
			double union = first.union(second).size();

			res = 1 - (intersection / union);
			distances_cache.put(model_pair_dir, res);
			distances_cache.put(model_pair_inv, res);

			// System.out.println("Inserting " + res);

		} else {

			if (distances_cache.get(model_pair_dir) != null)
				res = distances_cache.get(model_pair_dir);
			else
				res = distances_cache.get(model_pair_inv);

			// System.out.println("Retreiveing " + res);

		}

		return res;
	}

	// /

	public class PathDi implements Callable<PathDistance> {
		private Model m1;
		private Model m2;

		public PathDi(Model m1, Model m2) {
			this.m1 = m1;
			this.m2 = m2;
		}

		@Override
		public PathDistance call() throws Exception {
			return new PathDistance(m1, m2, getExplanationDistance(m1, m2));

			// return new HTTPResponse(url.openStream());

		}

	}

}
