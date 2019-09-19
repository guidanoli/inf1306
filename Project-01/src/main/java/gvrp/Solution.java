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
//		boolean customerInRoute = new boolean[instance]
		HashSet<Customer> customersInRoutes = new HashSet<Customer>();
		int customerCount = 0;
		for (Route route : this) {
			int routeSize = route.size();
			if (route.getCapacity() > instance.getCapacity()) return false;
			if (routeSize == 0) return false;
			if (route.getCost() == 0) return false;
			customerCount += routeSize;
			customersInRoutes.addAll(route);
			if (customerCount != customersInRoutes.size()) return false;
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
