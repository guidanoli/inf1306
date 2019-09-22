package gvrp.construction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import gvrp.Customer;
import gvrp.Instance;
import gvrp.Point;
import gvrp.Route;
import gvrp.Solution;

public class Scan implements ConstructiveMetaheuristic {

	class CustomerAngle {
		Customer customer;
		Double theta;
		
		public CustomerAngle(Customer customer, Double theta) {
			this.customer = customer;
			this.theta = theta;
		}
		public Double getTheta() {
			return theta;
		}
		public Customer getCustomer() {
			return customer;
		}
		@Override
		public String toString() {
			return String.format("C%d@%.2fpi", customer.getId(),
					(theta /  Math.PI));
		}
	}
		
	public Solution construct(Instance instance) {
		Solution solution = new Solution(instance);
		TreeSet<CustomerAngle> thetas = new TreeSet<CustomerAngle>(
				Comparator.comparingDouble(t -> t.getTheta())); /* sorts by angle */
		Point depot = instance.getDepot();
		boolean [] visitedSets = new boolean[instance.getSets().size()+1];
		Arrays.fill(visitedSets, false); /* Indexed by (1,#sets) */
		for (Customer c : instance.getCustomers()) {
			if (c.getSet() == null) continue; /* ignore depot */
			Point cpos = c.getPoint();
			Double ctheta = cpos.angleFrom(depot);
			thetas.add(new CustomerAngle(c, ctheta));
		}
		Iterator<Route> routeIter = solution.iterator();
		Route currentRoute = routeIter.next();
		for (CustomerAngle ca : thetas) {
			int setId = ca.getCustomer().getSet().getId();
			if (visitedSets[setId]) continue;
			visitedSets[setId] = true;
			boolean addedCustomer = currentRoute.addCustomer(ca.getCustomer());
			if (!addedCustomer) {
				if (!routeIter.hasNext()) break; /* No more routes */
				currentRoute = routeIter.next();
				/* This must never fail since demand < route capacity */
				currentRoute.addCustomer(ca.getCustomer());
			}
		}
		return solution;
	}

}
