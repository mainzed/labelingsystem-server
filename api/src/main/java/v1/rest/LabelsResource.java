package v1.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rdf.RDF;
import rdf.RDF4J_20;
import v1.utils.uuid.UniqueIdentifier;
import exceptions.ConfigException;
import exceptions.Logging;
import exceptions.RdfException;
import exceptions.ResourceNotAvailableException;
import exceptions.SparqlParseException;
import exceptions.SparqlQueryException;
import exceptions.UniqueIdentifierException;
import v1.utils.transformer.Transformer;
import v1.utils.config.ConfigProperties;
import v1.utils.retcat.RetcatItems;
import v1.utils.generalfuncs.GeneralFunctions;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom.JDOMException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.simple.parser.JSONParser;
import v1.utils.retcat.RetcatItem;

@Path("/labels")
public class LabelsResource {

	private static String OUTSTRING = "";

	@GET
	@Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
	public Response getLabels(
			@HeaderParam("Accept") String acceptHeader,
			@HeaderParam("Accept-Encoding") String acceptEncoding,
			@QueryParam("pretty") boolean pretty,
			@QueryParam("sort") String sort,
			@QueryParam("fields") String fields,
			@QueryParam("offset") String offset,
			@QueryParam("limit") String limit,
			@QueryParam("creator") String creator,
			@QueryParam("releaseType") String releaseType,
			@QueryParam("prefLabel") String prefLabel,
			@QueryParam("vocab") String vocab,
			@QueryParam("equalConcepts") String equalConcepts,
			@QueryParam("revisions") String revisions,
			@QueryParam("creatorInfo") String creatorInfo)
			throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
		try {
			// QUERY STRING
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			List<RetcatItem> retcatlist = RetcatItems.getAllRetcatItems();
			String query = rdf.getPREFIXSPARQL();
			query += "SELECT ?s ?p ?o WHERE { "
					+ "?s ?p ?o . "
					+ "?s a ls:Label . "
					+ "?s dc:identifier ?identifier . "
					+ "OPTIONAL { ?s dc:creator ?creator . } " // because of sorting, filtering
					+ "OPTIONAL { ?s skos:prefLabel ?prefLabel . } " // because of sorting, filtering
					+ "OPTIONAL { ?s skos:inScheme ?vocab . } " // because of filtering
					+ "OPTIONAL { ?s ls:hasContext ?context . } "; // because of filtering
			// FILTERING
			if (creator != null) {
				query += "FILTER(?creator=\"" + creator + "\") ";
			}
			if (vocab != null) {
				query += "FILTER(?vocab=<" + rdf.getPrefixItem("ls_voc:" + vocab) + ">) ";
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
			long ctm_start = System.currentTimeMillis();
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> s = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "s");
			List<String> p = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> o = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			System.out.print("querytime: ");
			System.out.println(System.currentTimeMillis() - ctm_start);
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource is not available");
			}
			for (int i = 0; i < s.size(); i++) {
				rdf.setModelTriple(s.get(i), p.get(i), o.get(i));
			}
			JSONArray outArray = new JSONArray();
			if (acceptHeader.contains("application/json") || acceptHeader.contains("text/html")) {
				JSONObject jsonObject = (JSONObject) new JSONParser().parse(rdf.getModel("RDF/JSON"));
				Set keys = jsonObject.keySet();
				Iterator a = keys.iterator();
				while (a.hasNext()) {
					String key = (String) a.next();
					JSONObject tmpObject = (JSONObject) jsonObject.get(key);
					JSONArray idArray = (JSONArray) tmpObject.get(rdf.getPrefixItem("dc:identifier"));
					JSONObject idObject = (JSONObject) idArray.get(0);
					String h = (String) idObject.get("value");
					JSONObject tmpObject2 = new JSONObject();
					tmpObject2.put(key, tmpObject);
					String hh = tmpObject2.toString();
					JSONObject tmp = Transformer.label_GET(hh, h, fields, retcatlist, equalConcepts, revisions, creatorInfo);
					outArray.add(tmp);
				}
			}
			System.out.print("finaltime: ");
			System.out.println(System.currentTimeMillis() - ctm_start);
			if (acceptHeader.contains("application/json")) {
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(outArray.toString()).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).build();
				} else {
					OUTSTRING = outArray.toString();
					if (acceptEncoding.contains("gzip")) {
						return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
					} else {
						return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
					}
				}
			} else if (acceptHeader.contains("application/rdf+json")) {
				return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
			} else if (acceptHeader.contains("text/html")) {
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(outArray.toString()).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
				} else {
					OUTSTRING = outArray.toString();
					if (acceptEncoding.contains("gzip")) {
						return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
					} else {
						return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
					}
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
				JsonObject json = parser.parse(outArray.toString()).getAsJsonObject();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				OUTSTRING = outArray.toString();
				if (acceptEncoding.contains("gzip")) {
					return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
				} else {
					return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
				}
			}
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				JSONArray outArray = new JSONArray();
				return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}")
	@Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
	public Response getLabel(@PathParam("label") String label, 
			@HeaderParam("Accept") String acceptHeader, 
			@QueryParam("pretty") boolean pretty, 
			@QueryParam("equalConcepts") String equalConcepts, 
			@QueryParam("revisions") String revisions, 
			@QueryParam("creatorInfo") String creatorInfo, 
			@HeaderParam("Accept-Encoding") String acceptEncoding) 
			throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			List<RetcatItem> retcatlist = RetcatItems.getAllRetcatItems();
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			if (acceptHeader.contains("application/json")) {
				String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null, retcatlist, equalConcepts, revisions, creatorInfo).toJSONString();
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(out).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).build();
				} else {
					OUTSTRING = out.toString();
					if (acceptEncoding.contains("gzip")) {
						return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
					} else {
						return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
					}
				}
			} else if (acceptHeader.contains("application/rdf+json")) {
				return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
			} else if (acceptHeader.contains("text/html")) {
				String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null, retcatlist, equalConcepts, revisions, creatorInfo).toJSONString();
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(out).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
				} else {
					OUTSTRING = out.toString();
					if (acceptEncoding.contains("gzip")) {
						return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
					} else {
						return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
					}
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
				String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null, retcatlist, equalConcepts, revisions, creatorInfo).toJSONString();
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(out).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
				} else {
					OUTSTRING = out.toString();
					if (acceptEncoding.contains("gzip")) {
						return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
					} else {
						return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
					}
				}
			}
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				JSONArray outArray = new JSONArray();
				return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}.json")
	@Produces("application/json;charset=UTF-8")
	public Response getLabel_JSON(@PathParam("label") String label, 
			@QueryParam("pretty") boolean pretty, 
			@QueryParam("equalConcepts") String equalConcepts, 
			@QueryParam("revisions") String revisions, 
			@QueryParam("creatorInfo") String creatorInfo, 
			@HeaderParam("Accept-Encoding") String acceptEncoding) 
			throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			List<RetcatItem> retcatlist = RetcatItems.getAllRetcatItems();
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null, retcatlist, equalConcepts, revisions, creatorInfo).toJSONString();
			if (pretty) {
				JsonParser parser = new JsonParser();
				JsonObject json = parser.parse(out).getAsJsonObject();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				OUTSTRING = out.toString();
				if (acceptEncoding.contains("gzip")) {
					return Response.ok(new FeedReturnStreamingOutput()).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
				} else {
					return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
				}
			}
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}.xml")
	@Produces("application/xml;charset=UTF-8")
	public Response getLabel_XML(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
			return Response.ok(RDFoutput).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}.rdf")
	@Produces("application/rdf+xml;charset=UTF-8")
	public Response getLabelRDF_XML(@PathParam("label") String label) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
			return Response.ok(RDFoutput).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}.ttl")
	@Produces("text/turtle;charset=UTF-8")
	public Response getLabelRDF_Turtle(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			return Response.ok(rdf.getModel("Turtle")).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}.n3")
	@Produces("text/n3;charset=UTF-8")
	public Response getLabelRDF_N3(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			return Response.ok(rdf.getModel("N-Triples")).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}.jsonrdf")
	@Produces("application/json;charset=UTF-8")
	public Response getLabelRDF_JSONRDF(@PathParam("label") String label, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}.jsonld")
	@Produces("application/ld+json;charset=UTF-8")
	public Response getLabelRDF_JSONLD(@PathParam("label") String label, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_lab";
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{label}/hierarchy")
	@Produces("application/json;charset=UTF-8")
	public Response getLabel_BNR(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			String query = GeneralFunctions.getHierarchyForLabelsOneStep(label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			HashSet<String> nT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "narrowerTerm");
			HashSet<String> bT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "broaderTerm");
			HashSet<String> rT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "relatedTerm");
			JSONObject jsonOut = new JSONObject();
			JSONArray ntArray = new JSONArray();
			for (String item : nT) {
				if (item != null) {
					ntArray.add(item);
				}
			}
			jsonOut.put("narrower", ntArray);
			JSONArray btArray = new JSONArray();
			for (String item : bT) {
				if (item != null) {
					btArray.add(item);
				}
			}
			jsonOut.put("broader", btArray);
			JSONArray rtArray = new JSONArray();
			for (String item : rT) {
				if (item != null) {
					rtArray.add(item);
				}
			}
			jsonOut.put("related", rtArray);
			return Response.ok(jsonOut.toJSONString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/{label}/relations")
	@Produces("application/json;charset=UTF-8")
	public Response getLabel_Relations(@PathParam("label") String label) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			String query = GeneralFunctions.getRelationsForLabelsByCreator(label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			HashSet<String> nT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "nt");
			HashSet<String> bT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "bt");
			HashSet<String> rT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "rt");
			HashSet<String> nmT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "nmt");
			HashSet<String> bmT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "bmt");
			HashSet<String> rmT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "rmt");
			HashSet<String> cmT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "cmt");
			HashSet<String> emT = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "emt");
			JSONObject jsonOut = new JSONObject();
			JSONArray ntArray = new JSONArray();
			for (String item : nT) {
				if (item != null) {
					ntArray.add(item);
				}
			}
			jsonOut.put("narrower", ntArray);
			JSONArray btArray = new JSONArray();
			for (String item : bT) {
				if (item != null) {
					btArray.add(item);
				}
			}
			jsonOut.put("broader", btArray);
			JSONArray rtArray = new JSONArray();
			for (String item : rT) {
				if (item != null) {
					rtArray.add(item);
				}
			}
			jsonOut.put("related", rtArray);
			JSONArray nmtArray = new JSONArray();
			for (String item : nmT) {
				if (item != null) {
					nmtArray.add(item);
				}
			}
			jsonOut.put("narrowMatch", nmtArray);
			JSONArray bmtArray = new JSONArray();
			for (String item : bmT) {
				if (item != null) {
					bmtArray.add(item);
				}
			}
			jsonOut.put("broadMatch", bmtArray);
			JSONArray rmtArray = new JSONArray();
			for (String item : rmT) {
				if (item != null) {
					rmtArray.add(item);
				}
			}
			jsonOut.put("relatedMatch", rmtArray);
			JSONArray cmtArray = new JSONArray();
			for (String item : cmT) {
				if (item != null) {
					cmtArray.add(item);
				}
			}
			jsonOut.put("closeMatch", cmtArray);
			JSONArray emtArray = new JSONArray();
			for (String item : emT) {
				if (item != null) {
					emtArray.add(item);
				}
			}
			jsonOut.put("exactMatch", emtArray);
			return Response.ok(jsonOut.toJSONString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response postLabel(String json) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
		try {
			// get variables
			String item = "ls_lab";
			String itemID = UniqueIdentifier.getUUID();
			// parse data
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
			String creator = (String) jsonObject.get("creator");
			String vocabID = (String) jsonObject.get("vocabID");
			// create triples
			json = Transformer.label_POST(json, itemID, creator);
			String triples = createLabelSPARQLUPDATE(item, itemID, creator, vocabID);
			// input triples
			RDF4J_20.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), json);
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), triples);
			// get result als json
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			List<RetcatItem> retcatlist = RetcatItems.getAllRetcatItems();
			String query = GeneralFunctions.getAllElementsForItemID(item, itemID);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + itemID + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + itemID, predicates.get(i), objects.get(i));
			}
			String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), itemID, null, retcatlist, "false", "false", "false").toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@PUT
	@Path("/{label}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response updateLabel(@PathParam("label") String label, String json)
			throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			String item = "ls_lab";
			// check if resource exists
			String queryExist = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> resultExist = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), queryExist);
			if (resultExist.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			// get info
			JSONObject requestObject = (JSONObject) new JSONParser().parse(json);
			String user = (String) requestObject.get("creator");
			String vocabID = (String) requestObject.get("vocabID");
			// insert data
			String json_new = json;
			json = Transformer.label_POST(json, label, user);
			// get json old
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			List<RetcatItem> retcatlist = RetcatItems.getAllRetcatItems();
			String json_old = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null, retcatlist, "false", "false", "false").toJSONString();
			// set revision
			// get release type of vocabulary
			query = rdf.getPREFIXSPARQL();
			query += "SELECT * WHERE { ls_voc:" + vocabID + " ls:hasReleaseType ?rt . }";
			result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> releaseTypeList = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "rt");
			String releaseType = releaseTypeList.get(0);
			if (releaseType != null) {
				if (releaseType.contains("Public") || releaseType.contains("public")) {
					// get difference
					HashMap<String, String> revisions = Transformer.getLabelDifference(json_old, json_new);
					RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), revisionSPARQLUPDATE(item, label, revisions));
				} else {
					RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), modifySPARQLUPDATE(item, label));
				}
			} else {
				RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), modifySPARQLUPDATE(item, label));
			}
			// general label action
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deleteItemsSPARQLUPDATE(label));
			RDF4J_20.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), json);
			// get result als json
			rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			query = GeneralFunctions.getAllElementsForItemID(item, label);
			result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + label, predicates.get(i), objects.get(i));
			}
			String out = Transformer.label_GET(rdf.getModel("RDF/JSON"), label, null, retcatlist, "false", "false", "false").toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@DELETE
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response deleteLabel(@PathParam("label") String label) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			String item = "ls_lab";
			// check if resource exists
			String queryExist = GeneralFunctions.getAllElementsForItemID(item, label);
			List<BindingSet> resultExist = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), queryExist);
			if (resultExist.size() < 1) {
				throw new ResourceNotAvailableException("resource " + label + " is not available");
			}
			// delete data
			RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deleteLabelSPARQLUPDATE(label));
			String out = Transformer.empty_JSON("label").toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LabelsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	private static String createLabelSPARQLUPDATE(String item, String itemid, String creator, String vocabID) throws ConfigException, IOException, UniqueIdentifierException {
		Calendar calender = Calendar.getInstance();
		Date d = calender.getTime();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String date = formatter.format(d);
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String triples = prefixes + "INSERT DATA { ";
		triples += item + ":" + itemid + " a ls:Label . ";
		triples += item + ":" + itemid + " a skos:Concept . ";
		triples += item + ":" + itemid + " dc:creator \"" + creator + "\"" + " . ";
		triples += item + ":" + itemid + " dct:creator ls_age:" + creator + " . ";
		triples += item + ":" + itemid + " dc:identifier \"" + itemid + "\"" + " . ";
		triples += item + ":" + itemid + " dct:license <http://creativecommons.org/licenses/by/4.0/> . ";
		triples += item + ":" + itemid + " dc:created \"" + date + "\"" + " . ";
		triples += item + ":" + itemid + " dc:modified \"" + date + "\"" + " . ";
		triples += item + ":" + itemid + " skos:inScheme ls_voc:" + vocabID + " . ";
		triples += " }";
		return triples;
	}

	private static String modifySPARQLUPDATE(String item, String itemid) throws ConfigException, IOException, UniqueIdentifierException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		Calendar calender = Calendar.getInstance();
		Date date = calender.getTime();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String dateiso = formatter.format(date);
		String triples = prefixes + "INSERT DATA { ";
		triples += item + ":" + itemid + " dc:modified \"" + dateiso + "\"" + " . ";
		triples += " }";
		return triples;
	}

	private static String revisionSPARQLUPDATE(String item, String itemid, HashMap<String, String> revisions) throws ConfigException, IOException, UniqueIdentifierException, RepositoryException, MalformedQueryException, QueryEvaluationException, SparqlQueryException, SparqlParseException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String triples = prefixes + "INSERT DATA { ";
		Calendar calender = Calendar.getInstance();
		Date date = calender.getTime();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String dateiso = formatter.format(date);
		String revID = UniqueIdentifier.getUUID();
		String revID2 = UniqueIdentifier.getUUID();
		String revID3 = UniqueIdentifier.getUUID();
		triples += item + ":" + itemid + " dc:modified \"" + dateiso + "\"" + " . ";
		if (!revisions.isEmpty()) {
			if (revisions.get("releaseTypeChange").equals("true")) {
				triples += item + ":" + itemid + " ls:released \"" + dateiso + "\"" + " . ";
			} else {
				triples += item + ":" + itemid + " skos:changeNote ls_rev:" + revID + " . ";
				triples += "ls_rev" + ":" + revID + " a ls:Revision . ";
				triples += "ls_rev" + ":" + revID + " a prov:Activity . ";
				triples += "ls_rev" + ":" + revID + " dc:identifier \"" + revID + "\"" + " . ";
				triples += "ls_rev" + ":" + revID + " ls:action \"" + revisions.get("action") + "\"" + " . ";
				triples += "ls_rev" + ":" + revID + " ls:objectType \"" + revisions.get("objectType") + "\"" + " . ";
				triples += "ls_rev" + ":" + revID + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
				triples += "ls_rev" + ":" + revID + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
				triples += "ls_rev" + ":" + revID + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
				if (revisions.get("action") != null) {
					if (revisions.get("bidirectional") != null) { // if bidirectional link
						String query = rdf.getPREFIXSPARQL();
						query += "SELECT * WHERE { ls_lab:" + revisions.get("bidirectional") + " skos:inScheme ?v. ?v ls:hasReleaseType ?rt . }";
						List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
						List<String> releaseTypeList = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "rt");
						String releaseType = releaseTypeList.get(0);
						if (releaseType.contains("Public") || releaseType.contains("public")) { // if label is public
							if (revisions.get("action").equals("add")) {
								if (revisions.get("objectType").equals("narrower")) {
									triples += item + ":" + revisions.get("bidirectional") + " skos:changeNote ls_rev:" + revID2 + " . ";
									triples += "ls_rev" + ":" + revID2 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID2 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID2 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:action \"" + revisions.get("action") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:objectType \"" + "broader" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
								}
								if (revisions.get("objectType").equals("broader")) {
									triples += item + ":" + revisions.get("bidirectional") + " skos:changeNote ls_rev:" + revID2 + " . ";
									triples += "ls_rev" + ":" + revID2 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID2 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID2 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:action \"" + revisions.get("action") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:objectType \"" + "narrower" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
								}
							} else if (revisions.get("action").equals("delete")) {
								if (revisions.get("objectType").equals("narrower")) {
									triples += item + ":" + revisions.get("bidirectional") + " skos:changeNote ls_rev:" + revID2 + " . ";
									triples += "ls_rev" + ":" + revID2 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID2 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID2 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:action \"" + revisions.get("action") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:objectType \"" + "broader" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
								}
								if (revisions.get("objectType").equals("broader")) {
									triples += item + ":" + revisions.get("bidirectional") + " skos:changeNote ls_rev:" + revID2 + " . ";
									triples += "ls_rev" + ":" + revID2 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID2 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID2 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:action \"" + revisions.get("action") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:objectType \"" + "narrower" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
								}
							} else if (revisions.get("action").equals("modify")) {
								if (revisions.get("objectType").equals("narrower")) {
									triples += item + ":" + revisions.get("bidirectional-del") + " skos:changeNote ls_rev:" + revID2 + " . ";
									triples += "ls_rev" + ":" + revID2 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID2 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID2 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:action \"" + "delete" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:objectType \"" + "broader" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
									triples += item + ":" + revisions.get("bidirectional-add") + " skos:changeNote ls_rev:" + revID3 + " . ";
									triples += "ls_rev" + ":" + revID3 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID3 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID3 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:action \"" + "add" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:objectType \"" + "broader" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
								}
								if (revisions.get("objectType").equals("broader")) {
									triples += item + ":" + revisions.get("bidirectional-del") + " skos:changeNote ls_rev:" + revID2 + " . ";
									triples += "ls_rev" + ":" + revID2 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID2 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID2 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:action \"" + "delete" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:objectType \"" + "narrower" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID2 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
									triples += item + ":" + revisions.get("bidirectional-add") + " skos:changeNote ls_rev:" + revID3 + " . ";
									triples += "ls_rev" + ":" + revID3 + " a ls:Revision . ";
									triples += "ls_rev" + ":" + revID3 + " a prov:Activity . ";
									triples += "ls_rev" + ":" + revID3 + " dc:identifier \"" + revID2 + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:action \"" + "add" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:objectType \"" + "narrower" + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:valueBefore \"" + revisions.get("valueBefore") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " ls:valueAfter \"" + revisions.get("valueAfter") + "\"" + " . ";
									triples += "ls_rev" + ":" + revID3 + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
								}
							}
						}
					}
				}
			}
		}
		triples += " }";
		return triples;
	}

	private static String deleteItemsSPARQLUPDATE(String id) throws IOException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "DELETE { ?label ?p ?o. ?resource skos:broader ?label . ?resource skos:narrower ?label . ?resource skos:exactMatch ?label . } "
				+ "WHERE { "
				+ "?label ?p ?o. "
				+ "?label dc:identifier ?identifier. "
				// filter delete all broader,narrower,exactMatches wo ?label das OBJECT ist
				+ "OPTIONAL { ?resource skos:broader ?label . } "
				+ "OPTIONAL { ?resource skos:narrower ?label . } "
				+ "OPTIONAL { ?resource skos:exactMatch ?label . } "
				+ "FILTER (?identifier=\"$identifier\") "
				+ "FILTER (?p IN (skos:prefLabel,skos:scopeNote,ls:thumbnail,ls:hasReleaseType,dc:language,skos:related,skos:broader,skos:narrower,skos:closeMatch,skos:exactMatch,skos:relatedMatch,skos:narrowMatch,skos:broadMatch,rdfs:seeAlso)) "
				+ "}";
		update = update.replace("$identifier", id);
		return update;
	}

	private static String deleteLabelSPARQLUPDATE(String id) throws IOException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "DELETE { ?label ?p ?o. } "
				+ "WHERE { "
				+ "?label ?p ?o. "
				+ "?label dc:identifier ?identifier. "
				+ "FILTER (?identifier=\"$identifier\") "
				+ "}";
		update = update.replace("$identifier", id);
		return update;
	}

	private static class FeedReturnStreamingOutput implements StreamingOutput {

		@Override
		public void write(OutputStream output) throws IOException, WebApplicationException {
			try {
				output = GZIP(OUTSTRING, output);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	private static OutputStream GZIP(String input, OutputStream baos) throws IOException {
		try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
			gzos.write(input.getBytes("UTF-8"));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return baos;
	}

}
