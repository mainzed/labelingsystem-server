package exceptions;

public class WaybacklinkException extends Exception {

	/**
	 * EXCEPTION for Config functions
	 * @param message
	 */
	public WaybacklinkException(String message) {
        super(message);
    }
	
	/**
	 * EXCEPTION for Config functions
	 */
	public WaybacklinkException() {
        super();
    }
}

