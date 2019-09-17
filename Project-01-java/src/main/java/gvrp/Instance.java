package gvrp;

import java.util.*;

public class Instance {

	public static class Builder {
		
		String name;
		Point depot;
		ArrayList<CustomerSet> customerSets;
		int vehicleCount;
		int vehicleCapacity;
		
		public Builder() {}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder depot(Point pos) {
			this.depot = pos;
			return this;
		}
		
		public Builder vehicleCount(int count) {
			this.vehicleCount = count;
			return this;
		}
		
		public Builder vehicleCapacity(int capacity) {
			this.vehicleCapacity = capacity;
			return this;
		}
		
		public Builder addSet(CustomerSet set) {
			this.customerSets.add(set);
			return this;
		}
		
		public Instance build() {
			return new Instance(name, depot, customerSets, vehicleCount, vehicleCapacity);
		}
		
	}
	
	String name;
	Point depot;
	ArrayList<CustomerSet> customerSets;
	int vehicleCount;
	int vehicleCapacity;
	
	public Instance(String name, Point depot, ArrayList<CustomerSet> sets, int vCount, int vCap) {
		this.name = name;
		this.depot = depot;
		this.customerSets = sets;
		this.vehicleCount = vCount;
		this.vehicleCapacity = vCap;
	}
	
	public static Instance parse(Scanner sc) {
		Builder builder = new Builder();
		return builder.build();
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		sj.add(name);
		sj.add("#vehicles = " + vehicleCount);
		sj.add("capacity = " + vehicleCapacity);
		sj.add("depot = " + depot.toString());
		sj.add("#sets = " + customerSets.size());
		for (CustomerSet set : customerSets) {
			sj.add(set.toString());
		}
		return sj.toString();
	}
	
}
