package gvrp.construction;

import gvrp.Instance;
import gvrp.Solution;

public class Greedy implements ConstructiveMetaheuristic {
	
	public Solution construct(Instance instance) {
		Solution solution = new Solution(instance);
		return solution;
	}
	
}
