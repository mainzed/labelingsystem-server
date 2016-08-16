package exceptions;

/**
 * EXCEPTION if something happens while SPARQL the triplestore
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
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
