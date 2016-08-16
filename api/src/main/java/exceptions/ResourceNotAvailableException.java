package exceptions;

/**
 * EXCEPTION for warnings if no requested resource is available
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 06.02.2015
 */
public class ResourceNotAvailableException extends Exception {

	/**
	 * EXCEPTION for warnings if no requested resource is available
	 *
	 * @param message
	 */
	public ResourceNotAvailableException(String message) {
		super(message);
	}
	
	/**
	 * EXCEPTION for warnings if no requested resource is available
	 */
	public ResourceNotAvailableException() {
		super();
	}
}
