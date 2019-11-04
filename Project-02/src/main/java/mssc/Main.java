package mssc;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import mssc.Solution;
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
	
	@Parameter(names = {"-isinfo"}, description = "Initial Solution Info")
	boolean initialSolutionInfo = false;
	
	@Parameter(names = {"-fsinfo"}, description = "Final Solution Info")
	boolean finalSolutionInfo = false;
	
	@Parameter(names = {"-seed"}, description = "RNG seed")
	long seed = 0;
	
	@Parameter(names = {"-help", "--help"}, description = "Help with application parameters", help = true)
	boolean help = false;
	
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
		if (mode.equals("manual")) {
			/* No input path provided will pop up JFileChooser */
			File instanceFile = promptForFolder();
			if (!solveInstance(instanceFile))
				return;
		} else if (mode.equals("auto")) {
			/* Parse input file */
			File inputFile = new File(inputFilePath);
			Scanner sc = null;
			try {
				sc = new Scanner(inputFile);
				while (sc.hasNextLine()) {
					String instanceFilePath = Paths.get(instanceDirPath, sc.nextLine()).toString();
					File instanceFile = new File(instanceFilePath);
					if (!solveInstance(instanceFile)) {
						System.out.println(">>> Could not solve instance!");
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
	 * Solves MSSC instance
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
		
		String filename = instanceFile.toPath().getFileName().toString();
		String firstNumberStr = filename.replaceAll(".*?(\\d+).*", "$1");
		
		Integer firstNumber = null;
		try {
			firstNumber = Integer.parseInt(firstNumberStr);
		} catch (NumberFormatException nfe) {
			System.out.println("Could not find number in instance file name for number of clusters.");
			System.out.println("Prompting user for input.");
			try {
				do {
					firstNumber = Integer.parseInt(JOptionPane
							.showInputDialog("Insert the number of clusters:"));
				} while (firstNumber != null && firstNumber <= 0);
			} catch (NumberFormatException nfe2) {
				System.out.println(">>> Invalid input");
				return false;
			}
		}
				
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
			instance = Instance.parse(sc, firstNumber);
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
		
		if (isVerbose)
			System.out.println("Constructing initial solution with '" + constructiveMetaheuristic +
					"' constructive metaheuristic");
		
		Solution initialSolution = SolutionFactory.construct(instance, constructiveMetaheuristic);
		
		if (initialSolution == null) {
			System.out.printf(">>> '%s' is not a valid constructive metaheuristic.\n",
					constructiveMetaheuristic);
			return false;
		}

		if (initialSolutionInfo)
			System.out.println(initialSolution);
		
		if (!initialSolution.isValid(isVerbose)) {
			System.out.println(">>> Initial solution is invalid.");
			return false;
		}
		
		double initialSolutionCost = initialSolution.getCost();
		
		if (isVerbose)
			System.out.println("Initial solution cost: " + initialSolutionCost);
				
		initialSolution.kMeans();
		
		double finalSolutionCost = initialSolution.getCost();
		
		if (finalSolutionInfo)
			System.out.println(initialSolution);
		
		if (isVerbose)
			System.out.println("Final solution cost: " + finalSolutionCost);
		
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
