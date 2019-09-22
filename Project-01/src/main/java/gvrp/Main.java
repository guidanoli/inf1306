package gvrp;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import gvrp.construction.SolutionFactory;

public class Main {
	
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
	
	BKS bestKnownSolutions;
	
	/**
	 * Runs the GVRP solver according to parameters parsed in command line
	 * 
	 * @param args - command line arguments (see README.md)
	 */
	public static void main(String[] args) {
		Main main = new Main();
		JCommander.newBuilder().addObject(main).build().parse(args);
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
			bestKnownSolutions = new BKS(bksScanner);
			bksScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		if (inputFilePath == null) {
			/* No input path provided will pop up JFileChooser */
			File instanceFile = promptForFolder();
			if (!solveInstance(instanceFile))
				return;
		} else {
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
			System.out.println(String.format("'%s' is not a valid constructive metaheuristic.",
					constructiveMetaheuristic));
			return false;
		}

		if (initialSolutionInfo)
			System.out.println(initialSolution);
		
		if (!initialSolution.isValid(isVerbose)) {
			System.out.println("Initial solution is invalid.");
			return false;
		}
		
		if (isVerbose) {
			int cost = initialSolution.getCost();
			double fraction = bestKnownSolutions.getBKSFraction(initialSolution);
			System.out.printf("Initial cost: %d (%.2f%% from optimal solution)\n", cost, fraction*100);
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
