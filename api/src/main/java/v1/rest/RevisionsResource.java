package v1.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rdf.RDF;
import rdf.RDF4J_20;
import exceptions.ConfigException;
import exceptions.Logging;
import exceptions.RdfException;
import exceptions.ResourceNotAvailableException;
import v1.utils.transformer.Transformer;
import v1.utils.config.ConfigProperties;
import v1.utils.generalfuncs.GeneralFunctions;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

@Path("/revisions")
public class RevisionsResource {

	@GET
	@Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
	public Response getRevisions(
			@HeaderParam("Accept") String acceptHeader,
			@HeaderParam("Accept-Encoding") String acceptEncoding,
			@QueryParam("pretty") boolean pretty)
			throws IOException, JDOMException, ConfigException, ParserConfigurationException, TransformerException {
		try {
			String OUTSTRING = "";
			// QUERY STRING
			RDF rdf = new RDF();
			String query = rdf.getPREFIXSPARQL();
			query += "SELECT * WHERE { "
					+ "?s ?p ?o . "
					+ "?s a ls:Revision . "
					+ "?s dc:identifier ?identifier . "
					+ " } ";
			// QUERY TRIPLESTORE
			long ctm_start = System.currentTimeMillis();
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> s = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "s");
			List<String> p = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> o = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
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
					JSONObject tmp = Transformer.revision_GET(hh, h);
					outArray.add(tmp);
				}
			}
			if (acceptHeader.contains("application/json")) {
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(outArray.toString()).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).build();
				} else {
					OUTSTRING = outArray.toString();
					if (acceptEncoding.contains("gzip")) {
						// set outputstream
						final String OUTSTRING_FINAL = OUTSTRING;
						StreamingOutput stream;
						stream = new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								try {
									output = GZIP(OUTSTRING_FINAL, output);
								} catch (Exception e) {
									System.out.println(e.toString());
								}
							}
						};
						return Response.ok(stream).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
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
						// set outputstream
						final String OUTSTRING_FINAL = OUTSTRING;
						StreamingOutput stream;
						stream = new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								try {
									output = GZIP(OUTSTRING_FINAL, output);
								} catch (Exception e) {
									System.out.println(e.toString());
								}
							}
						};
						return Response.ok(stream).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
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
					// set outputstream
					final String OUTSTRING_FINAL = OUTSTRING;
					StreamingOutput stream;
					stream = new StreamingOutput() {
						@Override
						public void write(OutputStream output) throws IOException, WebApplicationException {
							try {
								output = GZIP(OUTSTRING_FINAL, output);
							} catch (Exception e) {
								System.out.println(e.toString());
							}
						}
					};
					return Response.ok(stream).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
				} else {
					return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
				}
			}
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				JSONArray outArray = new JSONArray();
				return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}")
	@Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8", "application/rdf+xml;charset=UTF-8", "text/turtle;charset=UTF-8", "text/n3;charset=UTF-8", "application/ld+json;charset=UTF-8", "application/rdf+json;charset=UTF-8"})
	public Response getRevision(@PathParam("revision") String revision, @HeaderParam("Accept") String acceptHeader, @QueryParam("pretty") boolean pretty, @HeaderParam("Accept-Encoding") String acceptEncoding) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			String OUTSTRING = "";
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
			}
			if (acceptHeader.contains("application/json")) {
				String out = Transformer.revision_GET(rdf.getModel("RDF/JSON"), revision).toJSONString();
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(out).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).build();
				} else {
					OUTSTRING = out.toString();
					if (acceptEncoding.contains("gzip")) {
						// set outputstream
						final String OUTSTRING_FINAL = OUTSTRING;
						StreamingOutput stream;
						stream = new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								try {
									output = GZIP(OUTSTRING_FINAL, output);
								} catch (Exception e) {
									System.out.println(e.toString());
								}
							}
						};
						return Response.ok(stream).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
					} else {
						return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
					}
				}
			} else if (acceptHeader.contains("application/rdf+json")) {
				return Response.ok(rdf.getModel("RDF/JSON")).header("Content-Type", "application/json;charset=UTF-8").build();
			} else if (acceptHeader.contains("text/html")) {
				String out = Transformer.revision_GET(rdf.getModel("RDF/JSON"), revision).toJSONString();
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(out).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
				} else {
					OUTSTRING = out.toString();
					if (acceptEncoding.contains("gzip")) {
						// set outputstream
						final String OUTSTRING_FINAL = OUTSTRING;
						StreamingOutput stream;
						stream = new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								try {
									output = GZIP(OUTSTRING_FINAL, output);
								} catch (Exception e) {
									System.out.println(e.toString());
								}
							}
						};
						return Response.ok(stream).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
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
				String out = Transformer.revision_GET(rdf.getModel("RDF/JSON"), revision).toJSONString();
				if (pretty) {
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(out).getAsJsonObject();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
				} else {
					OUTSTRING = out.toString();
					if (acceptEncoding.contains("gzip")) {
						// set outputstream
						final String OUTSTRING_FINAL = OUTSTRING;
						StreamingOutput stream;
						stream = new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								try {
									output = GZIP(OUTSTRING_FINAL, output);
								} catch (Exception e) {
									System.out.println(e.toString());
								}
							}
						};
						return Response.ok(stream).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
					} else {
						return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
					}
				}
			}
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}.json")
	@Produces("application/json;charset=UTF-8")
	public Response getRevision_JSON(@PathParam("revision") String revision, @QueryParam("pretty") boolean pretty, @HeaderParam("Accept-Encoding") String acceptEncoding) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			String OUTSTRING = "";
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
			}
			String out = Transformer.revision_GET(rdf.getModel("RDF/JSON"), revision).toJSONString();
			if (pretty) {
				JsonParser parser = new JsonParser();
				JsonObject json = parser.parse(out).getAsJsonObject();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				return Response.ok(gson.toJson(json)).header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				OUTSTRING = out.toString();
				if (acceptEncoding.contains("gzip")) {
					// set outputstream
					final String OUTSTRING_FINAL = OUTSTRING;
					StreamingOutput stream;
					stream = new StreamingOutput() {
						@Override
						public void write(OutputStream output) throws IOException, WebApplicationException {
							try {
								output = GZIP(OUTSTRING_FINAL, output);
							} catch (Exception e) {
								System.out.println(e.toString());
							}
						}
					};
					return Response.ok(stream).header("Content-Type", "application/json;charset=UTF-8").header("Content-Encoding", "gzip").build();
				} else {
					return Response.ok(OUTSTRING).header("Content-Type", "application/json;charset=UTF-8").build();
				}
			}
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}.xml")
	@Produces("application/xml;charset=UTF-8")
	public Response getRevision_XML(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}.rdf")
	@Produces("application/rdf+xml;charset=UTF-8")
	public Response getRevisionRDF_XML(@PathParam("revision") String revision) throws IOException, JDOMException, RdfException, ParserConfigurationException, TransformerException {
		try {
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}.ttl")
	@Produces("text/turtle;charset=UTF-8")
	public Response getRevisionRDF_Turtle(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
			}
			return Response.ok(rdf.getModel("Turtle")).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}.n3")
	@Produces("text/n3;charset=UTF-8")
	public Response getRevisionRDF_N3(@PathParam("revision") String revision) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
			}
			return Response.ok(rdf.getModel("N-Triples")).build();
		} catch (Exception e) {
			if (e.toString().contains("ResourceNotAvailableException")) {
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}.jsonrdf")
	@Produces("application/json;charset=UTF-8")
	public Response getRevisionRDF_JSONRDF(@PathParam("revision") String revision, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/{revision}.jsonld")
	@Produces("application/ld+json;charset=UTF-8")
	public Response getRevisionRDF_JSONLD(@PathParam("revision") String revision, @QueryParam("pretty") boolean pretty) throws IOException, JDOMException, TransformerException, ParserConfigurationException {
		try {
			RDF rdf = new RDF();
			String item = "ls_rev";
			String query = GeneralFunctions.getAllElementsForItemID(item, revision);
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
			List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
			if (result.size() < 1) {
				throw new ResourceNotAvailableException();
			}
			for (int i = 0; i < predicates.size(); i++) {
				rdf.setModelTriple(item + ":" + revision, predicates.get(i), objects.get(i));
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
				return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RevisionsResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
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
