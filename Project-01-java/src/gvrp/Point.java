package gvrp;

public class Point {

	int x;
	int y;
	
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
	
}
