package mssc.construction;

import java.util.Random;

import mssc.Instance;
import mssc.Solution;

public interface ConstructiveMetaheuristic {

	public Solution construct(Instance instance, int id, Random rng);
	
}
