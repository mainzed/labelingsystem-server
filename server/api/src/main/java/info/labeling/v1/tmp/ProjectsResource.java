package info.labeling.v1.tmp;

import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.identifier.UniqueIdentifier;
import de.i3mainz.ls.rdfutils.exceptions.ConfigException;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import de.i3mainz.ls.rdfutils.exceptions.RdfException;
import de.i3mainz.ls.rdfutils.exceptions.ResourceNotAvailableException;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.IOException;
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
import org.openrdf.query.BindingSet;

/**
 * REST API for Projects
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.04.2016
 */
@Path("/v1/projects")
public class ProjectsResource {

    @GET
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8"})
    public Response getProjects(@HeaderParam("Accept") String acceptHeader) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = rdf.getPREFIXSPARQL();
            query += "SELECT * WHERE { "
                    + "?s a ?type . "
                    + "?s ls:identifier ?identifier . "
                    + "?s dct:creator ?creatorURI . "
                    + "?s dc:creator ?creator . "
                    + "?s rdfs:label ?label . "
                    + "?s ls:projectLang ?projectLang . "
                    + "FILTER (?type=ls:Project) . "
                    + "}";
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> uris = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
            List<String> types = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "type");
            List<String> identifiers = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "identifier");
            List<String> creatoruris = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "creatorURI");
            List<String> creators = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "creator");
            List<String> labels = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "label");
            List<String> projectlangs = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "projectLang");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < uris.size(); i++) {
                rdf.setModelTriple(uris.get(i), "rdf:type", types.get(i));
                rdf.setModelTriple(uris.get(i), "ls:identifier", identifiers.get(i));
                rdf.setModelTriple(uris.get(i), "dct:creator", creatoruris.get(i));
                rdf.setModelTriple(uris.get(i), "dc:creator", creators.get(i));
                rdf.setModelTriple(uris.get(i), "rdfs:label", labels.get(i));
                rdf.setModelTriple(uris.get(i), "ls:projectLang", projectlangs.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{project}")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8"})
    public Response getProject(@PathParam("project") String project, @HeaderParam("Accept") String acceptHeader) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_pro";
            String query = getProjectSPARQL(item, project);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + project, predicates.get(i), objects.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{project}.json")
    @Produces("application/json;charset=UTF-8")
    public Response getProject_JSON(@PathParam("project") String project) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_pro";
            String query = getProjectSPARQL(item, project);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + project, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("RDF/JSON")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{project}.xml")
    @Produces("application/xml;charset=UTF-8")
    public Response getProject_XML(@PathParam("project") String project) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_pro";
            String query = getProjectSPARQL(item, project);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + project, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{project}.rdf")
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getProjectRDF_XML(@PathParam("project") String project) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_pro";
            String query = getProjectSPARQL(item, project);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + project, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{project}.ttl")
    @Produces("text/turtle;charset=UTF-8")
    public Response getProjectRDF_Turtle(@PathParam("project") String project) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_pro";
            String query = getProjectSPARQL(item, project);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + project, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("Turtle")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{project}.n3")
    @Produces("text/n3;charset=UTF-8")
    public Response getProjectRDF_N3(@PathParam("project") String project) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_pro";
            String query = getProjectSPARQL(item, project);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + project, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("N-Triples")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{project}.jsonld")
    @Produces("application/ld+json;charset=UTF-8")
    public Response getProjectRDF_JSONLD(@PathParam("project") String project) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_pro";
            String query = getProjectSPARQL(item, project);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + project, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("JSON-LD")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }
	
	@POST
	@Path("/user/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response postProject(@PathParam("user") String user, String json) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            // get variables
            String item = "ls_pro";
            String itemID = UniqueIdentifier.getUUID();
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            // create triples
            json = json.replace("#uri#", rdf.getPrefixItem(item + ":" + itemID));
            String triples = createProjectSPARQLUPDATE(user, item, itemID);
            // input triples
            Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), triples);
            return Response.status(Response.Status.CREATED).entity(json).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @PUT
    @Path("/{project}")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response updateProject(@PathParam("project") String project, String json) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String triples = putProjectSPARQLUPDATE(project);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), triples);
            Sesame2714.inputRDFfromRDFJSONString(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), json);
            return Response.status(Response.Status.CREATED).entity(json).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @DELETE
    @Path("/{project}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response deleteProject(@PathParam("project") String project) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            String triples = deleteProjectSPARQLUPDATE(project);
            Sesame2714.SPARQLupdate(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), triples);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.ProjectsResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    private static String getProjectSPARQL(String item, String itemid) throws ConfigException, IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String query = prefixes + "SELECT * WHERE { ";
        query += item + ":" + itemid + " ?p ?o. } ";
        query += "ORDER BY ASC(?p)";
        return query;
    }

    private static String createProjectSPARQLUPDATE(String user, String item, String itemid) throws ConfigException, IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " a ls:Project .";
        triples += item + ":" + itemid + " ls:identifier \"" + itemid + "\"" + " .";
        triples += item + ":" + itemid + " dc:creator \"" + user + "\"" + " .";
        triples += item + ":" + itemid + " dct:creator ls_age:" + user + " .";
        triples += " }";
        return triples;
    }

    private static String putProjectSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?project ?p ?o. } "
                + "WHERE { "
                + "?project ?p ?o. "
                + "?project ls:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "FILTER (?p IN (rdfs:label,ls:projectLang, dc:creator, dct:creator)) "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }
    
    private static String deleteProjectSPARQLUPDATE(String id) throws IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String update = prefixes
                + "DELETE { ?project ?p ?o. ?vocabulary ls:belongsToProject ?project.} "
                + "WHERE { "
                + "?project ?p ?o. "
                + "OPTIONAL { ?vocabulary ls:belongsToProject ?project. } "
                + "?project ls:identifier ?identifier. "
                + "FILTER (?identifier=\"$identifier\") "
                + "}";
        update = update.replace("$identifier", id);
        return update;
    }

}
