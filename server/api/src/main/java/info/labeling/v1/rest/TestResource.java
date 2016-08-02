package info.labeling.v1.rest;

import info.labeling.exceptions.SesameSparqlException;
import info.labeling.rdf.RDF4J_20M3;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryException;

@Path("/Test")
public class TestResource {

	@GET
	public Response getXml() throws RepositoryException, MalformedQueryException, UpdateExecutionException, SesameSparqlException {
		// UPDATE
		String a = String.valueOf(System.currentTimeMillis());
		//String a = "a";
		RDF4J_20M3.SPARQLupdate("tmp", "http://143.93.114.135/rdf4j-server", "INSERT DATA { <http://subject.info> <http://predicate.info> \"" + "flo" + "\" .}");
		return Response.ok().build();
	}

}
