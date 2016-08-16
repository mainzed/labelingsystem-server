package exceptions;

/**
 * EXCEPTION for warnings if RDF model parsing is wrong
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 06.02.2015
 */
public class RdfException extends Exception{
	
	/**
	 * EXCEPTION for warnings if RDF model parsing is wrong
	 * @param message
	 */
	public RdfException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for warnings if RDF model parsing is wrong
	 */
	public RdfException() {
        super();
    }
	
}

