package gvrp;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringJoiner;

@SuppressWarnings("serial")
public class Route extends LinkedList<Customer> {

	Point depot;
	int id;
	int maxCap;
	
	public Route(int id, Point depot, int maximumCapacity) {
		this.id = id;
		this.depot = depot;
		this.maxCap = maximumCapacity;
	}
	
	public int getId() {
		return id;
	}
	
	/**
	 * @return the capacity occupied by all the clients in the route
	 */
	public int getCapacity() {
		int totalCap = 0;
		for (Customer customer : this) {
			totalCap += customer.getDemand();
		}
		return totalCap;
	}
	
	/**
	 * @return the cost of the route trajectory, starting and ending in the depot,
	 * through all the customers in between.
	 */	
	public int getCost() {
		if (isEmpty()) return 0; /* No customers */
		
		int totalCost = 0;
		Customer first = getFirst();
		totalCost += first.distanceFrom(depot);
		if (size() == 1) return 2*totalCost; /* One customer */
		
		ListIterator<Customer> li = listIterator(1);
		Customer prev = first;
		while (li.hasNext()) {
			Customer curr = li.next();
			totalCost += prev.distanceFrom(curr);
			prev = curr;
		}
		totalCost += prev.distanceFrom(depot);
		return totalCost;
	}
	
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(", ");
		for (Customer c : this) {
			sj.add(String.format("C%d", c.getId()));
		}
		return String.format("R%d = { cost = %d, trajectory = [%s] }", id, getCost(), sj.toString());
	}
	
	public boolean addCustomer(Customer c) {
		int customerCost = c.getDemand();
		if (customerCost + getCapacity() > maxCap) return false;
		if (contains(c)) return false;
		addLast(c);
		return true;
	}
	
}
