package gvrp.search;

import java.util.ArrayList;
import java.util.Random;

import gvrp.Route;
import gvrp.Solution;
import gvrp.diff.Move;

public class LocalSearch {

	Solution solution;
	Random random = new Random();
	
	public LocalSearch(Solution s0) {
		this.solution = s0;
	}
	
	public Solution getBestSolution() {
		return solution;
	}
	
	public int findLocalMinimum(int maxNumberOfIterations) {
		Random random = new Random();
		int improvements = 0;
		int initialCost = solution.getCost();
		for (int i = 0; i < maxNumberOfIterations; i++) {
			ArrayList<Move> moves = new ArrayList<Move>(solution.size());
			for (Route route : solution) {
				int routeSize = route.size();
				Move move = route.opt2(random.nextInt(routeSize), random.nextInt(routeSize));
				if (move != null)
					moves.add(move);
			}
			
			if (initialCost <= solution.getCost()) {
				for (Move move : moves)
					move.undo();
			} else {
				improvements++;
			}
		}
		return improvements;
	}
	
}
