package gvrp;

import java.util.LinkedList;
import java.util.ListIterator;

@SuppressWarnings("serial")
public class Route extends LinkedList<Customer> {

	Point depot;
	int id;
	int cost = 0;
	int cap = 0;
	
	public Route(int id, Point depot) {
		this.id = id;
		this.depot = depot;
	}
	
	public int getId() {
		return id;
	}
	
	/**
	 * @return the capacity occupied by all the clients in the route
	 */
	public int getCapacity() {
		return cap;
	}
	
	/**
	 * <p>Updates the route occupied capacity.
	 * 
	 * <p><b>Contract:</b>
	 * Whenever an update to the route has been made, this method has to be called.
	 */
	public void updateCapacity() {
		cap = calculateCapacity();
	}
	
	private int calculateCapacity() {
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
		return cost;
	}
	
	/**
	 * <p>Updates the route occupied capacity.
	 * 
	 * <p><b>Contract:</b>
	 * Whenever an update to the route has been made, this method has to be called.
	 */
	public void updateCost() {
		cost = calculateCost();
	}
	
	private int calculateCost() {
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
		return String.format("R%d = { cost = %d, trajectory = %s }", id, super.toString());
	}
	
}
