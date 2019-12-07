package mssc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Solution extends HashMap<Point, Point> {

	private static final long serialVersionUID = -6755886026474677503L;
	
	Instance instance;
	int id;
	ArrayList<Point> clusters = new ArrayList<>();
	
	public Solution(Instance instance, int id) {
		this.instance = instance;
		this.id = id;
		for (int i = 0; i < instance.getNumOfClusters(); i++)
			clusters.add(new Point(i+1, "c", instance.getDimension()));
			
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
				if (isVerbose) {
					System.out.println(this);
					System.out.println("Cluster "+c+" is empty!");
				}
				return false;
			}
		}
		return true;
	}
	
	public double getCost() {
		return entrySet().parallelStream().collect(Collectors.summingDouble((e) ->
			e.getKey().getSumOfSquaresTo(e.getValue())));
	}
	
	public void decodeFromClusterPositions() {
		/* Assigning each entity to its closest cluster */
		for (Point e : instance.getEntities()) {
			Point ec = clusters.get(0);
			double dist = ec.getSumOfSquaresTo(e);
			for (Point c : clusters) {
				double cDist = c.getSumOfSquaresTo(e);
				if (cDist < dist) {
					dist = cDist;
					ec = c;
				}
			}
			put(e, ec);
		}
	}
	
	public void decodeFromClusterPositionsIgnoring(Point cluster) {
		/* Assigning each entity to its closest cluster */
		Point firstC = clusters.get(0).equals(cluster) ? clusters.get(1) : clusters.get(0);
		for (Point e : instance.getEntities()) {
			Point ec = firstC;
			double dist = ec.getSumOfSquaresTo(e);
			for (Point c : clusters) {
				if (c.equals(cluster)) continue;
				double cDist = c.getSumOfSquaresTo(e);
				if (cDist < dist) {
					dist = cDist;
					ec = c;
				}
			}
			put(e, ec);
		}
	}
		
	public Point getRandomEntityDistantFromCenter(Random rng) {
		double distSum = 0.0;
		for (Point e : instance.getEntities())
			distSum += Math.sqrt(get(e).getSumOfSquaresTo(e));
		double Fp = 0.0;
		double u = rng.nextDouble();
		Point lastEntity = null;
		for (Point e : instance.getEntities()) {
			double p =  Math.sqrt(get(e).getSumOfSquaresTo(e)) / distSum;
			if (p > 0.0) {
				Fp += p;
				if (u <= Fp) return e;
				lastEntity = e;
			}
		}
		/* In case the sum of p don't add up to 1.0 exactly */
		return lastEntity;
	}
	
	public boolean canBeRemovedFromCluster(Point e, Point newCluster) {
		Point c = get(e);
		for (Point pe : keySet()) {
			if (get(pe).equals(c) && !pe.equals(e))
				return true;
		}
		return false;
	}
	
	public void unassignCluster(Point c) {
		for (int i = 0; i < c.size(); i++)
			c.set(i, Double.MAX_VALUE);
	}
	
	/**
	 * K-means consists on switching between two procedures:
	 * <li>Assigning each entity to its closest cluster
	 * <li>Aligning the cluster to the centroid of its entities
	 * Until stability is achieved.
	 */
	public void kMeans() {
		HashMap<Point, Point> changedEntities = new HashMap<>();
		int [] clusterSizes = new int[clusters.size()+1];
		Arrays.fill(clusterSizes, 0);
		for (Point e : instance.getEntities())
			++clusterSizes[get(e).getId()];
		
		/* Finding centroids first */
		for (Point c : clusters)
		c.replaceAll((d) -> 0.0); /* Zero the clusters */
			
		for (Point e : instance.getEntities()) {
			Point c = get(e);
			int cSize = clusterSizes[c.getId()];
			for (int i = 0; i < instance.getDimension(); i++)
				c.set(i, c.get(i) + e.get(i) / cSize); /* Sums itself to the cluster */
		}
				
		do {
			changedEntities.clear(); /* Forgets changed entities */
			
			/* Assigning each entity to its closest cluster */
			for (Point e : instance.getEntities()) {
				Point ec = clusters.get(0);
				double dist = ec.getSumOfSquaresTo(e);
				for (Point c : clusters) {
					double cDist = c.getSumOfSquaresTo(e); 
					if (cDist < dist) {
						dist = cDist;
						ec = c;
					}
				}
				Point prevC = get(e);
				if (prevC != ec) {
					put(e, ec);
					changedEntities.put(e, prevC);
				}
			}
			
			if (changedEntities.isEmpty())
				break; /* If clusters didn't change, don't even bother... */

			/* Aligning the cluster to the centroid of its entities */
			changedEntities.forEach((e,iCluster) -> {
				Point fCluster = get(e);
				int fSize = clusterSizes[fCluster.getId()];
				int iSize = clusterSizes[iCluster.getId()];
				for (int i = 0; i < instance.getDimension(); i++) {
					double ei = e.get(i);
					iCluster.set(i, (iCluster.get(i) * iSize - ei) / (iSize - 1));
					fCluster.set(i, (fCluster.get(i) * fSize + ei) / (fSize + 1));
				}
				++clusterSizes[fCluster.getId()];
				--clusterSizes[iCluster.getId()];
			});
						
		} while (!changedEntities.isEmpty());
		
		while (true) {
			boolean hasEmptyCluster = false;
			for (Point c : clusters) {
				if (!containsValue(c)) {
					hasEmptyCluster = true;
					Random random = new Random(Objects.hash(c.getId(), id));
					Point randomPoint = getRandomEntityDistantFromCenter(random);
					while (clusterSizes[get(randomPoint).getId()] == 1)
						randomPoint = getRandomEntityDistantFromCenter(random);
					--clusterSizes[get(randomPoint).getId()];
					++clusterSizes[c.getId()];
					c.copyFrom(randomPoint);
					decodeFromClusterPositions();
				}
			}
			if (!hasEmptyCluster) break;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Solution) {
			Solution sol = (Solution) o;
			if (sol.instance != instance) return false;
			for (Point myCluster : clusters) {
				boolean contains = false;
				for (Point solCluster : sol.clusters) {
					if (myCluster.equals(solCluster)) {
						contains = true;
						break;
					}
				}
				if (!contains) return false;
			}
			return true;
		}
		return false;
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
	
	@Override
	public int hashCode() {
		return id;
	}
	
}
