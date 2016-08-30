package exceptions;

public class SparqlUpdateException extends Exception {

	public SparqlUpdateException(String message) {
        super(message);
    }
	
	public SparqlUpdateException() {
        super();
    }
}