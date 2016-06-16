package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.rdfutils.exceptions.ConfigException;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import de.i3mainz.ls.rdfutils.exceptions.RdfException;
import de.i3mainz.ls.rdfutils.exceptions.ResourceNotAvailableException;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom.JDOMException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openrdf.query.BindingSet;

/**
 * REST API for Agents
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 06.04.2016
 */
@Path("/v1/agents")
public class AgentsResource {

    @GET
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8"})
    public Response getAgents(@HeaderParam("Accept") String acceptHeader) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = rdf.getPREFIXSPARQL();
            query += "SELECT * WHERE { "
                    + "?s a ?type . "
                    + "?s ls:identifier ?identifier . "
                    + "?s ls:hasGUI ?hasGUI . "
                    + "?s foaf:accountName ?accountName . "
                    + "?s foaf:mbox ?mbox . "
                    + "?s foaf:firstName ?firstName . "
                    + "?s foaf:lastName ?lastName . "
                    + "OPTIONAL { ?s foaf:homepage ?homepage . } "
                    + "OPTIONAL { ?s foaf:img ?img . } "
                    + "OPTIONAL { ?s geo:lat ?lat . } "
                    + "OPTIONAL { ?s geo:lon ?lon . } "
                    + "OPTIONAL { ?s ls:useSparqlEndpoint ?useSparqlEndpoint . } "
                    + "?s ls:sameAs ?sameAs . "
                    + "FILTER (?type=ls:Agent) . "
                    + "}";
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> uris = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
            List<String> types = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "type");
            List<String> identifiers = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "identifier");
            List<String> hasguis = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "hasGUI");
            List<String> accountnames = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "accountName");
            List<String> mboxs = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "mbox");
            List<String> firstnames = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "firstName");
            List<String> lastnames = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "lastName");
            List<String> homepages = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "homepage"); //OPTIONAL
            List<String> imgs = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "img"); //OPTIONAL
            List<String> lats = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "lat"); //OPTIONAL
            List<String> lons = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "lon"); //OPTIONAL
            List<String> usesparqlendpoints = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "useSparqlEndpoint"); //OPTIONAL
            List<String> sameass = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "sameAs");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < uris.size(); i++) {
                rdf.setModelTriple(uris.get(i), "rdf:type", types.get(i));
                rdf.setModelTriple(uris.get(i), "ls:identifier", identifiers.get(i));
                rdf.setModelTriple(uris.get(i), "ls:hasGUI", hasguis.get(i));
                rdf.setModelTriple(uris.get(i), "foaf:accountName", accountnames.get(i));
                rdf.setModelTriple(uris.get(i), "foaf:mbox", mboxs.get(i));
                rdf.setModelTriple(uris.get(i), "foaf:firstName", firstnames.get(i));
                rdf.setModelTriple(uris.get(i), "foaf:lastName", lastnames.get(i));
                if (homepages.get(i)!=null){
                    rdf.setModelTriple(uris.get(i), "foaf:homepage", homepages.get(i));
                }
                if (imgs.get(i)!=null){
                    rdf.setModelTriple(uris.get(i), "foaf:img", imgs.get(i));
                }
                if (lats.get(i)!=null){
                    rdf.setModelTriple(uris.get(i), "geo:lat", lats.get(i));
                }
                if (lons.get(i)!=null){
                    rdf.setModelTriple(uris.get(i), "geo:lon", lons.get(i));
                }
                if (usesparqlendpoints.get(i)!=null){
                    rdf.setModelTriple(uris.get(i), "ls:useSparqlEndpoint", usesparqlendpoints.get(i));
                }
                rdf.setModelTriple(uris.get(i), "ls:sameAs", sameass.get(i));
            }
            if (acceptHeader.contains("application/json")) {
                return Response.ok(rdf.getModel("RDF/JSON")).build();
            } else if (acceptHeader.contains("text/html")) {
                return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
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
                return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{agent}")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8"})
    public Response getAgent(@PathParam("agent") String agent, @HeaderParam("Accept") String acceptHeader) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_age";
            String query = getAgentSPARQL(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            if (acceptHeader.contains("application/json")) {
                return Response.ok(rdf.getModel("RDF/JSON")).build();
            } else if (acceptHeader.contains("text/html")) {
                return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
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
                return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
            }
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{agent}.json")
    @Produces("application/json;charset=UTF-8")
    public Response getAgent_JSON(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_age";
            String query = getAgentSPARQL(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("RDF/JSON")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{agent}.xml")
    @Produces("application/xml;charset=UTF-8")
    public Response getAgent_XML(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_age";
            String query = getAgentSPARQL(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{agent}.rdf")
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getAgentRDF_XML(@PathParam("agent") String agent) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_age";
            String query = getAgentSPARQL(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{agent}.ttl")
    @Produces("text/turtle;charset=UTF-8")
    public Response getAgentRDF_Turtle(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_age";
            String query = getAgentSPARQL(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{agent}.n3")
    @Produces("text/n3;charset=UTF-8")
    public Response getAgentRDF_N3(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_age";
            String query = getAgentSPARQL(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{agent}.jsonld")
    @Produces("application/ld+json;charset=UTF-8")
    public Response getAgentRDF_JSONLD(@PathParam("agent") String agent) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_age";
            String query = getAgentSPARQL(item, agent);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + agent, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("JSON-LD")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response postAgent(String json) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // get variables
            String item = "ls_age";
            String itemID = "";
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            // parse itemid
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            for (Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                JSONObject jsonObjectFirst = (JSONObject) jsonObject.get(key);
                JSONArray userArray = (JSONArray) jsonObjectFirst.get("http://xmlns.com/foaf/0.1/accountName");
                JSONObject userArrayFirstElement = (JSONObject) userArray.get(0);
                Object userObjectValue = userArrayFirstElement.get("value");
                itemID = userObjectValue.toString();
            }
            // create triples
            json = json.replace("#uri#", rdf.getPrefixItem(item + ":" + itemID));
            String triples = createAgentSPARQLUPDATE(item, itemID);
            // input triples
            Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), triples);
            return Response.status(Response.Status.CREATED).entity(json).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PUT
    @Path("/{agent}")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateAgent(@PathParam("agent") String agent, String json) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String triples = putAgentSPARQLUPDATE(agent);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), triples);
            Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            return Response.status(Response.Status.CREATED).entity(json).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @DELETE
    @Path("/{agent}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response deleteAgent(@PathParam("agent") String agent) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String triples = deleteAgentSPARQLUPDATE(agent);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), triples);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AgentsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    private static String getAgentSPARQL(String item, String itemid) throws ConfigException, IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String query = prefixes + "SELECT * WHERE { ";
        query += item + ":" + itemid + " ?p ?o. } ";
        query += "ORDER BY ASC(?p)";
        return query;
    }

    private static String createAgentSPARQLUPDATE(String item, String itemid) throws ConfigException, IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " a ls:Agent . ";
        triples += item + ":" + itemid + " a foaf:Agent . ";
        triples += item + ":" + itemid + " ls:identifier \"" + itemid + "\"" + " . ";
        triples += item + ":" + itemid + " ls:sameAs "
                + "<" + PropertiesLocal.getPropertyParam("ls_detailhtml")
                .replace("$host", PropertiesLocal.getPropertyParam("host"))
                .replace("$itemid", itemid).replace("$item", "agent") + ">" + " . ";
        triples += " }";
        return triples;
    }

    private static String putAgentSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?agent ?p ?o. } "
                + "WHERE { "
                + "?agent ?p ?o. "
                + "?agent ls:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "FILTER (?p IN (ls:hasGUI, foaf:mbox, foaf:firstName, foaf:lastName, foaf:homepage, foaf:img, geo:lat, geo:lon)) "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

    private static String deleteAgentSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?agent ?p ?o. } "
                + "WHERE { "
                + "?agent ?p ?o. "
                + "?agent ls:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

}
