package gvrp;

import java.util.ArrayList;
import java.util.HashSet;

@SuppressWarnings("serial")
public class Solution extends ArrayList<Route> {

	private Instance instance;
	
	/**
	 * @return the instance
	 */
	public Instance getInstance() {
		return instance;
	}

	public Solution(Instance instance) {
		this.instance = instance;
		for (int i = 1; i <= instance.getFleet(); i++) {
			add(new Route(i, instance.getDepot()));
		}
	}
	
	public boolean isValid() {
		HashSet<Customer> customersInRoutes = new HashSet<Customer>();
		int customerCount = 0;
		for (Route route : this) {
			int customersInRoute = route.size();
			if (route.getCapacity() > instance.getCapacity()) return false; /* Route capacity surpasses maximum */
			if (customersInRoute == 0) return false; /* Empty route */
			customerCount += customersInRoute;
			customersInRoutes.addAll(route);
			if (customerCount != customersInRoutes.size()) return false; /* Overlapping customer sets */
		}
		return true;
	}
	
	public int cost() {
		int totalCost = 0;
		for (Route route : this) {
			totalCost += route.cost;
		}
		return totalCost;
	}
	
}
