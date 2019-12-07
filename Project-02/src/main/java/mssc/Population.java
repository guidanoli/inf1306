package mssc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.TreeSet;

import extern.HungarianAlgorithm;
import mssc.construction.SolutionFactory;

public class Population extends ArrayList<Solution> {

	private static final long serialVersionUID = 387605027565896505L;

	Random random = new Random();
	Instance instance;
	int maxsize, minsize;
	long generation = 0;
	int solutionIdCounter = 0;
	HashMap<Solution, Double> costMap = new HashMap<>();
	
	public Population(Instance instance, int minsize, int maxsize,
									String constructiveMetaheuristic) {
		super(minsize);
		this.instance = instance;
		this.maxsize = maxsize;
		this.minsize = minsize;
		for (int i = 0; i < maxsize; i++) {
			Solution s = SolutionFactory.construct(instance, solutionIdCounter, constructiveMetaheuristic);
			++solutionIdCounter;
			costMap.put(s, s.getCost());
			add(s);
		}
	}
	
	public void nextGeneration() {
		final int parentCount = size();
		final int numOfClusters = instance.getNumOfClusters();
		final int matingPoolSize = 2;
		/* PARENT SELECTION THROUGH BINARY TOURNAMENT */
		ArrayList<Solution> matingPool = new ArrayList<>(matingPoolSize);
		for (int i = 0; i < matingPoolSize; i++) {
			Solution s1 = get(random.nextInt(parentCount));
			Solution s2 = get(random.nextInt(parentCount));
			Solution winner = costMap.get(s1) < costMap.get(s2) ? s1 : s2;
			matingPool.add(winner);
		}
		/* BREEDING */
		for (int i = 1; i < matingPoolSize; i += 2) {
			Solution firstParent = matingPool.get(i-1);
			Solution secondParent = matingPool.get(i);
			Solution offspring = new Solution(instance, solutionIdCounter);
			++solutionIdCounter;
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
			ArrayList<Point> offspringClusters = offspring.getClusters();
			for (int j = 0; j < numOfClusters; j++) {
				Point c1 = firstParentClusters.get(j);
				Point c2 = secondParentClusters.get(matching[j]);
				/* Bernoulli with p = 0.5 */
				Point chosenCluster = random.nextBoolean() ? c1 : c2;
				offspringClusters.get(j).copyFrom(chosenCluster);
			}
			offspring.decodeFromClusterPositions();
			/* MUTATION */
			Point randomCluster = offspringClusters.get(random.nextInt(numOfClusters));
			offspring.decodeFromClusterPositionsIgnoring(randomCluster);
			Point randomPoint = offspring.getRandomEntityDistantFromCenter(random);
			while (true) {
				boolean coincides = false;
				for (Point cluster : offspringClusters) {
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
			ArrayList<Solution> cloneSolutions = new ArrayList<>();
			cloneSolutionIndexes.forEach((i) -> cloneSolutions.add(get(i)));
			cloneSolutions.forEach((s) -> {
				remove(s);
				costMap.remove(s);
			});
			/* ELIMINATION OF THE WORST */
			int currentSize = size();
			TreeSet<Solution> ranking = new TreeSet<>((s1,s2) -> s1.getCost() > s2.getCost() ? -1 : 1);
			ranking.addAll(this);
			ArrayList<Solution> worstSolutions = new ArrayList<>(currentSize - minsize);
			for (Solution sol : ranking) {
				if (currentSize == minsize) break;
				worstSolutions.add(sol);
				costMap.remove(sol);
				--currentSize;
			}
			removeAll(worstSolutions);
		}
		++generation;
	}
	
	public boolean isValid(boolean isVerbose) {
		int size = size();
		if (size < minsize) {
			if (isVerbose)
				System.out.printf("Population size is too low (%d < %d)!\n", size(), minsize);
			return false;
		}
		if (size > maxsize) {
			if (isVerbose)
				System.out.printf("Population size is too high (%d > %d)\n!", size(), maxsize);
			return false;
		}
		for (Solution s : this) {
			if (!s.isValid(isVerbose)) {
				System.out.println("Invalid solution #" + s.id + "!");
				return false; 
			}
		}
		return true;
	}
	
	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}
	
	public long getGenerationCount() {
		return generation;
	}
	
	public double getAverageFitness() {
		double averageFitness = 0.0;
		for (Solution s : this) {
			if (!costMap.containsKey(s)) costMap.put(s, s.getCost());
			averageFitness += costMap.get(s);
		}
		averageFitness /= size();
		return averageFitness;
	}
	
	public Solution getBestSolution() {
		Optional<Entry<Solution, Double>> minCostEntry = costMap.entrySet()
				.stream()
				.min(Comparator.comparingDouble(Map.Entry::getValue));
		return minCostEntry.get().getKey();
	}
	
	public double getBestSolutionFitness() {
		return costMap.get(getBestSolution());
	}
	
	@Override
	public String toString() {
		double averageFitness = getAverageFitness();
		double bestSolutionCost = getBestSolutionFitness();
		return String.format("Generation: %d\tPopulation size: %d\tAverage = %f\tBest = %f",
				generation, size(), averageFitness, bestSolutionCost);
	}
	
}
