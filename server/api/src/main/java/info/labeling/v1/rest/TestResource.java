package info.labeling.v1.rest;

import info.labeling.exceptions.SesameSparqlException;
import info.labeling.rdf.RDF4J_20M3;
import info.labeling.v1.utils.ConfigProperties;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

@Path("/Test")
public class TestResource {

	public static String jsonstring = "[{\"creator\":\"demo\",\"statusType\":\"active\",\"created\":\"2016-07-25T14:30:52.512+0200\",\"description\":{\"lang\":\"de\",\"value\":\"Beispielvokabular mit Zeiten von der CAA 2015 in Siena\"},\"title\":{\"lang\":\"de\",\"value\":\"Zeiten\"},\"license\":\"http://creativecommons.org/licenses/by/4.0/\",\"releaseType\":\"public\",\"contributors\":[\"demo\"],\"id\":\"7e34e500-53e8-48a3-b20c-aa6c8faee743\",\"revisionIDs\":[\"d59ea762-27ce-429e-95ae-2713d77c012f\"]},{\"creator\":\"demo\",\"statusType\":\"active\",\"created\":\"2016-07-26T08:56:56.416+0200\",\"description\":{\"lang\":\"de\",\"value\":\"vocabulary example containing bones presented at CAA 2015 in Siena\"},\"title\":{\"lang\":\"en\",\"value\":\"bones\"},\"license\":\"http://creativecommons.org/licenses/by/4.0/\",\"releaseType\":\"public\",\"contributors\":[\"demo\"],\"id\":\"8dec8019-7295-4928-95dc-16a4fb321704\",\"revisionIDs\":[\"ae7780eb-8fb9-422f-84d0-7764560b44b7\"]},{\"creator\":\"demo\",\"statusType\":\"active\",\"created\":\"2016-07-26T13:45:07.458+0200\",\"description\":{\"lang\":\"de\",\"value\":\"Klassifizierung für Vornamenvarianten\"},\"title\":{\"lang\":\"de\",\"value\":\"Klassifizierung\"},\"license\":\"http://creativecommons.org/licenses/by/4.0/\",\"releaseType\":\"public\",\"contributors\":[\"demo\"],\"id\":\"175c24d2-35dd-44a8-a6e6-162ed0b9aba3\",\"revisionIDs\":[\"b0efa381-b23d-472f-9bad-d9525880c2ba\"]},{\"creator\":\"demo\",\"statusType\":\"active\",\"created\":\"2016-07-25T13:40:29.267+0200\",\"description\":{\"lang\":\"de\",\"value\":\"Vornamenvokabular der Schweizer Rechtsquellenstiftung\"},\"title\":{\"lang\":\"de\",\"value\":\"Vornamen\"},\"license\":\"http://creativecommons.org/licenses/by/4.0/\",\"releaseType\":\"public\",\"contributors\":[\"demo\"],\"id\":\"fec825cf-4201-4311-955a-11bd5df7e992\",\"revisionIDs\":[\"a9245e4c-9929-4920-b7d4-16e127329ee6\"]}]";

	@GET
	@Produces("application/json;charset=UTF-8")
	public Response get(@HeaderParam("Accept-Encoding") String acceptEncoding) throws RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException {
		// UPDATE
		//String a = String.valueOf(System.currentTimeMillis());
		//String a = "a";
		//RDF4J_20M3.SPARQLupdate("tmp", "http://143.93.114.135/rdf4j-server", "INSERT DATA { <http://subject.info/a> <http://predicate.info/p> \"" + a + "\" .}");
		// QUERY
		//List<BindingSet> bs = RDF4J_20M3.SPARQLquery("tmp", "http://143.93.114.135/rdf4j-server", "SELECT * WHERE { ?s <http://predicate.info/p> ?o }");
		//List<String> list = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(bs, "s");
		int z = 0;
		if (acceptEncoding.contains("gzip")) {
			return Response.ok(new FeedReturnStreamingOutput()).header("Content-Encoding", "gzip").build();
		} else {
			return Response.ok(jsonstring).build();
		}
	}

	private static class FeedReturnStreamingOutput implements StreamingOutput {

		@Override
		public void write(OutputStream output) throws IOException, WebApplicationException {
			try {
				output = GZIP(jsonstring, output);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	private static OutputStream GZIP(String input, OutputStream baos) throws IOException {
		try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
			gzos.write(input.getBytes("UTF-8"));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return baos;
	}

}
