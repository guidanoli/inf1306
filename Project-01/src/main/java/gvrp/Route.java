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
	 * Shifts a customer within a route
	 * @param p - position of customer in this route
	 * @param q - position of destiny of the same customer (to the right)
	 * @param dmatrix - distance matrix
	 * @return success or not
	 */
	public boolean intraShift(int p, int q, DistanceMatrix dmatrix) {
		int size = size();
		if (size < 2) return false;
		
		/* Filtering arbitrary input
		 * such that 0 >= p < q > size */
		p = Math.abs(p) % size;
		q = Math.abs(q) % size;
		p = Math.min(p, q);
		q = Math.max(p, q);
		
		if (p == q) return false;
				
		/*
		 * Calculating the delta of route cost by the following expression
		 * 
		 * BEFORE
		 * ... -- x -- p -- y -- ... -- q -- w -- ...
		 * 
		 * AFTER
		 * ... -- x -- y -- ... -- q -- p -- w -- ...
		 * 
		 * delta = dxy + dqp + dpw - dxp - dpy - dqw
		 * if p and q are neighbours, y === q
		 * There is improvement iff delta < 0
		 */
		
		int x = p - 1, y = p + 1, w = q + 1; /* vertices */
		/* if x == -1, x is depot. if w == size, w is depot */
		int dxy, dqp, dpw, dxp, dpy, dqw; /* distances */
		Customer cx = null, cy = null, cw = null, cp = get(p), cq = get(q);
		
		if (x != -1) cx = get(x);
		cy = get(y);
		if (w != size) cw = get(w);
		
		if (cx == null) {
			dxy = dmatrix.getDistanceFromDepot(cy);
			dxp = dmatrix.getDistanceFromDepot(cp);
		} else {
			dxy = dmatrix.getDistanceBetween(cx, cy);
			dxp = dmatrix.getDistanceBetween(cx, cp);
		}
		
		if (cw == null) {
			dpw = dmatrix.getDistanceFromDepot(cp);
			dqw = dmatrix.getDistanceFromDepot(cq);
		} else {
			dpw = dmatrix.getDistanceBetween(cp, cw);
			dqw = dmatrix.getDistanceBetween(cq, cw);
		}
		
		dqp = dmatrix.getDistanceBetween(cp, cq);
		dpy = dmatrix.getDistanceBetween(cp, cy);
		
		int delta = dxy + dqp + dpw - dxp - dpy - dqw;
		if (delta >= 0) return false; /* does not accept solutions of same cost */
		
		/* Local search is then applied */
		add(q, remove(p));
		
		recalculateDistanceMap(cx == null ? p : x, cw == null ? q : w, dmatrix);
		
		return true;
	}
	
		
	/**
	 * Shifts a customer from one route to another
	 * @param r - another route
	 * @param p - position of customer in this route
	 * @param q - position of another customer in the route r
	 * @param dmatrix - distance matrix
	 * @return success or not
	 */
	public boolean interShift(Route r, int p, int q, DistanceMatrix dmatrix) {
		if (this == r) return false;
		int size = size();
		int rSize = r.size();
		if (size < 2 || rSize == 0) return false; /* Can't leave this route empty */
		
		p = Math.abs(p) % size; /* p in [0,size-1] */
		q = Math.abs(q) % rSize; /* q in [0,rSize-1] */
		
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
		 * delta = dxy + dzp + dpq - dxp - dpy - dzq
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

	/**
	 * Swaps two customers from the same route
	 * @param p - position of customer from this route
	 * @param q - position of the other customer in the route
	 * @param dmatrix - distance matrix
	 * @return success or not
	 */
	public boolean intraSwap(int p, int q, DistanceMatrix dmatrix) {
		int size = size();
		if (size < 2) return false;
		
		/* Filtering arbitrary input
		 * such that 0 >= p < q > size */
		p = Math.abs(p) % size;
		q = Math.abs(q) % size;
		p = Math.min(p, q);
		q = Math.max(p, q);
		
		if (p == q) return false;
				
		/*
		 * Calculating the delta of route cost by the following expression
		 * 
		 * BEFORE
		 * ... -- x -- p -- y -- ... -- z -- q -- w -- ...
		 * 
		 * AFTER
		 * ... -- x -- q -- y -- ... -- z -- p -- w -- ...
		 * 
		 * delta = dxq + dqy + dzp + dpw - dxp - dpy - dzq - dqw
		 * Restriction 1: if p and q are neighbours, y === z === p
		 * There is improvement iff delta < 0
		 */
		
		int x = p - 1, y = p + 1, z = q - 1, w = q + 1; /* vertices */
		if (z < y) z = y; /* Restriction 1 */
		/* if x == -1, x is depot. if w == size, w is depot */
		int dxq, dqy, dzp, dpw, dxp, dpy, dzq, dqw; /* distances */
		Customer cx = null, cy = null, cz = null, cw = null, cp = get(p), cq = get(q);
		
		if (x != -1) cx = get(x);
		cy = get(y);
		cz = get(z);
		if (w != size) cw = get(w);
		
		if (cx == null) {
			dxp = dmatrix.getDistanceFromDepot(cp);
			dxq = dmatrix.getDistanceFromDepot(cq);
		} else {
			dxp = dmatrix.getDistanceBetween(cx, cp);
			dxq = dmatrix.getDistanceBetween(cx, cq);
		}
		
		if (cw == null) {
			dpw = dmatrix.getDistanceFromDepot(cp);
			dqw = dmatrix.getDistanceFromDepot(cq);
		} else {
			dpw = dmatrix.getDistanceBetween(cp, cw);
			dqw = dmatrix.getDistanceBetween(cq, cw);
		}
		
		dzq = dmatrix.getDistanceBetween(cz, cq);
		dzp = dmatrix.getDistanceBetween(cz, cp);
		dqy = dmatrix.getDistanceBetween(cq, cy);
		dpy = dmatrix.getDistanceBetween(cp, cy);
		
		int delta = dxq + dqy + dzp + dpw - dxp - dpy - dzq - dqw;
		if (delta >= 0) return false; /* does not accept solutions of same cost */
		
		/* Local search is then applied */
		
		/* The order of the operations
		 * is really important here */
		remove(q);
		remove(p);
		add(p, cq);
		add(q, cp);
		
		recalculateDistanceMap(cx == null ? p : x, cw == null ? q : w, dmatrix);
		
		return true;
	}
	
	/**
	 * Swaps two customers from different routes
	 * @param r - another route
	 * @param p - position of customer from this route
	 * @param q - position of customer in the other route
	 * @param dmatrix - distance matrix
	 * @return success or not
	 */
	public boolean interSwap(Route r, int p, int q, DistanceMatrix dmatrix) {
		if (this == r) return false;
		int size = size();
		int rSize = r.size();
		if (size == 0 || rSize == 0) return false;
		
		p = Math.abs(p) % size; /* p in [0,size-1] */
		q = Math.abs(q) % rSize; /* q in [0,rSize-1] */
		
		/*
		 * Calculating the delta of route cost by the following expression
		 * 
		 * BEFORE
		 * This route: ... -- x -- p -- y -- ...
		 * Route r: ... -- z -- q -- w -- ...
		 * 
		 * AFTER
		 * This route: ... -- x -- q -- y -- ...
		 * Route r: ... -- z -- p -- w -- ...
		 * 
		 * delta = dxq + dqy + dzp + dpw - dxp - dpy - dzq - dqw
		 * There is improvement iff delta < 0
		 */
		
		int x = p - 1, y = p + 1, z = q - 1, w = q + 1; /* vertices */
		/* if x,z == -1, x,z is depot. if y,w == size, y,w is depot */
		int dxq, dqy, dzp, dpw, dxp, dpy, dzq, dqw; /* distances */
		Customer cx = null, cy = null, cz = null, cw = null, cp = get(p), cq = r.get(q);
		
		/*
		 * Checking if this route and route r have enough capacity
		 */
		
		int demandGap = cp.getDemand() - cq.getDemand();
		if (r.getCapacity() + demandGap > r.maxCap ||
				getCapacity() - demandGap > maxCap)
			return false;

		/*
		 * Checking if there is a cost improvement
		 */
		
		if (x != -1) cx = get(x);
		if (y != size) cy = get(y);
		if (z != -1) cz = r.get(z);
		if (w != rSize) cw = r.get(w);
		
		/* Can be computed multiple times */
		int dp = dmatrix.getDistanceFromDepot(cp);
		int dq = dmatrix.getDistanceFromDepot(cq);
		
		if (cx == null) {
			dxp = dp;
			dxq = dq;
		} else {
			dxp = dmatrix.getDistanceBetween(cx, cp);
			dxq = dmatrix.getDistanceBetween(cx, cq);
		}
		
		if (cy == null) {
			dqy = dq;
			dpy = dp;
		} else {
			dqy = dmatrix.getDistanceBetween(cq, cy);
			dpy = dmatrix.getDistanceBetween(cp, cy);
		}
		
		if (cz == null) {
			dzq = dq;
			dzp = dp;
		} else {
			dzq = dmatrix.getDistanceBetween(cz, cq);
			dzp = dmatrix.getDistanceBetween(cz, cp);
		}
		
		if (cw == null) {
			dpw = dp;
			dqw = dq;
		} else {
			dpw = dmatrix.getDistanceBetween(cp, cw);
			dqw = dmatrix.getDistanceBetween(cq, cw);
		}
		
		int delta = dxq + dqy + dzp + dpw - dxp - dpy - dzq - dqw;
		if (delta >= 0) return false; /* does not accept solutions of same cost */
		
		/* Local search is then applied */
		
		/* The order of the operations
		 * is really important here */
		remove(p);
		r.remove(q);
		add(p, cq);
		r.add(q, cp);
		
		/* Updates customers' route */
		cp.insertInRoute(r);
		cq.insertInRoute(this);
		
		recalculateDistanceMap(cx == null ? p : x, cy == null ? p : y, dmatrix);
		r.recalculateDistanceMap(cz == null ? q : z, cw == null ? q : w, dmatrix);
		
		return true;
	}
		

	/**
	 * Reverses a sequence of customers from the same route
	 * @param p - position of the first customer from this route
	 * @param q - position of the second customer in the route
	 * @param dmatrix - distance matrix
	 * @return success or not
	 */
	public boolean intra2Opt(int p, int q, DistanceMatrix dmatrix) {
		return false;
	}
	
	/**
	 * Swaps two customers from different routes
	 * @param r - another route
	 * @param p - position of customer from this route
	 * @param q - position of customer in the other route
	 * @param dmatrix - distance matrix
	 * @return success or not
	 */
	public boolean inter2OptStar(Route r, int p, int q, DistanceMatrix dmatrix) {
		return false;
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
