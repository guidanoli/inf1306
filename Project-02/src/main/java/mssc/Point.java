package mssc;

/**
 * 
 * A point is an array of integers, such that the i-th integer
 * in the array represents the i-th coordinate in the R^n space.
 * 
 * This class eases arithmetic operations such as means and
 * distances, which are crucial to the MSSC problem.
 * 
 * @author guidanoli
 *
 */
public class Point {

	int coordinates[];
	
	public Point(int [] coordinates) {
		this.coordinates = coordinates;
	}
	
}
