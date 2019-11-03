package mssc.construction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import mssc.Cluster;
import mssc.Instance;
import mssc.Point;
import mssc.Solution;

public class RandomSolution implements ConstructiveMetaheuristic {

	public Solution construct(Instance instance, Random rng) {
		Solution solution = new Solution(instance);
		ArrayList<Point> randomOrder = new ArrayList<>(instance.getEntities());
		Collections.shuffle(randomOrder, rng);
		int clusterIndex = 0;
		int numOfClusters = instance.getNumOfClusters();
		for (Point p : randomOrder) {
			solution.get(clusterIndex).add(p);
			clusterIndex = ++clusterIndex % numOfClusters;
		}
		for (Cluster c : solution)
			c.updateCentroid();
		return solution;
	}

}
