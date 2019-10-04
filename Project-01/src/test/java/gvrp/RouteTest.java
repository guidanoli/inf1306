package gvrp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

class RouteTest {

	Route route;
	Point depot;
	DistanceMatrix dmatrix;
	
	Route newRoute(int id) {
		return new Route(id, depot, 10000);
	}
	
	int counter = 1;
	
	void buildRoute(Route baseRoute, int seed, int size) {
		ArrayList<Customer> customers = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Customer c = new Customer(counter);
			new CustomerSet(i+1, seed*(i+1) - seed + 1).add(c);
			c.setPosition(new Point(seed*i,-(seed+2)*i*i));
			customers.add(c);
			counter++;
		}
		ArrayList<Customer> dmatrixArray = new ArrayList<>(customers);
		Customer depotCustomer = new Customer(0);
		depotCustomer.setPosition(depot);
		new CustomerSet(0,0).add(depotCustomer);
		dmatrixArray.add(depotCustomer);
		dmatrix = new DistanceMatrix(dmatrixArray, depot);
		for (Customer customer : customers) {
			baseRoute.addCustomer(customer, dmatrix);
		}
	}
	
	@BeforeEach
	void beforeEach() {
		depot = new Point(0,0);
		route = newRoute(0);
	}
	
	@Nested
	@DisplayName("the getCost method")
	class GetCostTest {
				
		final int SIZE = 10; /* See that this value is dependent on the route capacity. */
		final int PHI = 3; /* Average number of customers per set */
		
		DistanceMatrix dmatrix;
		int initialCost;
				
		@BeforeEach
		void fillRoute() {
			ArrayList<Integer> x = new ArrayList<>(SIZE), y = new ArrayList<>(SIZE);
			for(int i = 0; i < SIZE; i++) {
				x.add(i); y.add(i);
			}
			Collections.shuffle(x);
			Collections.shuffle(y);
			
			ArrayList<Customer> customers = new ArrayList<>(SIZE);
			for(int i = 0; i < SIZE; i++) {
				Customer c = new Customer(i+1);
				new CustomerSet(i+1, i+1).add(c);
				c.setPosition(new Point(x.get(i), y.get(i)));
				customers.add(c);
			}
			
			ArrayList<Customer> dmatrixArray = new ArrayList<>(customers);
			Customer depotCustomer = new Customer(0);
			depotCustomer.setPosition(depot);
			new CustomerSet(0,0).add(depotCustomer);
			dmatrixArray.add(depotCustomer);
			dmatrix = new DistanceMatrix(dmatrixArray, depot);
			for (Customer customer : customers) {
				route.addCustomer(customer, dmatrix);
			}
			
			initialCost = route.getCost();
		}
				
		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("after intra shift")
		void testIntraShift(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			boolean shifted = route.intraShift(n % SIZE, n / SIZE, dmatrix, true);
			if (shifted) assertTrue(initialCost > route.getCost(), () -> "should output a lower cost when improves");
			else assertEquals(initialCost, route.getCost(), () -> "but stay the same when does not improve");
		}
		
		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("after intra swap")
		void testIntraSwap(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			boolean swapped = route.intraSwap(n % SIZE, n / SIZE, dmatrix, true);
			if (swapped) assertTrue(initialCost > route.getCost(), () -> "should output a lower cost when improves");
			else assertEquals(initialCost, route.getCost(), () -> "but stay the same when does not improve");
		}

		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("after intra 2-Opt")
		void testIntra2Opt(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			boolean reversed = route.intra2Opt(n % SIZE, n / SIZE, dmatrix, true);
			if (reversed) assertTrue(initialCost > route.getCost(), () -> "should output a lower cost when improves");
			else assertEquals(initialCost, route.getCost(), () -> "but stay the same when does not improve");
		}
		
		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("after inter shift")
		void testInterShift(RepetitionInfo info) {
			int size = info.getCurrentRepetition();
			int n = size - 1;
			
			Route anotherRoute = newRoute(1);
			ArrayList<Integer> x = new ArrayList<>(size), y = new ArrayList<>(size);
			for(int i = 0; i < size; i++) {
				x.add(i); y.add(i);
			}
			Collections.shuffle(x);
			Collections.shuffle(y);
			
			ArrayList<Customer> customers = new ArrayList<>();
			for(int i = 0; i < size; i++) {
				Customer c = new Customer(SIZE+i+1);
				new CustomerSet(SIZE+i+1, i+1).add(c);
				c.setPosition(new Point(x.get(i), y.get(i)));
				customers.add(c);
			}
			
			ArrayList<Customer> al = new ArrayList<>(route);
			al.addAll(customers);
			Customer depotCustomer = new Customer(0);
			depotCustomer.setPosition(depot);
			new CustomerSet(0,0).add(depotCustomer);
			al.add(depotCustomer);
			dmatrix = new DistanceMatrix(al, depot);
			for (Customer customer : customers) { 
				boolean added = anotherRoute.addCustomer(customer, dmatrix);
				assertTrue(added, "Could not add " + customer + " to " + anotherRoute);
			}
			initialCost = route.getCost() + anotherRoute.getCost();
			
			boolean shifted = route.interShift(anotherRoute, n % SIZE, n / SIZE, dmatrix, true);
			int newCost = route.getCost() + anotherRoute.getCost();
			if (shifted) assertTrue(initialCost > newCost, () -> "when improves");
			else assertEquals(initialCost, newCost, () -> "but not when does not improve");
			
			checkDistanceMapOfRoute(anotherRoute);
		}
		
		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("after inter swap")
		void testInterSwap(RepetitionInfo info) {
			int size = info.getCurrentRepetition();
			int n = size - 1;
			
			Route anotherRoute = newRoute(1);
			ArrayList<Integer> x = new ArrayList<>(size), y = new ArrayList<>(size);
			for(int i = 0; i < size; i++) {
				x.add(i); y.add(i);
			}
			Collections.shuffle(x);
			Collections.shuffle(y);
			
			ArrayList<Customer> customers = new ArrayList<>();
			for(int i = 0; i < size; i++) {
				Customer c = new Customer(SIZE+i+1);
				new CustomerSet(SIZE+i+1, i+1).add(c);
				c.setPosition(new Point(x.get(i), y.get(i)));
				customers.add(c);
			}
			
			ArrayList<Customer> al = new ArrayList<>(route);
			al.addAll(customers);
			Customer depotCustomer = new Customer(0);
			depotCustomer.setPosition(depot);
			new CustomerSet(0,0).add(depotCustomer);
			al.add(depotCustomer);
			dmatrix = new DistanceMatrix(al, depot);
			for (Customer customer : customers) { 
				boolean added = anotherRoute.addCustomer(customer, dmatrix);
				assertTrue(added, "Could not add " + customer + " to " + anotherRoute);
			}
			initialCost = route.getCost() + anotherRoute.getCost();
			
			boolean swapped = route.interSwap(anotherRoute, n % SIZE, n / SIZE, dmatrix, true);
			int newCost = route.getCost() + anotherRoute.getCost();
			if (swapped) assertTrue(initialCost > newCost, () -> "when improves");
			else assertEquals(initialCost, newCost, () -> "but not when does not improve");
			
			checkDistanceMapOfRoute(anotherRoute);
		}
		
		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("after inter 2-opt*")
		void testInter2OptStar(RepetitionInfo info) {
			int size = info.getCurrentRepetition();
			int n = size - 1;
			
			Route anotherRoute = newRoute(1);
			ArrayList<Integer> x = new ArrayList<>(size), y = new ArrayList<>(size);
			for(int i = 0; i < size; i++) {
				x.add(i); y.add(i);
			}
			Collections.shuffle(x);
			Collections.shuffle(y);
			
			ArrayList<Customer> customers = new ArrayList<>();
			for(int i = 0; i < size; i++) {
				Customer c = new Customer(SIZE+i+1);
				new CustomerSet(SIZE+i+1, i+1).add(c);
				c.setPosition(new Point(x.get(i), y.get(i)));
				customers.add(c);
			}
			
			ArrayList<Customer> al = new ArrayList<>(route);
			al.addAll(customers);
			Customer depotCustomer = new Customer(0);
			depotCustomer.setPosition(depot);
			new CustomerSet(0,0).add(depotCustomer);
			al.add(depotCustomer);
			dmatrix = new DistanceMatrix(al, depot);
			for (Customer customer : customers) { 
				boolean added = anotherRoute.addCustomer(customer, dmatrix);
				assertTrue(added, "Could not add " + customer + " to " + anotherRoute);
			}
			initialCost = route.getCost() + anotherRoute.getCost();
			
			boolean swapped = route.inter2OptStar(anotherRoute, n % SIZE, n / SIZE, dmatrix, true);
			int newCost = route.getCost() + anotherRoute.getCost();
			if (swapped) assertTrue(initialCost > newCost, () -> "when improves");
			else assertEquals(initialCost, newCost, () -> "but not when does not improve");
			
			checkDistanceMapOfRoute(anotherRoute);
		}
		
		void initMultipleSetRoute() {
			route = newRoute(0);
			ArrayList<Integer> x = new ArrayList<>(SIZE), y = new ArrayList<>(SIZE);
			for(int i = 0; i < SIZE; i++) {
				x.add(i); y.add(i);
			}
			Collections.shuffle(x);
			Collections.shuffle(y);
			
			int numberOfSets = SIZE/PHI + 1;
			ArrayList<CustomerSet> sets = new ArrayList<>(numberOfSets);
			for (int i = 0; i < numberOfSets; i++) {
				sets.add(new CustomerSet(i+1, i+1));
			}
			ArrayList<Customer> customers = new ArrayList<>(SIZE);
			for(int i = 0; i < SIZE; i++) {
				Customer c = new Customer(i+1);
				CustomerSet cset = sets.get(i % numberOfSets);
				cset.add(c);
				c.setPosition(new Point(x.get(i), y.get(i)));
				customers.add(c);
			}
			
			ArrayList<Customer> dmatrixArray = new ArrayList<>(customers);
			Customer depotCustomer = new Customer(0);
			depotCustomer.setPosition(depot);
			new CustomerSet(0,0).add(depotCustomer);
			dmatrixArray.add(depotCustomer);
			dmatrix = new DistanceMatrix(dmatrixArray, depot);
			HashSet<CustomerSet> alreadyAddedSets = new HashSet<>(); 
			for (Customer customer : customers) {
				CustomerSet customerSet = customer.getSet();
				if (!alreadyAddedSets.add(customerSet)) continue;
				route.addCustomer(customer, dmatrix);
			}
			
			initialCost = route.getCost();
		}
		
		@RepeatedTest(value = SIZE)
		@DisplayName("after a shortest path algorithm")
		void testShortestPath() {
			initMultipleSetRoute();
			route.findShortestPath(dmatrix);
			int finalCost = route.getCost();
			assertTrue(finalCost <= initialCost,
					() -> "cost should not increase");
		}
		
		void checkDistanceMapOfRoute(Route r) {
			for(int i = 0; i < r.size() - 1; i++) {
				Customer ci = r.get(i);
				Customer cipp = r.get(i+1);
				int dist = dmatrix.getDistanceBetween(ci, cipp);
				assertTrue(dist >= r.dLeft.get(cipp) - r.dLeft.get(ci),
						ci + " " + cipp + " dist=" + dist + " in map=" + (r.dLeft.get(cipp) - r.dLeft.get(ci)));
				assertTrue(dist >= r.dRight.get(ci) - r.dRight.get(cipp),
						ci + " " + cipp + " dist=" + dist + " in map=" + (r.dRight.get(ci) - r.dRight.get(cipp)));
			}
		}
		
		@AfterEach
		void checkDistanceMaps() {
			checkDistanceMapOfRoute(route);
		}
		
	}
	
	@Nested
	@DisplayName("the removeCustomer method")
	class RemoveTest {
		
		DistanceMatrix dmatrix = null;
		
		Customer getSampleCustomer(int id, int setId, Point pos) {
			Customer c = new Customer(id);
			new CustomerSet(setId,1).add(c);
			c.setPosition(pos);
			return c;
		}
		
		DistanceMatrix getDmatrix(Customer... customers) {
			ArrayList<Customer> customerList = new ArrayList<Customer>(Arrays.asList(customers));
			return new DistanceMatrix(customerList, depot);
		}
		
		@Test
		@DisplayName("on an empty route")
		void emptyTest() {
			Customer c = getSampleCustomer(1, 1, depot);
			dmatrix = getDmatrix(c);
			assertFalse(route.removeCustomer(c, dmatrix),
					() -> "should return false");
		}
		
		@Test
		@DisplayName("on a route with one customer")
		void singleTest() {
			Customer c = getSampleCustomer(1, 1, depot);
			dmatrix = getDmatrix(c);
			route.addCustomer(c, dmatrix);
			assertTrue(route.removeCustomer(c, dmatrix),
					() -> "should return true");
		}
		
		@RepeatedTest(value=2)
		@DisplayName("on a route with multiple customers")
		void multipleTest(RepetitionInfo info) {
			int n = info.getCurrentRepetition() + 1;
			Customer [] customerArray = new Customer[n];
			for (int i = 0; i < n; i++) {
				Customer c = getSampleCustomer(i+1, i+1, new Point(i+1, i+1));
				customerArray[i] = c;
			}
			dmatrix = getDmatrix(customerArray);
			for (int i = 0; i < n; i++)
				route.addCustomer(customerArray[i], dmatrix);
			checkDistanceMapOfRoute(route, dmatrix);
			for (int i = 0; i < n; i++) {
				boolean removed = route.removeCustomer(customerArray[i], dmatrix);
				assertTrue(removed,
						() -> "should return true");
			}
		}
		
		@RepeatedTest(value=2)
		@DisplayName("on a route with multiple customers on different sets")
		void multipleSetsTest(RepetitionInfo info) {
			int n = info.getCurrentRepetition() + 1;
			int phi = 3; /* Customers per set */
			Customer [] customerArray = new Customer[n*phi];
			for (int j = 0; j < phi; j++) /* One for each set */
				for (int i = 0; i < n; i++) {
					int id = i+j*n+1;
					Customer c = getSampleCustomer(id, i+1, new Point(id, id));
					customerArray[id-1] = c;
				}
			dmatrix = getDmatrix(customerArray);
			for (int i = 0; i < n; i++) /* One of each set */
				route.addCustomer(customerArray[i], dmatrix);
			checkDistanceMapOfRoute(route, dmatrix);
			for (int i = 0; i < n; i++) {
				int iniCap = route.getCapacity();
				int iniSize = route.size();
				boolean removed = route.removeCustomer(customerArray[i], dmatrix);
				assertEquals(iniSize-1, route.size(),
						() -> "size should decrease by one");
				assertEquals(iniCap-customerArray[i].getDemand(), route.getCapacity(),
						() -> "capacity should decrease by the removed customer's demand");
				assertTrue(removed,
						() -> "should return true");
			}
		}
		
		void checkDistanceMapOfRoute(Route r, DistanceMatrix dmatrix) {
			if (dmatrix == null) return;
			for(int i = 0; i < r.size() - 1; i++) {
				Customer ci = r.get(i);
				Customer cipp = r.get(i+1);
				int dist = dmatrix.getDistanceBetween(ci, cipp);
				assertTrue(dist >= r.dLeft.get(cipp) - r.dLeft.get(ci),
						ci + " " + cipp + " dist=" + dist + " in map=" + (r.dLeft.get(cipp) - r.dLeft.get(ci)));
				assertTrue(dist >= r.dRight.get(ci) - r.dRight.get(cipp),
						ci + " " + cipp + " dist=" + dist + " in map=" + (r.dRight.get(ci) - r.dRight.get(cipp)));
			}
		}
		
		@AfterEach
		void afterEach() {
			checkDistanceMapOfRoute(route, dmatrix);
			dmatrix = null;
		}
		
	}
	
	@AfterEach
	void afterEach() {
		assertNotNull(route,
				() -> "Route must not be null");
		for (int i = 0; i < route.size(); i++) {
			Customer ci = route.get(i);
			assertTrue(ci.isInRoute(),
					() -> "All customers in route should be associated via the redundant field 'route', which should not be null");
			assertEquals(route, ci.getRoute(),
					() -> "All customers in route should be associated via the redundant field 'route', which should be equal to the route it's in");
			for (int j = i+1; j < route.size(); j++) {
				assertNotEquals(ci, route.get(j),
						() -> "There must not be repeated customers");
			}
		}
	}
	
	
	
}

