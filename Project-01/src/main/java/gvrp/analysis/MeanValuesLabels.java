package gvrp.analysis;

import java.util.function.Function;

public enum MeanValuesLabels {

	iscost("Initial solution cost", (d) -> String.format("%.2f%%", d*100));
	
	private String label;
	private Function<Double, String> convert;
	MeanValuesLabels(String label, Function<Double, String> convert) {
		this.label = label;
		this.convert = convert;
	}
	public String getLabel() {
		return label;
	}
	public String convert(Double value) {
		return convert.apply(value);
	}
}
