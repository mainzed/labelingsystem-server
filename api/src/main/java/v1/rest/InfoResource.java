package v1.rest;

import exceptions.Logging;
import exceptions.SesameSparqlException;
import exceptions.SparqlParseException;
import exceptions.SparqlQueryException;
import java.io.File;
import rdf.RDF;
import rdf.RDF4J_20;
import v1.utils.config.ConfigProperties;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
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
import org.json.simple.JSONObject;

@Path("/info")
public class InfoResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getAPIpage() throws URISyntaxException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, IOException, SparqlQueryException, SparqlParseException {
        JSONObject outObject = new JSONObject();
        try {
            // get last modified data
            File file = new File(InfoResource.class.getClassLoader().getResource("config.properties").getFile());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            outObject.put("last modified", sdf.format(file.lastModified()));
            // get label data
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String query = "";
            List<BindingSet> result = null;
            // metadata
            outObject.put("name", ConfigProperties.getPropertyParam("name"));
            outObject.put("owner", ConfigProperties.getPropertyParam("owner"));
            outObject.put("license", ConfigProperties.getPropertyParam("license"));
            // vocabs
            query = rdf.getPREFIXSPARQL();
            query += "SELECT (COUNT(DISTINCT ?v) AS ?vcount) WHERE { ?v a ls:Vocabulary. }";
            result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
            HashSet<String> count_v = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "vcount");
            query = rdf.getPREFIXSPARQL();
            query += "SELECT (COUNT(DISTINCT ?v) AS ?vcount) WHERE { ?v a ls:Vocabulary. ?v ls:hasReleaseType ls:Public. }";
            result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
            HashSet<String> count_vp = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "vcount");
            JSONObject oVocabs = new JSONObject();
            oVocabs.put("count", Integer.parseInt(count_v.iterator().next()));
            oVocabs.put("public", Integer.parseInt(count_vp.iterator().next()));
            outObject.put("vocabs", oVocabs);
            // labels
            query = rdf.getPREFIXSPARQL();
            query += "SELECT (COUNT(DISTINCT ?l) AS ?lcount) WHERE { ?l a ls:Label. }";
            result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
            HashSet<String> count_l = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "lcount");
            query = rdf.getPREFIXSPARQL();
            query += "SELECT (COUNT(DISTINCT ?l) AS ?lcount) WHERE { ?l a ls:Label. ?l skos:inScheme ?v. ?v ls:hasReleaseType ls:Public. }";
            result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
            HashSet<String> count_lp = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "lcount");
            JSONObject oLabels = new JSONObject();
            oLabels.put("count", Integer.parseInt(count_l.iterator().next()));
            oLabels.put("public", Integer.parseInt(count_lp.iterator().next()));
            outObject.put("labels", oLabels);
            // triples
            query = rdf.getPREFIXSPARQL();
            query += "SELECT (COUNT(?s) AS ?scount) WHERE { ?s ?p ?o. }";
            result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
            HashSet<String> count_s = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "scount");
            outObject.put("triples", Integer.parseInt(count_s.iterator().next()));
            // agents
            query = rdf.getPREFIXSPARQL();
            query += "SELECT (COUNT(DISTINCT ?a) AS ?acount) WHERE { ?a a ls:Agent. }";
            result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
            HashSet<String> count_a = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "acount");
            outObject.put("agents", Integer.parseInt(count_a.iterator().next()));
            // revisions
            query = rdf.getPREFIXSPARQL();
            query += "SELECT (COUNT(DISTINCT ?r) AS ?rcount) WHERE { ?r a ls:Revision. }";
            result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
            HashSet<String> count_r = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "rcount");
            outObject.put("revisions", Integer.parseInt(count_r.iterator().next()));
            return Response.ok(outObject).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.InfoResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

}
