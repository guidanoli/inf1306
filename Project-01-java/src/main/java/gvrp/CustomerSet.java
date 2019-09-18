package gvrp;

import java.util.*;

@SuppressWarnings("serial")
public class CustomerSet extends HashSet<Customer> {

	int demand;
	int id;
	
	public CustomerSet(int id) {
		this.id = id;
	}
	
	public void setDemand(int demand) {
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
		return String.format("S[%d] = { demand = %d, customers = %s }", id, demand, super.toString());
	}
	
}
