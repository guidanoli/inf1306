package gvrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.TreeSet;

/**
 * The gamma set is a set that contains the closest k vertices
 * of a given vertex set in a graph
 * 
 * @author guidanoli
 *
 */
public class GammaSet {
	
	HashMap<Customer, ArrayList<Customer>> map = new HashMap<>();
	
	public GammaSet(Instance instance, int k, boolean showGamma) {
		ArrayList<Customer> customers = instance.getCustomers();
		int n = instance.getNumberOfCustomers();
		DistanceMatrix dmatrix = instance.getDistancematrix();
		for (int i = 0; i < n; i++) {
			/* For a given set i
			 */
			final Customer ci = customers.get(i);
			TreeSet<Customer> heap = new TreeSet<>((c1,c2)-> dmatrix.getDistanceBetween(ci, c1)<dmatrix.getDistanceBetween(ci, c2)?-1:1);
			for (int j = 0; j < n; j++) {
				/* Add all neighbouring sets to a heap sorted by the
				 * distance to the set i
				 */
				if (i == j) continue;
				heap.add(customers.get(j));
			}
			ArrayList<Customer> kClosestCustomers = new ArrayList<>(k);
			for (int j = 0; j < k; j++) {
				/* Add the k closest to the map
				 */
				if (heap.isEmpty()) break;
				Customer cj = heap.pollFirst();
				kClosestCustomers.add(cj);
			}
			if (showGamma) {
				StringJoiner sj = new StringJoiner(", ");
				for (Customer customer : kClosestCustomers)
					sj.add(customer.toCompactString());
				System.out.println("map["+customers.get(i).toCompactString()+"]: ["+sj.toString()+"]");
			}
			map.put(customers.get(i), kClosestCustomers);
		}
	}
	
	/**
	 * Obtains the k closest neighbours of a given customer
	 * @param reference - reference customer
	 * @return list of the k closest customers ordered by proximity
	 */
	public ArrayList<Customer> getClosestNeighbours(Customer reference) {
		return map.get(reference);
	}
	
}
