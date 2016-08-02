package exceptions;

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
