package gvrp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringJoiner;

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
			add(new Route(i, instance.getDepot(), instance.getCapacity()));
		}
	}
	
	public Solution(Solution anotherSolution) {
		super(anotherSolution);
		this.instance = anotherSolution.instance;
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
				if (setCustomer.isInRoute())
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
	
	public int getCost() {
		int totalCost = 0;
		for (Route route : this) {
			totalCost += route.getCost();
		}
		return totalCost;
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
