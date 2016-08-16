package exceptions;

/**
 * EXCEPTION for Label Import problems
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class CsvLabelImportException extends Exception{
	
	/**
	 * EXCEPTION for Label Import problems
	 * @param message
	 */
	public CsvLabelImportException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for Label Import problems
	 */
	public CsvLabelImportException() {
        super();
    }
	
}
