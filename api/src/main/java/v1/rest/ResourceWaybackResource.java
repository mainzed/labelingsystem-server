package v1.rest;

import exceptions.ResourceNotAvailableException;
import exceptions.SesameSparqlException;
import v1.utils.config.ConfigProperties;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

@Path("/resourcewayback")
public class ResourceWaybackResource {

	@GET
	@Produces("application/json;charset=UTF-8")
	public Response redirectToWayback(@QueryParam("url") String url) throws URISyntaxException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
		URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + "/v1/retcat/waybacklink" + "?url=" + url);
		return Response.temporaryRedirect(targetURIForRedirection).build();
	}

}
