package info.labeling.v1.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.rdfutils.exceptions.AutocompleteLengthException;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.PropertiesLocal;
import java.net.URLDecoder;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.query.BindingSet;

@Path("/v1/autocomplete")
public class AutocompleteResource {

    @GET
    @Path("/label")
    @Produces("application/json;charset=UTF-8")
    public Response getSuggestionsForLabels(@QueryParam("query") String requestquery) {
        try {
            String substing = requestquery.toLowerCase();
            substing = URLDecoder.decode(substing, "UTF-8");
            int suggestions = 20;
            int minLength = 1;
            if (substing.length() <= minLength) {
                throw new AutocompleteLengthException();
            } else {
                RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
                String query = rdf.getPREFIXSPARQL();
                query += "SELECT * WHERE { "
                        + "?s a ls:Label . "
                        + "?s skos:prefLabel ?acquery . "
                        + "FILTER(regex(?acquery, '" + substing + "', 'i'))"
                        + "} "
                        + "ORDER BY ASC(?acquery)"
                        + "LIMIT " + suggestions;
                List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam("repository"), PropertiesLocal.getPropertyParam("sesame_server"), query);
                List<String> suggestion_uri = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
                List<String> suggestion_string = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "acquery");
                JSONObject jsonobj_query = new JSONObject(); 
                JSONArray jsonarray_suggestions = new JSONArray(); 
                for (int i=0; i<suggestion_uri.size(); i++) {
                    JSONObject jsonobj_suggestion = new JSONObject();
                    jsonobj_suggestion.put("value", suggestion_string.get(i));
                    jsonobj_suggestion.put("data", suggestion_uri.get(i));
                    jsonarray_suggestions.add(jsonobj_suggestion);
                }
                jsonobj_query.put("suggestions", jsonarray_suggestions);
                jsonobj_query.put("query", substing);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(jsonobj_query)).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutocompleteResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }
    
    @GET
    @Path("/label/{filter}/{value}")
    @Produces("application/json;charset=UTF-8")
    public Response getSuggestionsForLabelsFilter(@QueryParam("query") String requestquery, @PathParam("filter") String filter, @PathParam("value") String value) {
        try {
            String substing = requestquery.toLowerCase();
            substing = URLDecoder.decode(substing, "UTF-8");
            int suggestions = 20;
            int minLength = 1;
            if (substing.length() <= minLength) {
                throw new AutocompleteLengthException();
            } else {
                RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
                String query = rdf.getPREFIXSPARQL();
                query += "SELECT * WHERE { "
                        + "?s a ls:Label . "
                        + "?s skos:prefLabel ?acquery . "
                        + "?s dc:creator ?creator . "
                        + "?s skos:inScheme ?vocabulary . "
                        + "FILTER(regex(?acquery, '" + substing + "', 'i'))";
                if (filter.equals("creator")) {
                    query += "FILTER(?creator=\"" + value + "\")";
                } else if (filter.equals("vocabulary")) {
                    query += "FILTER(?vocabulary=<" + rdf.getPrefixItem("ls_voc:" + value) + ">)";
                }
                query += "} "
                        + "ORDER BY ASC(?acquery)"
                        + "LIMIT " + suggestions;

                List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam("repository"), PropertiesLocal.getPropertyParam("sesame_server"), query);
                List<String> suggestion_uri = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
                List<String> suggestion_string = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "acquery");
                JSONObject jsonobj_query = new JSONObject(); 
                JSONArray jsonarray_suggestions = new JSONArray(); 
                for (int i=0; i<suggestion_uri.size(); i++) {
                    JSONObject jsonobj_suggestion = new JSONObject();
                    jsonobj_suggestion.put("value", suggestion_string.get(i));
                    jsonobj_suggestion.put("data", suggestion_uri.get(i));
                    jsonarray_suggestions.add(jsonobj_suggestion);
                }
                jsonobj_query.put("suggestions", jsonarray_suggestions);
                jsonobj_query.put("query", substing);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(jsonobj_query)).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutocompleteResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/agent")
    @Produces("application/json;charset=UTF-8")
    public Response getSuggestionsForAgents(@QueryParam("query") String requestquery) {
        try {
            String substing = requestquery.toLowerCase();
            substing = URLDecoder.decode(substing, "UTF-8");
            int suggestions = 10;
            int minLength = 1;
            if (substing.length() <= minLength) {
                throw new AutocompleteLengthException();
            } else {
                RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
                String query = rdf.getPREFIXSPARQL();
                query += "SELECT * WHERE { "
                        + "?s a ls:Agent . "
                        + "?s foaf:accountName ?acquery . "
                        + "FILTER(regex(?acquery, '" + substing + "', 'i'))";
                query += "} "
                        + "ORDER BY ASC(?acquery) "
                        + "LIMIT " + suggestions;
                List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam("repository"), PropertiesLocal.getPropertyParam("sesame_server"), query);
                List<String> suggestion_uri = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
                List<String> suggestion_string = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "acquery");
                JSONObject jsonobj_query = new JSONObject();
                JSONArray jsonarray_suggestions = new JSONArray();
                for (int i = 0; i < suggestion_uri.size(); i++) {
                    JSONObject jsonobj_suggestion = new JSONObject();
                    jsonobj_suggestion.put("value", suggestion_string.get(i));
                    jsonobj_suggestion.put("data", suggestion_uri.get(i));
                    jsonarray_suggestions.add(jsonobj_suggestion);
                }
                jsonobj_query.put("suggestions", jsonarray_suggestions);
                jsonobj_query.put("query", substing);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(jsonobj_query)).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutocompleteResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/vocabulary")
    @Produces("application/json;charset=UTF-8")
    public Response getSuggestionsForVocabs(@QueryParam("query") String requestquery) {
        try {
            String substing = requestquery.toLowerCase();
            substing = URLDecoder.decode(substing, "UTF-8");
            int suggestions = 10;
            int minLength = 1;
            if (substing.length() <= minLength) {
                throw new AutocompleteLengthException();
            } else {
                RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
                String query = rdf.getPREFIXSPARQL();
                query += "SELECT * WHERE { "
                        + "?s a ls:Vocabulary . "
                        + "?s dc:title ?acquery . "
                        + "FILTER(regex(?acquery, '" + substing + "', 'i'))";
                query += "} "
                        + "ORDER BY ASC(?acquery) "
                        + "LIMIT " + suggestions;
                List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam("repository"), PropertiesLocal.getPropertyParam("sesame_server"), query);
                List<String> suggestion_uri = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
                List<String> suggestion_string = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "acquery");
                JSONObject jsonobj_query = new JSONObject();
                JSONArray jsonarray_suggestions = new JSONArray();
                for (int i = 0; i < suggestion_uri.size(); i++) {
                    JSONObject jsonobj_suggestion = new JSONObject();
                    jsonobj_suggestion.put("value", suggestion_string.get(i));
                    jsonobj_suggestion.put("data", suggestion_uri.get(i));
                    jsonarray_suggestions.add(jsonobj_suggestion);
                }
                jsonobj_query.put("suggestions", jsonarray_suggestions);
                jsonobj_query.put("query", substing);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(jsonobj_query)).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutocompleteResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

}
