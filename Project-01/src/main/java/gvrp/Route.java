package gvrp;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringJoiner;

import gvrp.diff.IntraOpt2;
import gvrp.diff.IntraRelocate;
import gvrp.diff.Move;

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
	
	public Route(Route anotherRoute) {
		super(anotherRoute); /* Copies customers */
		this.depot = anotherRoute.depot;
		this.id = anotherRoute.id;
		this.maxCap = anotherRoute.maxCap;
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
	
	/**
	 * Takes two random numbers and operate a random shift
	 * @param r1 - random number #1
	 * @param r2 - random number #2
	 * @return move
	 */
	public Move shift(int r1, int r2) {
		int size = size();
		if (size < 2) return null;
		int p1 = r1 % (size - 1); /* p1 in [0,size-2] */
		int p2 = p1 + 1 + r2 % (size - 1 - p1); /* p2 in [p1+1,size-1] */
		add(p2, remove(p1)); /* 0 is in the beginning and size-1 is at the end */
		return new IntraRelocate(this, p1, p2);
	}
	
	/**
	 * Flips a random sequence within the route
	 * @param r1 - random number #1
	 * @param r2 - random number #2
	 * @return move
	 */
	public Move opt2(int r1, int r2) {
		int size = size();
		if (size < 2) return null;
		int p1 = r1 % (size - 1); /* p1 in [0,size-2] */
		int p2 = p1 + 1 + r2 % (size - 1 - p1); /* p2 in [p1+1,size-1] */
		for (int i = 0; i < p2 - p1; i++)
			add(p1, remove(p2)); /* flips sequence [p1,p2] */
		return new IntraOpt2(this, p1, p2);
	}
	
}
