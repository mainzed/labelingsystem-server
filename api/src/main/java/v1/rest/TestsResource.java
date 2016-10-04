package v1.rest;

import exceptions.ConfigException;
import exceptions.Logging;
import java.io.IOException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom.JDOMException;
import rdf.RDF;
import rdf.RDF4J_20;
import v1.utils.config.ConfigProperties;

@Path("/tests")
public class TestsResource {

	@POST
	@Path("/init")
    public Response initTest() throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // delete "test" user's vocabs and labels
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String prefixes = rdf.getPREFIXSPARQL();
			String update = prefixes
                + "DELETE { ?vocab ?p1 ?o1. ?label ?p2 ?o2. ?o3 ?p3 ?vocab. ?o4 ?p4 ?label. } "
                + "WHERE { "
                + "?o3 ?p3 ?vocab. "
				+ "?vocab ?p1 ?o1. "
				+ "?vocab dc:creator \"test\". "
				+ "?o4 ?p4 ?label. "
				+ "?label ?p2 ?o2. "
				+ "?label dc:creator \"test\". "
                + "}";
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), update);
			// load rdf
            String fileURL = ConfigProperties.getPropertyParam("http_protocol") + "://" + ConfigProperties.getPropertyParam("host") + "/tests/test.rdf";
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"),ConfigProperties.getPropertyParam("ts_server"), "LOAD <" + fileURL + ">");
			// output
			return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.TestsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }
}
