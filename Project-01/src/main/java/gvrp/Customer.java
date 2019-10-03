package gvrp;

public class Customer {

	CustomerSet set;
	Point pos;
	int id;
	
	public Customer(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public Point getPoint() {
		return pos;
	}
	
	public CustomerSet getSet() {
		return set;
	}
	
	public int getDemand() {
		return set.getDemand();
	}
	
	public void setSet(CustomerSet set) {
		this.set = set;
	}
	
	public void setPosition(Point pos) {
		this.pos = pos;
	}
	
	public int distanceFrom(Customer customer) {
		return this.pos.distanceTo(customer.pos);
	}
	
	public int distanceFrom(Point point) {
		return this.pos.distanceTo(point);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Customer) {
			Customer customer = (Customer) o;
			return customer.id == this.id;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	public String toCompactString() {
		return String.format("C%d", id);
	}
	
	@Override
	public String toString() {
		return String.format("C%d = %s", id, pos.toString());
	}
	
}
