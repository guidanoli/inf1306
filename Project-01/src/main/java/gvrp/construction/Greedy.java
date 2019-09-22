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
		int dimension = instance.getNumberOfCustomers();
		
		boolean [] visited = new boolean[dimension];
		Arrays.fill(visited, false);
		visited[0] = true;
		Predicate<Integer> isVisited = (i) -> visited[i];
		
		Integer currentId = dmatrix.getClosestNeighbourId(0, isVisited);
		while (currentId != null) {
			visited[currentId] = true;
			int closest = dmatrix.getClosestNeighbourId(currentId, isVisited);
			Customer currentCustomer = instance.getCustomers().get(closest);
			boolean addedCustomer = currentRoute.addCustomer(currentCustomer);
			if (!addedCustomer) {
				if (!routeIter.hasNext()) break;
				currentRoute = routeIter.next();
				if (!currentRoute.addCustomer(currentCustomer)) break;
			}
			currentId = closest;
		}
		return solution;
	}
	
}
