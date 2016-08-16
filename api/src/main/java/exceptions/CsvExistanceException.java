package exceptions;

/**
 * EXCEPTION for CSV existence errors
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class CsvExistanceException extends Exception {
	
	/**
	 * EXCEPTION for CSV existence errors
	 * @param message
	 */
	public CsvExistanceException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for CSV existence errors
	 */
	public CsvExistanceException() {
        super();
    }
	
}
