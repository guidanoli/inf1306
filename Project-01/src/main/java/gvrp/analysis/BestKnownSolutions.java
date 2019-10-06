package gvrp.analysis;

import java.util.HashMap;
import java.util.Scanner;

import gvrp.Instance;
import gvrp.Solution;

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
			if (!sc.hasNextDouble()) return; /* end parsing */
			Double bks = sc.nextDouble();
			map.put(instanceName, bks);
		}
	}
	
	/**
	 * Obtain best known solution
	 * @param instanceName - instance name
	 * @return bks
	 */
	public Double getBKS(String instanceName) {
		return map.get(instanceName);
	}
	
	/**
	 * Obtain percentage of solution cost in relation to best known one
	 * @param solution - solution being analysed
	 * @return discrepancy of solution in relation to best known solution
	 */
	public Double getBKSFraction(Solution solution) {
		return getBKSFraction(solution.getInstance(), solution.getCost());
	}
	
	/**
	 * Obtain percentage of the cost in relation to best known cost
	 * @param instance - instance being analysed
	 * @param solutionCost - sum of all route costs (Euclidian distance)
	 * @return discrepancy in relation to best known solution
	 */
	public Double getBKSFraction(Instance instance, int solutionCost) {
		String instanceName = instance.getName();
		Double bks = getBKS(instanceName);
		return Math.abs(solutionCost-bks)/bks;
	}
	
}
