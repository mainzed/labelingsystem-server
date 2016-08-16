package exceptions;

/**
 * EXCEPTION for Config functions
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class ConfigException extends Exception {

	/**
	 * EXCEPTION for Config functions
	 * @param message
	 */
	public ConfigException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for Config functions
	 */
	public ConfigException() {
        super();
    }
}

