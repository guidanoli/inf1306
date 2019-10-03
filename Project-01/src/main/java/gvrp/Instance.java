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
		
		String instanceName = null;
		ArrayList<CustomerSet.Builder> customerSetsBuilders = new ArrayList<CustomerSet.Builder>();
		ArrayList<Customer> customers = new ArrayList<Customer>();
		int fleetSize = 0;
		int vehicleCapacity = 0;
		int k = 20;
		boolean showGamma = false;
		
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
			this.instanceName = name;
			return this;
		}
				
		/**
		 * Sets the count of vehicles
		 * @param count - vehicle count
		 * @return builder
		 */
		public Builder fleetSize(int count) {
			this.fleetSize = count;
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
			CustomerSet.Builder targetSetBuilder = customerSetsBuilders.get(setId - 1);
			targetSetBuilder.demand(demand);
			return this;
		}
		
		/**
		 * Sets count of customer sets. Ignores if count < 0.
		 * @param count - count of sets
		 * @return builder
		 */
		public Builder customerSetCount(int count) {
			for (int i = 1; i <= count; i++) {
				this.customerSetsBuilders.add(new CustomerSet.Builder().id(i));
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
			CustomerSet.Builder targetSetBuilder = customerSetsBuilders.get(setId - 1);
			targetSetBuilder.addCustomer(targetCustomer);
			return this;
		}
		
		public Builder setK(int k) {
			if (k > 0)
				this.k = k;
			return this;
		}
		
		public Builder showGamma(boolean show) {
			this.showGamma = show;
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
			ArrayList<CustomerSet> customerSets = new ArrayList<CustomerSet>(customerSetsBuilders.size());
			for (CustomerSet.Builder builder : customerSetsBuilders) {
				CustomerSet set = builder.build();
				customerSets.add(set);
			}
			return new Instance(instanceName, depot, customers, customerSets, fleetSize, vehicleCapacity, k, showGamma);
		}
		
	}
	
	private final String name;
	private final Point depot;
	private final DistanceMatrix dmatrix;
	private final GammaSet gamma;
	private final ArrayList<Customer> customers;
	private final ArrayList<CustomerSet> sets;
	private final int fleet;
	private final int capacity;
	private final int numOfSets;
	private final int numOfCustomers;
	
	/**
	 * @return the gamma set
	 * @see GammaSet
	 */
	public GammaSet getGammaSet() {
		return gamma;
	}
	
	/**
	 * @return number of sets
	 */
	public int getNumberOfSets() {
		return numOfSets;
	}
	
	/**
	 * @return the distance matrix
	 * @see DistanceMatrix
	 */
	public DistanceMatrix getDistancematrix() {
		return dmatrix;
	}

	/**
	 * @return number of customers
	 */
	public int getNumberOfCustomers() {
		return numOfCustomers;
	}
	
	/**
	 * @return the customers
	 */
	public ArrayList<Customer> getCustomers() {
		return customers;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the depot
	 */
	public Point getDepot() {
		return depot;
	}

	/**
	 * @return the sets
	 */
	public ArrayList<CustomerSet> getSets() {
		return sets;
	}

	/**
	 * @return the fleet
	 */
	public int getFleet() {
		return fleet;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Instance constructor
	 * @param name - instance name
	 * @param depot - depot
	 * @param sets - customer sets
	 * @param vCount - vehicle count
	 * @param vCap - vehicle capacity
	 * @param k - gamma set size
	 */
	private Instance(String name, Point depot, ArrayList<Customer> customers, ArrayList<CustomerSet> sets, int vCount, int vCap, int k, boolean showGamma) {
		this.name = name;
		this.depot = depot;
		this.sets = sets;
		this.fleet = vCount;
		this.capacity = vCap;
		this.customers = customers;
		this.dmatrix = new DistanceMatrix(customers, depot); /* Must be before the gamma set initialization */
		
		/* Constance variables */
		numOfSets = sets.size();
		numOfCustomers = customers.size();
		
		/* Gamma set initialization */
		this.gamma = new GammaSet(this, k, showGamma);
	}
	
	/**
	 * Parse instance data from scanner
	 * @param sc - scanner
	 * @param k - gamma set size
	 * @param showGamma - display gamma set
	 * @return instance object
	 * @throws NoSuchElementException
	 * @throws IllegalStateException
	 * @throws InputMismatchException
	 */
	public static Instance parse(Scanner sc, int k, boolean showGamma) throws NoSuchElementException, IllegalStateException, InputMismatchException {
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
		builder.fleetSize(vCount);
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
		/* Customer positions */
		sc.next("NODE_COORD_SECTION");
		for (int i = 0; i < dimension; i++) {
			int id = sc.nextInt();
			int x = sc.nextInt();
			int y = sc.nextInt();
			builder.customerPosition(id, x, y);
		}
		/* Customer sets */
		sc.next("GVRP_SET_SECTION");
		for (int i = 0; i < setCount; i++) {
			int setId = sc.nextInt();
			int customerId = sc.nextInt();
			while (customerId != -1) {
				builder.customerSet(customerId, setId);
				customerId = sc.nextInt();
			}
		}
		/* Customer sets demands */
		sc.next("DEMAND_SECTION");
		for (int i = 0; i < setCount; i++) {
			int setId = sc.nextInt();
			int demand = sc.nextInt();
			builder.customerSetDemand(setId, demand);
		}
		/* Gamma set */
		builder.setK(k)
			.showGamma(showGamma);
		/* Build instance */
		return builder.build();
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("name = " + name);
		sj.add("fleet = " + fleet);
		sj.add("capacity = " + capacity);
		sj.add("depot = " + depot);
		sj.add("sets = ");
		if (sets != null) {
			for (CustomerSet set : sets)
				sj.add(set.toString());
		} else {
			sj.add("null");
		}
		return sj.toString();
	}
	
}
