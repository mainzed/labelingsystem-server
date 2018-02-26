package v1.rest;

import exceptions.ResourceNotAvailableException;
import exceptions.SesameSparqlException;
import v1.utils.config.ConfigProperties;
import v1.utils.retcat.LocalRetcatItems;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import link.labeling.retcat.classes.RetcatItem;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

@Path("/resourceinfo")
public class ResourceInfoResource {

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response redirectToRetcat(@QueryParam("uri") String uri) throws URISyntaxException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, org.eclipse.rdf4j.repository.RepositoryException, org.eclipse.rdf4j.query.MalformedQueryException, org.eclipse.rdf4j.query.QueryEvaluationException, link.labeling.retcat.exceptions.ResourceNotAvailableException {
        List<RetcatItem> retcatlist = LocalRetcatItems.getLocalCatalogue();
        boolean match = false;
        for (RetcatItem item : retcatlist) {
            if (uri.contains(item.getPrefix())) {
                URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + item.getLabelURL() + "?uri=" + uri + "&type=" + item.getType());
                return Response.temporaryRedirect(targetURIForRedirection).build();
            }
        }
        if (!match) {
            URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + "/v1/retcat/info/html" + "?url=" + uri);
            return Response.temporaryRedirect(targetURIForRedirection).build();
        }
        return Response.ok().build();
    }

}
