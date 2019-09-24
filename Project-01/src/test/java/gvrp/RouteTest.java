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
	
	@BeforeEach
	void beforeEach() {
		route = new Route(0, new Point(0,0), 100);
	}
	
	@Nested
	@DisplayName("the intraroute manipulation methods")
	class IntraManipulationTest {
		
		Route initialRoute;
		Move move;
		final int SIZE = 5;
		
		@BeforeEach
		void beforeEach() {
			for (int i = 0; i < SIZE; i++) {
				Customer c = new Customer(i+1);
				c.setSet(new CustomerSet(0, i*i));
				c.setPosition(new Point(5*i,-7*i*i));
				route.addCustomer(c);
			}
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
			System.out.println(route);
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
