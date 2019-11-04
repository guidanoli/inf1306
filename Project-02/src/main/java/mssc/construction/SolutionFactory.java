package mssc.construction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import mssc.Instance;
import mssc.Solution;

public class SolutionFactory {

	private static Map<String, ConstructiveMetaheuristic> metaheuristics;
	private static Random random = new Random();
	
	static {
		metaheuristics = new HashMap<>();
		metaheuristics.put("random", new RandomSolution());
	}
	
	public static Solution construct(Instance instance, String algorithm) {
		ConstructiveMetaheuristic cmh = (ConstructiveMetaheuristic) metaheuristics.get(algorithm);
		if (cmh == null) return null;
		return cmh.construct(instance, random);
	}
	
	public static void setRandomSeed(long seed) {
		random.setSeed(seed);
	}
	
}
