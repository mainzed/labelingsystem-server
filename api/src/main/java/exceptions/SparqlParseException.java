package exceptions;

public class SparqlParseException extends Exception {

	public SparqlParseException(String message) {
        super(message);
    }
	
	public SparqlParseException() {
        super();
    }
}