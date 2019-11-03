package mssc;

import java.util.HashSet;
import java.util.StringJoiner;

public class Cluster extends HashSet<Point> {

	private static final long serialVersionUID = 4587339280747852544L;
	
	Point centroid;
	
	public Cluster(int numOfDimensions) {
		centroid = new Point(-1, numOfDimensions);
	}
	
	/**
	 * Updates centroid of cluster such that
	 * <code>centroid = (a1, a2, ..., ad)</code> and
	 * <code>ai = sum(xi, i=1..n) / n</code>
	 * being x the points contained within the cluster
	 */
	public void updateCentroid() {
		for (int i = 0; i < centroid.size(); i++) {
			double iCoordSum = 0.0;
			for (Point p : this) iCoordSum += p.get(i);
			centroid.set(i, iCoordSum / centroid.size());
		}
	}
	
	public double sumOfSquares() {
		double sum = 0.0;
		for (Point p : this)
			sum += centroid.getSumOfSquaresTo(p);
		return sum;
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(", ");
		for (Point p : this) {
			if (sj.toString().length() > 70) {
				sj.add("...");
				break;
			}
			sj.add(p.toShorterString());
		}
		return "{" + sj + "} (" + size() + " entities)";
	}
	
}
