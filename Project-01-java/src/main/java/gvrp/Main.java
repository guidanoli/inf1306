package gvrp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Main {

	@Parameter(names = "-input", description = "Input file")
	String inputFilePath = null;
	
	@Parameter(names = "-persist", description = "Persist parsing all input files")
	boolean persist = false;
	
	/**
	 * Runs the GVRP solver, printing the results to the standard output
	 * @param args - command line arguments (see README.md)
	 */
	public static void main(String [] args) {
		Main main = new Main();
        JCommander.newBuilder()
            .addObject(main)
            .build()
            .parse(args);
        
		if (main.inputFilePath == null) {
			/* No input path provided will pop up JFileChooser */
			File instanceFile = promptForFolder();
			if (!solveInstance(instanceFile)) return;
		} else {
			/* Parse input file */
			File inputFile = new File(main.inputFilePath);
			Scanner sc = null;
			try {
				sc = new Scanner(inputFile);
				while (sc.hasNext()) {
					String instanceFilePath = sc.next();
					File instanceFile = new File(instanceFilePath);
					if (!solveInstance(instanceFile)) {
						/* -persist will continue parsing */
						if (!main.persist) break;
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
	 * @param instanceFile - file with instance data
	 * @return {@code true} if no errors occurred,
	 * {@code false} otherwise
	 */
	public static boolean solveInstance(File instanceFile) {
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
		
		System.out.println(instance);
		return true;
	}
	
	public static File promptForFolder()
	{
	    JFileChooser fc = new JFileChooser();
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    /* Does not work for JAR -- will start at user home */
	    fc.setCurrentDirectory(new File("./data/GVRP3"));
	    fc.setFileFilter(new FileNameExtensionFilter("GVRP instance", "gvrp"));

	    if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	    {
	        return fc.getSelectedFile();
	    }

	    return null;
	}
	
}
