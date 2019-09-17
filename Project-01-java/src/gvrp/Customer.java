package gvrp;

public class Customer {

	public class Builder {
	
		CustomerSet set;
		Point pos;
		
		public Builder customerSet(CustomerSet set) {
			this.set = set;
			return this;
		}
		
		public Builder position(Point pos) {
			this.pos = pos;
			return this;
		}
		
		public Customer build() {
			return new Customer(set, pos);
		}
		
	}
	
	CustomerSet set;
	Point pos;

	public Customer(CustomerSet set, Point pos) {
		this.set = set;
		this.pos = pos;
	}
	
	public int getDemand() {
		return set.getDemand();
	}
	
}
