package exceptions;

/**
 * EXCEPTION for UUIDs
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class UniqueIdentifierException extends Exception {
    
	/**
	 * EXCEPTION for UUIDs
	 * @param message
	 */
	public UniqueIdentifierException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for UUIDs
	 */
	public UniqueIdentifierException() {
        super();
    }
}
