package exceptions;

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