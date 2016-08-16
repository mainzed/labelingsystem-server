package exceptions;

/**
 * EXCEPTION if autocomplete length is wrong
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class AutocompleteLengthException extends Exception {
	
	/**
	 * EXCEPTION if autocomplete length is wrong
	 * @param message
	 */
	public AutocompleteLengthException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION if autocomplete length is wrong
	 */
	public AutocompleteLengthException() {
        super();
    }
	
}
