package info.labeling.v1.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.labeling.rdf.RDF;
import info.labeling.rdf.Sesame2714;
import info.labeling.exceptions.ConfigException;
import info.labeling.exceptions.Logging;
import info.labeling.exceptions.RdfException;
import info.labeling.exceptions.ResourceNotAvailableException;
import info.labeling.exceptions.UniqueIdentifierException;
import info.labeling.v1.restconfig.PATCH;
import info.labeling.v1.utils.Transformer;
import info.labeling.v1.utils.ConfigProperties;
import info.labeling.v1.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom.JDOMException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.query.BindingSet;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * REST API for Vocabularies
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 27.06.2016
 */
@Path("/agents")
public class AgentsResource {

    @GET
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
    public Response getAgents(
            @HeaderParam("Accept") String acceptHeader,
            @QueryParam("pretty") boolean pretty,
            @QueryParam("sort") String sort)
            throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // QUERY STRING
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String query = rdf.getPREFIXSPARQL();
            query += "SELECT * WHERE { "
                    + "?s a ls:Agent . "
                    + "?s dc:identifier ?identifier . "
                    + "OPTIONAL { ?s foaf:accountName ?name . } " // because of sorting
                    + "OPTIONAL { ?s foaf:firstName ?firstName . } " // because of sorting
                    + "OPTIONAL { ?s foaf:lastName ?lastName . } " // because of sorting
                    + " } ";
            // SORTING
            List<String> sortList = new ArrayList<String>();
            if (sort != null) {
                String sortArray[] = sort.split(",");
                for (String element : sortArray) {
                    if (sort != null) {
                        String sortDirection = element.substring(0, 1);
                        if (sortDirection.equals("+")) {
                            sortDirection = "ASC";
                        } else {
                            sortDirection = "DESC";
                        }
                        element = element.substring(1);
                        sortList.add(sortDirection + "(?" + element + ") ");
                    }
                }
                query += "ORDER BY ";
                for (String element : sortList) {
                    query += element;
                }
            }
            // QUERY TRIPLESTORE
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            HashSet<String> ids = Sesame2714.getValuesFromBindingSet_UNIQUESET(result, "identifier");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            JSONObject out = new JSONObject();
            JSONObject outObject = new JSONObject();
            JSONArray outArray = new JSONArray();
            for (String element : ids) {
                String item = "ls_age";
                query = Utils.getAllElementsForItemID(item, element);
                result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
                List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
                List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
                if (result.size() < 1) {
                    throw new ResourceNotAvailableException();
                }
                for (int j = 0; j < predicates.size(); j++) {
                    rdf.setModelTriple(item + ":" + element, predicates.get(j), objects.get(j));
                }
                if (acceptHeader.contains("application/json") || acceptHeader.contains("text/html")) {
                    outObject = Transformer.agent_GET(rdf.getModel("RDF/JSON"), element);
                    outArray.add(outObject);
                }
                out.put("agents", outArray);
            }
            if (acceptHeader.contains("application/json")) {
                if (pretty) {
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(out.toString()).getAsJsonObject();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    return Response.ok(gson.toJson(json)).build();
                } else {
                    return Response.ok(out).build();
                }
            } else if (acceptHeader.contains("application/rdf+json")) {
                return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
            } else if (acceptHeader.contains("text/html")) {
                if (pretty) {
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(out.toString()).getAsJsonObject();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
                } else {
                    return Response.ok(out).header("Content-Type", "application/json;charset=UTF-8").build();
                }
            } else if (acceptHeader.contains("application/xml")) {
                return Response.ok(rdf.getModel("RDF/XML")).build();
            } else if (acceptHeader.contains("application/rdf+xml")) {
                return Response.ok(rdf.getModel("RDF/XML")).build();
            } else if (acceptHeader.contains("text/turtle")) {
                return Response.ok(rdf.getModel("Turtle")).build();
            } else if (acceptHeader.contains("text/n3")) {
                return Response.ok(rdf.getModel("N-Triples")).build();
            } else if (acceptHeader.contains("application/ld+json")) {
                return Response.ok(rdf.getModel("JSON-LD")).build();
            } else if (pretty) {
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(out.toString()).getAsJsonObject();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.ok(out).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
    public Response getAgent(@PathParam("agent") String agent, @HeaderParam("Accept") String acceptHeader, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            if (acceptHeader.contains("application/json")) {
                String out = Transformer.agent_GET(rdf.getModel("RDF/JSON"), agent).toJSONString();
                if (pretty) {
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(out).getAsJsonObject();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    return Response.ok(gson.toJson(json)).build();
                } else {
                    return Response.ok(out).build();
                }
            } else if (acceptHeader.contains("application/rdf+json")) {
                return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
            } else if (acceptHeader.contains("text/html")) {
                String out = Transformer.agent_GET(rdf.getModel("RDF/JSON"), agent).toJSONString();
                if (pretty) {
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(out).getAsJsonObject();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
                } else {
                    return Response.ok(out).header("Content-Type", "application/json;charset=UTF-8").build();
                }
            } else if (acceptHeader.contains("application/xml")) {
                return Response.ok(rdf.getModel("RDF/XML")).build();
            } else if (acceptHeader.contains("application/rdf+xml")) {
                return Response.ok(rdf.getModel("RDF/XML")).build();
            } else if (acceptHeader.contains("text/turtle")) {
                return Response.ok(rdf.getModel("Turtle")).build();
            } else if (acceptHeader.contains("text/n3")) {
                return Response.ok(rdf.getModel("N-Triples")).build();
            } else if (acceptHeader.contains("application/ld+json")) {
                return Response.ok(rdf.getModel("JSON-LD")).build();
            } else {
                String out = Transformer.agent_GET(rdf.getModel("RDF/JSON"), agent).toJSONString();
                if (pretty) {
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(out).getAsJsonObject();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
                } else {
                    return Response.ok(out).header("Content-Type", "application/json;charset=UTF-8").build();
                }
            }
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}.json")
    @Produces("application/json;charset=UTF-8")
    public Response getAgent_JSON(@PathParam("agent") String agent, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            String out = Transformer.agent_GET(rdf.getModel("RDF/JSON"), agent).toJSONString();
            if (pretty) {
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(out).getAsJsonObject();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.ok(out).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}.xml")
    @Produces("application/xml;charset=UTF-8")
    public Response getAgent_XML(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}.rdf")
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getAgentRDF_XML(@PathParam("agent") String agent) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}.ttl")
    @Produces("text/turtle;charset=UTF-8")
    public Response getAgentRDF_Turtle(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("Turtle")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}.n3")
    @Produces("text/n3;charset=UTF-8")
    public Response getAgentRDF_N3(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("N-Triples")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}.jsonrdf")
    @Produces("application/json;charset=UTF-8")
    public Response getAgentRDF_JSONRDF(@PathParam("agent") String agent, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            String out = rdf.getModel("RDF/JSON");
            if (pretty) {
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(out).getAsJsonObject();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.ok(out).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{agent}.jsonld")
    @Produces("application/ld+json;charset=UTF-8")
    public Response getAgentRDF_JSONLD(@PathParam("agent") String agent, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String item = "ls_age";
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            String out = rdf.getModel("JSON-LD");
            if (pretty) {
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(out).getAsJsonObject();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.ok(out).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response postAgent(String json) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // get variables
            String item = "ls_age";
            String itemID = "";
            // parse name
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            JSONObject agentObject = (JSONObject) jsonObject.get("agent");
            JSONArray nameArray = (JSONArray) agentObject.get("name");
            for (Object element : nameArray) {
                itemID = (String) element;
            }
            // create triples
            json = Transformer.agent_POST(json, itemID);
            String triples = createAgentSPARQLUPDATE(item, itemID);
            // input triples
            Sesame2714.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), json);
            Sesame2714.SPARQLupdate(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), triples);
            // get result als json
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, itemID);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + itemID, predicates.get(i), objects.get(i));
            }
            String out = Transformer.agent_GET(rdf.getModel("RDF/JSON"), itemID).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PUT
    @Path("/{agent}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateAgent(@PathParam("agent") String agent, String json)
            throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_age";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            json = Transformer.agent_POST(json, agent);
            Sesame2714.SPARQLupdate(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), putAgentSPARQLUPDATE(agent));
            Sesame2714.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), json);
            // get result als json
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            String out = Transformer.agent_GET(rdf.getModel("RDF/JSON"), agent).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PATCH
    @Path("/{agent}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateAgentPATCH(@PathParam("agent") String agent, String json)
            throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_age";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            json = Transformer.agent_POST(json, agent);
            Sesame2714.SPARQLupdate(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), patchAgentSPARQLUPDATE(agent, json));
            if (!json.contains("flush")) {
                Sesame2714.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), json);
            }
            // get result als json
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            String out = Transformer.agent_GET(rdf.getModel("RDF/JSON"), agent).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @DELETE
    @Path("/{agent}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response deleteAgent(@PathParam("agent") String agent) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_age";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, agent);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            Sesame2714.SPARQLupdate(ConfigProperties.getPropertyParam(ConfigProperties.getREPOSITORY()), ConfigProperties.getPropertyParam(ConfigProperties.getSESAMESERVER()), deleteAgentSPARQLUPDATE(agent));
            // get result als json
            String out = Transformer.empty_JSON("agent").toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AgentsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    private static String createAgentSPARQLUPDATE(String item, String itemid) throws ConfigException, IOException, UniqueIdentifierException {
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " a ls:Agent . ";
        triples += item + ":" + itemid + " a foaf:Agent . ";
        triples += item + ":" + itemid + " dc:identifier \"" + itemid + "\"" + " . ";
		triples += item + ":" + itemid + " foaf:accountName \"" + itemid + "\"" + " . ";
        triples += " }";
        return triples;
    }    

    private static String putAgentSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?agent ?p ?o. } "
                + "WHERE { "
                + "?agent ?p ?o. "
                + "?agent dc:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "FILTER (?p IN (foaf:mbox,foaf:firstName,foaf:lastName,foaf:homepage,foaf:img,geo:lat,geo:lon)) "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

    private static String patchAgentSPARQLUPDATE(String id, String json) throws IOException, ParseException {
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        JSONObject agentObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_age" + ":" + id));
        List<String> deleteList = new ArrayList<String>();
        // for patch
        JSONArray flushArray = (JSONArray) agentObject.get(rdf.getPrefixItem("flush"));
        if (flushArray != null && !flushArray.isEmpty()) {
            for (Object element : flushArray) {
                // nur optional
                if (element.equals("homepage")) {
                    deleteList.add("foaf:homepage");
                } else if (element.equals("img")) {
                    deleteList.add("foaf:img");
                } else if (element.equals("lat")) {
                    deleteList.add("geo:lat");
                } else if (element.equals("lon")) {
                    deleteList.add("geo:lon");
                }
            }
        }
        // for else
        JSONArray mboxArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:mbox"));
        if (mboxArray != null && !mboxArray.isEmpty()) {
            deleteList.add("foaf:mbox");
        }
        JSONArray firstNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:firstName"));
        if (firstNameArray != null && !firstNameArray.isEmpty()) {
            deleteList.add("foaf:firstName");
        }
        JSONArray lastNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:lastName"));
        if (lastNameArray != null && !lastNameArray.isEmpty()) {
            deleteList.add("foaf:lastName");
        }
        JSONArray homepageArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:homepage"));
        if (homepageArray != null && !homepageArray.isEmpty()) {
            deleteList.add("foaf:homepage");
        }
        JSONArray imgArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:img"));
        if (imgArray != null && !imgArray.isEmpty()) {
            deleteList.add("foaf:img");
        }
        JSONArray latArray = (JSONArray) agentObject.get(rdf.getPrefixItem("geo:lat"));
        if (latArray != null && !latArray.isEmpty()) {
            deleteList.add("geo:lat");
        }
        JSONArray lonArray = (JSONArray) agentObject.get(rdf.getPrefixItem("geo:lon"));
        if (lonArray != null && !lonArray.isEmpty()) {
            deleteList.add("geo:lon");
        }
        // SEND DELETE
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?agent ?p ?o. } "
                + "WHERE { "
                + "?agent ?p ?o. "
                + "?agent dc:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") ";

        update += "FILTER (?p IN (";
        for (String element : deleteList) {
            update += element + ",";
        }
        update = update.substring(0, update.length() - 1);
        update += ")) }";
        update = update.replace("$identifier", id);
        return update;
    }

    private static String deleteAgentSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?agent ?p ?o. } "
                + "WHERE { "
                + "?agent ?p ?o. "
                + "?agent dc:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

}
