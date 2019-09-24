package gvrp.analysis;

import java.util.HashMap;
import java.util.Scanner;

import gvrp.Solution;

/**
 * <p>Reads best-known solutions from file formatted as:
 * <p>Each line: [Instance name] [BKS]
 * 
 * @author guidanoli
 *
 */
public class BKS {

	HashMap<String, Double> map = new HashMap<String, Double>();
	
	/**
	 * Parses input from scanner until no more valid tokens are available 
	 * @param sc - scanner
	 */
	public BKS(Scanner sc) {
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
	
	public Double getBKSFraction(Solution solution) {
		String instanceName = solution.getInstance().getName();
		double solutionCost = solution.getCost(); 
		return Math.abs(solutionCost-getBKS(instanceName)) / getBKS(instanceName);
	}
	
}
