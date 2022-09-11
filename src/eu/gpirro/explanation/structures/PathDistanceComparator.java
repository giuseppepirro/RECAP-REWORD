package eu.gpirro.explanation.structures;

import java.util.Comparator;

public class PathDistanceComparator implements Comparator<PathDistance> {

	@Override
	public int compare(PathDistance a, PathDistance b) {
		if (a.getDistance() < b.getDistance())
			return -1;

		if (a.getDistance() == b.getDistance())

			return 0;

		return 1;
	}

}
