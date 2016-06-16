package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.Sesame2714;
import de.i3mainz.ls.rdfutils.exceptions.ConfigException;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import de.i3mainz.ls.rdfutils.exceptions.RdfException;
import de.i3mainz.ls.rdfutils.exceptions.ResourceNotAvailableException;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom.JDOMException;
import org.openrdf.query.BindingSet;

/**
 * REST API for Revisions
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.04.2016
 */
@Path("/v1/revisions")
public class RevisionsResource {

    @GET
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8"})
    public Response getRevision(@HeaderParam("Accept") String acceptHeader) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String query = rdf.getPREFIXSPARQL();
            query += "SELECT * WHERE { "
                    + "?s a ?type . "
                    + "?s ls:identifier ?identifier . "
                    + "?s dct:creator ?creatorURI . "
                    + "?s dc:creator ?creator . "
                    + "?s dct:date ?date . "
                    + "?s dc:description ?description . "
                    + "?s dct:type ?revisiontype . "
                    + "?s prov:startedAtTime ?startedAtTime . "
                    + "FILTER (?type=ls:Revision) . "
                    + "}";
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> uris = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
            List<String> types = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "type");
            List<String> identifiers = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "identifier");
            List<String> creatoruris = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "creatorURI");
            List<String> creators = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "creator");
            List<String> dates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "date");
            List<String> descriptions = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "description");
            List<String> revisiontypes = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "revisiontype");
            List<String> startedattimes = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "startedAtTime");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < uris.size(); i++) {
                rdf.setModelTriple(uris.get(i), "rdf:type", types.get(i));
                rdf.setModelTriple(uris.get(i), "ls:identifier", identifiers.get(i));
                rdf.setModelTriple(uris.get(i), "dct:creator", creatoruris.get(i));
                rdf.setModelTriple(uris.get(i), "dc:creator", creators.get(i));
                rdf.setModelTriple(uris.get(i), "dct:date", dates.get(i));
                rdf.setModelTriple(uris.get(i), "dc:description", descriptions.get(i));
                rdf.setModelTriple(uris.get(i), "dct:type", revisiontypes.get(i));
                rdf.setModelTriple(uris.get(i), "prov:startedAtTime", startedattimes.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{revision}")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8"})
    public Response getRevision(@PathParam("revision") String revision, @HeaderParam("Accept") String acceptHeader) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_rev";
            String query = getRevisionSPARQL(item, revision);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{revision}.json")
    @Produces("application/json;charset=UTF-8")
    public Response getRevision_JSON(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_rev";
            String query = getRevisionSPARQL(item, revision);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("RDF/JSON")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{revision}.xml")
    @Produces("application/xml;charset=UTF-8")
    public Response getRevision_XML(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_rev";
            String query = getRevisionSPARQL(item, revision);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{revision}.rdf")
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getRevisionRDF_XML(@PathParam("revision") String revision) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_rev";
            String query = getRevisionSPARQL(item, revision);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
            }
            String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
            return Response.ok(RDFoutput).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{revision}.ttl")
    @Produces("text/turtle;charset=UTF-8")
    public Response getRevisionRDF_Turtle(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_rev";
            String query = getRevisionSPARQL(item, revision);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("Turtle")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{revision}.n3")
    @Produces("text/n3;charset=UTF-8")
    public Response getRevisionRDF_N3(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_rev";
            String query = getRevisionSPARQL(item, revision);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("N-Triples")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    @GET
    @Path("/{revision}.jsonld")
    @Produces("application/ld+json;charset=UTF-8")
    public Response getRevisionRDF_JSONLD(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
        try {
            RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
            String item = "ls_rev";
            String query = getRevisionSPARQL(item, revision);
            List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
            List<String> predicates = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "p");
            List<String> objects = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "o");
            if (result.size() < 1) {
                throw new ResourceNotAvailableException();
            }
            for (int i = 0; i < predicates.size(); i++) {
                rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
            }
            return Response.ok(rdf.getModel("JSON-LD")).build();
        } catch (Exception e) {
            if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
        }
    }

    private static String getRevisionSPARQL(String item, String itemid) throws ConfigException, IOException {
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String query = prefixes + "SELECT * WHERE { ";
        query += item + ":" + itemid + " ?p ?o. } ";
        query += "ORDER BY ASC(?p)";
        return query;
    }

}
