package info.labeling.v1.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.rdfutils.exceptions.ConfigException;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import de.i3mainz.ls.rdfutils.exceptions.RdfException;
import de.i3mainz.ls.rdfutils.exceptions.ResourceNotAvailableException;
import de.i3mainz.ls.rdfutils.exceptions.UniqueIdentifierException;
import info.labeling.v1.restconfig.PATCH;
import info.labeling.v1.utils.Transformer;
import info.labeling.v1.utils.PropertiesLocal;
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
 * REST API rectCATS items
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 30.06.2016
 */
@Path("/v1/retcats")
public class RetcatsResource {

    @GET
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
    public Response getRetcats(
            @HeaderParam("Accept") String acceptHeader,
            @QueryParam("pretty") boolean pretty,
            @QueryParam("sort") String sort)
            throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // QUERY STRING
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = rdf.getPREFIXSPARQL();
            query += "SELECT * WHERE { "
                    + "?s a ls:RetCAT . "
                    + "?s dc:identifier ?identifier . "
                    + "OPTIONAL { ?s dc:title ?title . } " // because of sorting
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
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            HashSet<String> ids = Sesame2714.getValuesFromBindingSet_UNIQUESET(result, "identifier");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            JSONObject out = new JSONObject();
            JSONObject outObject = new JSONObject();
            JSONArray outArray = new JSONArray();
            for (String element : ids) {
                String item = "ls_ret";
                query = Utils.getAllElementsForItemID(item, element);
                result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
                List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
                List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
                if (result.size() < 1) {
                    throw new ResourceNotAvailableException();
                }
                for (int j = 0; j < predicates.size(); j++) {
                    rdf.setModelTriple(item + ":" + element, predicates.get(j), objects.get(j));
                }
                if (acceptHeader.contains("application/json") || acceptHeader.contains("text/html")) {
                    outObject = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), element);
                    outArray.add(outObject);
                }
                out.put("retcats", outArray);
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
    public Response getRetcat(@PathParam("retcat") String retcat, @HeaderParam("Accept") String acceptHeader, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            if (acceptHeader.contains("application/json")) {
                String out = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), retcat).toJSONString();
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
                String out = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), retcat).toJSONString();
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
                String out = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), retcat).toJSONString();
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}.json")
    @Produces("application/json;charset=UTF-8")
    public Response getRetcat_JSON(@PathParam("retcat") String retcat, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            String out = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), retcat).toJSONString();
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}.xml")
    @Produces("application/xml;charset=UTF-8")
    public Response getRetcat_XML(@PathParam("retcat") String retcat) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}.rdf")
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getRetcatRDF_XML(@PathParam("retcat") String retcat) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}.ttl")
    @Produces("text/turtle;charset=UTF-8")
    public Response getRetcatRDF_Turtle(@PathParam("retcat") String retcat) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("Turtle")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}.n3")
    @Produces("text/n3;charset=UTF-8")
    public Response getRetcatRDF_N3(@PathParam("retcat") String retcat) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("N-Triples")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}.jsonrdf")
    @Produces("application/json;charset=UTF-8")
    public Response getRetcatRDF_JSONRDF(@PathParam("retcat") String retcat, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{retcat}.jsonld")
    @Produces("application/ld+json;charset=UTF-8")
    public Response getRetcatRDF_JSONLD(@PathParam("retcat") String retcat, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_ret";
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response postRetcat(String json) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // get variables
            String item = "ls_ret";
            String itemID = "";
            // parse name
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            JSONObject retcatObject = (JSONObject) jsonObject.get("retcat");
            JSONArray nameArray = (JSONArray) retcatObject.get("title");
            for (Object element : nameArray) {
                itemID = (String) element;
            }
            // create triples
            json = Transformer.retcat_POST(json, itemID);
            String triples = createRetcatSPARQLUPDATE(item, itemID);
            // input triples
            Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), triples);
            // get result als json
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, itemID);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + itemID, predicates.get(i), objects.get(i));
            }
            String out = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), itemID).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PUT
    @Path("/{retcat}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateRetcat(@PathParam("retcat") String retcat, String json)
            throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_ret";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            json = Transformer.retcat_POST(json, retcat);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), putRetcatSPARQLUPDATE(retcat));
            Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            // get result als json
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            String out = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), retcat).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PATCH
    @Path("/{retcat}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateRetcatPATCH(@PathParam("retcat") String retcat, String json)
            throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_ret";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            json = Transformer.retcat_POST(json, retcat);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), patchRetcatSPARQLUPDATE(retcat, json));
            if (!json.contains("flush")) {
                Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            }
            // get result als json
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + retcat, predicates.get(i), objects.get(i));
            }
            String out = Transformer.retcat_GET(rdf.getModel("RDF/JSON"), retcat).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @DELETE
    @Path("/{retcat}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response deleteRetcat(@PathParam("retcat") String retcat) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_ret";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, retcat);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), deleteRetcatSPARQLUPDATE(retcat));
            // get result als json
            String out = Transformer.empty_JSON("retcat").toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    private static String createRetcatSPARQLUPDATE(String item, String itemid) throws ConfigException, IOException, UniqueIdentifierException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " a ls:RetCAT . ";
        triples += item + ":" + itemid + " a dcat:Dataset . ";
        triples += item + ":" + itemid + " dc:identifier \"" + itemid + "\"" + " . ";
		triples += item + ":" + itemid + " dc:title \"" + itemid + "\"" + " . ";
		triples += "ls_ret:catalog a dcat:Catalog . ";
		triples += "ls_ret:catalog dcat:dataset " + item + ":" + itemid + " . ";
		triples += "ls_ret:catalog dc:title \"Labeling System Default Reference Thesaurus Catalog\" . ";
		triples += "ls_ret:catalog dc:description \"Labeling System Default Reference Thesaurus Catalog\" . ";
        triples += " }";
        return triples;
    }    

    private static String putRetcatSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?retcat ?p ?o. } "
                + "WHERE { "
                + "?retcat ?p ?o. "
                + "?retcat dc:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "FILTER (?p IN (dc:description,dct:publisher,dcat:theme,dct:license,dcat:accessURL,ls:retcatsquery,ls:retcatsvar)) "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

    private static String patchRetcatSPARQLUPDATE(String id, String json) throws IOException, ParseException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        JSONObject retcatObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_ret" + ":" + id));
        List<String> deleteList = new ArrayList<String>();
        // for patch
        JSONArray flushArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("flush"));
        if (flushArray != null && !flushArray.isEmpty()) {
            for (Object element : flushArray) {
                // nur optional
            }
        }
        // for else
        JSONArray descriptionArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dc:description"));
        if (descriptionArray != null && !descriptionArray.isEmpty()) {
            deleteList.add("dc:description");
        }
        JSONArray publisherArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dct:publisher"));
        if (publisherArray != null && !publisherArray.isEmpty()) {
            deleteList.add("dct:publisher");
        }
		JSONArray themeArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dcat:theme"));
        if (themeArray != null && !themeArray.isEmpty()) {
            deleteList.add("dcat:theme");
        }
        JSONArray licenseArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dct:license"));
        if (licenseArray != null && !licenseArray.isEmpty()) {
            deleteList.add("dct:license");
        }
        JSONArray endpointArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dcat:accessURL"));
        if (endpointArray != null && !endpointArray.isEmpty()) {
            deleteList.add("dcat:accessURL");
        }
        JSONArray queryArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("ls:retcatsquery"));
        if (queryArray != null && !queryArray.isEmpty()) {
            deleteList.add("ls:retcatsquery");
        }
        JSONArray varArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("ls:retcatsvar"));
        if (varArray != null && !varArray.isEmpty()) {
            deleteList.add("ls:retcatsvar");
        }
        // SEND DELETE
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?retcat ?p ?o. } "
                + "WHERE { "
                + "?retcat ?p ?o. "
                + "?retcat dc:identifier ?identifier. "
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

    private static String deleteRetcatSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?retcat ?p ?o. ?catalog dcat:dataset ?retcat. } "
                + "WHERE { "
                + "?retcat ?p ?o. "
                + "?retcat dc:identifier ?identifier. "
				+ "?catalog dcat:dataset ?retcat. "
                + "FILTER (?identifier=\"$identifier\") "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

}
