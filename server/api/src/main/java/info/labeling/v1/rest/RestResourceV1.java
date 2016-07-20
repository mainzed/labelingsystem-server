package info.labeling.v1.rest;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/v1/")
public class RestResourceV1 {

	@GET
	public Response getAPIpage() throws URISyntaxException {
		return Response.noContent().build();
		//URI uri = new URI("https://gist.github.com/florianthiery/539427960fb629ec861d6e9e8cb0a0db");
		//return Response.seeOther(uri).build();
	}

}
