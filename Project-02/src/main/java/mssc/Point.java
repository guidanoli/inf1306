package mssc;

import java.util.ArrayList;
import java.util.StringJoiner;

public class Point extends ArrayList<Double>{

	private static final long serialVersionUID = 1489868924280779119L;

	int id;
	
	public Point(int id, int numOfDimensions) {
		super(numOfDimensions);
		for (int i = 0; i < numOfDimensions; i++) add(0.0);
		this.id = id;
	}
	
	/**
	 * Obtain the distance between two points as the
	 * Euclidian distance squared
	 * @param anotherPoint - another point
	 * @return <code>sum((xi - yi)^2, i=1..n)</code>
	 */
	public double getSumOfSquaresTo(Point anotherPoint) {
		double sumOfSquares = 0.0;
		double diff;
		for (int i = 0; i < size(); i++) {
			diff = get(i) - anotherPoint.get(i);
			sumOfSquares += diff * diff;
		}
		return sumOfSquares;
	}
		
	public String toShorterString() {
		return "e" + id;
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(", ");
		for (Double coord : this)
			sj.add(Double.toString(coord));
		return "(" + sj + ")";
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof Point) {
			Point p = (Point) arg0;
			int size = size();
			if (size != p.size()) return false;
			for (int i = 0; i < size; i++) {
				if (get(i) != p.get(i)) return false;
			}
			return true;
		}
		return false;
	}
	
}
