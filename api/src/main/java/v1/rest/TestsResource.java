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
import v1.utils.db.SQlite;

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
                + "DELETE { ?revision ?p2 ?o. } "
                + "WHERE { "
				+ "?revision ?p2 ?o. "
				+ "?revision a ls:Revision. "
				+ "?label ?p ?revision. "
				+ "?label dc:creator \"test\". "
                + "}";
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), update);
			update = prefixes
                + "DELETE { ?o3 ?p3 ?vocab. } "
                + "WHERE { "
                + "?o3 ?p3 ?vocab. "
				+ "?vocab ?p1 ?o1. "
				+ "?vocab dc:creator \"test\". "
                + "}";
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), update);
			update = prefixes
                + "DELETE { ?o4 ?p4 ?label. } "
                + "WHERE { "
				+ "?o4 ?p4 ?label. "
				+ "?label ?p2 ?o2. "
				+ "?label dc:creator \"test\". "
                + "}";
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), update);
			update = prefixes
                + "DELETE { ?vocab ?p1 ?o1. ?label ?p2 ?o2. } "
                + "WHERE { "
				+ "?vocab ?p1 ?o1. "
				+ "?vocab dc:creator \"test\". "
				+ "?label ?p2 ?o2. "
				+ "?label dc:creator \"test\". "
                + "}";
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), update);
			// load rdf
            String fileURL = ConfigProperties.getPropertyParam("http_protocol") + "://" + ConfigProperties.getPropertyParam("host") + "/tests/test.rdf";
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"),ConfigProperties.getPropertyParam("ts_server"), "LOAD <" + fileURL + ">");
			// delete from sqlite db
			SQlite.deleteRetcatEntryForList("2420a664-7b1c-4da6-aba3-d0694221ee8a");
			SQlite.deleteRetcatEntry("2420a664-7b1c-4da6-aba3-d0694221ee8a");
			SQlite.deleteRetcatEntryForList("b82b65ba-f75f-4018-b10c-4cfb227aeddd");
			SQlite.deleteRetcatEntry("b82b65ba-f75f-4018-b10c-4cfb227aeddd");
			SQlite.deleteRetcatEntryForList("ed089ba2-27f1-4a72-a769-78e98704ce36");
			SQlite.deleteRetcatEntry("ed089ba2-27f1-4a72-a769-78e98704ce36");
			// output
			return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.TestsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }
}
