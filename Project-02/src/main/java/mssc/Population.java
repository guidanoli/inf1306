package mssc;

import java.util.ArrayList;
import java.util.StringJoiner;

import mssc.construction.SolutionFactory;

public class Population extends ArrayList<Solution> {

	private static final long serialVersionUID = 387605027565896505L;

	public Population(Instance instance, int size, String constructiveMetaheuristic) {
		super(size);
		for (int i = 0; i < size; i++)
			add(SolutionFactory.construct(instance, constructiveMetaheuristic));
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		int index = 1;
		double costSum = 0;
		for (Solution s : this) {
			Double cost = s.getCost();
			costSum += cost;
			sj.add(String.format("[%d] = %f", index, cost));
			++index;
		}
		double avgCost = costSum / size();
		return String.format("%s\nAverage fitness = %f", sj.toString(), avgCost);
	}
	
}
