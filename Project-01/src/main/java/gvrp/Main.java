package gvrp;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import gvrp.analysis.BKS;
import gvrp.analysis.MeanValuesLabels;
import gvrp.analysis.MeanValuesList;
import gvrp.construction.SolutionFactory;
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
	
	@DynamicParameter(names = "-M", description = "Get mean of certain attribute")
	Map<String, String> meanValues = new HashMap<>();
	
	@Parameter(names = {"-help", "--help"}, description = "Help with application parameters", help = true)
	boolean help = false;
		
	@Parameter(names = {"-niter"}, description = "Number of iterations per local search", validateWith = PositiveInteger.class)
	int numOfIterations = 1000000;
	
	@Parameter(names = {"-seed"}, description = "RNG seed")
	long seed = 0;
	
	MeanValuesList meanValuesList = new MeanValuesList();
	BKS bestKnownSolutions;
		
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
			System.out.println("\nMean values:");
			main.meanValuesList.forEach((k,v) -> {
				MeanValuesLabels symbol = MeanValuesLabels.valueOf(k);
				Double meanValue = main.meanValuesList.getMean(k);
				System.out.println(symbol.getLabel() + "\t" + symbol.convert(meanValue));			
			});
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
			bestKnownSolutions = new BKS(bksScanner);
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
			instance = Instance.parse(sc);
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
			meanValuesList.addValueToMean("iscost", initialFraction);
			if (isVerbose)
				System.out.printf("Initial cost: %d (%.2f%% from optimal solution)\n", initialCost, initialFraction*100);
		}
		
		
		long elapsedNanos = System.nanoTime();
		Solution currentSolution = new Solution(initialSolution);
		elapsedNanos = System.nanoTime() - elapsedNanos;
		if (meanValues.containsKey("sclonetime")) {
			meanValuesList.addValueToMean("sclonetime", (double) elapsedNanos);
			if (isVerbose)
				System.out.println("Nanoseconds required to clone solution: " + elapsedNanos);
		}
		
		LocalSearch localSearch = new LocalSearch(currentSolution, seed);
		int improvementCount = localSearch.findLocalMinimum((s,i) -> i < numOfIterations && bestKnownSolutions.getBKSFraction(s) > 0.05);
		
		Solution finalSolution = localSearch.getBestSolution();
		
		if (improvementCount == 0) {
			if (isVerbose)
				System.out.println("Could not find local minima.");
			if (meanValues.containsKey("improvement"))
				meanValuesList.addValueToMean("improvement", 0.0d);
		} else {
			double fraction = bestKnownSolutions.getBKSFraction(finalSolution);
			double improvement = initialFraction - fraction;
			if (meanValues.containsKey("improvement"))
				meanValuesList.addValueToMean("improvement", improvement);
			if (isVerbose)
				System.out.printf("Found local minima (%d improvements --- %.4f%% of improvement)\n", improvementCount, improvement*100);
		}
		
		int finalCost = currentSolution.getCost();
		double finalFraction = bestKnownSolutions.getBKSFraction(currentSolution);
		if (meanValues.containsKey("fscost")) {
			meanValuesList.addValueToMean("fscost", finalFraction);
			if (isVerbose)
				System.out.printf("Final cost: %d (%.2f%% from optimal solution)\n", finalCost, finalFraction*100);
		}
		
		return true;
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
