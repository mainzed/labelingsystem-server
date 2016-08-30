package exceptions;

public class JsonFormatException extends Exception {

	public JsonFormatException(String message) {
        super(message);
    }
	
	public JsonFormatException() {
        super();
    }
}

