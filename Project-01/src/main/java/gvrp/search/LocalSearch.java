package gvrp.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gvrp.Customer;
import gvrp.DistanceMatrix;
import gvrp.GammaSet;
import gvrp.Route;
import gvrp.Solution;

public class LocalSearch {

	Solution solution;
	Random random = new Random();
	
	public LocalSearch(Solution s0, long seed) {
		this.solution = s0;
		random.setSeed(seed);
	}
	
	public Solution getBestSolution() {
		return solution;
	}

	/**
	 * Finds local minimal solution
	 */
	public int findLocalMinimum() {
		int numOfImprovements = 0;
		int numOfCustomers = solution.getInstance().getNumberOfCustomers();
		DistanceMatrix dmatrix = solution.getInstance().getDistancematrix();
		ArrayList<Customer> customers = solution.getInstance().getCustomers();
		GammaSet gamma = solution.getInstance().getGammaSet();
		
		/* Preprocess some stuff */
		ArrayList<Integer> iOrder = new ArrayList<>(numOfCustomers);
		for (int i = 0; i < numOfCustomers; ++i) iOrder.add(i);
		int jSize = Math.min(solution.getInstance().getGammaK(), solution.getInstance().getNumberOfSets()-1);
		ArrayList<Integer> jOrder = new ArrayList<>(jSize);
		for (int i = 0; i < jSize; ++i) jOrder.add(i);
		ArrayList<Customer> gammaSubset = null;
		boolean improvedOnce = false;
		
		/* Start main loop */
		while (true) {
			
			/* Shuffle i orders so not to leave a bias */
			Collections.shuffle(iOrder, random);
			Collections.shuffle(jOrder, random);
			
			improvedOnce = false;
			for (Integer i : iOrder) {
				Customer ci = customers.get(i);
				if (!ci.isInRoute()) continue;
				/* Through all customers that are in route */
				gammaSubset = gamma.getClosestNeighbours(ci);
				/* Obtain the k nearest customers */
				for (Integer j : jOrder) {
					Customer cj = gammaSubset.get(j);
					if (!cj.isInRoute()) continue;
					/* For each neighbouring customer that is also in a route */
					boolean improved = false;
					Route ri = ci.getRoute(), rj = cj.getRoute();
					Integer ciIndex = ri.indexOf(ci), cjIndex = rj.indexOf(cj);
					/* Check whether they are in the same route or
					 * not and explore the neighbourhood accordingly */
					if (ri == rj) {
						/* C[i] and C[j] are in the same route
						 * --> Intra route neighbourhood
						 */
						improved = ri.shiftSmart(ciIndex, cjIndex, dmatrix);
					} else {
						/* C[i] and C[j] are in different routes
						 * --> Inter route neighbourhood
						 */
						improved = ri.shiftInterSmart(rj, ciIndex, cjIndex, dmatrix);
						if (improved) {
							/* Route j has changed!
							 */
							rj.findShortestPath(dmatrix);
						}
					}
					if (improved) {
						improvedOnce = true; /* Guarantee there was one improvement */
						/* Route i has changed!
						 */
						ri.findShortestPath(dmatrix);
						if (!ci.isInRoute()) break;
						++numOfImprovements;
					}					
				}
			}
			if (!improvedOnce) {
				/* Found no improvement
				 * --> Local minimal */
				break;
			}
		}
		return numOfImprovements;
	}
			
}
