package mssc;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

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
	
	@Parameter(names = {"-ipinfo"}, description = "Initial Population Info")
	boolean initialPopulationInfo = false;
	
	@Parameter(names = {"-fpinfo"}, description = "Final Population Info")
	boolean finalPopulationInfo = false;
	
	@Parameter(names = {"-ipsize"}, description = "Initial Population Size")
	int initialPopulationSize = 100;
	
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
		SolutionFactory.setRandomSeed(seed);
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
				sc.useLocale(Locale.US); /* Doubles uses dots */
				while (sc.hasNextLine()) {
					String instanceFilePath = Paths.get(instanceDirPath, sc.nextLine()).toString();
					File instanceFile = new File(instanceFilePath);
					if (!instanceFile.exists()) {
						System.out.println(">>> File "+instanceFilePath+" could not be opened!");
						/* -persist will continue parsing */
						if (!isPersistant)
							break;
					}
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
			sc.useLocale(Locale.US); /* Doubles uses dots */
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
		
		Population population = new Population(instance, initialPopulationSize, constructiveMetaheuristic);
		
		if (initialPopulationInfo)
			System.out.println(population);
		
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
