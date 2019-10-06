package gvrp.jcommander;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * 
 * A validator that makes sure the value of the parameter is a
 * real number between 0 and 1.
 * 
 * @author guidanoli
 *
 */
public class ZeroToOneDouble implements IParameterValidator {

	@Override
	public void validate(String name, String value)
			throws ParameterException {
		double fraction = Double.parseDouble(value);
		if (fraction < 0.0 || fraction > 1.0) {
			throw new ParameterException("Parameter" + name
					+ "should be between 0 and 1 (found " + value + ")");
		}
	}

}
