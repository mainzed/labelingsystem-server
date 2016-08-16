package exceptions;

/**
 * EXCEPTION for warnings if no SPARQL input was done
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class NoInputException extends Exception {
    
	/**
	 * EXCEPTION for warnings if no SPARQL input was done
	 * @param message
	 */
	public NoInputException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for warnings if no SPARQL input was done
	 */
	public NoInputException() {
        super();
    }
}
