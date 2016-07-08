package info.labeling.v1.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

@Path("Login")
public class LoginResource {

	@GET
    @Produces("application/xml")
	public String getXml() {
		//HttpSession session = null;
		return null;
	}

}
