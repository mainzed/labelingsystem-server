package v1.rest;

import exceptions.ResourceNotAvailableException;
import exceptions.SesameSparqlException;
import v1.utils.config.ConfigProperties;
import v1.utils.retcat.RetcatItems;
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
import v1.utils.retcat.RetcatItem;

@Path("/resourceinfo")
public class ResourceInfoResource {

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response redirectToRetcat(@QueryParam("url") String url) throws URISyntaxException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
        List<RetcatItem> retcatlist = RetcatItems.getAllRetcatItems();
        boolean match = false;
        for (RetcatItem item : retcatlist) {
            if (url.contains(item.getPrefix())) {
                URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + item.getLabelURL() + "?url=" + url + "&type=" + item.getType());
                return Response.temporaryRedirect(targetURIForRedirection).build();
            }
        }
        if (!match) {
            URI targetURIForRedirection = new URI(ConfigProperties.getPropertyParam("api") + "/v1/retcat/info/html" + "?url=" + url);
            return Response.temporaryRedirect(targetURIForRedirection).build();
        }
        return Response.ok().build();
    }

}
