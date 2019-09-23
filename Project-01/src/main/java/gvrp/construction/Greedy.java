package gvrp.construction;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

import gvrp.Customer;
import gvrp.DistanceMatrix;
import gvrp.Instance;
import gvrp.Route;
import gvrp.Solution;

public class Greedy implements ConstructiveMetaheuristic {
	
	public Solution construct(Instance instance) {
		Solution solution = new Solution(instance);
		DistanceMatrix dmatrix = instance.getDistancematrix();
		
		Iterator<Route> routeIter = solution.iterator();
		Route currentRoute = routeIter.next();
		
		boolean [] visitedSets = new boolean[instance.getSets().size()+1];
		Arrays.fill(visitedSets, false); /* Indexed by (1,#sets) */
		Predicate<Integer> isVisited = (i) -> i == 0 ? true : visitedSets[instance.getCustomers().get(i).getSet().getId()];
		
		Integer currentId = dmatrix.getClosestNeighbourId(0, isVisited);
		while (currentId != null) {
			Customer currentCustomer = instance.getCustomers().get(currentId);
			visitedSets[currentCustomer.getSet().getId()] = true;
			boolean addedCustomer = currentRoute.addCustomer(currentCustomer);
			if (!addedCustomer) {
				if (!routeIter.hasNext()) break; /* No more routes */
				currentRoute = routeIter.next();
				/* This must never fail since demand < route capacity */
				currentRoute.addCustomer(currentCustomer);
			}
			currentId = dmatrix.getClosestNeighbourId(currentId, isVisited);
		}
		return solution;
	}
	
}
