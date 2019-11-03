package mssc;

import java.util.ArrayList;
import java.util.StringJoiner;

public class Solution extends ArrayList<Cluster> {

	private static final long serialVersionUID = -6755886026474677503L;
	
	Instance instance;
	
	public Solution(Instance instance) {
		this.instance = instance;
		for (int i = 0; i < instance.getNumOfClusters(); i++)
			add(new Cluster(instance.getDimension()));
			
	}
		
	/**
	 * A solution is considered valid when:
	 * <li>Every entity is assigned to exactly one cluster
	 * <li>No cluster is empty
	 * In mathematical terms, if ci is the i-th cluster and
	 * is a subset of E, which is the set of all entities:
	 * <li><code>sum(|ci|, i=1..n) = |E|</code> (disjoint)
	 * <li><code>union(ci, i=1..n) = E</code> (complete)
	 * <li><code>prod(|ci|, i=1..n) != 0</code> (non-empty)
	 * @return if the solution is valid or not
	 */
	public boolean isValid(boolean isVerbose) {
		int elementCount = 0;
		Cluster elementSet = new Cluster(instance.getDimension());
		for (Cluster c : this) {
			int size = c.size();
			if (size == 0) {
				if (isVerbose) System.out.println("Empty clusters");
				return false;
			}
			elementCount += size;
			elementSet.addAll(c);
		}
		if (elementCount != instance.getNumOfEntities()) {
			System.out.println("Clusters with intersections");
			return false;
		}
		if (elementSet.size() != instance.getNumOfEntities()) {
			System.out.println("Missing points from clusters");
			return false;
		}
		return true;
	}
	
	public double getCost() {
		return stream()
				.mapToDouble((c) -> c.sumOfSquares())
				.sum();
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		for (int i = 0; i < size(); i++)
			sj.add("c" + (i+1) + " = " + get(i));
		return sj.toString();
	}
	
}
