package exceptions;

/**
 * EXCEPTION for CSV language errors
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class CsvLanguageException extends Exception {
	
	/**
	 * EXCEPTION for CSV language errors
	 * @param message
	 */
	public CsvLanguageException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for CSV language errors
	 */
	public CsvLanguageException() {
        super();
    }
	
}
