package gvrp;

import java.util.*;

@SuppressWarnings("serial")
public class CustomerSet extends HashSet<Customer> {

	int demand;
	int id;
	
	public CustomerSet(int demand) {
		this.demand = demand;
	}
	
	public int getDemand() {
		return demand;
	}
	
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
		StringJoiner sj = new StringJoiner("\n");
		for (Customer customer : this) {
			sj.add(customer.toString());
		}
		return String.format("Set #%d:\n%s", id, sj.toString());
	}
	
}
