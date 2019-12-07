package mssc.construction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import mssc.Instance;
import mssc.Point;
import mssc.Solution;

public class RandomSolution implements ConstructiveMetaheuristic {

	public Solution construct(Instance instance, int id, Random rng) {
		Solution solution = new Solution(instance, id);
		/* Arrange all entities in a random order */
		ArrayList<Point> randomOrder = new ArrayList<>(instance.getEntities());
		Collections.shuffle(randomOrder, rng);
		ArrayList<Point> clusters = solution.getClusters();
		int clusterIndex = 0;
		int numOfClusters = instance.getNumOfClusters();
		/* Then assign each entity to a random cluster or the closest one */
		for (Point p : randomOrder) {
			if (clusterIndex < numOfClusters) {
				Point cluster = clusters.get(clusterIndex);
				cluster.copyFrom(p);
				solution.put(p, cluster);
				++clusterIndex;
			} else {
				Point closestCluster = null;
				double closestDist = Double.MAX_VALUE;
				for (Point c : clusters) {
					double dist = c.getSumOfSquaresTo(p);
					if (dist < closestDist) {
						closestDist = dist;
						closestCluster = c;
					}
				}
				solution.put(p, closestCluster);
			}
		}
		return solution;
	}

}
