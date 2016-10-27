package exceptions;

public class ValidateJSONObjectException extends Exception {

	public ValidateJSONObjectException(String message) {
		super(message);
	}

	public ValidateJSONObjectException() {
		super();
	}
}