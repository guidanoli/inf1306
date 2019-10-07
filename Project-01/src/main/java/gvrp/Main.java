package gvrp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import gvrp.analysis.*;
import gvrp.construction.*;
import gvrp.jcommander.*;
import gvrp.search.*;

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
	
	@Parameter(names = {"-gammak"}, description = "Closest neighbours set maximum size", validateWith = PositiveInteger.class)
	int gammak = 20;
	
	@Parameter(names = {"-gamma"}, description = "Display gamma set")
	boolean showgamma = false;
	
	@Parameter(names = {"-seconds"}, description = "Maximum time taken by each instance (in seconds)", validateWith = PositiveDouble.class)
	double secondsPerInstance = 10.0;
	
	@Parameter(names = {"-perturbation"}, description = "Perturbation magnitude", validateWith = ZeroToOneDouble.class)
	double IlsPertubationFraction = 0.25;
	
	@Parameter(names = {"-threshold"}, description = "Solution quality threshold (compared to BKS)", validateWith = ZeroToOneDouble.class)
	double qualityThreshold = 0.0;
	
	@Parameter(names = {"-noise"}, description = "Save all data points (not just improving ones - may be noisy)")
	boolean saveNoise = false;
	
	@Parameter(names = {"-live"}, description = "Print data points live (might interfeer simulation)")
	boolean livePrinting = false;
	
	@Parameter(names = {"-csvdir"}, description = "Path to where .csv files are saved")
	File CSVdirectory = new File("data/results");
	
	@Parameter(names = {"-csv"}, description =  "Save results in a .csv file in the -csvdir directory")
	boolean saveCSV = false;
	
	@Parameter(names = {"-csvts"}, description = "Save improvements time stamps in a .csv file in the -csvdir directory")
	boolean saveTimeSteps = false;
	
	AnalyticalValuesList meanValuesList = new AnalyticalValuesList();
	BestKnownSolutions bestKnownSolutions;
	UtilsCSV csv, csvTimeStamps;
	
	String [] csvColumns = {
		"Instance name",
		"Instance size",
		"Initial solution %",
		"Final solution %",
		"Last iteration timestep (ms)"
	};
	
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
			if (main.saveCSV)
				main.csv.writeLine();
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
				String value = symbol.convert(analyticalValue);
				String label = symbol.getLabel();
				if (main.saveCSV)
					main.csv.writeLine(label, value);
				System.out.println(value + "\t" + label);
			}
			if (main.saveCSV) {
				try {
					main.csv.writeToFile();
					main.csvTimeStamps.writeToFile();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
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
		if (saveCSV) {
			csv = new UtilsCSV("Report", CSVdirectory);
			writeCSVHeader(csv);
			csv.writeLine(csvColumns);
			
			csvTimeStamps = new UtilsCSV("TS", CSVdirectory);
			writeCSVHeader(csvTimeStamps);
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
			return;
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
		
		sc.close();

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
		double initialFraction = bestKnownSolutions.getBKSFraction(instance, initialCost);
		if (meanValues.containsKey("iscost")) {
			meanValuesList.addValueToList("iscost", initialFraction);
			if (isVerbose)
				System.out.printf("Initial cost: %d %s\n", initialCost, formatBKSComparison(initialFraction));
			if (meanValues.containsKey("optcnt") && initialFraction == 0.0)
				meanValuesList.addValueToList("optcnt", 1.0);
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
		double firstSPFraction = bestKnownSolutions.getBKSFraction(instance, firstSPCost);
		if (meanValues.containsKey("fspcost")) {
			meanValuesList.addValueToList("fspcost", firstSPFraction);
			if (isVerbose)
				System.out.printf("Cost after first local search: %d %s\n", firstSPCost, formatBKSComparison(firstSPFraction));
			if (meanValues.containsKey("optcnt") && firstSPFraction == 0.0)
				meanValuesList.addValueToList("optcnt", 1.0);
		}
		
		IteratedLocalSearch ils = new IteratedLocalSearch(seed);
		
		final long t0 = System.nanoTime();
		ArrayList<Double> timesteps = new ArrayList<>();
		ArrayList<Double> fractions = new ArrayList<>();
		
		/* Fist data point */
		timesteps.add(0.0);
		fractions.add(initialFraction);
		
		if (livePrinting) {
			System.out.printf("0.000000 ms\t%g%%\n", 100*initialFraction);
		}
		
		/* should this point be registered, being d = last data point - current data point */
		Predicate<Double> registerDataPoint = saveNoise ? (d) -> d != 0 : (d) -> d > 0;
		
		/* should the I.L.S. continue, being s the current solution */
		Predicate<Solution> stoppingCriterion = (s) -> {
			double deltaT = System.nanoTime() - t0;
			double bksFraction = bestKnownSolutions.getBKSFraction(s);
			boolean continueILS = (deltaT < secondsPerInstance*1E9) &&
					(bksFraction > qualityThreshold);
			double bksDifference = fractions.get(fractions.size()-1) - bksFraction;
			if (registerDataPoint.test(bksDifference)) {
				if (livePrinting)
					System.out.printf("%.6f ms\t%g%%\n", deltaT/1E6, bksFraction*100);
				timesteps.add(deltaT);
				fractions.add(bksFraction);
			}
			return continueILS; /* whether to continue or not */
		};
		
		currentSolution = ils.explore(initialSolution, IlsPertubationFraction, stoppingCriterion);
		
		int finalCost = currentSolution.getCost();
		double finalFraction = bestKnownSolutions.getBKSFraction(instance, finalCost);
		if (meanValues.containsKey("fscost")) {
			meanValuesList.addValueToList("fscost", finalFraction);
			if (isVerbose)
				System.out.printf("Final cost: %d %s\n", finalCost, formatBKSComparison(finalFraction));
			if (meanValues.containsKey("optcnt") && finalFraction == 0.0)
				meanValuesList.addValueToList("optcnt", 1.0);
		}
		
		if (!livePrinting)
			for (int i = 0; i < timesteps.size(); i++)
				System.out.printf("%.6f ms\t%g%%\n", timesteps.get(i)/1E6, 100*fractions.get(i));
		
		if (saveTimeSteps) {
			csvTimeStamps.writeLine(instance.getName());
			for (int i = 0; i < timesteps.size(); i++) {
				String strTS = String.format("%.6f", timesteps.get(i)/1E6);
				Double fraction = fractions.get(i);
				csvTimeStamps.writeLine(strTS, Double.toString(fraction));
			}
		}
		
		if (saveCSV) {
			csv.writeLine(
					instance.getName(),
					Integer.toString(instance.getNumberOfCustomers()),
					Double.toString(initialFraction),
					Double.toString(finalFraction),
					Double.toString(timesteps.get(timesteps.size()-1)/1E6));
		}
		
		return true;
	}

	public void writeCSVHeader(UtilsCSV csv) {
		csv.writeLine("Mode", mode);
		if (mode.equals("auto")) {
			csv.writeLine("Input file", inputFilePath);
			csv.writeLine("Instance directory", instanceDirPath);
		}
		csv.writeLine("Best known solution file", bksPath);
		csv.writeLine("Constructive metaheuristic", constructiveMetaheuristic);
		csv.writeLine("Random seed", Long.toString(seed));
		csv.writeLine("Gamma set size", Integer.toString(gammak));
		csv.writeLine("Seconds per instance", Double.toString(secondsPerInstance));
		csv.writeLine("Pertubation fraction", Double.toString(IlsPertubationFraction));
		csv.writeLine("Solution quality threshold", Double.toString(qualityThreshold));
		csv.writeLine();
	}
	
	public String formatBKSComparison(double fraction) {
		if (fraction == 0) {
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
