package gvrp.jcommander;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class PositiveDouble implements IParameterValidator {

	@Override
	public void validate(String name, String value) throws ParameterException {
		double i = Double.parseDouble(value);
		if (i < 0) {
			throw new ParameterException("Parameter" + name
					+ "should be a positive real number (found " + value + ")");
		}
	}

}
