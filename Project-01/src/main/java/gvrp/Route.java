package gvrp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringJoiner;

import gvrp.diff.InterOpt2Star;
import gvrp.diff.IntraOpt2;
import gvrp.diff.IntraRelocate;
import gvrp.diff.Move;

@SuppressWarnings("serial")
public class Route extends LinkedList<Customer> {

	Point depot;
	int id;
	int maxCap;
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
		Customer lastCustomer = getLast();
		return dLeft.get(lastCustomer) + dRight.get(lastCustomer);
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
		Customer beforeC = null;
		if (!isEmpty()) beforeC = getLast();
		addLast(c);
		Iterator<Customer> iter = descendingIterator();
		Customer last = null;
		if(iter.hasNext()) {
			last = iter.next(); // skips c
			int distanceToDepot = c.distanceFrom(depot);
			if (beforeC == null) {
				dLeft.put(c, distanceToDepot);
			} else {
				int distanceToPrevious = c.distanceFrom(beforeC);
				dLeft.put(c, dLeft.get(beforeC) + distanceToPrevious);
			}
			dRight.put(c, distanceToDepot);
			while(iter.hasNext()) {
				Customer customer = iter.next();
				dRight.put(customer, dRight.get(last) + customer.distanceFrom(last));
				last = customer;
			}
		}
		return true;
	}
	
	/**
	 * Recalculates the distance map in a given range of customers. Both indexes are bounded
	 * between 0 and size-1. Lower and upper bounds don't have to be mapped to the left and
	 * right distances map, but the lower bounds' predecessor and right bound's successor must be.
	 * @param lowerBound - lowest index of customer with left distance outdated
	 * @param upperBound - highest index of customer with right distance outdated
	 * @param dmatrix - distance matrix
	 */
	private void recalculateDistanceMap(int lowerBound, int upperBound, DistanceMatrix dmatrix) {		
		int size = size();
		Customer lb = null, ub = null;
		int lcount = 0, rcount = 0;
				
		/*
		 * Calculating the distance of a customer from depot
		 * 
		 * FROM LEFT
		 * dl(i) = dl(i-1) + D(i,i-1)
		 * dl(0) = D(0,depot)
		 */
		
		if (lowerBound == 0) {
			lb = getFirst();
			lcount = dmatrix.getDistanceFromDepot(lb);
		} else {
			lb = get(lowerBound);
			Customer prev = get(lowerBound-1);
			lcount = dmatrix.getDistanceBetween(prev, lb) + dLeft.get(prev);
		}

		dLeft.put(lb, lcount);
		Iterator<Customer> iterator = listIterator(lowerBound);
		if(iterator.hasNext()) {
			Customer prev = iterator.next();
			while(iterator.hasNext()) {
				Customer c = iterator.next();
				lcount += dmatrix.getDistanceBetween(prev, c);
				dLeft.put(c, lcount);
				prev = c;
			}
		}
		
		/* 
		 * FROM RIGHT
		 * dr(i) = dr(i+1) + D(i,i+1)
		 * dr(N) = D(N,depot)
		 */
		
		if (upperBound == size-1) {
			ub = getLast();
			rcount = dmatrix.getDistanceFromDepot(ub);
		} else {
			ub = get(upperBound);
			Customer post = get(upperBound+1);
			rcount = dmatrix.getDistanceBetween(post, ub) + dRight.get(post);
		}

		dRight.put(ub, rcount);
		Iterator<Customer> invIterator = descendingIterator();
		while(invIterator.hasNext() && !invIterator.next().equals(ub));
		Customer post = ub; /* Begin with upper bound */
		while(invIterator.hasNext()) {
			Customer c = invIterator.next();
			rcount += dmatrix.getDistanceBetween(c, post);
			dRight.put(c, rcount);
			post = c;
		}
		
	}
	
	/**
	 * Takes two random numbers and operate a random shift
	 * @param r1 - random number #1
	 * @param r2 - random number #2
	 * @param dmatrix - distance matrix
	 */
	public boolean shiftSmart(int r1, int r2, DistanceMatrix dmatrix) {
		int size = size();
		if (size < 2) return false;
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
	 * Takes two random numbers and operate a random shift inter route
	 * @param r1 - random number #1
	 * @param r2 - random number #2
	 * @param dmatrix - distance matrix
	 */
	public boolean shiftInterSmart(Route r, int r1, int r2, DistanceMatrix dmatrix) {
		int size = size();
		int rSize = r.size();
		if (size < 2) return false; /* Can't leave this route empty */
		int p = r1 % size; /* p in [0,size-1] */
		int q = r2 % rSize; /* q in [0,rSize-1] */
		
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
		
		recalculateDistanceMap(cx == null ? p : x, cy == null ? p-1 : y-1, dmatrix); /* y decreased by one because p is removed from this route */
		r.recalculateDistanceMap(cz == null ? q : z, q+1, dmatrix); /* q increased by one because p is inserted in the route r*/
		
		return true;
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
	
	/**
	 * Flips a random sequence from two routes
	 * @param r - another route
	 * @param r1 - random number #1
	 * @param r2 - random number #2
	 * @return move
	 */
	public Move opt2Star(Route r, int r1, int r2) {
		int size = size();
		int rSize = r.size();
		if (size < 2 || rSize < 2) return null;
		int p = r1 % (size - 1); /* p in [0,size-1] */
		int rP = r2 % (rSize - 1); /* rp in [0,rSize-1] */
		/* swap tails */
		for (int i = 0; i < rSize - 1 - rP; i++)
			add(p + 1, r.removeLast());
		for (int i = 0; i < size - 1 - p; i++)
			r.add(rP + 1, removeLast());
		return new InterOpt2Star(this, r, p, rP);
	}
	
}
