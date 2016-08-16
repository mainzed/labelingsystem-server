package v1.rest;

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
		return Response.ok("{\"name\": \"Labeling System API Version 1.0\",\"doc\": \"https://docs.google.com/spreadsheets/d/1GYEkott-hClPab3ZJIrkuRd2PnO8o8jHkaATTks7qx8\"}").build();
		
	}

}
