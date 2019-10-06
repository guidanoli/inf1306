package gvrp;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.util.Random;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import gvrp.construction.SolutionFactory;
import gvrp.search.LocalSearch;

class RouteTest {

	static Instance instance;
	final static int numOfRoutes = 9;
	Solution solution;
	Route route;
	int routeSize;
	DistanceMatrix dmatrix;
	int initialCost;
	
	void getRouteInfo(int n) {
		route = solution.get(n);
		routeSize = route.size();
		initialCost = route.getCost();
		dmatrix = instance.getDistancematrix();
	}	
	
	interface IntraMove {
		public boolean move(int p, int q, DistanceMatrix dmatrix, boolean onlyImprove);
	}
	
	interface InterMove {
		public boolean move(Route r, int p, int q, DistanceMatrix dmatrix, boolean onlyImprove);
	}
	
	@BeforeAll
	static void loadInstance() {
		File instanceFile = new File("data/GVRP3/G-n262-k25-C88-V9.gvrp");
		/* Largest instance file in hand */
		try {
			Scanner sc = new Scanner(instanceFile);
			instance = Instance.parse(sc, 20, false);
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
			assumeFalse(false); /* Ignore */
			return;
		}
	}
	
	@BeforeEach
	void beforeEach() {
		solution = SolutionFactory.construct(instance, "greedy");
	}
	
	@Nested
	@DisplayName("the getCost method")
	class GetCostTest {
		
		void testIntraMove(int n, IntraMove intraMove) {
			getRouteInfo(n);
			for (int i = 0; i < routeSize*routeSize; i++) {
				boolean done = intraMove.move(i % routeSize, i / routeSize, dmatrix, true);
				if (done) {
					int newCost = route.getCost();
					assertTrue(initialCost > newCost, () -> "should output a lower cost when improves");
					initialCost = newCost;
				} else {
					assertEquals(initialCost, route.getCost(), () -> "but stay the same when does not improve");
				}
			}
		}
		
		void testInterMove(int n, InterMove interMove) {
			int routeIndex = n / numOfRoutes, anotherRouteIndex = n % numOfRoutes;
			if (routeIndex == anotherRouteIndex) return;
			
			getRouteInfo(routeIndex);
			Route anotherRoute = solution.get(anotherRouteIndex);
			
			int anotherRouteSize = anotherRoute.size();
			int anotherInitialCost = anotherRoute.getCost();
			
			int summedUpCosts = initialCost + anotherInitialCost;
			
			for (int i = 0; i < routeSize; i++) {
				for (int j = 0; j < anotherRouteSize; j++) {;
					boolean done = interMove.move(anotherRoute, i, j, dmatrix, true);
					int newCost = route.getCost() + anotherRoute.getCost();
					if (done) {
						assertTrue(summedUpCosts > newCost,
								() -> "should output a lower cost when improves");
						summedUpCosts = newCost;
					} else {
						assertEquals(summedUpCosts, newCost,
								() -> "but stay the same when does not improve");
					}
					checkDistanceMapOfRoute(route);
					checkDistanceMapOfRoute(anotherRoute);
				}
			}
		}
		
		@RepeatedTest(value = numOfRoutes)
		@DisplayName("after intra shift")
		void testIntraShift(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			testIntraMove(n, (p,q,d,o) -> route.intraShift(p,q,d,o));
		}
		
		@RepeatedTest(value = numOfRoutes)
		@DisplayName("after intra swap")
		void testIntraSwap(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			testIntraMove(n, (p,q,d,o) -> route.intraSwap(p,q,d,o));
		}

		@RepeatedTest(value = numOfRoutes)
		@DisplayName("after intra 2-Opt")
		void testIntra2Opt(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			testIntraMove(n, (p,q,d,o) -> route.intra2Opt(p,q,d,o));
		}
		
		@RepeatedTest(value = numOfRoutes*numOfRoutes)
		@DisplayName("after inter shift")
		void testInterShift(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			testInterMove(n, (r,p,q,d,o) -> route.interShift(r,p,q,d,o));
		}
		
		@RepeatedTest(value = numOfRoutes*numOfRoutes)
		@DisplayName("after inter swap")
		void testInterSwap(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			testInterMove(n, (r,p,q,d,o) -> route.interSwap(r,p,q,d,o));
		}
		
		@RepeatedTest(value = numOfRoutes*numOfRoutes)
		@DisplayName("after inter 2-opt*")
		void testInter2OptStar(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			testInterMove(n, (r,p,q,d,o) -> route.inter2OptStar(r,p,q,d,o));
		}
		
		@RepeatedTest(value = numOfRoutes)
		@DisplayName("after a shortest path algorithm")
		void testShortestPath(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			getRouteInfo(n);
			route.findShortestPath(dmatrix);
			int finalCost = route.getCost();
			assertTrue(finalCost <= initialCost,
					() -> "cost should not increase");
		}
				
		@AfterEach
		void afterEach() {
			if (route == null) return; /* ignore edge cases */
			for (int i = 0; i < route.size(); i++) {
				Customer ci = route.get(i);
				assertTrue(solution.isCustomerInRoute(ci),
						() -> "All customers in route should be associated via the redundant field 'route', which should not be null");
				assertEquals(route, solution.getCustomerRoute(ci),
						() -> "All customers in route should be associated via the redundant field 'route', which should be equal to the route it's in");
				for (int j = i+1; j < route.size(); j++) {
					assertNotEquals(ci, route.get(j),
							() -> "There must not be repeated customers");
				}
			}
		}
		
	}
	
