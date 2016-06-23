package info.labeling.v1.restconfig;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class CORSFilter implements ContainerResponseFilter {

	@Override
	public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
		String method = request.getMethod();
		if (method.equals("GET")) {
			response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
		} else {
			//response.getHttpHeaders().add("Access-Control-Allow-Origin", "null");
			response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
		}
		response.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		response.getHttpHeaders().add("Access-Control-Allow-Headers", "content-type, accept");
		response.getHttpHeaders().add("Access-Control-Allow-Credentials", "false");
		return response;
	}

}

