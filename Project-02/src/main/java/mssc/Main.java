package mssc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import mssc.analysis.BestKnownSolutions;
import mssc.analysis.UtilsCSV;
import mssc.construction.SolutionFactory;

public class Main {

	@Parameter(names = "-mode", description = "Input mode")
	String mode = "auto";
		
	@Parameter(names = "-ifile", description = "Input file")
	String inputFilePath = "all.txt";
	
	@Parameter(names = {"-persist", "-persistant"}, description = "Persist parsing all input files")
	boolean isPersistant = false;
	
	@Parameter(names = "-idir", description = "Instance files directory")
	String instanceDirPath = "data/MSSC";
	
	@Parameter(names = { "-v", "-verbose" }, description = "Verbosity")
	boolean isVerbose = false;
	
	@Parameter(names = {"-iinfo"}, description = "Instance information")
	boolean instanceInfo = false;
	
	@Parameter(names = "-constructive", description = "Constructive metaheuristic")
	String constructiveMetaheuristic = "random";
	
	@Parameter(names = {"-minpsize"}, description = "Initial Population Size")
	int minPopulationSize = 10;
	
	@Parameter(names = {"-maxpsize"}, description = "Maximum Population Size")
	int maxPopulationSize = 20;
	
	@Parameter(names = {"-ngen"}, description = "Maximum number of generations")
	long maxNumOfGenerations = 5000;
	
	@Parameter(names = {"-ngen-wo-improv"}, description = "Maximum number of generations without improving")
	long noImprovementLimit = 5000;
		
	@Parameter(names = "-bks", description = "Best Known Solution file")
	String bksPath = "data/bks.txt";
	
	@Parameter(names = {"-csvdir"}, description = "Path to where .csv files are saved")
	File CSVdirectory = new File("data/results");
	
	@Parameter(names = {"-csv"}, description =  "Save results in a .csv file in the -csvdir directory")
	boolean saveCSV = false;

	@DynamicParameter(names = {"-CSV"}, description = "CSV data")
	Map<String, String> csvData = new HashMap<>();
	
	@Parameter(names = {"-seed"}, description = "RNG seed")
	long seed = 0;
	
	@Parameter(names = {"-help", "--help"}, description = "Help with application parameters", help = true)
	boolean help = false;
	
	BestKnownSolutions bestKnownSolutions;
	UtilsCSV csv;
	