	@Nested
	@DisplayName("the removeCustomer method")
	class RemoveTest {
				
		@RepeatedTest(value = numOfRoutes)
		@DisplayName("on a route with customers")
		void singleTest(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			getRouteInfo(n);
			Random random = new Random();
			for (int i = 0; i < routeSize; i++) {
				int rIndex = random.nextInt(routeSize-i);
				Customer c = route.get(rIndex);
				assertTrue(route.removeCustomer(c, dmatrix),
						() -> "should return true");
				assertFalse(solution.isCustomerInRoute(c),
						() -> "Removed customer should be out of the hash map");
			}
		}
				
		@AfterEach
		void afterEach() {
			checkDistanceMapOfRoute(route);
		}
		
	}
	
	@Test
	@DisplayName("the equals method")
	public void testEquality() {
		assertTrue(solution.equals(solution),
				() -> "should always return true");
	}
	
	@Nested
	@DisplayName("a copy of the solution")
	class CloningTest {
		
		Solution copy;
		LocalSearch ls = new LocalSearch(0);
		
		@BeforeEach
		void copySolution() {
			copy = new Solution(solution);
		}
		
		@Test
		@DisplayName("should be equal")
		void testEquality() {
			assertEquals(solution, copy,
					() -> "should output an equal copy");
			assertEquals(solution.map, copy.map,
					() -> "should have equal maps");
		}
		
		@Test
		@DisplayName("should be independent")
		void testIndependentFields() {
			/* Map */
			assertFalse(solution.map.isEmpty(),
					() -> "solution map should not be empty");
			solution.map.clear();
			assertTrue(solution.map.isEmpty(),
					() -> "solution map should be empty after called clear()");
			assertFalse(copy.map.isEmpty(),
					() -> "should have independent maps");
			
			/* Routes */
			for (Route route : solution) {
				assertFalse(route.isEmpty(),
						() -> "solution routes should not be empty");
				route.clear();
				assertTrue(route.isEmpty(),
						() -> "solution map should be empty after called clear()");
			}
			for (Route copyRoute : copy) {
				assertFalse(copyRoute.isEmpty(),
						() -> "should have independent routes");
			}
			
			/* Route hash maps */
			for (Route route : solution) {
				assertAll("Solution routes should not be empty",
						() -> assertFalse(route.dLeft.isEmpty(),
								() -> "left route should not be empty"),
						() -> assertFalse(route.dRight.isEmpty(),
								() -> "left route should not be empty"));
				route.dLeft.clear();
				route.dRight.clear();
				assertAll("Solution routes hash maps should be empty after called clear()",
						() -> assertTrue(route.dLeft.isEmpty(),
								() -> "left route hash maps should not be empty"),
						() -> assertTrue(route.dRight.isEmpty(),
								() -> "left route hash maps should not be empty"));
			}
			for (Route route : copy) {
				assertAll("should have independent hash maps",
						() -> assertFalse(route.dLeft.isEmpty(),
								() -> "left route hash maps should not be empty"),
						() -> assertFalse(route.dRight.isEmpty(),
								() -> "left route hash maps should not be empty"));
			}
		}
		
		@Test
		@DisplayName("should have independent routes")
		void testIndependentSolutions() {
			assertFalse(solution.isEmpty(),
					() -> "solution should not be empty");
			solution.clear();
			assertTrue(solution.isEmpty(),
					() -> "solution map should be empty after called clear()");
			assertFalse(copy.isEmpty(),
					() -> "should have independent maps");
		}
		
		@Test
		@DisplayName("should not interfeer after local search")
		void testShortestPath() {
			int changes = ls.findLocalMinimum(copy);
			assertTrue(changes > 0,
					() -> "local search should alter the copy state");
			assertFalse(deepEquals(solution, copy),
					() -> "the two should be different");
		}
		
		@Test
		@DisplayName("should not interfeer after pertubation")
		void testPertubation() {
			ls.perturbSolution(copy, copy.size());
			assertFalse(deepEquals(solution, copy),
					() -> "the two should be different");
		}
		
		boolean deepEquals(Solution s1, Solution s2) {
			if (s1 == s2 || s1.equals(s2) || s1.map.equals(s2.map)) return true;
			return false;
		}
		
		
	}
	
	void checkDistanceMapOfRoute(Route r) {
		for(int i = 0; i < r.size() - 1; i++) {
			Customer ci = r.get(i);
			Customer cipp = r.get(i+1);
			int dist = dmatrix.getDistanceBetween(ci, cipp);
			assertTrue(dist >= r.dLeft.get(cipp) - r.dLeft.get(ci),
					() -> ci + " " + cipp + " dist=" + dist + " in map=" + (r.dLeft.get(cipp) - r.dLeft.get(ci)));
			assertTrue(dist >= r.dRight.get(ci) - r.dRight.get(cipp),
					() -> ci + " " + cipp + " dist=" + dist + " in map=" + (r.dRight.get(ci) - r.dRight.get(cipp)));
		}
	}
	
}

