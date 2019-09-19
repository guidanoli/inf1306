package gvrp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PointTest {

	@Test
	void testEquals() {
		assertAll(
				() -> assertEquals(new Point(4,5), new Point(4,5),
						() -> "(+,+) should equal (+,+)"),
				() -> assertEquals(new Point(4,-5), new Point(4,-5),
						() -> "(+,-) should equal (+,-)"),
				() -> assertEquals(new Point(-4,5), new Point(-4,5),
						() -> "(-,+) should equal (-,+)"),
				() -> assertEquals(new Point(-4,-5), new Point(-4,-5),
						() -> "(-,-) should equal (-,-)"),
				() -> assertNotEquals(new Point(4,5), new Point(-4,5),
						() -> "(+,+) should not equal (-,+)"),
				() -> assertNotEquals(new Point(4,5), new Point(4,-5),
						() -> "(+,+) should not equal (+,-)"),
				() -> assertNotEquals(new Point(4,5), new Point(-4,-5),
						() -> "(+,+) should not equal (-,-)")
				);
	}
	
	@Test
	void testDist() {
		Point [] points = {
				new Point(0,7),
				new Point(-2,11),
				new Point(3,-13),
				new Point(-5,-17)
		};
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < points.length; j++) {
				if (i == j) {
					assertEquals(0, points[i].distanceTo(points[i]),
							() -> "Distance between the point and itself should be zero");
				}
				if (j >= i) {
					assertEquals(points[i].distanceTo(points[j]), points[j].distanceTo(points[i]),
							() -> "Distance should be comutative");
				}
				assertTrue(points[i].distanceTo(points[j]) >= 0,
						() -> "Distance between two points should never be negative");
			}
		}
	}

}