	HashMap<String, String> csvLabelMap = new HashMap<>();
	{
		csvLabelMap.put("name", "Instance");
		csvLabelMap.put("m", "Number of Clusters");
		csvLabelMap.put("fitness", "Best Fitness");
		csvLabelMap.put("bks", "%BKS");
		csvLabelMap.put("time", "Time (s)");
	}
	
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
	}
	
	/**
	 * Read instance files and run the solver/debugger, depending on the arguments
	 * parsed to the application.
	 */
	public void run() {
		try {
			File bksFile = new File(bksPath);
			Scanner bksScanner = new Scanner(bksFile);
			bksScanner.useLocale(Locale.US);
			bestKnownSolutions = new BestKnownSolutions(bksScanner);
			bksScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		if (saveCSV) {
			csv = new UtilsCSV("Report", CSVdirectory);
			String [] columns = new String[csvData.size()];
			Arrays.fill(columns, "");
			csvData.forEach((k,v) -> columns[Integer.parseInt(k)-1] = csvLabelMap.get(v));
			csv.writeLine(columns);
		}
		SolutionFactory.setRandomSeed(seed);
		if (mode.equals("manual")) {
			/* No input path provided will pop up JFileChooser */
			File instanceFile = promptForFolder();
			Integer numberOfClusters = null;
			try {
				do {
					numberOfClusters = Integer.parseInt(JOptionPane
							.showInputDialog("Insert the number of clusters for instance " +
											instanceFile.getName() + ":"));
				} while (numberOfClusters != null && numberOfClusters <= 0);
			} catch (NumberFormatException nfe2) {
				System.out.println(">>> Invalid input");
				return;
			}
			if (!solveInstance(instanceFile, numberOfClusters))
				return;
		} else if (mode.equals("auto")) {
			/* Parse input file */
			File inputFile = new File(inputFilePath);
			Scanner sc = null;
			try {
				sc = new Scanner(inputFile);
				sc.useLocale(Locale.US); /* Doubles uses dots */
				while (sc.hasNext()) {
					String instanceFilename = sc.next();
					if (!sc.hasNext()) {
						System.out.println(">>> Parsing error: m enumeration mode missing");
						break;	
					}
					ArrayList<Integer> mList = new ArrayList<>();
					String enumerationMode = sc.next();
					if (enumerationMode.equals("steps")) {
						if (!sc.hasNextInt()) {
							System.out.println(">>> Parsing error: initial m value missing");
							break;
						}
						int m_ini = sc.nextInt();
						if (!sc.hasNextInt()) {
							System.out.println(">>> Parsing error: final m value missing");
							break;
						}
						int m_fin = sc.nextInt();
						if (!sc.hasNextInt()) {
							System.out.println(">>> Parsing error: m value step missing");
							break;
						}
						int m_step = sc.nextInt();
						for (int m = m_ini; m <= m_fin; m += m_step) mList.add(m);
					} else if (enumerationMode.equals("enum")) {
						if (!sc.hasNextInt()) {
							System.out.println(">>> Parsing error: m value missing");
							break;
						}
						do {
							mList.add(sc.nextInt());
						} while(sc.hasNextInt());
					}
					String instanceFilePath = Paths.get(instanceDirPath, instanceFilename).toString();
					File instanceFile = new File(instanceFilePath);
					if (!instanceFile.exists()) {
						System.out.println(">>> File "+instanceFilePath+" could not be opened!");
						break;
					}
					for (int m : mList) {
						if (!solveInstance(instanceFile, m)) {
							System.out.println(">>> Could not solve instance!");
							/* -persist will continue parsing */
							if (!isPersistant)
								break;
						}
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
		if (saveCSV) {
			String csvFilePath = null;
			try {
				csvFilePath = csv.writeToFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			System.out.println("Saved CSV to " + csvFilePath);
		}
		System.out.println("Done!");
	}
	
	/**
	 * Solves MSSC instance
	 * 
	 * @param instanceFile - file with instance data
	 * @return {@code true} if no errors occurred, {@code false} otherwise
	 */
	public boolean solveInstance(File instanceFile, int numberOfClusters) {
		/* Null file is always invalid */
		if (instanceFile == null) {
			return false;
		}

		System.out.println("instance \"" + instanceFile.getName() + "\"\tm = " + numberOfClusters);
						
		/* Try to create Scanner object */
		Scanner sc = null;
		try {
			sc = new Scanner(instanceFile);
			sc.useLocale(Locale.US); /* Doubles uses dots */
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		/* Try to parse instance file */
		Instance instance = null;
		try {
			instance = Instance.parse(sc, instanceFile.getName(), numberOfClusters);
		} catch (NoSuchElementException nsee) {
			nsee.printStackTrace();
			sc.close();
			return false;
		} catch (IllegalStateException ilse) {
			ilse.printStackTrace();
			sc.close();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			sc.close();
			return false;
		}
		sc.close();
		
		if (instance == null) {
			System.out.println(">>> Parsing error");
			return false;
		}
		
		if (instanceInfo)
			System.out.println(instance);
		
		final long t0 = System.nanoTime();
		Population population = new Population(instance, minPopulationSize,
									maxPopulationSize, constructiveMetaheuristic);
		population.setRandomSeed(seed);

		if (!population.isValid(isVerbose)) {
			System.out.println(">>> Invalid initial population.");
			return false;
		}
		
		if (isVerbose)
			System.out.println(population);
		
		double currentBestFitness = population.getBestSolutionFitness();
		long generationsWithoutImprovement = 0;
		while (population.getGenerationCount() < maxNumOfGenerations) {
			population.nextGeneration();
			if (!population.isValid(isVerbose)) {
				System.out.println(">>> Invalid population at generation " + population.getGenerationCount());
				return false;
			}
			double bestFitness = population.getBestSolutionFitness();
			if (Double.compare(currentBestFitness, bestFitness) == 0)
				++generationsWithoutImprovement;
			else
				generationsWithoutImprovement = 0;
			if (generationsWithoutImprovement >= noImprovementLimit)
				break;
			currentBestFitness = bestFitness;
			if (isVerbose)
				System.out.println(population);
		}
		double deltaTms = (System.nanoTime() - t0)/1E9;
		
		double bestFitness = population.getBestSolutionFitness();
		Double finalFraction = bestKnownSolutions.getBKSFraction(instance, numberOfClusters, bestFitness);
		
		if (isVerbose)
			if (finalFraction != null)
				System.out.println("Best solution cost = " + bestFitness + " (" + finalFraction + "%)");
			else
				System.out.println("Best solution cost = " + bestFitness);
		
		if (saveCSV) {
			HashMap<String, String> csvDataMap = new HashMap<>();
			csvDataMap.put("name", instance.getName());
			csvDataMap.put("m", Integer.toString(numberOfClusters));
			csvDataMap.put("fitness", String.format(Locale.US, "%5f", bestFitness));
			if (finalFraction != null) {
				if (finalFraction < 1e-5)
					csvDataMap.put("bks", String.format(Locale.US, "%5f", 0.0));
				else
					csvDataMap.put("bks", String.format(Locale.US, "%5f", finalFraction));
			}
			csvDataMap.put("time", Double.toString(deltaTms));
			
			String [] dataArray = new String[csvData.size()];
			Arrays.fill(dataArray, "");
			csvData.forEach((k,v) -> dataArray[Integer.parseInt(k)-1] = csvDataMap.get(v));
			csv.writeLine(dataArray);
		}
		
		return true;
	}
	
	public File promptForFolder() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		/* Does not work for JAR -- will start at user home */
		fc.setCurrentDirectory(new File(instanceDirPath));
		fc.setFileFilter(new FileNameExtensionFilter("MSSC instance", "txt"));

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}

		return null;
	}
	
}
