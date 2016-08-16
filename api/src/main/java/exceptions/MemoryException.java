package exceptions;

/**
 * EXCEPTION for data that is too much to display
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 11.06.2015
 */
public class MemoryException extends Exception {

	/**
	 * EXCEPTION for Config functions
	 * @param message
	 */
	public MemoryException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for Config functions
	 */
	public MemoryException() {
        super();
    }
}

