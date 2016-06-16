package info.labeling.v1.tmp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.rdfutils.exceptions.AutocompleteLengthException;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.PropertiesLocal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.query.BindingSet;

@Path("/v1/autocomplete")
public class AutocompleteResource {

	@GET
	@Produces("application/json;charset=UTF-8")
	public Response getSuggestions(@QueryParam("query") String requestquery, @QueryParam("creator") String creator, @QueryParam("vocabulary") String vocabulary) {
		try {
			String substing = requestquery.toLowerCase();
			substing = URLDecoder.decode(substing, "UTF-8");
			int suggestions = 20;
			// substring in prefLabels
			if (substing.length() <= 1) {
				throw new AutocompleteLengthException();
			} else {
				// QUERY FOR TRIPLESTORE
				RDF rdf = new RDF(PropertiesLocal.getHOST());
				String prefixes = rdf.getPREFIXSPARQL();
				String query = prefixes;
				// START BUILD JSON
				JSONObject jsonobj_query = new JSONObject(); // {}
				if (vocabulary == null && creator == null) { // no filter set
					query += "SELECT * WHERE { "
							+ "?label a ls:Label . "
							+ "?label skos:prefLabel ?prefLabel . "
							+ "?label skos:prefLabel ?prefLabels . "
							+ "?label ls:identifier ?labelIdentifier . "
							+ "OPTIONAL { ?label skos:altLabel ?altLabel . }"
							+ "OPTIONAL { ?label skos:altLabel ?altLabels . }"
							+ "FILTER(regex(?prefLabel, '" + substing + "', 'i') || regex(?altLabel, '" + substing + "', 'i'))"
							+ "} "
							+ "ORDER BY ASC(?prefLabel)"
							+ "LIMIT " + suggestions;
				} else if (creator != null) { // creator filter
					query += "SELECT * WHERE { "
							+ "?label a ls:Label . "
							+ "?label skos:prefLabel ?prefLabel . "
							+ "?label skos:prefLabel ?prefLabels . "
							+ "?label ls:identifier ?labelIdentifier . "
							+ "?label dc:creator ?creator . "
							+ "OPTIONAL { ?label skos:altLabel ?altLabel . }"
							+ "OPTIONAL { ?label skos:altLabel ?altLabels . }"
							+ "FILTER(regex(?prefLabel, '" + substing + "', 'i') || regex(?altLabel, '" + substing + "', 'i'))"
							+ "FILTER(?creator=\""+creator+"\")"
							+ "} "
							+ "ORDER BY ASC(?prefLabel)"
							+ "LIMIT " + suggestions;
				} else if (vocabulary != null) { // vocabulary filter
					query += "SELECT * WHERE { "
							+ "?label a ls:Label . "
							+ "?label skos:prefLabel ?prefLabel . "
							+ "?label skos:prefLabel ?prefLabels . "
							+ "?label ls:identifier ?labelIdentifier . "
							+ "?label skos:inScheme ?vocabulary . "
							+ "OPTIONAL { ?label skos:altLabel ?altLabel . }"
							+ "OPTIONAL { ?label skos:altLabel ?altLabels . }"
							+ "FILTER(regex(?prefLabel, '" + substing + "', 'i') || regex(?altLabel, '" + substing + "', 'i'))"
							+ "FILTER(?vocabulary=ls_voc:"+vocabulary+")"
							+ "} "
							+ "ORDER BY ASC(?prefLabel)"
							+ "LIMIT " + suggestions;
				}
				// EXECUTE QUERY
				List<BindingSet> query_result = Sesame2714.SPARQLquery(PropertiesLocal.getREPOSITORY(), PropertiesLocal.getSESAMESERVER(),query);
				// results
				List<String> query_identifier = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(query_result, "labelIdentifier");
				List<String> query_prefLabel = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(query_result, "prefLabel");
				List<String> query_altLabel = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(query_result, "altLabel");
				// create labeloutput object for each unique id
				HashSet<String> hs = new HashSet<String>();
				for (String query_identifier1 : query_identifier) {
					hs.add(query_identifier1);
				}
				//
				JSONArray jsonarray_suggestions = new JSONArray(); // []
				for(String temp: hs) {
					for (String query_identifier1 : query_identifier) {
						if (temp.equals(query_identifier1)) {
							
						}
					}
					String match = "";
					JSONObject jsonobj_suggestion = new JSONObject(); // {}
					// autocomplete required
					jsonobj_suggestion.put("value", match);
					jsonobj_suggestion.put("data", query_identifier);
					// autocomplete more information
					jsonarray_suggestions.add(jsonobj_suggestion);
				}
				jsonobj_query.put("suggestions", jsonarray_suggestions);
				jsonobj_query.put("query", substing);
				// pretty print JSON output (Gson)
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				return Response.ok(gson.toJson(jsonobj_query)).header("Content-Type", "application/json;charset=UTF-8").build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}
