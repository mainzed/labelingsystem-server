package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/sparql")
public class SparqlResource {

    private static String QUERY = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
    private static String FORMAT = "xml";
    private static String CONTENT_TYPE = "application/sparql-results+xml";

    // https://www.w3.org/TR/sparql11-protocol/
    // http://de.slideshare.net/olafhartig/querying-linked-data-with-sparql
    @GET
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/sparql-results+json;charset=UTF-8", "application/sparql-results+xml;charset=UTF-8", "text/plain;charset=UTF-8", "text/csv;charset=UTF-8", "text/tsv;charset=UTF-8"})
    public Response getSPARQLresultsGET(@HeaderParam("Accept") String acceptHeader, @QueryParam("query") String query, @QueryParam("out") String out) {
        try {
            // parse query
            if (query != null) {
                QUERY = query;
                QUERY = URLDecoder.decode(QUERY, "UTF-8");
            } else {
                throw new NullPointerException();
            }
            // set format if requested by var
            if (out != null) {
                if (out.equals("json")) {
                    FORMAT = "json";
                    CONTENT_TYPE = "application/sparql-results+json;charset=UTF-8";
                } else if (out.equals("xml")) {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml;charset=UTF-8";
                } else if (out.equals("csv")) {
                    FORMAT = "csv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else if (out.equals("tsv")) {
                    FORMAT = "tsv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml;charset=UTF-8";
                }
            } else // set format if requested by accept header
            if (acceptHeader != null) {
                if (acceptHeader.contains("application/sparql-results+xml")) {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml;charset=UTF-8";
                } else if (acceptHeader.contains("application/sparql-results+json")) {
                    FORMAT = "json";
                    CONTENT_TYPE = "application/sparql-results+json;charset=UTF-8";
                } else if (acceptHeader.contains("text/csv")) {
                    FORMAT = "csv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else if (acceptHeader.contains("text/tsv")) {
                    FORMAT = "tsv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else if (acceptHeader.contains("application/xml")) {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/xml;charset=UTF-8";
                } else if (acceptHeader.contains("application/json")) {
                    FORMAT = "json";
                    CONTENT_TYPE = "application/json;charset=UTF-8";
                } else {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml";
                }
            }
            // get output
            return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", CONTENT_TYPE).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.SparqlResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }
	
	@POST
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/sparql-results+json;charset=UTF-8", "application/sparql-results+xml;charset=UTF-8", "text/plain;charset=UTF-8", "text/csv;charset=UTF-8", "text/tsv;charset=UTF-8"})
    public Response getSPARQLresultsGET2(@HeaderParam("Accept") String acceptHeader, @FormParam("query") String query, @FormParam("out") String out) {
        try {
            // parse query
            if (query != null) {
                QUERY = query;
                QUERY = URLDecoder.decode(QUERY, "UTF-8");
            } else {
                throw new NullPointerException();
            }
            // set format if requested by var
            if (out != null) {
                if (out.equals("json")) {
                    FORMAT = "json";
                    CONTENT_TYPE = "application/sparql-results+json;charset=UTF-8";
                } else if (out.equals("xml")) {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml;charset=UTF-8";
                } else if (out.equals("csv")) {
                    FORMAT = "csv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else if (out.equals("tsv")) {
                    FORMAT = "tsv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml;charset=UTF-8";
                }
            } else // set format if requested by accept header
            if (acceptHeader != null) {
                if (acceptHeader.contains("application/sparql-results+xml")) {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml;charset=UTF-8";
                } else if (acceptHeader.contains("application/sparql-results+json")) {
                    FORMAT = "json";
                    CONTENT_TYPE = "application/sparql-results+json;charset=UTF-8";
                } else if (acceptHeader.contains("text/csv")) {
                    FORMAT = "csv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else if (acceptHeader.contains("text/tsv")) {
                    FORMAT = "tsv";
                    CONTENT_TYPE = "text/plain;charset=UTF-8";
                } else if (acceptHeader.contains("application/xml")) {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/xml;charset=UTF-8";
                } else if (acceptHeader.contains("application/json")) {
                    FORMAT = "json";
                    CONTENT_TYPE = "application/json;charset=UTF-8";
                } else {
                    FORMAT = "xml";
                    CONTENT_TYPE = "application/sparql-results+xml";
                }
            }
            // get output
            return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", CONTENT_TYPE).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.SparqlResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    private static class FeedReturnStreamingOutput implements StreamingOutput {

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            try {
                Sesame2714.SPARQLqueryOutputFileOS(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()),
                        PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), QUERY, FORMAT, output);
            } catch (Exception e) {
            }
        }
    }

}
