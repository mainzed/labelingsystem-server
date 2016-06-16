package info.labeling.v1.rest;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class RestResource {

	@GET
	public Response getAPIpage() throws URISyntaxException {
		return Response.noContent().build();
	}

}
