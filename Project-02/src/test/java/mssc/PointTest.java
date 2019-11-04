package mssc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

class PointTest {

	/* Number of dimensions */
	final static int D = 4;
	
	/* Number of points for all quadrants plus origin */
	final static int Q = (2 << D) + 1;
	
	final static Point [] p = new Point[Q];
	
	@BeforeAll
	static void beforeAll() {
		/* The 2^D quadrants */
		for (int i = 0; i < Q-1; i++) {
			/* i = b1b2b3... where bn is whether the 
			 * i-th point will have a positive or
			 * negative n-th coordinate */
			p[i] = new Point(i, D);
			for (int j = 0; j < D; j++) {
				boolean positiveValue = ((i >> j) & 1) == 0;
				p[i].set(j, positiveValue ? 1.0 : -1.0);					
			}
		}
		/* Origin */
		p[Q-1] = new Point(Q-1, D);
		for (int i = 0; i < D; i++)
			p[Q-1].set(i, 0.0);
	}
	
	@RepeatedTest(value = Q)
	@DisplayName("Distance of a point from itself")
	void testSamePoint(RepetitionInfo info) {
		int n = info.getCurrentRepetition() - 1;
		assertEquals(0.0, p[n].getSumOfSquaresTo(p[n]),
				() -> "should be zero");
	}
	
	@RepeatedTest(value = Q*Q)
	@DisplayName("Distance between two points")
	void testPositiveDistance(RepetitionInfo info) {
		int n = info.getCurrentRepetition() - 1;
		int i = n % Q, j = n / Q;
		assertTrue(p[i].getSumOfSquaresTo(p[j]) >= 0.0,
				() -> "should not be negative");
	}
	
	@RepeatedTest(value = Q-1)
	@DisplayName("Distance from origin of (+-1, +-1, ...)")
	void testDistanceFromOrigin(RepetitionInfo info) {
		int n = info.getCurrentRepetition() - 1;
		assertEquals(D, p[n].getSumOfSquaresTo(p[Q-1]),
				() -> "should be #dimensions");
	}

	@RepeatedTest(value = Q)
	@DisplayName("Any point")
	void testEqualsItself(RepetitionInfo info) {
		int n = info.getCurrentRepetition() - 1;
		Point copy = new Point(p[n].getId(), p[n].size());
		for (int i = 0; i < p[n].size(); i++)
			copy.set(i, p[n].get(i));
		assertEquals(p[n], copy,
				() -> "should equal itself");
	}
	
	@RepeatedTest(value = Q*Q)
	@DisplayName("Any point")
	void testDoesNotEqualAnother(RepetitionInfo info) {
		int n = info.getCurrentRepetition() - 1;
		int i = n % Q, j = n / Q;
		if (i == j) return;
		assertNotEquals(p[i], p[j],
				() -> "should not equal any other point");
	}
	
}
