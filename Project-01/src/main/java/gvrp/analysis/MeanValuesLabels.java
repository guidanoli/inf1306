package gvrp.analysis;

import java.util.function.Function;

public enum MeanValuesLabels {

	iscost("Initial solution cost", (d) -> String.format("%.3f%%", d*100), 1),
	fspcost("First shortest path cost", (d) -> String.format("%.3f%%", d*100), 2),
	fscost("Final solution cost", (d) -> String.format("%.3f%%", d*100), 3),
	sclonetime("Solution cloning time", (d) -> String.format("%.3f ms", d/1000), 4),
	improvement("Solution improvement after local search", (d) -> String.format("%.3f%%", d*100), 5);
	
	private String label;
	private Function<Double, String> convert;
	private int position;
	MeanValuesLabels(String label, Function<Double, String> convert, int position) {
		this.label = label;
		this.convert = convert;
		this.position = position;
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
