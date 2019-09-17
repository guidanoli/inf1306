package gvrp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CustomerTest {

	@Test
	void testBuilder() {
		Customer customer = new Customer.Builder()
				.id(1)
				.position(new Point(4,6))
				.customerSet(new CustomerSet(5))
				.build();
		assertNotNull(customer,
				() -> "Should build a new customer");
		assertEquals(1, customer.id,
				() -> "Should assign id to new customer");
		assertEquals(new Point(4,6), customer.pos,
				() -> "Should assign point to new customer");
		assertEquals(new CustomerSet(5), customer.set,
				() -> "Should assign set to new customer");
	}

}
