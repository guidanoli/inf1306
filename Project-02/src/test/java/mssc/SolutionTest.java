package mssc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import mssc.construction.RandomSolution;
import mssc.construction.SolutionFactory;

class SolutionTest {

	final static String instanceDirPath = Paths.get("data", "MSSC").toString();
	final static String instanceFilename = "data.txt";
	final static int numberOfClusters = 10;
	static Instance instance;
		
	@BeforeAll
	static void initiateInstance() {
		String instanceFilePath = Paths.get(instanceDirPath, instanceFilename).toString();
		File instanceFile = new File(instanceFilePath);
		if (!instanceFile.exists())
			fail("File "+instanceFilePath+" could not be opened!");
		Scanner sc = null;
		try {
			sc = new Scanner(instanceFile);
			sc.useLocale(Locale.US); /* Doubles uses dots */
		} catch (FileNotFoundException e) {
			fail("File "+instanceFilePath+" could not be scanned!");
			e.printStackTrace();
		}
		/* Try to parse instance file */
		instance = null;
		try {
			instance = Instance.parse(sc, numberOfClusters);
		} catch (NoSuchElementException nsee) {
			nsee.printStackTrace();
		} catch (IllegalStateException ilse) {
			ilse.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
			if (instance == null) {
				fail("Error while parsing file "+instanceFilePath);
			}
		}
	}
	
	@Test
	@DisplayName("when creating a solution through random construction")
	void testRandomConstructiveMetaheuristic() {
		Solution s = SolutionFactory.construct(instance, "random");
		if (s == null) fail("Invalid metaheuristic");
		assertTrue(s.isValid(false), "it should create a valid solution");
	}
	
	@Test
	@DisplayName("when creating two identical solutions")
	void testEquals() {
		Random rng1 = new Random(0), rng2 = new Random(0);
		RandomSolution randomSol = new RandomSolution();
		Solution s1 = randomSol.construct(instance, rng1),
				s2 = randomSol.construct(instance, rng2);
		assertTrue(s1.equals(s2), "equals should return true");
		s1.kMeans();
		s2.kMeans();
		assertTrue(s1.equals(s2), "equals should return true even after k-Means");
	}

}
