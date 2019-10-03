package gvrp.search;

import java.util.Random;
import java.util.function.BiPredicate;

import gvrp.DistanceMatrix;
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
	 * Finds local minimum until stopping criterion is met
	 * @param stoppingCriterion - stopping criterion that takes in
	 * the current solution and iteration index and outputs if should keep on searching for local minimum
	 * @return number of improvements
	 */
	public int findLocalMinimum(BiPredicate<Solution, Integer> stoppingCriterion) {
		int numOfRoutes = solution.size();
		int i = 0;
		int numOfImprovements = 1;
		DistanceMatrix dmatrix = solution.getInstance().getDistancematrix();
		/* First, find the shortest path in each route */
		for (Route r : solution)
			r.findShortestPath(solution.getInstance().getDistancematrix());
		while (stoppingCriterion.test(solution, i)) {
			Route r = solution.get(random.nextInt(numOfRoutes));
			boolean improved = false;
			if (i % 2 == 0 && numOfRoutes > 1) {
				Route rr = solution.get(random.nextInt(numOfRoutes));
				while (rr != r) {
					rr = solution.get(random.nextInt(numOfRoutes));
				}
				improved = r.shiftInterSmart(rr, random.nextInt(r.size()), random.nextInt(rr.size()), dmatrix);
				if (improved) {
					r.findShortestPath(solution.getInstance().getDistancematrix());
					rr.findShortestPath(solution.getInstance().getDistancematrix());
				}
			} else {
				improved= r.shiftSmart(random.nextInt(r.size()), random.nextInt(r.size()), dmatrix);
				if (improved) {
					r.findShortestPath(solution.getInstance().getDistancematrix());
				}
			}
			if (improved) {
				++numOfImprovements;
			}
			++i;
		}
		return numOfImprovements;
	}
			
}
