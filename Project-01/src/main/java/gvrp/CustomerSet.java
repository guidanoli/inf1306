package gvrp;

import java.util.*;

@SuppressWarnings("serial")
public class CustomerSet extends HashSet<Customer> {

	static class Builder {
		
		HashSet<Customer> customerSet = new HashSet<Customer>();
		int setDemand = -1;
		int setId = -1;
		
		public Builder() {}
		public Builder addCustomer(Customer customer) {
			customerSet.add(customer);
			return this;
		}
		public Builder demand(int demand) {
			setDemand = demand;
			return this;
		}
		public Builder id(int id) {
			setId = id;
			return this;
		}
		public CustomerSet build() {
			CustomerSet set = new CustomerSet(setId, setDemand);
			for (Customer customer : customerSet) {
				set.add(customer);
			}
			return set;
		}
	}
	
	int demand;
	int id;
	
	public CustomerSet(int id, int demand) {
		this.id = id;
		this.demand = demand;
	}
		
	public int getDemand() {
		return demand;
	}
	
	@Override
	public boolean add(Customer e) {
		boolean added = super.add(e);
		if (added) {
			e.setSet(this);
		}
		return added;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CustomerSet) {
			CustomerSet set = (CustomerSet) o;
			return set.id == this.id;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("S%d = { demand = %d, customers = %s }", id, demand, super.toString());
	}
	
}
