package gvrp;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import gvrp.analysis.BestKnownSolutions;
import gvrp.analysis.AnalyticalValuesLabels;
import gvrp.analysis.AnalyticalValuesList;
import gvrp.construction.SolutionFactory;
import gvrp.search.IteratedLocalSearch;
import gvrp.search.LocalSearch;

public class Main {
	
	@Parameter(names = "-mode", description = "Input mode")
	String mode = "auto";
	
	@Parameter(names = "-ifile", description = "Input file")
	String inputFilePath = "all.txt";

	@Parameter(names = {"-persist", "-persistant"}, description = "Persist parsing all input files")
	boolean isPersistant = false;

	@Parameter(names = "-idir", description = "Instance files directory")
	String instanceDirPath = "data/GVRP3";

	@Parameter(names = { "-v", "-verbose" }, description = "Verbosity")
	boolean isVerbose = false;

	@Parameter(names = "-bks", description = "Best Known Solution file")
	String bksPath = "data/bks.txt";
	
	@Parameter(names = {"-iinfo"}, description = "Instance information")
	boolean instanceInfo = false;
	
	@Parameter(names = {"-isinfo"}, description = "Initial Solution Info")
	boolean initialSolutionInfo = false;
	
	@Parameter(names = "-dmatrix", description = "Display distance matrix")
	boolean displaysDistanceMatrix = false;
	
	@Parameter(names = "-constructive", description = "Constructive metaheuristic")
	String constructiveMetaheuristic = "greedy";
	
	@DynamicParameter(names = {"-M", "-A"}, description = "Get analytical data after simulations")
	Map<String, String> meanValues = new HashMap<>();
	
	@Parameter(names = {"-help", "--help"}, description = "Help with application parameters", help = true)
	boolean help = false;
	
	@Parameter(names = {"-seed"}, description = "RNG seed")
	long seed = 0;
	
	@Parameter(names = {"-gammak"}, description = "Closest neighbours size", validateWith = PositiveInteger.class)
	int gammak = 20;
	
	@Parameter(names = {"-gamma"}, description = "Display gamma set")
	boolean showgamma = false;
	
	@Parameter(names = {"-seconds"}, description = "Maximum time taken by each instance (in seconds)", validateWith = PositiveInteger.class)
	int secondsPerInstance = 1;
	
	AnalyticalValuesList meanValuesList = new AnalyticalValuesList();
	BestKnownSolutions bestKnownSolutions;
		
	/**
	 * Runs the GVRP solver according to parameters parsed in command line
	 * 
	 * @param args - command line arguments (see README.md)
	 */
	public static void main(String[] args) {
		Main main = new Main();
		JCommander jcommander = JCommander
				.newBuilder()
				.addObject(main)
				.build();
		jcommander.parse(args);
		if (main.help == true) {
			jcommander.usage();
			return;
		}
		main.run();
		if (!main.meanValuesList.isEmpty()) {
			System.out.println("Analytics:");
			TreeSet<AnalyticalValuesLabels> sortedMeanValues = new TreeSet<>((sym1,sym2) -> {
				return sym1.getPosition()-sym2.getPosition();
			});
			main.meanValuesList.forEach((k,v)->sortedMeanValues.add(AnalyticalValuesLabels.valueOf(k)));
			for (AnalyticalValuesLabels symbol : sortedMeanValues) {
				Double analyticalValue;
				if (symbol.takesTheMean())
					analyticalValue = main.meanValuesList.getMean(symbol.name());
				else
					analyticalValue = main.meanValuesList.getSum(symbol.name());
				System.out.println(symbol.convert(analyticalValue) + "\t" + symbol.getLabel());
			}
		}
	}

