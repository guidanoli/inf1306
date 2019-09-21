package gvrp.construction;

import java.util.HashMap;
import java.util.Map;

import gvrp.Instance;
import gvrp.Solution;

public class SolutionFactory {

	private static Map<String, ConstructiveMetaheuristic> metaheuristics;
	
	static {
		metaheuristics = new HashMap<>();
		metaheuristics.put("greedy", new Greedy());
	}
	
	public static Solution construct(Instance instance, String algorithm) {
		ConstructiveMetaheuristic cmh = (ConstructiveMetaheuristic) metaheuristics.get(algorithm);
		if (cmh == null) return null;
		return cmh.construct(instance);
	}
	
}
