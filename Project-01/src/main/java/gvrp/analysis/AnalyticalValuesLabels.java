package gvrp.analysis;

import java.util.function.Function;

public enum AnalyticalValuesLabels {

	iscost("Initial solution cost", (d) -> String.format("%.3f%%", d*100), 1, true),
	fspcost("First shortest path cost", (d) -> String.format("%.3f%%", d*100), 2, true),
	fscost("Final solution cost", (d) -> String.format("%.3f%%", d*100), 3, true),
	sclonetime("Solution cloning time", (d) -> String.format("%.3f ms", d/1000), 4, true),
	improvement("Solution improvement after local search", (d) -> String.format("%.3f%%", d*100), 5, true),
	optcnt("#Optimal solutions", (d) -> ((Long)Math.round(d)).toString(), 6, false);
	
	private String label;
	private Function<Double, String> convert;
	private int position;
	private boolean mean; /* if false, is sum */
	
	AnalyticalValuesLabels(String label, Function<Double, String> convert, int position, boolean mean) {
		this.label = label;
		this.convert = convert;
		this.position = position;
		this.mean = mean;
	}
	
	public boolean takesTheMean() {
		return mean;
	}
	public int getPosition() {
		return position;
	}
	public String getLabel() {
		return label;
	}
	public String convert(Double value) {
		return convert.apply(value);
	}
}
