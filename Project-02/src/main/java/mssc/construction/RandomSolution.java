package mssc.construction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import mssc.Instance;
import mssc.Point;
import mssc.Solution;

public class RandomSolution implements ConstructiveMetaheuristic {

	public Solution construct(Instance instance, Random rng) {
		Solution solution = new Solution(instance);
		/* Arrange all entities in a random order */
		ArrayList<Point> randomOrder = new ArrayList<>(instance.getEntities());
		Collections.shuffle(randomOrder, rng);
		ArrayList<Point> clusters = solution.getClusters();
		int clusterIndex = 0;
		int numOfClusters = instance.getNumOfClusters();
		/* Then assign each entity to a random cluster */
		for (Point p : randomOrder) {
			solution.put(p, clusters.get(clusterIndex));
			clusterIndex = ++clusterIndex % numOfClusters;
		}
		/* Then make each cluster coincide with one of its entities */
		HashSet<Point> visitedPoints = new HashSet<Point>();
		for (Point c : clusters) {
			Point p;
			do {
				int rInt = rng.nextInt(instance.getNumOfEntities());
				p = instance.getEntityAt(rInt);
			} while (visitedPoints.contains(p));
			c.copyFrom(p);
			visitedPoints.add(p);
		}
		return solution;
	}

}
