package v1.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rdf.RDF;
import rdf.RDF4J_20M3;
import v1.utils.uuid.UniqueIdentifier;
import exceptions.ConfigException;
import exceptions.Logging;
import exceptions.RdfException;
import exceptions.ResourceNotAvailableException;
import exceptions.UniqueIdentifierException;
import v1.restconfig.PATCH;
import v1.utils.transformer.Transformer;
import v1.utils.config.ConfigProperties;
import v1.utils.generalfuncs.GeneralFunctions;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * REST API for Vocabularies
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 23.06.2016
 */
@Path("/vocabs")
public class VocabsResource {

	private static String OUTSTRING = "";

	@GET
	@Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
	public Response getVocabularies(
			@HeaderParam("Accept") String acceptHeader,
			@HeaderParam("Accept-Encoding") String acceptEncoding,
			@QueryParam("pretty") boolean pretty,
			@QueryParam("sort") String sort,
			@QueryParam("fields") String fields,
			@QueryParam("offset") String offset,
			@QueryParam("limit") String limit,
			@QueryParam("creator") String creator,
			@QueryParam("contributor") String contributor,
			@QueryParam("statusType") String statusType,
			@QueryParam("releaseType") String releaseType,
			@QueryParam("draft") String draft)
			throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
		try {
			// QUERY STRING
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = rdf.getPREFIXSPARQL();
			query += "SELECT * WHERE { "
					+ "?s ?p ?o . "
					+ "?s a ls:Vocabulary . "
					+ "?s dc:identifier ?identifier . "
					+ "OPTIONAL { ?s dc:creator ?creator . } " // because of sorting, filtering
					+ "OPTIONAL { ?s dc:contributor ?contributor . } " // because of sorting, filtering
					+ "OPTIONAL { ?s dc:title ?title . } " // because of sorting
					+ "OPTIONAL { ?s ls:hasStatusType ?statusType . } "; // because of filtering
			// FILTERING
			if (draft == null) {
				query += "?s ls:hasReleaseType ls:Public . ";
			}
			if (creator != null) {
				query += "FILTER(?creator=\"" + creator + "\") ";
			}
			if (contributor != null) {
				query += "FILTER(?contributor=\"" + contributor + "\") ";
			}
			if (statusType != null) {
				query += "FILTER(?statusType=<" + rdf.getPrefixItem("ls:" + statusType) + ">) ";
			}
			if (releaseType != null) {
				query += "FILTER(?releaseType=<" + rdf.getPrefixItem("ls:" + releaseType) + ">) ";
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
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> s = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "s");
			List<String> p = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> o = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			System.out.print("querytime: ");
			System.out.println(System.currentTimeMillis() - ctm_start);
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
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
					JSONObject tmp = Transformer.vocabulary_GET(hh, h, fields);
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}")
	@Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
	public Response getVocabulary(@PathParam("vocabulary") String vocabulary, @HeaderParam("Accept") String acceptHeader, @QueryParam("pretty") boolean pretty, @HeaderParam("Accept-Encoding") String acceptEncoding) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			if (acceptHeader.contains("application/json")) {
				String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
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
				String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
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
				String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.json")
	@Produces("application/json;charset=UTF-8")
	public Response getVocabulary_JSON(@PathParam("vocabulary") String vocabulary, @QueryParam("pretty") boolean pretty, @HeaderParam("Accept-Encoding") String acceptEncoding) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.xml")
	@Produces("application/xml;charset=UTF-8")
	public Response getVocabulary_XML(@PathParam("vocabulary") String vocabulary) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
			return Response.ok(RDFoutput).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.rdf")
	@Produces("application/rdf+xml;charset=UTF-8")
	public Response getVocabularyRDF_XML(@PathParam("vocabulary") String vocabulary) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
			return Response.ok(RDFoutput).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.ttl")
	@Produces("text/turtle;charset=UTF-8")
	public Response getVocabularyRDF_Turtle(@PathParam("vocabulary") String vocabulary) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			return Response.ok(rdf.getModel("Turtle")).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.n3")
	@Produces("text/n3;charset=UTF-8")
	public Response getVocabularyRDF_N3(@PathParam("vocabulary") String vocabulary) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			return Response.ok(rdf.getModel("N-Triples")).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.jsonrdf")
	@Produces("application/json;charset=UTF-8")
	public Response getVocabularyRDF_JSONRDF(@PathParam("vocabulary") String vocabulary, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.jsonld")
	@Produces("application/ld+json;charset=UTF-8")
	public Response getVocabularyRDF_JSONLD(@PathParam("vocabulary") String vocabulary, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{vocabulary}.skos")
	@Produces("application/rdf+xml;charset=UTF-8")
	public Response getVocabularyRDF_SKOS(@PathParam("vocabulary") String vocabulary) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String item = "ls_voc";
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			item = "ls_voc";
			query = GeneralFunctions.getAllLabelsForVocabulary(vocabulary);
			result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> labels = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "id");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < labels.size(); i++) {
				item = "ls_lab";
				query = GeneralFunctions.getAllElementsForItemID(item, labels.get(i));
				result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
				predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
				objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
				if (labels.size() < 1) {
					throw new ResourceNotAvailableException();
				}
				for (int j = 0; j < predicates.size(); j++) {
					rdf.setModelTriple(item + ":" + labels.get(i), predicates.get(j), objects.get(j));
				}
			}
			String RDFoutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + rdf.getModel("RDF/XML");
			return Response.ok(RDFoutput).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@POST
	@Path("/user/{user}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response postVocabulary(@PathParam("user") String user, String json) throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
		try {
			// get variables
			String item = "ls_voc";
			String itemID = UniqueIdentifier.getUUID();
			// create triples
			json = Transformer.vocabulary_POST(json, itemID);
			String triples = createVocabularySPARQLUPDATE(item, itemID, user);
			// input triples
			RDF4J_20M3.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), json);
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), triples);
			// get result als json
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = GeneralFunctions.getAllElementsForItemID(item, itemID);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + itemID, predicates.get(i), objects.get(i));
			}
			String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), itemID, null).toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@PUT
	@Path("/{vocabulary}/user/{user}/type/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response updateVocabulary(@PathParam("vocabulary") String vocabulary, @PathParam("user") String user, @PathParam("type") String type, String json)
			throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			String item = "ls_voc";
			// check if resource exists
			String queryExist = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> resultExist = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), queryExist);
			if (resultExist.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			// insert data
			json = Transformer.vocabulary_POST(json, vocabulary);
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), putVocabularyREVISION(item, vocabulary, user, type));
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), putVocabularySPARQLUPDATE(vocabulary));
			RDF4J_20M3.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), json);
			// get result als json
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@PATCH
	@Path("/{vocabulary}/user/{user}/type/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response updateVocabularyPATCH(@PathParam("vocabulary") String vocabulary, @PathParam("user") String user, @PathParam("type") String type, String json)
			throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			String item = "ls_voc";
			// check if resource exists
			String queryExist = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> resultExist = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), queryExist);
			if (resultExist.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			// insert data
			json = Transformer.vocabulary_POST(json, vocabulary);
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), putVocabularyREVISION(item, vocabulary, user, type));
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), patchVocabularySPARQLUPDATE(vocabulary, json));
			if (!json.contains("flush")) {
				RDF4J_20M3.inputRDFfromRDFJSONString(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), json);
			}
			// get result als json
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@DELETE
	@Path("/{vocabulary}/user/{user}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response deleteVocabulary(@PathParam("vocabulary") String vocabulary, @PathParam("user") String user) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			String item = "ls_voc";
			// check if resource exists
			String queryExist = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> resultExist = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), queryExist);
			if (resultExist.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			// insert data
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deleteVocabularyREVISION(item, vocabulary, user));
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deleteVocabularySPARQLUPDATE(vocabulary));
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deleteVocabularyStatusTypeSPARQLUPDATE(vocabulary));
			// get result als json
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}
	
	@DELETE
	@Path("/{vocabulary}/deprecated/user/{user}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response setDeprecatedVocabulary(@PathParam("vocabulary") String vocabulary, @PathParam("user") String user) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			String item = "ls_voc";
			// check if resource exists
			String queryExist = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> resultExist = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), queryExist);
			if (resultExist.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			// insert data
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deprecatedVocabularyREVISION(item, vocabulary, user));
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deleteVocabularyStatusTypeSPARQLUPDATE(vocabulary));
			// set deprecated labels
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), addLabelStatusTypeDeprecatedInSchemeSPARQLUPDATE(vocabulary));
			RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), deleteLabelStatusTypeActiveInSchemeSPARQLUPDATE(vocabulary));
			// get result als json
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = GeneralFunctions.getAllElementsForItemID(item, vocabulary);
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + vocabulary, predicates.get(i), objects.get(i));
			}
			String out = Transformer.vocabulary_GET(rdf.getModel("RDF/JSON"), vocabulary, null).toJSONString();
			return Response.status(Response.Status.CREATED).entity(out).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.VocabsResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	private static String createVocabularySPARQLUPDATE(String item, String itemid, String user) throws ConfigException, IOException, UniqueIdentifierException {
		String revID = UniqueIdentifier.getUUID();
		Calendar calender = Calendar.getInstance();
		Date date = calender.getTime();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String dateiso = formatter.format(date);
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String triples = prefixes + "INSERT DATA { ";
		triples += item + ":" + itemid + " a ls:Vocabulary . ";
		triples += item + ":" + itemid + " a skos:ConceptScheme . ";
		triples += item + ":" + itemid + " ls:hasStatusType ls:Active . ";
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

	private static String putVocabularyREVISION(String item, String itemid, String user, String type) throws ConfigException, IOException, UniqueIdentifierException {
		String typeArray[] = type.split(",");
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String triples = prefixes + "INSERT DATA { ";
		Calendar calender = Calendar.getInstance();
		Date date = calender.getTime();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String dateiso = formatter.format(date);
		for (String entry : typeArray) {
			if (!entry.contains("DescriptionRevision") && !entry.contains("ShareRevision") && !entry.contains("SystemRevision")) {
				entry = "ModifyRevision";
			}
			String revID = UniqueIdentifier.getUUID();
			triples += item + ":" + itemid + " dc:modified \"" + dateiso + "\"" + " . ";
			triples += item + ":" + itemid + " skos:changeNote ls_rev:" + revID + " . ";
			triples += "ls_rev" + ":" + revID + " a ls:Revision . ";
			triples += "ls_rev" + ":" + revID + " a prov:Activity . ";
			triples += "ls_rev" + ":" + revID + " a prov:Modify . ";
			triples += "ls_rev" + ":" + revID + " dc:identifier \"" + revID + "\"" + " . ";
			triples += "ls_rev" + ":" + revID + " dc:creator \"" + user + "\"" + " . ";
			triples += "ls_rev" + ":" + revID + " dct:creator ls_age:" + user + " . ";
			triples += "ls_rev" + ":" + revID + " dc:description \"" + entry + "\"" + " . ";
			triples += "ls_rev" + ":" + revID + " dct:type ls:" + entry + " . ";
			triples += "ls_rev" + ":" + revID + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
		}
		triples += " }";
		return triples;
	}

	private static String deleteVocabularyREVISION(String item, String itemid, String user) throws ConfigException, IOException, UniqueIdentifierException {
		String revID = UniqueIdentifier.getUUID();
		Calendar calender = Calendar.getInstance();
		Date date = calender.getTime();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String dateiso = formatter.format(date);
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
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
	
	private static String deprecatedVocabularyREVISION(String item, String itemid, String user) throws ConfigException, IOException, UniqueIdentifierException {
        String revID = UniqueIdentifier.getUUID();
        Calendar calender = Calendar.getInstance();
        Date date = calender.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateiso = formatter.format(date);
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String triples = prefixes + "INSERT DATA { ";
        triples += item + ":" + itemid + " ls:hasStatusType ls:Deprecated . ";
        triples += item + ":" + itemid + " skos:changeNote ls_rev:" + revID + " . ";
        triples += "ls_rev" + ":" + revID + " a ls:Revision . ";
        triples += "ls_rev" + ":" + revID + " a prov:Activity . ";
        triples += "ls_rev" + ":" + revID + " a prov:Modify . ";
        triples += "ls_rev" + ":" + revID + " dc:identifier \"" + revID + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dc:creator \"" + user + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:creator ls_age:" + user + " . ";
        triples += "ls_rev" + ":" + revID + " dc:description \"" + "DeprecatedRevision" + "\"" + " . ";
        triples += "ls_rev" + ":" + revID + " dct:type ls:" + "DeprecatedRevision" + " . ";
        triples += "ls_rev" + ":" + revID + " prov:startedAtTime \"" + dateiso + "\"" + " . ";
        triples += " }";
        return triples;
    }

	private static String putVocabularySPARQLUPDATE(String id) throws IOException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "DELETE { ?vocabulary ?p ?o. } "
				+ "WHERE { "
				+ "?vocabulary ?p ?o. "
				+ "?vocabulary dc:identifier ?identifier. "
				+ "FILTER (?identifier=\"$identifier\") "
				+ "FILTER (?p IN (skos:hasTopConcept,dct:contributor,dc:contributor,dc:title,dc:description,ls:hasReleaseType,dcat:theme)) "
				+ "}";
		update = update.replace("$identifier", id);
		return update;
	}

	private static String patchVocabularySPARQLUPDATE(String id, String json) throws IOException, ParseException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		JSONObject vocabularyObject = (JSONObject) new JSONParser().parse(json);
		List<String> deleteList = new ArrayList<String>();
		// for patch
		JSONArray flushArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("flush"));
		if (flushArray != null && !flushArray.isEmpty()) {
			for (Object element : flushArray) {
				// nur optional
				if (element.equals("topConcept")) {
					deleteList.add("skos:hasTopConcept");
				}
			}
		}
		// for else
		JSONArray titleArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:title"));
		if (titleArray != null && !titleArray.isEmpty()) {
			deleteList.add("dc:title");
		}
		JSONArray descriptionArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:description"));
		if (descriptionArray != null && !descriptionArray.isEmpty()) {
			deleteList.add("dc:description");
		}
		JSONArray hasTopConceptArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("skos:hasTopConcept"));
		if (hasTopConceptArray != null && !hasTopConceptArray.isEmpty()) {
			deleteList.add("skos:hasTopConcept");
		}
		JSONArray contributorArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:contributor"));
		if (contributorArray != null && !contributorArray.isEmpty()) {
			deleteList.add("dct:contributor");
			deleteList.add("dc:contributor");
		}
		JSONArray hasReleaseTypeArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:hasReleaseType"));
		if (hasReleaseTypeArray != null && !hasReleaseTypeArray.isEmpty()) {
			deleteList.add("ls:hasReleaseType");
		}
		JSONArray themenArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dcat:theme"));
		if (themenArray != null && !themenArray.isEmpty()) {
			deleteList.add("dcat:theme");
		}
		// SEND DELETE
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "DELETE { ?vocabulary ?p ?o. } "
				+ "WHERE { "
				+ "?vocabulary ?p ?o. "
				+ "?vocabulary dc:identifier ?identifier. "
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

	private static String deleteVocabularySPARQLUPDATE(String id) throws IOException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "DELETE { ?vocabulary ?p ?o. } "
				+ "WHERE { "
				+ "?vocabulary ?p ?o. "
				+ "?vocabulary dc:identifier ?identifier. "
				+ "FILTER (?identifier=\"$identifier\") "
				+ "FILTER (?p IN (skos:hasTopConcept,dc:title,dc:description,ls:hasReleaseType,dcat:theme,dct:creator,dc:creator,dct:contributor,dc:contributor,dct:license,dc:created,dc:modified)) "
				+ "}";
		update = update.replace("$identifier", id);
		return update;
	}

	private static String deleteVocabularyStatusTypeSPARQLUPDATE(String id) throws IOException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "DELETE { ?vocabulary ls:hasStatusType ls:Active. } "
				+ "WHERE { "
				+ "?vocabulary ls:hasStatusType ls:Active. "
				+ "?vocabulary dc:identifier ?identifier. "
				+ "FILTER (?identifier=\"$identifier\") "
				+ "}";
		update = update.replace("$identifier", id);
		return update;
	}
	
	private static String deleteLabelStatusTypeActiveInSchemeSPARQLUPDATE(String vocid) throws IOException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "DELETE { ?label ls:hasStatusType ls:Active. } "
				+ "WHERE { "
				+ "?label ls:hasStatusType ls:Active. "
				+ "?label skos:inScheme ?vocab. "
				+ "?vocab dc:identifier ?identifier. "
				+ "FILTER (?identifier=\"$identifier\") "
				+ "}";
		update = update.replace("$identifier", vocid);
		return update;
	}
	
	private static String addLabelStatusTypeDeprecatedInSchemeSPARQLUPDATE(String vocid) throws IOException {
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String update = prefixes
				+ "INSERT { ?label ls:hasStatusType ls:Deprecated. } "
				+ "WHERE { "
				+ "?label ls:hasStatusType ls:Active. "
				+ "?label skos:inScheme ?vocab. "
				+ "?vocab dc:identifier ?identifier. "
				+ "FILTER (?identifier=\"$identifier\") "
				+ "}";
		update = update.replace("$identifier", vocid);
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
