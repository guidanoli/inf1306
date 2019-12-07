package mssc.analysis;

import java.util.HashMap;
import java.util.Scanner;

import mssc.Instance;

/**
 * <p>Reads best-known solutions from file formatted as:
 * <p>Each line: [Instance name] [BKS]
 * 
 * @author guidanoli
 *
 */
public class BestKnownSolutions {

	HashMap<String, Double> map = new HashMap<String, Double>();
	
	/**
	 * Parses input from scanner until no more valid tokens are available 
	 * @param sc - scanner
	 */
	public BestKnownSolutions(Scanner sc) {
		while (sc.hasNext()) {
			String instanceName = sc.next();
			if (!sc.hasNextInt()) return; /* end parsing */
			int m = sc.nextInt();
			if (!sc.hasNextDouble()) return; /* end parsing */
			Double bks = sc.nextDouble();
			String key = getKey(instanceName, m);
			map.put(key, bks);
		}
	}
	
	private String getKey(String instanceName, int m) {
		return String.format("%s-%d", instanceName, m);
	}
	
	/**
	 * Obtain percentage of the cost in relation to best known cost
	 * @param instance - instance being analyzed
	 * @param solutionCost - objective function over solution
	 * @return discrepancy in relation to best known solution or null if BKS is not registered
	 */
	public Double getBKSFraction(Instance instance, int m, double solutionCost) {
		String instanceName = instance.getName();
		String key = getKey(instanceName, m);
		Double bks = map.get(key);
		if (bks == null) return null;
		return (solutionCost-bks)/bks;
	}
	
}
