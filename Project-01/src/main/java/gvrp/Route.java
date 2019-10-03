package gvrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringJoiner;

@SuppressWarnings("serial")
public class Route extends LinkedList<Customer> {

	Point depot;
	int id;
	int maxCap;
	
	/* Distance buffer
	 * key = null -> distance from depot to depot */
	HashMap<Customer, Integer> dLeft = new HashMap<>();
	HashMap<Customer, Integer> dRight = new HashMap<>();
	
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
		
		Iterator<Customer> li = listIterator();
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
			sj.add(String.format("C%d(%d,d=%d)", c.getId(), c.getSet().getId(), c.getDemand()));
		}
		return String.format("R%d = { cost = %d, cap = %d, trajectory = [%s] }", id, getCost(), getCapacity(), sj.toString());
	}
	
	public boolean removeCustomer(Customer c, DistanceMatrix dmatrix) {
		int index = indexOf(c);
		boolean removed = remove(c);
		if (!removed) return false;
		if (isEmpty()) return true;
		int lb = Math.max(0, index-1);
		int ub = Math.min(size()-1, index+1);
		recalculateDistanceMap(lb, ub, dmatrix);
		c.removeFromRoute(); /* Updates customer route */
		return true;
	}
	
	public boolean addCustomer(Customer c, DistanceMatrix dmatrix) {
		int setDemand = c.getSet().getDemand();
		if (setDemand + getCapacity() > maxCap) return false;
		if (contains(c)) return false;
		addLast(c);
		int size = size();
		recalculateDistanceMap(size-1, size-1, dmatrix);
		c.insertInRoute(this); /* Updates customer route */
		return true;
	}

	private void recalculateLayerDistanceMap(DistanceMatrix dmatrix, CustomerSet baseLayer, CustomerSet newLayer, HashMap<Customer, Integer> map) {
		for (Customer newc : newLayer) {
			int shortestDistance = Integer.MAX_VALUE;
			for (Customer basec : baseLayer) {
				int distance = dmatrix.getDistanceBetween(basec, newc) + map.get(basec);
				if (distance < shortestDistance) {
					shortestDistance = distance;
				}
			}
			map.put(newc, shortestDistance);
		}
	}
	
	private void recalculateLayerRightDistanceMap(DistanceMatrix dmatrix, CustomerSet posterior, CustomerSet current) {
		recalculateLayerDistanceMap(dmatrix, posterior, current, dRight);
	}
	
	private void recalculateLayerLeftDistanceMap(DistanceMatrix dmatrix, CustomerSet previous, CustomerSet current) {
		recalculateLayerDistanceMap(dmatrix, previous, current, dLeft);
	}
	
	/**
	 * Recalculates the distance map in a given range of customers sets. Both indexes are bounded
	 * between 0 and size-1. Lower and upper bounds don't have to be mapped to the left and
	 * right distances map, but the lower bounds' predecessor and right bound's successor must be.
	 * All customers within each set are taken into account.
	 * 
	 * @param lowerBound - lowest index of customer with left distance outdated
	 * @param upperBound - highest index of customer with right distance outdated
	 * @param dmatrix - distance matrix
	 */
	private void recalculateDistanceMap(int lowerBound, int upperBound, DistanceMatrix dmatrix) {
		int size = size();
		Customer lb = null, ub = null;
				
		/* 
		 * FROM RIGHT
		 * dr(i) = dr(i+1) + D(i,i+1)
		 * dr(N) = D(N,depot)
		 */
		
		if (upperBound == size-1) {
			ub = getLast();
			for (Customer setCustomer : ub.getSet())
				dRight.put(setCustomer, dmatrix.getDistanceFromDepot(setCustomer));
		} else {
			ub = get(upperBound);
			Customer post = get(upperBound+1);
			recalculateLayerRightDistanceMap(dmatrix, post.getSet(), ub.getSet());
		}

		Iterator<Customer> invIterator = descendingIterator();
		while(invIterator.hasNext() && !invIterator.next().equals(ub));
		Customer post = ub; /* Begin with upper bound */
		while(invIterator.hasNext()) {
			Customer c = invIterator.next();
			recalculateLayerRightDistanceMap(dmatrix, post.getSet(), c.getSet());
			post = c;
		}
		
		/*
		 * Calculating the distance of a customer from depot
		 * 
		 * FROM LEFT
		 * dl(i) = dl(i-1) + D(i,i-1)
		 * dl(0) = D(0,depot)
		 */
		
		if (lowerBound == 0) {
			lb = getFirst();
			int shortestDistance = Integer.MAX_VALUE;
			for (Customer setCustomer : lb.getSet()) {
				int distance = dmatrix.getDistanceFromDepot(setCustomer);
				int accumulatedDistance = distance + dRight.get(setCustomer);
				if (accumulatedDistance < shortestDistance) {
					shortestDistance = accumulatedDistance;
				}
				dLeft.put(setCustomer, distance);
			}
			/* Closest costumer to the depot (from the right-hand side) */
			dRight.put(null, shortestDistance);
		} else {
			lb = get(lowerBound);
			Customer prev = get(lowerBound-1);
			recalculateLayerLeftDistanceMap(dmatrix, prev.getSet(), lb.getSet());
		}

		Iterator<Customer> iterator = listIterator(lowerBound);
		if(iterator.hasNext()) {
			Customer prev = iterator.next();
			while(iterator.hasNext()) {
				Customer curr = iterator.next();
				recalculateLayerLeftDistanceMap(dmatrix, prev.getSet(), curr.getSet());
				prev = curr;
			}
		}
		
	}
	
	/**
	 * Takes two random numbers and operate a random shift
	 * @param r1 - random number #1 (good between 0 and size-2)
	 * @param r2 - random number #2 (good between 0 and size-1)
	 * @param dmatrix - distance matrix
	 */
	public boolean shiftSmart(int r1, int r2, DistanceMatrix dmatrix) {
		int size = size();
		if (size < 2) return false;
		
		/* Filtering random input
		 * such that 0 >= r1 < r2 > size */
		r1 = Math.abs(r1);
		r2 = Math.abs(r2);
		r1 = Math.min(r1, r2);
		r2 = Math.max(r1, r2);
		
		int p1 = r1 % (size - 1); /* p1 in [0,size-2] */
		int p2 = p1 + 1 + r2 % (size - 1 - p1); /* p2 in [p1+1,size-1] */
		
		/*
		 * Calculating the delta of route cost by the following expression
		 * 
		 * BEFORE
		 * ... -- x -- p1 -- y -- ... -- p2 -- w -- ...
		 * 
		 * AFTER
		 * ... -- x -- y -- ... -- p2 -- p1 -- w -- ...
		 * 
		 * delta = dxy + dp2p1 + dp1w - dxp1 - dp1y - dp2w
		 * if p1 and p2 are neighbours, y == p2
		 * There is improvement iff delta < 0
		 */
		
		int x = p1 - 1, y = p1 + 1, w = p2 + 1; /* vertices */
		/* if x == -1, x is depot. if w == size, w is depot */
		int dxy, dp2p1, dp1w, dxp1, dp1y, dp2w; /* distances */
		Customer cx = null, cy = null, cw = null, cp1 = get(p1), cp2 = get(p2);
		
		if (x != -1) cx = get(x);
		cy = get(y);
		if (w != size) cw = get(w);
		
		if (cx == null) {
			dxy = dmatrix.getDistanceFromDepot(cy);
			dxp1 = dmatrix.getDistanceFromDepot(cp1);
		} else {
			dxy = dmatrix.getDistanceBetween(cx, cy);
			dxp1 = dmatrix.getDistanceBetween(cx, cp1);
		}
		
		if (cw == null) {
			dp1w = dmatrix.getDistanceFromDepot(cp1);
			dp2w = dmatrix.getDistanceFromDepot(cp2);
		} else {
			dp1w = dmatrix.getDistanceBetween(cp1, cw);
			dp2w = dmatrix.getDistanceBetween(cp2, cw);
		}
		
		dp2p1 = dmatrix.getDistanceBetween(cp1, cp2);
		dp1y = dmatrix.getDistanceBetween(cp1, cy);
		
		int delta = dxy + dp2p1 + dp1w - dxp1 - dp1y - dp2w;
		if (delta >= 0) return false; /* does not accept solutions of same cost */
		
		/* Local search is then applied */
		add(p2, remove(p1));
		
		recalculateDistanceMap(cx == null ? p1 : x, cw == null ? p2 : w, dmatrix);
		
		return true;
	}
	
		
	/**
	 * Takes two random numbers and operate a random shift inter route
	 * @param r - another route
	 * @param r1 - random number #1 (good between 0 and size-1)
	 * @param r2 - random number #2 (good between 0 and r.size-1)
	 * @param dmatrix - distance matrix
	 */
	public boolean shiftInterSmart(Route r, int r1, int r2, DistanceMatrix dmatrix) {
		if (this == r) return false;
		int size = size();
		int rSize = r.size();
		if (size < 2) return false; /* Can't leave this route empty */
		int p = Math.abs(r1) % size; /* p in [0,size-1] */
		int q = Math.abs(r2) % rSize; /* q in [0,rSize-1] */
		
		/*
		 * Calculating the delta of route cost by the following expression
		 * 
		 * BEFORE
		 * This route: ... -- x -- p -- y -- ...
		 * Route r: ... -- z -- q -- ...
		 * 
		 * AFTER
		 * This route: ... -- x -- y -- ...
		 * Route r: ... -- z -- p -- q -- ...
		 * 
		 * delta = dxy + dzp + dpq - dxp - dpy -dzq
		 * There is improvement iff delta < 0
		 */
		
		int x = p - 1, y = p + 1, z = q - 1; /* vertices */
		/* If index is either -1 or the size of route, it is the depot */
		int dxy, dzp, dpq, dxp, dpy, dzq; /* distances */
		Customer cx = null, cy = null, cz = null, cp = get(p), cq = r.get(q);
		
		/*
		 * Checking if route r has enough capacity
		 */
		
		if (cp.getDemand() + r.getCapacity() > r.maxCap) return false;
		
		/*
		 * Checking if there is a cost improvement
		 */
		
		if (x != -1) cx = get(x);
		if (y != size) cy = get(y);
		if (z != -1) cz = r.get(z);
		
		if (cx == null) {
			dxy = cy == null ? 0 : dmatrix.getDistanceFromDepot(cy);
			dxp = dmatrix.getDistanceFromDepot(cp);
		} else {
			dxy = cy == null ? dmatrix.getDistanceFromDepot(cx) : dmatrix.getDistanceBetween(cx, cy);
			dxp = dmatrix.getDistanceBetween(cx, cp);
		}
		
		if (cy == null) {
			dpy = dmatrix.getDistanceFromDepot(cp);
		} else {
			dpy = dmatrix.getDistanceBetween(cp, cy);
		}
		
		if (cz == null) {
			dzp = dmatrix.getDistanceFromDepot(cp);
			dzq = dmatrix.getDistanceFromDepot(cq);
		} else {
			dzp = dmatrix.getDistanceBetween(cz, cp);
			dzq = dmatrix.getDistanceBetween(cz, cq);
		}
		
		dpq = dmatrix.getDistanceBetween(cp, cq);
		
		int delta = dxy + dzp + dpq - dxp - dpy - dzq;
		if (delta >= 0) return false; /* does not accept solutions of same cost */
		
		/* Local search is then applied */
		r.add(q, remove(p));
		
		cp.insertInRoute(r); /* Updates customer route */
		
		recalculateDistanceMap(cx == null ? p : x, cy == null ? p-1 : y-1, dmatrix); /* y decreased by one because p is removed from this route */
		r.recalculateDistanceMap(cz == null ? q : z, q+1, dmatrix); /* q increased by one because p is inserted in the route r*/
		
		return true;
	}
	
	public void findShortestPath(DistanceMatrix dmatrix) {
		if (isEmpty()) return; /* Do nothing for empty routes */
		
		ArrayList<Customer> newRoute = new ArrayList<>(size());
		recalculateDistanceMap(0, 0, dmatrix);
		
		/*
		 * Bellman-Ford algorithm
		 */
		
		Customer previous = null;
		for (Customer customer : this) {
			int distanceFromRight = dRight.get(previous);
			/* Distance of previous point to right-hand side depot */
			Customer closestCustomer = null;
			for (Customer setCustomer : customer.getSet()) {
				int distanceToCustomer;
				if (previous == null) {
					/* If set is the first set and last customer is the depot */
					distanceToCustomer = dmatrix.getDistanceFromDepot(setCustomer);
				} else {
					/* If set is not the first set and there is a previous customer */
					distanceToCustomer = dmatrix.getDistanceBetween(previous, setCustomer);
				}
				/* Check if set customer is the chosen one */
				if (distanceToCustomer + dRight.get(setCustomer) == distanceFromRight) {
					closestCustomer = setCustomer;
					break;
				}
			}
			previous = closestCustomer;
			newRoute.add(previous);
		}
		replaceAll((c) -> {
			/* Updates customer route and route itself */
			c.removeFromRoute();
			Customer newCustomer = newRoute.get(indexOf(c));
			newCustomer.insertInRoute(this);
			return newCustomer;
		});
	}
	
}
