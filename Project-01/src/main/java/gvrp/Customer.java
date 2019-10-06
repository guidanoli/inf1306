package gvrp;

public class Customer {

	public static class Builder {
		
		CustomerSet set;
		Point pos;
		Integer id;
		
		/**
		 * Constructs the builder
		 */
		public Builder() {}
		
		public Builder set(CustomerSet set) {
			this.set = set;
			return this;
		}
		
		public Builder pos(Point pos) {
			this.pos = pos;
			return this;
		}
		
		public Builder id(int id) {
			this.id = id;
			return this;
		}
		
		public Customer build() {
			return new Customer(set, pos, id);
		}
		
	}
	
	CustomerSet set;
	Point pos;
	int id;
	
	public Customer(CustomerSet set, Point pos, int id) {
		this.set = set;
		this.pos = pos;
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
