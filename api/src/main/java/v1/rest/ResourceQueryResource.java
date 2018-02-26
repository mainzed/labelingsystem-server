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
import v1.utils.generalfuncs.GeneralFunctions;

@Path("/resourcequery")
public class ResourceQueryResource {

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response redirectToRetcat(@QueryParam("retcat") String retcat, @QueryParam("query") String query) throws URISyntaxException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, org.eclipse.rdf4j.repository.RepositoryException, org.eclipse.rdf4j.query.MalformedQueryException, org.eclipse.rdf4j.query.QueryEvaluationException, link.labeling.retcat.exceptions.ResourceNotAvailableException {
        List<RetcatItem> retcatlist = LocalRetcatItems.getLocalCatalogue();
        boolean match = false;
        if (retcat != null) {
            //ckeck if own vocab
            if (retcat.contains("this.")) {
                String vocab = retcat.split("this.")[1];
                query = GeneralFunctions.encodeURIComponent(query);
                URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + "/v1/retcat/query/labelingsystem/" + vocab + "?query=" + query);
                return Response.temporaryRedirect(targetURIForRedirection).build();
            }
            // look for other endpoints
            for (RetcatItem item : retcatlist) {
                if (retcat.contains(item.getName())) {
                    query = GeneralFunctions.encodeURIComponent(query);
                    URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + item.getQueryURL() + "?query=" + query);
                    return Response.temporaryRedirect(targetURIForRedirection).build();
                }
            }
        }
        if (!match) {
            URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + "/v1/retcat/query/html" + "?url=" + query);
            return Response.temporaryRedirect(targetURIForRedirection).build();
        }
        return Response.ok().build();
    }

}
