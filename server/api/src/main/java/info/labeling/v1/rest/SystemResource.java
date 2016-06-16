package info.labeling.v1.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author florian.thiery
 */
@Path("/v1/system")
public class SystemResource {

	@Context
	private UriInfo context;

	/**
	 * Creates a new instance of SpecialResource
	 */
	public SystemResource() {
	}

	/**
	 * Retrieves representation of an instance of de.i3mainz.rest.SystemResource
	 * @return an instance of java.lang.String
	 */
	@GET
    @Produces("application/json")
	public String getJson() {
		//TODO return proper representation object
		throw new UnsupportedOperationException();
	}

	/**
	 * PUT method for updating or creating an instance of SystemResource
	 * @param content representation for the resource
	 * @return an HTTP response with content of the updated or created resource.
	 */
	@PUT
    @Consumes("application/json")
	public void putJson(String content) {
	}
}
