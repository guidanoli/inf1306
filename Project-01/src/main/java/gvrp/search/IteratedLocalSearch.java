package gvrp.search;

import java.util.function.Predicate;

import gvrp.Solution;

public class IteratedLocalSearch {

	private long seed;
	public IteratedLocalSearch(long seed) {
		this.seed = seed;
	}
	
	public Solution explore(Solution bestSolution, Predicate<Solution> stoppingCriterion) {
		
		/* S0 ← InitialSolution;
		 * S ← LocalSearch(S0);
		 * repeat:
		 * 		S’ ← Perturbation(S, history);
		 * 		S’’ ← LocalSearch(S’);
		 * 		S ← AcceptanceCriterion(S, S’’, history);
		 * until stopping criterion is not satisfied anymore
		 * return S;
		 * 
		 * From the following paper:
		 * Mestria M.; Ochi L. S.; Martins L. S.;
		 * "Iterated Local Search para o problema do Caixeiro Viajante com Grupamentos"
		 */
		
		LocalSearch ls = new LocalSearch(seed);
		ls.findLocalMinimum(bestSolution); /* Local Search */
		Solution currentSolution = new Solution(bestSolution);
		int solutionSize = bestSolution.getInstance().getNumberOfCustomers();
		
		double t = 0.0;
		int improvements = 0;
		
		while (true) {
			ls.perturbSolution(currentSolution, (int)(solutionSize*Math.exp(-t))); /* Perturbation */
			improvements = ls.findLocalMinimum(currentSolution); /* Local Search */
			if (!stoppingCriterion.test(currentSolution)) break; /* Stopping Criterion */
			if (improvements > 0) {
				if (currentSolution.getCost() <= bestSolution.getCost()) {
					/* If an improvement is found, override best solution */
					bestSolution = currentSolution;
				} else {
					/* If no improvement is found, tries again with best solution */
					currentSolution = new Solution(bestSolution);
				}
			}
			++t;
		}
		
		return bestSolution;
	}
	
}
