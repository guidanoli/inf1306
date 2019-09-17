package gvrp;

public class Customer {

	public static class Builder {
	
		CustomerSet set;
		Point pos;
		int id;
		
		public Builder customerSet(CustomerSet set) {
			this.set = set;
			return this;
		}
		
		public Builder position(Point pos) {
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
	
	public int getDemand() {
		return set.getDemand();
	}
	
	public void setSet(CustomerSet set) {
		this.set = set;
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
	
	@Override
	public String toString() {
		return String.format("Customer #%d from Set #%d and Position %s", id, set.id, pos.toString());
	}
	
}
