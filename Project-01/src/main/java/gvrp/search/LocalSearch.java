package gvrp.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import gvrp.Customer;
import gvrp.DistanceMatrix;
import gvrp.GammaSet;
import gvrp.Instance;
import gvrp.Route;
import gvrp.Solution;

public class LocalSearch {

	Solution solution;
	Random random = new Random();
	
	/* Attempt to make a move with two customers and 
	 * returns whether it was successful or not */
	int numOfNeighbourhoodLevels = 2;
	
	public LocalSearch(Solution s0, long seed) {
		this.solution = s0;
		random.setSeed(seed);
	}
	
	/**
	 * Finds local minimal solution
	 */
	public int findLocalMinimum() {
		int numOfImprovements = 0;
		Instance instance = solution.getInstance();
		int numOfCustomers = instance.getNumberOfCustomers();
		DistanceMatrix dmatrix = instance.getDistancematrix();
		ArrayList<Customer> customers = instance.getCustomers();
		GammaSet gamma = instance.getGammaSet();
		
		/* Prepare some stuff beforehand */
		ArrayList<Integer> iOrder = new ArrayList<>(numOfCustomers);
		for (int i = 0; i < numOfCustomers; ++i) iOrder.add(i);
		int jSize = Math.min(instance.getGammaK(), instance.getNumberOfSets()-1);
		ArrayList<Integer> jOrder = new ArrayList<>(jSize);
		for (int i = 0; i < jSize; ++i) jOrder.add(i);
		ArrayList<Customer> gammaSubset = null;
		boolean improvedOnce = false;
		int neighboorhoodLevel = 0;
		
		/* Start main loop */
		while (neighboorhoodLevel < numOfNeighbourhoodLevels) {
			
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
						switch (neighboorhoodLevel) {
							case 0:
								improved = ri.intraShift(ciIndex, cjIndex, dmatrix);
								break;
							case 1:
								improved = ri.intraSwap(ciIndex, cjIndex, dmatrix);
								break;
							default:
								/* In case there are less neighbourhoods */
								improved = ri.intraShift(ciIndex, cjIndex, dmatrix);
								break;
						}
					} else {
						/* C[i] and C[j] are in different routes
						 * --> Inter route neighbourhood
						 */
						switch (neighboorhoodLevel) {
							case 0:
								improved = ri.interShift(rj, ciIndex, cjIndex, dmatrix);
								break;
							case 1:
								improved = ri.interShift(rj, ciIndex, cjIndex, dmatrix);
								break;
							default:
								/* In case there are less neighbourhoods */
								improved = ri.interShift(rj, ciIndex, cjIndex, dmatrix);
								break;
						}
						if (improved) {
							/* Route j has changed!
							 */
							rj.findShortestPath(dmatrix);
						}
					}
					if (improved) {
						neighboorhoodLevel = 0; /* Goes back to ground level */
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
				/* Found no improvement in the 
				 * current neighbourhood space */
				++neighboorhoodLevel;
			}
		}
		return numOfImprovements;
	}
			
}
