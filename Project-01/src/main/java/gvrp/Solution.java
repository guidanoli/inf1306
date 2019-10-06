package gvrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;

@SuppressWarnings("serial")
public class Solution extends ArrayList<Route> {

	Instance instance;
	HashMap<Customer, Route> map;
	
	/**
	 * @return the instance
	 */
	public Instance getInstance() {
		return instance;
	}

	public Solution(Instance instance) {
		this.instance = instance;
		this.map = new HashMap<Customer, Route>(instance.getNumberOfCustomers());
		DistanceMatrix dmatrix = instance.getDistancematrix();
		int maxCap = instance.getCapacity();
		for (int i = 1; i <= instance.getFleet(); i++) {
			add(new Route(i, maxCap, dmatrix, map));
		}
	}
	
	public Solution(Solution anotherSolution) {
		this.instance = anotherSolution.instance;
		this.map = new HashMap<Customer, Route>(instance.getNumberOfCustomers());
		this.map.putAll(anotherSolution.map);
		for (Route route : anotherSolution) {
			add(new Route(route, map));
		}
	}
	
	public boolean isValid(boolean printError) {
		HashSet<Customer> customersInRoutes = new HashSet<Customer>();
		HashSet<CustomerSet> customerSetInRoutes = new HashSet<CustomerSet>(); 
		int customerCount = 0;
		for (Route route : this) {
			int customersInRoute = route.size();
			if (route.getCapacity() > instance.getCapacity()) {
				if (printError) System.out.println("Route capacity surpasses maximum");
				return false;
			}
			if (customersInRoute == 0) {
				if (printError) System.out.println("Empty route");
				return false;
			}
			customerCount += customersInRoute;
			customersInRoutes.addAll(route);
			if (customerCount != customersInRoutes.size()) {
				if (printError) System.out.println("Overlapping customer sets");
				return false;
			}
			for (Customer customer : route) {
				CustomerSet customerSet = customer.getSet();
				if (!customerSetInRoutes.add(customerSet)) {
					if (printError) System.out.println("More than one customer per group in route");
					return false;
				}
			}
		}
		/* For redundancy, check also if all sets have exactly one customer in route */
		for (CustomerSet set : instance.getSets()) {
			int numOfCustomersInRoute = 0;
			for (Customer setCustomer : set) {
				if (isCustomerInRoute(setCustomer))
					numOfCustomersInRoute++;
			}
			if (numOfCustomersInRoute == 0) {
				if (printError) System.out.println("No customer from " + set.toCompactString() + " is in route.");
			} else if(numOfCustomersInRoute > 1) {
				if (printError) System.out.println("More than one customer from " + set.toCompactString() + " is in route.");
			}
		}
		int numOfSets = getInstance().getSets().size();
		if (customerSetInRoutes.size() != numOfSets) {
			if (printError) {
				HashSet<CustomerSet> copyOfSets = new HashSet<>(getInstance().getSets());
				copyOfSets.removeAll(customerSetInRoutes);
				System.out.println("Not all customer sets are in routes (" + copyOfSets.size() + " remaining): " + copyOfSets);
			}
			return false;
		}
		return true;
	}
	
	public Route getCustomerRoute(Customer customer) {
		return map.get(customer);
	}
	
	public boolean isCustomerInRoute(Customer customer) {
		return map.get(customer) != null;
	}
	
	public int getCost() {
		int totalCost = 0;
		for (Route route : this) {
			totalCost += route.getCost();
		}
		return totalCost;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Solution) {
			Solution sol = (Solution) o;
			if (!getInstance().equals(sol.getInstance())) return false;
			HashSet<Route> routeSet = new HashSet<>(this);
			HashSet<Route> otherRouteSet = new HashSet<>(sol);
			return routeSet.equals(otherRouteSet);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		for (Route route : this) {
			sj.add(route.toString());
		}
		return sj.toString();
	}
	
}
