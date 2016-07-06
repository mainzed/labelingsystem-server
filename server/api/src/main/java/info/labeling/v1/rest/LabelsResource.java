package info.labeling.v1.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.identifier.UniqueIdentifier;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
 * @version 23.06.2016
 */
@Path("/v1/labels")
public class LabelsResource {

    @GET
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
    public Response getLabels(
            @HeaderParam("Accept") String acceptHeader,
            @QueryParam("pretty") boolean pretty,
            @QueryParam("sort") String sort,
            @QueryParam("fields") String fields,
            @QueryParam("offset") String offset,
            @QueryParam("limit") String limit,
            @QueryParam("creator") String creator,
            @QueryParam("contributor") String contributor,
            @QueryParam("prefLabel") String prefLabel,
            @QueryParam("vocab") String vocab,
            @QueryParam("context") String context,
            @QueryParam("statusType") String statusType)
            throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // QUERY STRING
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = rdf.getPREFIXSPARQL();
            query += "SELECT * WHERE { "
                    + "?s a ls:Label . "
                    + "?s dc:identifier ?identifier . "
                    + "OPTIONAL { ?s dc:creator ?creator . } " // because of sorting, filtering
                    + "OPTIONAL { ?s dc:contributor ?contributor . } " // because of sorting, filtering
                    + "OPTIONAL { ?s skos:prefLabel ?prefLabel . } " // because of sorting, filtering
                    + "OPTIONAL { ?s skos:inScheme ?vocab . } " // because of filtering
                    + "OPTIONAL { ?s ls:hasContext ?context . } " // because of filtering
                    + "OPTIONAL { ?s ls:hasStatusType ?statusType . } "; // because of filtering
            // FILTERING
            if (creator != null) {
                query += "FILTER(?creator=\"" + creator + "\") ";
            }
            if (contributor != null) {
                query += "FILTER(?contributor=\"" + contributor + "\") ";
            }
            if (statusType != null) {
                query += "FILTER(?statusType=<" + rdf.getPrefixItem("ls:" + statusType) + ">) ";
            }
            if (vocab != null) {
                query += "FILTER(?vocab=<" + rdf.getPrefixItem("ls_voc:" + vocab) + ">) ";
            }
            if (context != null) {
                query += "FILTER(?context=\"" + context + "\") ";
            }
            query += " } ";
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
            // PAGING
            if (limit != null && offset != null) {
                query += "LIMIT " + limit + " OFFSET " + offset;
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
                String item = "ls_lab";
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
                    outObject = Transformer.label_GET(rdf.getModel("RDF/JSON"), element, fields);
                    outArray.add(outObject);
                }
                out.put("labels", outArray);
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
    public Response getLabel(@PathParam("label") String label, @HeaderParam("Accept") String acceptHeader, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            if (acceptHeader.contains("application/json")) {
                String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null).toJSONString();
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
                String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null).toJSONString();
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
                String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null).toJSONString();
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}.json")
    @Produces("application/json;charset=UTF-8")
    public Response getLabel_JSON(@PathParam("label") String label, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null).toJSONString();
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}.xml")
    @Produces("application/xml;charset=UTF-8")
    public Response getLabel_XML(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}.rdf")
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getLabelRDF_XML(@PathParam("label") String label) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}.ttl")
    @Produces("text/turtle;charset=UTF-8")
    public Response getLabelRDF_Turtle(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("Turtle")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}.n3")
    @Produces("text/n3;charset=UTF-8")
    public Response getLabelRDF_N3(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("N-Triples")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}.jsonrdf")
    @Produces("application/json;charset=UTF-8")
    public Response getLabelRDF_JSONRDF(@PathParam("label") String label, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @GET
    @Path("/{label}.jsonld")
    @Produces("application/ld+json;charset=UTF-8")
    public Response getLabelRDF_JSONLD(@PathParam("label") String label, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_lab";
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
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
                return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
        }
    }

    @POST
    @Path("/user/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response postLabel(@PathParam("user") String user, String json) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // get variables
            String item = "ls_lab";
            String itemID = UniqueIdentifier.getUUID();
            // create triples
            json = Transformer.label_POST(json, itemID);
            String triples = createLabelSPARQLUPDATE(item, itemID, user);
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
            String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), itemID, null).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PUT
    @Path("/{label}/user/{user}/type/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateLabel(@PathParam("label") String label, @PathParam("user") String user, @PathParam("type") String type, String json)
            throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_lab";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            json = Transformer.label_POST(json, label);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), putLabelREVISION(item, label, user, type));
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), putLabelSPARQLUPDATE(label));
            Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            // get result als json
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PATCH
    @Path("/{label}/user/{user}/type/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateLabelPATCH(@PathParam("label") String label, @PathParam("user") String user, @PathParam("type") String type, String json)
            throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_lab";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            json = Transformer.label_POST(json, label);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), putLabelREVISION(item, label, user, type));
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), patchLabelSPARQLUPDATE(label, json));
            if (!json.contains("flush")) {
                Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            }
            // get result als json
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @DELETE
    @Path("/{label}/user/{user}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response deleteLabel(@PathParam("label") String label, @PathParam("user") String user) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String item = "ls_lab";
            // check if resource exists
            String queryExist = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> resultExist = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), queryExist);
            if (resultExist.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            // insert data
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), deleteLabelREVISION(item, label, user));
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), deleteLabelSPARQLUPDATE(label));
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), deleteLabelStatusTypeSPARQLUPDATE(label));
            // get result als json
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = Utils.getAllElementsForItemID(item, label);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
            }
            String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null).toJSONString();
            return Response.status(Response.Status.CREATED).entity(out).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.LabelsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    private static String createLabelSPARQLUPDATE(String item, String itemid, String user) throws ConfigException, IOException, UniqueIdentifierException {
        String revID = UniqueIdentifier.getUUID();
        Calendar calender = Calendar.getInstance();
        Date date = calender.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateiso = formatter.format(date);
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " a ls:Label . ";
        triples += item + ":" + itemid + " a skos:ConceptScheme . ";
        triples += item + ":" + itemid + " ls:hasStatusType ls:Active . ";
        triples += item + ":" + itemid + " ls:sameAs "
                + "<" + PropertiesLocal.getPropertyParam("ls_detailhtml")
                .replace("$host", PropertiesLocal.getPropertyParam("host"))
                .replace("$itemid", itemid).replace("$item", "label") + ">" + " . ";
        triples += item + ":" + itemid + " dc:creator \"" + user + "\"" + " . ";
        triples += item + ":" + itemid + " dct:creator ls_age:" + user + " . ";
        triples += item + ":" + itemid + " dc:contributor \"" + user + "\"" + " . ";
        triples += item + ":" + itemid + " dct:contributor ls_age:" + user + " . ";
        triples += item + ":" + itemid + " dc:identifier \"" + itemid + "\"" + " . ";
        triples += item + ":" + itemid + " dct:license <http://creativecommons.org/licenses/by/4.0/> . ";
        triples += item + ":" + itemid + " dc:created \"" + dateiso + "\"" + " . ";
        triples += item + ":" + itemid + " skos:changeNote ls_rev:" + revID + " . ";
        triples += "ls_rev" + ":" + revID + " a ls:Revision . ";
        triples += "ls_rev" + ":" + revID + " a prov:Activity . ";
        triples += "ls_rev" + ":" + revID + " a prov:Modify . ";
        triples += "ls_rev" + ":" + revID + " dc:identifier \"" + revID + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dc:creator \"" + user + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:creator ls_age:" + user + " . ";
        triples += "ls_rev" + ":" + revID + " dc:description \"" + "CreateRevision" + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:type ls:CreateRevision . ";
        triples += "ls_rev" + ":" + revID + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
        triples += " }";
        return triples;
    }

    private static String putLabelREVISION(String item, String itemid, String user, String type) throws ConfigException, IOException, UniqueIdentifierException {
        String revID = UniqueIdentifier.getUUID();
        Calendar calender = Calendar.getInstance();
        Date date = calender.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateiso = formatter.format(date);
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " dc:modified \"" + dateiso + "\"" + " . ";
        triples += item + ":" + itemid + " skos:changeNote ls_rev:" + revID + " . ";
        triples += "ls_rev" + ":" + revID + " a ls:Revision . ";
        triples += "ls_rev" + ":" + revID + " a prov:Activity . ";
        triples += "ls_rev" + ":" + revID + " a prov:Modify . ";
        triples += "ls_rev" + ":" + revID + " dc:identifier \"" + revID + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dc:creator \"" + user + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:creator ls_age:" + user + " . ";
        triples += "ls_rev" + ":" + revID + " dc:description \"" + type + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:type ls:" + type + " . ";
        triples += "ls_rev" + ":" + revID + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
        triples += " }";
        return triples;
    }

    private static String deleteLabelREVISION(String item, String itemid, String user) throws ConfigException, IOException, UniqueIdentifierException {
        String revID = UniqueIdentifier.getUUID();
        Calendar calender = Calendar.getInstance();
        Date date = calender.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateiso = formatter.format(date);
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " ls:hasStatusType ls:Deleted . ";
        triples += item + ":" + itemid + " skos:changeNote ls_rev:" + revID + " . ";
        triples += "ls_rev" + ":" + revID + " a ls:Revision . ";
        triples += "ls_rev" + ":" + revID + " a prov:Activity . ";
        triples += "ls_rev" + ":" + revID + " a prov:Modify . ";
        triples += "ls_rev" + ":" + revID + " dc:identifier \"" + revID + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dc:creator \"" + user + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:creator ls_age:" + user + " . ";
        triples += "ls_rev" + ":" + revID + " dc:description \"" + "DeleteRevision" + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:type ls:" + "DeleteRevision" + " . ";
        triples += "ls_rev" + ":" + revID + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
        triples += " }";
        return triples;
    }

    private static String putLabelSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?label ?p ?o. ?resource skos:broader ?label . ?resource skos:narrower ?label .} "
                + "WHERE { "
                + "?label ?p ?o. "
                + "?label dc:identifier ?identifier. "
                // filter delete all broader/narrower wo ?label das OBJECT ist
                + "OPTIONAL { ?resource skos:broader ?label . } "
                + "OPTIONAL { ?resource skos:narrower ?label . } "
                + "FILTER (?identifier=\"$identifier\") "
                + "FILTER (?p IN (dct:contributor,dc:contributor,skos:prefLabel,skos:altLabel,skos:note,skos:definition,ls:preferredLabel,ls:preferredLang,ls:hasContext,skos:related,skos:broader,skos:narrower,skos:closeMatch,skos:exactMatch,skos:relatedMatch,skos:narrowMatch,skos:broadMatch,rdfs:seeAlso,rdfs:isDefinedBy,owl:sameAs,skos:inScheme)) "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

    private static String patchLabelSPARQLUPDATE(String id, String json) throws IOException, ParseException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        JSONObject labelObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_lab" + ":" + id));
        List<String> deleteList = new ArrayList<String>();
        // for patch
        JSONArray flushArray = (JSONArray) labelObject.get(rdf.getPrefixItem("flush"));
        if (flushArray != null && !flushArray.isEmpty()) {
            for (Object element : flushArray) {
                // nur optional
                if (element.equals("altLabel")) {
                    deleteList.add("skos:altLabel");
                } else if (element.equals("note")) {
                    deleteList.add("skos:note");
                } else if (element.equals("definition")) {
                    deleteList.add("skos:definition");
                } else if (element.equals("context")) {
                    deleteList.add("ls:context");
                } else if (element.equals("related")) {
                    deleteList.add("skos:related");
                } else if (element.equals("broader")) {
                    deleteList.add("skos:broader");
                } else if (element.equals("narrower")) {
                    deleteList.add("skos:narrower");
                } else if (element.equals("closeMatch")) {
                    deleteList.add("skos:closeMatch");
                } else if (element.equals("exactMatch")) {
                    deleteList.add("skos:exactMatch");
                } else if (element.equals("relatedMatch")) {
                    deleteList.add("skos:relatedMatch");
                } else if (element.equals("narrowMatch")) {
                    deleteList.add("skos:narrowMatch");
                } else if (element.equals("broadMatch")) {
                    deleteList.add("skos:broadMatch");
                } else if (element.equals("seeAlso")) {
                    deleteList.add("rdfs:seeAlso");
                } else if (element.equals("definedBy")) {
                    deleteList.add("rdfs:isDefinedBy");
                } else if (element.equals("sameAs")) {
                    deleteList.add("owl:sameAs");
                }
            }
        }
        // for else
        JSONArray contributorArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:contributor"));
        if (contributorArray != null && !contributorArray.isEmpty()) {
            deleteList.add("dct:contributor");
            deleteList.add("dc:contributor");
        }
        JSONArray prefLabelArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:prefLabel"));
        if (prefLabelArray != null && !prefLabelArray.isEmpty()) {
            deleteList.add("skos:prefLabel");
        }
        JSONArray altLabelArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:altLabel"));
        if (altLabelArray != null && !altLabelArray.isEmpty()) {
            deleteList.add("skos:altLabel");
        }
        JSONArray noteArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:note"));
        if (noteArray != null && !noteArray.isEmpty()) {
            deleteList.add("skos:note");
        }
        JSONArray definitionArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:definition"));
        if (definitionArray != null && !definitionArray.isEmpty()) {
            deleteList.add("skos:definition");
        }
        JSONArray preferredLabelArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:preferredLabel"));
        if (preferredLabelArray != null && !preferredLabelArray.isEmpty()) {
            deleteList.add("ls:preferredLabel");
        }
        JSONArray preferredLangArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:preferredLang"));
        if (preferredLangArray != null && !preferredLangArray.isEmpty()) {
            deleteList.add("ls:preferredLang");
        }
        JSONArray contextArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:context"));
        if (contextArray != null && !contextArray.isEmpty()) {
            deleteList.add("ls:context");
        }
        JSONArray relatedArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:related"));
        if (relatedArray != null && !relatedArray.isEmpty()) {
            deleteList.add("skos:related");
        }
        JSONArray broaderArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:broader"));
        if (broaderArray != null && !broaderArray.isEmpty()) {
            deleteList.add("skos:broader");
        }
        JSONArray narrowerArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:narrower"));
        if (narrowerArray != null && !narrowerArray.isEmpty()) {
            deleteList.add("skos:narrower");
        }
        JSONArray closeMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:closeMatch"));
        if (closeMatchArray != null && !closeMatchArray.isEmpty()) {
            deleteList.add("skos:closeMatch");
        }
        JSONArray exactMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:exactMatch"));
        if (exactMatchArray != null && !exactMatchArray.isEmpty()) {
            deleteList.add("skos:exactMatch");
        }
        JSONArray relatedMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:relatedMatch"));
        if (relatedMatchArray != null && !relatedMatchArray.isEmpty()) {
            deleteList.add("skos:relatedMatch");
        }
        JSONArray narrowMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:narrowMatch"));
        if (narrowMatchArray != null && !narrowMatchArray.isEmpty()) {
            deleteList.add("skos:narrowMatch");
        }
        JSONArray broadMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:broadMatch"));
        if (broadMatchArray != null && !broadMatchArray.isEmpty()) {
            deleteList.add("skos:broadMatch");
        }
        JSONArray seeAlsoArray = (JSONArray) labelObject.get(rdf.getPrefixItem("rdfs:seeAlso"));
        if (seeAlsoArray != null && !seeAlsoArray.isEmpty()) {
            deleteList.add("rdfs:seeAlso");
        }
        JSONArray isDefinedByArray = (JSONArray) labelObject.get(rdf.getPrefixItem("rdfs:isDefinedBy"));
        if (isDefinedByArray != null && !isDefinedByArray.isEmpty()) {
            deleteList.add("rdfs:isDefinedBy");
        }
        JSONArray sameAsArray = (JSONArray) labelObject.get(rdf.getPrefixItem("owl:sameAs"));
        if (sameAsArray != null && !sameAsArray.isEmpty()) {
            deleteList.add("owl:sameAs");
        }
        // SEND DELETE
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?label ?p ?o. ?resource skos:broader ?label . ?resource skos:narrower ?label .} "
                + "WHERE { "
                + "?label ?p ?o. "
                + "?label dc:identifier ?identifier. "
                + "?label dc:identifier ?identifier. "
                // filter delete all broader/narrower wo ?label das OBJECT ist
                + "OPTIONAL { ?resource skos:broader ?label . } "
                + "OPTIONAL { ?resource skos:narrower ?label . } "
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

    private static String deleteLabelSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?label ?p ?o. } "
                + "WHERE { "
                + "?label ?p ?o. "
                + "?label dc:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "FILTER (?p IN (skos:inScheme,dct:creator,dc:creator,dct:contributor,dc:contributor,dct:license,skos:prefLabel,skos:altLabel,skos:note,skos:definition,ls:preferredLabel,ls:preferredLang,ls:hasContext,skos:related,skos:broader,skos:narrower,skos:closeMatch,skos:exactMatch,skos:relatedMatch,skos:narrowMatch,skos:broadMatch,rdfs:seeAlso,rdfs:isDefinedBy,owl:sameAs,dc:created,dc:modified,ls:sameAs)) "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

    private static String deleteLabelStatusTypeSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?label ls:hasStatusType ls:Active. } "
                + "WHERE { "
                + "?label ls:hasStatusType ls:Active. "
                + "?label dc:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

}