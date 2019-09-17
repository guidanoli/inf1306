package gvrp;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

	public static void main(String [] args) {
		String instance = promptForFolder();
		System.out.println(instance);
	}

	public static String promptForFolder()
	{
	    JFileChooser fc = new JFileChooser();
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    fc.setCurrentDirectory(new java.io.File("./data/GVRP3"));
	    fc.setFileFilter(new FileNameExtensionFilter("GVRP instance", "gvrp"));

	    if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	    {
	        return fc.getSelectedFile().getAbsolutePath();
	    }

	    return null;
	}
	
}
