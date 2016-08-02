package exceptions;

public class SesameSparqlException extends Exception {

	/**
	 * EXCEPTION if something happens while SPARQL the triplestore
	 *
	 * @param message
	 */
	public SesameSparqlException(String message) {
		super(message);
	}

	/**
	 * EXCEPTION if something happens while SPARQL the triplestore
	 */
	public SesameSparqlException() {
		super();
	}

}
