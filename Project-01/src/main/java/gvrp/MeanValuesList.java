package gvrp;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class MeanValuesList extends HashMap<String, ArrayList<Double>>{
	
	public void addValueToMean(String key, Double value) {
		ArrayList<Double> array = get(key);
		if (array == null) {
			array = new ArrayList<Double>();
			put(key, array);
		}
		array.add(value);
	}
	
	public Double getMean(String key) {
		Double sum = 0.0d;
		ArrayList<Double> array = get(key);
		if (array == null) return null;
		for (Double value : array) {
			sum += value;
		}
		return sum / array.size();
	}
	
}
