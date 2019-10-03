package gvrp.construction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import gvrp.Customer;
import gvrp.CustomerSet;
import gvrp.DistanceMatrix;
import gvrp.Route;
import gvrp.Solution;

public class InsertionHeuristics {

	/**
	 * Tries to insert all unvisited sets in routes
	 * @param visitedSets - array of sets that were visited (true) and not visited (false)
	 * @param solution - incomplete solution
	 */
	public static void fixSolution(boolean [] visitedSets, Solution solution) {
		/* Remaining sets, ordered from with most demand to least demand */
		TreeSet<CustomerSet> remainingSets = new TreeSet<CustomerSet>((c1,c2)->c1.getDemand()>c2.getDemand()?-1:1);
		HashSet<CustomerSet> movedSets = new HashSet<>();
		HashSet<CustomerSet> swappedSets = new HashSet<>();
		DistanceMatrix dmatrix = solution.getInstance().getDistancematrix();
		for (int i = 1; i < visitedSets.length; i++) {
			if (!visitedSets[i])
				remainingSets.add(solution.getInstance().getSets().get(i-1));
		}
		
		if (remainingSets.isEmpty()) return; /* Don't even bother... */
		
		/* Get capacity gaps for each route*/
		int numOfRoutes = solution.size();
		HashMap<Route, Integer> routeGaps = new HashMap<>(numOfRoutes);
		int maxCap = solution.getInstance().getCapacity();
		for (int i = 0; i < numOfRoutes; i++) {
			Route route = solution.get(i);
			routeGaps.put(route, maxCap - route.getCapacity());
		}
		
		int impossibleSetCount = 0;
		while (!remainingSets.isEmpty()) {
			/* Until all sets have been associated to a route */
						
			CustomerSet set = null;
			int nextSetCounter = impossibleSetCount;
			for (CustomerSet s : remainingSets) {
				/* Obtains the set of highest demand that
				 * hasn't been tested yet
				 */
				if (nextSetCounter == 0) {
					set = s;
				}
				--nextSetCounter;
				break;
			}
			
			Customer customer = null;
			for (Customer c : set) {
				/* Get any customer from set
				 * The choice of customer could be optimized
				 * But this part is left to the Local Search
				 */
				customer = c;
				break;
			}
			
			HashSet<Route> availableRoutes = new HashSet<>();
			for (Route route : solution) {
				if (routeGaps.get(route) >= set.getDemand())
					availableRoutes.add(route);
			}
			
			Route randomRoute = null;
			for (Route r : availableRoutes) {
				/* Get any route available
				 * The choice of route could be optimized
				 * But this part is left to the Local Search
				 */
				randomRoute = r;
				break;
			}
			
			if (randomRoute == null) {
				Route targetRoute = null; /* Route from which target customer will be extracted */
				Customer targetCustomer = null; /* Customer that will be taken from its route */
				Route acceptingRoute = null; /* Target customer destiny */
				Customer acceptingCustomer = null; /* Customer to be swapped with target customer */
				int minimalResultingGap = Integer.MAX_VALUE; /* If no move is found, moves customer to another route resulting in a
															 minimal resulting gap in the target route the customer came from */
				int maximalResultingGap = -1; /* If no move is found, swaps customer with another customer in another route resulting
			 									in a maximal resulting gap in one of the routes */
				boolean foundMove = false;
				
				for (Route route : solution) {
					/* Routes with one (or zero) customers
					 * can't have customers taken, since they
					 * would be emptied.
					 */
					if (route.size() < 2) continue;
					for (Customer c : route) {
						int gapWithoutC = routeGaps.get(route) + c.getDemand();
						if (gapWithoutC >= set.getDemand()) {
							/* If c is removed from route, remaining set fits
							 */
							boolean foundAcceptingRoute = false;
							for (Route anotherRoute : solution) {
								/* Does not take the route itself into account
								 */
								if (anotherRoute == route) continue;
								if (routeGaps.get(anotherRoute) >= c.getDemand()) {
									/* There is another route that can take c
									 */
									acceptingRoute = anotherRoute;
									foundAcceptingRoute = true;
									break;
								}
							}
							if (foundAcceptingRoute) {
								/* There is an accepting route
								 */
								targetRoute = route;
								targetCustomer = c;
								foundMove = true;
								break;
							}
						}
						/* If c is removed from the route, remaining set does not fit
						 */
						if (!foundMove) {
							/* Does not take into account sets that have been moved
							 */
							if (!movedSets.contains(c.getSet())) {
								for (Route anotherRoute : solution) {
									/* Does not take the route itself into account
									 */
									if (anotherRoute == route) continue;
									/* Tries to find another route such that if c is
									 * added to it, it leaves a minimal gap, leaving
									 * more space for the other remaining sets, with
									 * higher priority
									 */
									int anotherRouteCurrentGap = routeGaps.get(anotherRoute);
									int customerDemand = c.getDemand();
									int resultingGap = anotherRouteCurrentGap - customerDemand;
									if (resultingGap < 0) continue;
									if (resultingGap < minimalResultingGap) {
										/* There is another route that can take c and
										 * leave a smaller gap in the accepting route
										 */
										minimalResultingGap = resultingGap;
										targetRoute = route;
										targetCustomer = c;
										acceptingRoute = anotherRoute;
										acceptingCustomer = null;
										/* foundMove flag isn't set to true because
										 * there could be better moves that fits much nicely */
									}
								}
							}
							
							/* Does not take into account sets that have been swapped
							 */
							if (!swappedSets.contains(c.getSet())) {
								for (Route anotherRoute : solution) {
									/* Does not take the route itself into account
									 */
									if (anotherRoute == route) continue;
									/* Tries to find another route such that if c is swapped
									 * with one customer from it, it leaves a maximal gap,
									 * leaving more space for the other remaining sets, with
									 * higher priority
									 */
									int anotherRouteCurrentGap = routeGaps.get(anotherRoute);
									int thisRouteCurrentGap = routeGaps.get(route);
									int customerDemand = c.getDemand();
									
									for (Customer anotherRouteCustomer : anotherRoute) {
										int anotherRouteCustomerDemand = anotherRouteCustomer.getDemand();
										int delta = customerDemand - anotherRouteCustomerDemand;
										int anotherRouteResultingGap = anotherRouteCurrentGap - delta;
										int thisRouteResultingGap = thisRouteCurrentGap + delta;
										if (anotherRouteResultingGap < 0 || thisRouteResultingGap < 0) continue; /* invalid moves */
										int largestDelta = Math.max(anotherRouteResultingGap, thisRouteResultingGap);
										if (largestDelta > maximalResultingGap) {
											maximalResultingGap = largestDelta;
											targetRoute = route;
											targetCustomer = c;
											acceptingRoute = anotherRoute;
											acceptingCustomer = anotherRouteCustomer;
										}
									}
								}
							}
						}
					}
					if (foundMove) {
						/* If one move is found, exits loop and
						 * applies it directly
						 */
						break;
					}
				}
				if (foundMove) {
					targetRoute.removeCustomer(targetCustomer, dmatrix);
					targetRoute.addCustomer(customer, dmatrix);
					acceptingRoute.addCustomer(targetCustomer, dmatrix);
					routeGaps.put(targetRoute, maxCap - targetRoute.getCapacity());
					routeGaps.put(acceptingRoute, maxCap - acceptingRoute.getCapacity());
					final CustomerSet finalSet = set;
					remainingSets.removeIf((s)->s.equals(finalSet));
					impossibleSetCount = 0;
				} else {
					if (targetRoute == null || targetCustomer == null || acceptingRoute == null) {
						++impossibleSetCount;
					} else {
						if (acceptingCustomer == null) {
							/* Relocate customer to another route */
							movedSets.add(targetCustomer.getSet());
							targetRoute.removeCustomer(targetCustomer, dmatrix);
							acceptingRoute.addCustomer(targetCustomer, dmatrix);
							routeGaps.put(targetRoute, maxCap - targetRoute.getCapacity());
							routeGaps.put(acceptingRoute, maxCap - acceptingRoute.getCapacity());
						} else {
							/* Swap customers */
							swappedSets.add(targetCustomer.getSet());
							swappedSets.add(acceptingCustomer.getSet());
							targetRoute.removeCustomer(targetCustomer, dmatrix);
							acceptingRoute.removeCustomer(acceptingCustomer, dmatrix);
							acceptingRoute.addCustomer(targetCustomer, dmatrix);
							targetRoute.addCustomer(acceptingCustomer, dmatrix);
							routeGaps.put(targetRoute, maxCap - targetRoute.getCapacity());
							routeGaps.put(acceptingRoute, maxCap - acceptingRoute.getCapacity());
						}
						impossibleSetCount = 0;
					}
				}
			} else {
				/* Adds the customer from the current set to the route
				 * Must never fail since it has been checked before if it could be added
				 */
				randomRoute.addCustomer(customer, dmatrix);
				routeGaps.put(randomRoute, maxCap - randomRoute.getCapacity());
				final CustomerSet finalSet = set;
				remainingSets.removeIf((s)->s.equals(finalSet));
				impossibleSetCount = 0;
			}
			if (impossibleSetCount == remainingSets.size()) {
				impossibleSetCount = 0; /* reset counter */
			}
		}
	}
	
}
