package gvrp.search;

import java.util.function.Predicate;

import gvrp.Solution;

public class IteratedLocalSearch {

	private long seed;
	public IteratedLocalSearch(long seed) {
		this.seed = seed;
	}
	
	public Solution explore(Solution solution, double pertubation, Predicate<Solution> stoppingCriterion) {
		
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
		int n = solution.getInstance().getNumberOfCustomers();
		int perturbationSize = Math.max((int) (n * pertubation), 1);
		
		ls.findLocalMinimum(solution); /* First Local Search */
		Solution bestSolution = new Solution(solution); /* Best solution */
		int bestCost = bestSolution.getCost();
		int currCost = bestCost;
				
		while (stoppingCriterion.test(solution)) { /* Stopping Criterion */
			ls.perturbSolution(solution, perturbationSize); /* Perturbation */
			ls.findLocalMinimum(solution); /* Local Search */
			currCost = solution.getCost();
			if (bestCost > currCost) {
				bestSolution = new Solution(solution); /* save best solution */
				bestCost = currCost;
			}
		}
		
		return bestSolution;
	}
	
}
