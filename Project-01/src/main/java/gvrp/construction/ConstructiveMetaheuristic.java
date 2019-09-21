package gvrp.construction;

import gvrp.Instance;
import gvrp.Solution;

public interface ConstructiveMetaheuristic {

	public Solution construct(Instance instance);
	
}
