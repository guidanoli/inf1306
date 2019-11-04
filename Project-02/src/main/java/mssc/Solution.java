package mssc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Solution extends HashMap<Point, Point> {

	private static final long serialVersionUID = -6755886026474677503L;
	
	Instance instance;
	ArrayList<Point> clusters = new ArrayList<>();
	
	public Solution(Instance instance) {
		this.instance = instance;
		for (int i = 0; i < instance.getNumOfClusters(); i++)
			clusters.add(new Point(i+1, instance.getDimension()));
			
	}
	
	public ArrayList<Point> getClusters() {
		return clusters;
	}
	
	/**
	 * <p>A solution is considered valid when:
	 * <li>Every entity is assigned to exactly one cluster
	 * <li>Every cluster has at least one entity
	 * <li>No cluster is empty
	 * <p>In mathematical terms, if ci is the i-th cluster and
	 * is a subset of E, which is the set of all entities:
	 * <li><code>sum(|ci|, i=1..n) = |E|</code> (disjoint) - HashMap guarantees this
	 * <li><code>union(ci, i=1..n) = E</code> (complete)
	 * <li><code>prod(|ci|, i=1..n) != 0</code> (non-empty)
	 * @return if the solution is valid or not
	 */
	public boolean isValid(boolean isVerbose) {
		if (size() != instance.getNumOfEntities()) {
			if (isVerbose) System.out.println("Missing points from clusters");
			return false;
		}
		for (Point c : clusters) {
			if (!containsValue(c)) {
				if (isVerbose) System.out.println("Empty clusters");
				return false;
			}
		}
		return true;
	}
	
	public double getCost() {
		return entrySet().parallelStream().collect(Collectors.summingDouble((e) ->
			e.getKey().getSumOfSquaresTo(e.getValue())));
	}
	
	/**
	 * K-means consists on switching between two procedures:
	 * <li>Assigning each entity to its closest cluster
	 * <li>Aligning the cluster to the centroid of its entities
	 * Until stability is achieved.
	 */
	public void kMeans() {
		boolean changedState;
		do {
			changedState = false; /* Assume unchanged */
			
			/* Assigning each entity to its closest cluster */
			for (Point e : keySet()) {
				Point ec = clusters.get(0);
				double dist = ec.getSumOfSquaresTo(e);
				for (Point c : clusters) {
					double cDist = c.getSumOfSquaresTo(e); 
					if (cDist < dist) {
						dist = cDist;
						ec = c;
					}
				}
				if (get(e) != ec) {
					put(e, ec);
					changedState = true;
				}
			}
			
			if (!changedState)
				break; /* If clusters didn't change, don't even bother... */
			
			/* Aligning the cluster to the centroid of its entities */
			int [] clusterSizes = new int[clusters.size()+1];
					
			for (Point c : clusters) {
				c.replaceAll((d) -> 0.0); /* Zero the clusters */
				clusterSizes[c.getId()] = 0;
			}
					
			for (Point e : keySet()) {
				Point c = get(e);
				for (int i = 0; i < instance.getDimension(); i++)
					c.set(i, e.get(i) + c.get(i)); /* Sums itself to the cluster */
				++clusterSizes[c.getId()];
			}
			
			for (Point c : clusters) {
				int cSize = clusterSizes[c.getId()];
				for (int i = 0; i < instance.getDimension(); i++)
					c.set(i, c.get(i) / cSize);
			}
		} while (changedState);
	}
		
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		TreeMap<Point, HashSet<Point>> clusterMap =
				new TreeMap<Point, HashSet<Point>>((p1, p2) -> p1.getId() - p2.getId());
		for (Point c : clusters) clusterMap.put(c, new HashSet<Point>());
		forEach((e, c) -> clusterMap.get(c).add(e));
		clusterMap.forEach((c, es) -> {
			StringJoiner csj = new StringJoiner(", ");
			for (Point p : es) {
				if (csj.toString().length() > 70) {
					csj.add("...");
					break;
				}
				csj.add(p.toShorterString());
			}
			double cost = 0.0;
			for (Point e : es) cost += e.getSumOfSquaresTo(c);
			sj.add("c" + c.getId() + " = {" + csj.toString() + "} cost = " + cost);
		});
		return sj.toString();
	}
	
}
