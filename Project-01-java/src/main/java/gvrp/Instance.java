package gvrp;

import java.util.*;
import java.util.regex.Pattern;

public class Instance {

	/**
	 * Instance builder
	 * 
	 * Does not offer any kind of guarantee to the final instance state
	 * May leave it in a corrupted or invalid state.
	 * 
	 * @author guidanoli
	 *
	 */
	public static class Builder {
		
		String name = null;
		ArrayList<CustomerSet> customerSets = new ArrayList<CustomerSet>();
		ArrayList<Customer> customers = new ArrayList<Customer>();
		int vehicleCount = 0;
		int vehicleCapacity = 0;
		
		/**
		 * Constructs the builder
		 */
		public Builder() {}
		
		/**
		 * Sets the instance name
		 * @param name - instance name
		 * @return builder
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}
				
		/**
		 * Sets the count of vehicles
		 * @param count - vehicle count
		 * @return builder
		 */
		public Builder vehicleCount(int count) {
			this.vehicleCount = count;
			return this;
		}
		
		/**
		 * Sets vehicle capacity
		 * @param capacity - vehicle capacity
		 * @return builder
		 */
		public Builder vehicleCapacity(int capacity) {
			this.vehicleCapacity = capacity;
			return this;
		}
		
		/**
		 * Sets the count of customers (including depot)
		 * @param count - number of nodes (customers + depot)
		 * @return build
		 */
		public Builder dimension(int d) {
			for (int i = 0; i < d; i++) {
				this.customers.add(new Customer(i));
			}
			return this;
		}
		
		/**
		 * Will throw error if customer does not exists
		 * @param customerId - customer id
		 * @param x - customer x coordinate
		 * @param y - customer y coordinate
		 * @return builder
		 */
		public Builder customerPosition(int customerId, int x, int y) {
			Customer targetCustomer = customers.get(customerId - 1);
			targetCustomer.setPosition(new Point(x, y));
			return this;
		}
		
		/**
		 * Will throw error if set does not exists
		 * @param setId - set id
		 * @param demand - set demand
		 * @return builder
		 */
		public Builder customerSetDemand(int setId, int demand) {
			CustomerSet targetSet = customerSets.get(setId - 1);
			targetSet.setDemand(demand);
			return this;
		}
		
		/**
		 * Sets count of customer sets. Ignores if count < 0.
		 * @param count - count of sets
		 * @return builder
		 */
		public Builder customerSetCount(int count) {
			for (int i = 0; i < count; i++) {
				this.customerSets.add(new CustomerSet(i+1));
			}
			return this;
		}
		
		/**
		 * Will throw error if customer or set does not exist
		 * @param customerId - customer id (1 to N)
		 * @param setId - set id (1 to N)
		 * @return builder
		 */
		public Builder customerSet(int customerId, int setId) {
			Customer targetCustomer = customers.get(customerId - 1);
			CustomerSet targetSet = customerSets.get(setId - 1);
			targetCustomer.set = targetSet;
			targetSet.add(targetCustomer);
			return this;
		}
		
		/**
		 * Considers the first customer with no set associated as being the depot
		 * @return instance object
		 */
		public Instance build() {
			Point depot = null;
			for (Customer customer : customers) {
				if (customer.set == null) {
					depot = customer.pos;
					break;
				}
			}
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
	
	public static Instance parse(Scanner sc) throws NoSuchElementException, IllegalStateException, InputMismatchException {
		Builder builder = new Builder();
		Pattern colons = Pattern.compile(":");
		sc.next("NAME"); sc.next(colons);
		/* Instance name */
		builder.name(sc.next());
		sc.next("COMMENT"); sc.next(colons); sc.next("GVRP");
		sc.next("DIMENSION"); sc.next(colons);
		/* Instance dimension */
		int dimension = sc.nextInt();
		if (dimension <= 0) {
			throw new IllegalStateException("Dimension must be a positive number");
		}
		builder.dimension(dimension);
		sc.next("VEHICLES"); sc.next(colons);
		/* Vehicle count */
		int vCount = sc.nextInt();
		if (vCount <= 0) {
			throw new IllegalStateException("Vehicle count must be a positive number");
		}
		builder.vehicleCount(vCount);
		sc.next("GVRP_SETS"); sc.next(colons);
		/* Set count */
		int setCount = sc.nextInt();
		if (setCount <= 0) {
			throw new IllegalStateException("Set count must be a positive number");
		}
		builder.customerSetCount(setCount);
		sc.next("CAPACITY"); sc.next(colons);
		/* Vehicle capacity */
		int vCap = sc.nextInt();
		if (vCap <= 0) {
			throw new IllegalStateException("Vehicle capacity must be a positive number");
		}
		builder.vehicleCapacity(vCap);
		sc.next("EDGE_WEIGHT_TYPE"); sc.next(colons); sc.next("EUC_2D");
		sc.next("NODE_COORD_SECTION");
		for (int i = 0; i < dimension; i++) {
			int id = sc.nextInt();
			int x = sc.nextInt();
			int y = sc.nextInt();
			builder.customerPosition(id, x, y);
		}
		sc.next("GVRP_SET_SECTION");
		for (int i = 0; i < setCount; i++) {
			int setId = sc.nextInt();
			int customerId = sc.nextInt();
			while (customerId != -1) {
				builder.customerSet(customerId, setId);
				customerId = sc.nextInt();
			}
		}
		sc.next("DEMAND_SECTION");
		for (int i = 0; i < setCount; i++) {
			int setId = sc.nextInt();
			int demand = sc.nextInt();
			builder.customerSetDemand(setId, demand);
		}
		return builder.build();
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("name = " + name);
		sj.add("#vehicles = " + vehicleCount);
		sj.add("capacity = " + vehicleCapacity);
		sj.add("sets = ");
		if (customerSets != null) {
			for (CustomerSet set : customerSets)
				sj.add(set.toString());
		} else {
			sj.add("null");
		}
		return sj.toString();
	}
	
}
