package mssc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import extern.HungarianAlgorithm;
import mssc.construction.SolutionFactory;

public class Population extends ArrayList<Solution> {

	private static final long serialVersionUID = 387605027565896505L;

	Random random = new Random();
	Instance instance;
	int maxsize, minsize;
	HashMap<Solution, Double> costMap = new HashMap<>();
	
	public Population(Instance instance, int minsize, int maxsize,
									String constructiveMetaheuristic) {
		super(minsize);
		this.instance = instance;
		this.maxsize = maxsize;
		this.minsize = minsize;
		for (int i = 0; i < minsize; i++) {
			Solution s = SolutionFactory.construct(instance, constructiveMetaheuristic);
			costMap.put(s, s.getCost());
			add(s);
		}
	}
	
	public void nextGeneration(int tournamentK) {
		final int parentCount = size();
		final int numOfClusters = instance.getNumOfClusters();
		final int matingPoolSize = parentCount / 2;
		/* PARENT SELECTION THROUGH BINARY TOURNAMENT */
		ArrayList<Solution> matingPool = new ArrayList<>(matingPoolSize);
		for (int i = 0; i < matingPoolSize; i++) {
			Solution winningSolution = get(random.nextInt(parentCount));
			for (int j = 1; j < tournamentK; j++) {
				Solution disputingSolution = get(random.nextInt(parentCount));
				if (costMap.get(disputingSolution) < costMap.get(winningSolution))
					winningSolution = disputingSolution;
			}
			matingPool.add(winningSolution);
		}
		/* BREEDING */
		for (int i = 0; i < matingPoolSize; i += 2) {
			Solution firstParent = matingPool.get(i);
			Solution secondParent = matingPool.get(i+1);
			Solution offspring = new Solution(instance);
			double [][] costMatrix = new double[numOfClusters][numOfClusters];
			ArrayList<Point> firstParentClusters = firstParent.getClusters();
			ArrayList<Point> secondParentClusters = secondParent.getClusters();
			for (int j = 0; j < numOfClusters; j++) {
				for (int k = 0; k < numOfClusters; k++) {
					Point c1 = firstParentClusters.get(j);
					Point c2 = secondParentClusters.get(k);
					costMatrix[j][k] = Math.sqrt(c1.getSumOfSquaresTo(c2));
				}
			}
			/* CROSSOVER */
			HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
			int [] matching = hungarian.execute();
			ArrayList<Point> offsprintClusters = offspring.getClusters();
			for (int j = 0; j < numOfClusters; j++) {
				Point c1 = firstParentClusters.get(j);
				Point c2 = secondParentClusters.get(matching[j]);
				/* Bernoulli with p = 0.5 */
				Point chosenCluster = random.nextBoolean() ? c1 : c2;
				offsprintClusters.get(j).copyFrom(chosenCluster);
			}
			offspring.decodeFromClusterPositions();
			/* MUTATION */
			Point randomCluster = offsprintClusters.get(random.nextInt(numOfClusters));
			offspring.unassignCluster(randomCluster);
			offspring.decodeFromClusterPositions();
			Point randomPoint = offspring.getRandomEntityDistantFromCenter(random);
			while (true) {
				boolean coincides = false;
				for (Point cluster : offsprintClusters) {
					if (cluster.equals(randomPoint)) {
						randomPoint = offspring.getRandomEntityDistantFromCenter(random);
						coincides = true;
					}
				}
				if (!coincides)
					break;
			}
			randomCluster.copyFrom(randomPoint);
			offspring.decodeFromClusterPositions();
			/* LOCAL SEARCH */
			offspring.kMeans();
			
			costMap.put(offspring, offspring.getCost());
			add(offspring);
		}
		if (size() > maxsize) {
			/* ELIMINATION OF CLONES */
			HashSet<Integer> cloneSolutionIndexes = new HashSet<>();
			for (int i = 0; i < size(); i++) {
				for (int j = i+1; j < size() && size() - cloneSolutionIndexes.size() > minsize; j++) {
					Solution s1 = get(i), s2 = get(j);
					if (s1.equals(s2)) {
						cloneSolutionIndexes.add(i);
						break;
					}
				}
			}
			for (int index : cloneSolutionIndexes) {
				System.out.println("Removing "+index);
				remove(index);
			}
		}
	}
	
	public boolean isValid(boolean isVerbose) {
		int size = size();
		if (size < minsize) {
			if (isVerbose)
				System.out.println("Population size is too low!");
			return false;
		}
		if (size > maxsize) {
			if (isVerbose)
				System.out.println("Population size is too high!");
			return false;
		}
		for (Solution s : this) if (!s.isValid(isVerbose)) return false; 
		return true;
	}
	
	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}
	
	@Override
	public String toString() {
		double averageFitness = 0.0;
		for (Solution s : this)
			averageFitness += costMap.get(s);
		averageFitness /= size();
		double fitnessVariance = 0.0;
		for (Solution s : this)
			fitnessVariance += Math.pow(costMap.get(s) - averageFitness, 2.0);
		fitnessVariance /= size();
		return String.format("Average = %f\nVariance = %f", averageFitness, fitnessVariance);
	}
	
}
