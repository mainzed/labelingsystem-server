package info.labeling.v1.rest;

import info.labeling.exceptions.ResourceNotAvailableException;
import info.labeling.exceptions.SesameSparqlException;
import info.labeling.v1.utils.ConfigProperties;
import info.labeling.v1.utils.RetcatItems;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

@Path("/resourcequery")
public class ResourceQueryResource {

	@GET
	@Produces("application/json;charset=UTF-8")
	public Response redirectToRetcat(@QueryParam("retcat") String retcat, @QueryParam("query") String query) throws URISyntaxException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
		List<String[]> retcatlist = RetcatItems.getAllItems();
		boolean match = false;
		for (String[] arrayItem : retcatlist) {
			if (retcat.contains(arrayItem[0])) {
				URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + arrayItem[1]+"?query="+query);
				return Response.temporaryRedirect(targetURIForRedirection).build();
			}
		}
		if (!match) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} else {
			return Response.ok().build();
		}
	}

}
