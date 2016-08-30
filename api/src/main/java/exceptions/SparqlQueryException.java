package exceptions;

public class SparqlQueryException extends Exception {

	public SparqlQueryException(String message) {
        super(message);
    }
	
	public SparqlQueryException() {
        super();
    }
}