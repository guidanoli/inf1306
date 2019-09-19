package gvrp;

public class Point {

	private int x;
	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	private int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int distanceTo(Point anotherPoint) {
		int dx = this.x - anotherPoint.x;
		int dy = this.y - anotherPoint.y;
		double dist = Math.sqrt( (double)(dx * dx + dy * dy) );
		int idist = (int) dist;
		return (dist - idist) < 0.5d ? idist : idist + 1;
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			Point p = (Point) o;
			return p.x == this.x && p.y == this.y;
		}
		return false;
	}
	
}
