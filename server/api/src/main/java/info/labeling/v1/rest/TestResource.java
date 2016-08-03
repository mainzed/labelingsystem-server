package info.labeling.v1.rest;

import info.labeling.exceptions.SesameSparqlException;
import info.labeling.rdf.RDF4J_20M3;
import java.util.HashSet;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

@Path("/Test")
public class TestResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response get() throws RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException {
		// UPDATE
		String a = String.valueOf(System.currentTimeMillis());
		//String a = "a";
		RDF4J_20M3.SPARQLupdate("tmp", "http://143.93.114.135/rdf4j-server", "INSERT DATA { <http://subject.info/a> <http://predicate.info/p> \"" + a + "\" .}");
		// QUERY
		List<BindingSet> bs = RDF4J_20M3.SPARQLquery("tmp", "http://143.93.114.135/rdf4j-server", "SELECT * WHERE { ?s <http://predicate.info/p> ?o }");
		List<String> list = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(bs, "s");
		return Response.ok(list.toString()).build();
	}

}
