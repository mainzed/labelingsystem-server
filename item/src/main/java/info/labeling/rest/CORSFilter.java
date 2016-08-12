package info.labeling.rest;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * CLASS to configure CORS
 *
 * @author Florian Thiery M.Sc.
 */
public class CORSFilter implements ContainerResponseFilter {

	/**
	 * configure CORS filter
	 * @param request this request
	 * @param response this response
	 * @return 
	 */
	@Override
	public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
		response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
		response.getHttpHeaders().add("Access-Control-Allow-Methods", "GET");
		response.getHttpHeaders().add("Access-Control-Allow-Headers", "Accept");
		response.getHttpHeaders().add("Access-Control-Allow-Credentials", "false");
		return response;
	}

}
