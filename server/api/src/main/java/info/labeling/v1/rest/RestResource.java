package info.labeling.v1.rest;

import java.net.URISyntaxException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class RestResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getAPIpage() throws URISyntaxException {
		return Response.ok("{\"info\": \"Welcome to the Labeling System API Version 1.0\",\"link\": \"\"}").build();
		
	}

}
