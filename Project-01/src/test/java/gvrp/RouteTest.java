package gvrp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import gvrp.diff.Move;

class RouteTest {

	Route route;
	
	Route newRoute(int id) {
		return new Route(id, new Point(0,0), 100);
	}
	
	int counter = 1;
	
	void buildRoute(Route baseRoute, int seed, int size) {
		for (int i = 0; i < size; i++) {
			Customer c = new Customer(counter);
			c.setSet(new CustomerSet(0, seed*i*i - seed + 1));
			c.setPosition(new Point(seed*i,-(seed+2)*i*i));
			baseRoute.addCustomer(c);
			counter++;
		}
	}
	
	@BeforeEach
	void beforeEach() {
		route = newRoute(0);
	}
	
	@Nested
	@DisplayName("the intraroute manipulation methods")
	class IntraManipulationTest {
		
		Route initialRoute;
		Move move;
		final int SIZE = 5;
		
		@BeforeEach
		void beforeEach() {
			buildRoute(route, 5, SIZE);
			/* clone initial route for later comparison */
			initialRoute = new Route(route);
			move = null;
		}
		
		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("the shift method")
		void testShift(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			move = route.shift(n % SIZE, n / SIZE);
		}
		
		@RepeatedTest(value = SIZE*SIZE)
		@DisplayName("the 2-opt method")
		void test2Opt(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1;
			move = route.opt2(n % SIZE, n / SIZE);
		}
		
		@AfterEach
		void afterEach() {
			assertEquals(initialRoute.size(), route.size(),
					() -> "should not remove or add customers");
			assertEquals(route, route,
					() -> "should let it equal itself");
			assertNotEquals(route, initialRoute,
					() -> "should let it differ from the initial route");
			HashSet<Customer> allCustomers = new HashSet<>(route);
			assertEquals(route.size(), allCustomers.size(),
					() -> "should not have repeated customers");
			assertNotNull(move,
					() -> "should result in a move");
			move.undo();
			assertEquals(initialRoute, route,
					() -> "Move should be correctly undone");
		}
		
	}
	
	@Nested
	@DisplayName("the interroute manipulation methods")
	class ExternalManipulationTest {
		
		Route route1, route2;
		Route ini1, ini2;
		final int SIZE1 = 10, SIZE2 = 2;
		Move move;
		
		@BeforeEach
		void beforeEach() {
			route1 = newRoute(1);
			route2 = newRoute(2);
			buildRoute(route1, 5, SIZE1);
			ini1 = new Route(route1);
			buildRoute(route2, 7, SIZE2);
			ini2 = new Route(route2);
			move = null;
		}

		@RepeatedTest(value = SIZE1*SIZE2)
		@DisplayName("the 2-opt-star method")
		void test2OptStar(RepetitionInfo info) {
			int n = info.getCurrentRepetition() - 1; 
			move = route1.opt2Star(route2, n % SIZE1, n / SIZE1);
		}
		
		@AfterEach
		void afterEach() {
			assertEquals(route1, route1,
					() -> "should let it equal itself");
			assertEquals(route2, route2,
					() -> "should let it equal itself");
			assertNotEquals(route1, ini1,
					() -> "should let it differ from the initial route");
			assertNotEquals(route2, ini2,
					() -> "should let it differ from the initial route");
			int summedUpSizes = route1.size() + route2.size();
			HashSet<Customer> allCustomers = new HashSet<>(summedUpSizes);
			allCustomers.addAll(route1);
			allCustomers.addAll(route2);
			assertEquals(summedUpSizes, allCustomers.size(),
					() -> "should not have repeated customers");
			assertNotNull(move,
					() -> "should result in a move");
			move.undo();
			assertEquals(ini1, route1,
					() -> "Move should be correctly undone");
			assertEquals(ini2, route2,
					() -> "Move should be correctly undone");
		}
		
	}
	
	@AfterEach
	void afterEach() {
		assertNotNull(route,
				() -> "Route must not be null");
		for (int i = 0; i < route.size(); i++) {
			for (int j = i+1; j < route.size(); j++) {
				assertNotEquals(route.get(i), route.get(j),
						() -> "There must not be repeated customers");
			}
		}
	}
	
}
