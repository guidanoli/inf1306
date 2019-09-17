package gvrp;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class CustomerSet extends ArrayList<Customer> {

	int demand;
	
	public CustomerSet(int demand) {
		this.demand = demand;
	}
	
	public int getDemand() {
		return demand;
	}

}