	/**
	 * Read instance files and run the solver/debugger, depending on the arguments
	 * parsed to the application.
	 */
	public void run() {
		try {
			File bksFile = new File(bksPath);
			Scanner bksScanner = new Scanner(bksFile);
			bestKnownSolutions = new BestKnownSolutions(bksScanner);
			bksScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		if (mode.equals("manual")) {
			/* No input path provided will pop up JFileChooser */
			File instanceFile = promptForFolder();
			if (!solveInstance(instanceFile))
				return;
		} else if(mode.equals("auto")) {
			/* Parse input file */
			File inputFile = new File(inputFilePath);
			Scanner sc = null;
			try {
				sc = new Scanner(inputFile);
				while (sc.hasNextLine()) {
					String instanceFilePath = Paths.get(instanceDirPath, sc.nextLine()).toString();
					File instanceFile = new File(instanceFilePath);
					if (!solveInstance(instanceFile)) {
						/* -persist will continue parsing */
						if (!isPersistant)
							break;
					}
					System.out.println();
				}
				sc.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
		} else {
			System.out.println(">>> Invalid mode '" + mode + "'");
		}
	}

	/**
	 * Solves GVRP instance
	 * 
	 * @param instanceFile - file with instance data
	 * @return {@code true} if no errors occurred, {@code false} otherwise
	 */
	public boolean solveInstance(File instanceFile) {
		/* Null file is always invalid */
		if (instanceFile == null) {
			return false;
		}

		System.out.println(instanceFile);

		/* Try to create Scanner object */
		Scanner sc = null;
		try {
			sc = new Scanner(instanceFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		/* Try to parse instance file */
		Instance instance = null;
		try {
			
			instance = Instance.parse(sc, gammak, showgamma);
		} catch (NoSuchElementException nsee) {
			nsee.printStackTrace();
			return false;
		} catch (IllegalStateException ilse) {
			ilse.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (instanceInfo)
			System.out.println(instance);

		if (displaysDistanceMatrix)
			System.out.println(instance.getDistancematrix());
		
		if (isVerbose)
			System.out.println("Constructing initial solution with '" + constructiveMetaheuristic +
					"' constructive metaheuristic");
		
		Solution initialSolution = SolutionFactory.construct(instance, constructiveMetaheuristic);
		
		if (initialSolution == null) {
			System.out.println(String.format(">>> '%s' is not a valid constructive metaheuristic.",
					constructiveMetaheuristic));
			return false;
		}

		if (initialSolutionInfo)
			System.out.println(initialSolution);
		
		if (!initialSolution.isValid(isVerbose)) {
			System.out.println(">>> Initial solution is invalid.");
			return false;
		}

		
		int initialCost = initialSolution.getCost();
		double initialFraction = bestKnownSolutions.getBKSFraction(initialSolution);
		if (meanValues.containsKey("iscost")) {
			meanValuesList.addValueToList("iscost", initialFraction);
			if (isVerbose)
				System.out.printf("Initial cost: %d %s\n", initialCost, formatBKSComparison(initialFraction));
		}
		
		
		long elapsedNanos = System.nanoTime();
		Solution currentSolution = new Solution(initialSolution);
		elapsedNanos = System.nanoTime() - elapsedNanos;
		if (meanValues.containsKey("sclonetime")) {
			meanValuesList.addValueToList("sclonetime", (double) elapsedNanos);
			if (isVerbose)
				System.out.println("Nanoseconds required to clone solution: " + elapsedNanos);
		}
		
		/* First, find the shortest path in each route */
		for (Route r : currentSolution)
			r.findShortestPath(currentSolution.getInstance().getDistancematrix());
		
		int firstSPCost = currentSolution.getCost();
		double firstSPFraction = bestKnownSolutions.getBKSFraction(currentSolution);
		if (meanValues.containsKey("fspcost")) {
			meanValuesList.addValueToList("fspcost", firstSPFraction);
			if (isVerbose)
				System.out.printf("Cost after first local search: %d %s\n", firstSPCost, formatBKSComparison(firstSPFraction));
		}
		
		LocalSearch localSearch = new LocalSearch(seed);
		int improvementCount = localSearch.findLocalMinimum(currentSolution);
		
		if (improvementCount == 0) {
			if (isVerbose)
				System.out.println("Could not find local minima.");
			if (meanValues.containsKey("improvement"))
				meanValuesList.addValueToList("improvement", 0.0d);
		} else {
			double fraction = bestKnownSolutions.getBKSFraction(currentSolution);
			double improvement = initialFraction - fraction;
			if (meanValues.containsKey("improvement"))
				meanValuesList.addValueToList("improvement", improvement);
			if (isVerbose)
				System.out.printf("Found local minima (%d improvements --- %.4f%% of improvement)\n", improvementCount, improvement*100);
		}
		
		IteratedLocalSearch ils = new IteratedLocalSearch(seed);
		
		final long t0 = System.nanoTime();
		ils.explore(initialSolution, (s) -> {
			if (bestKnownSolutions.getBKSFraction(s) <= 0.05) return false;
			long diff = System.nanoTime() - t0;
			return diff < secondsPerInstance * 1000000000L; /* 1 second */
		});
		
		int finalCost = currentSolution.getCost();
		double finalFraction = bestKnownSolutions.getBKSFraction(currentSolution);
		if (meanValues.containsKey("fscost")) {
			meanValuesList.addValueToList("fscost", finalFraction);
			if (isVerbose)
				System.out.printf("Final cost: %d %s\n", finalCost, formatBKSComparison(finalFraction));
		}
		
		return true;
	}

	public String formatBKSComparison(double fraction) {
		if (fraction == 0) {
			if (meanValues.containsKey("optcnt"))
				meanValuesList.addValueToList("optcnt", 1.0);
			return "(Optimal solution)";
		}
		return String.format("(%.2f%% from optimal solution)", fraction*100);
	}
	
	public File promptForFolder() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		/* Does not work for JAR -- will start at user home */
		fc.setCurrentDirectory(new File(instanceDirPath));
		fc.setFileFilter(new FileNameExtensionFilter("GVRP instance", "gvrp"));

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}

		return null;
	}

}
