package info.labeling.rest;

import info.labeling.exceptions.Logging;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom.JDOMException;

@Path("/")
public class GenericResource {

	@GET
	public Response getInfo() throws URISyntaxException {
		URI targetURIForRedirection = new URI("http://labeling.link");
		return Response.temporaryRedirect(targetURIForRedirection).build();
	}

	@GET
	@Path("/vocabulary/{vocabulary}")
	public Response getVocabulary(@HeaderParam("Accept") String acceptHeader, @PathParam("vocabulary") String itemID) throws IOException, JDOMException, ParserConfigurationException, TransformerException {
		try {
			String HOST_API = ConfigProperties.getPropertyParam("api");
			String HOST_HTMLPAGE = ConfigProperties.getPropertyParam("ls_detailhtml")
					.replace("$host", ConfigProperties.getPropertyParam("host"))
					.replace("$itemid", itemID)
					.replace("$item", "vocabulary");
			if (acceptHeader.startsWith("application/json")) {
				URI targetURIForRedirection = new URI(HOST_API + "vocabs/" + itemID + ".json");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/html")) {
				URI targetURIForRedirection = new URI(HOST_HTMLPAGE);
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "vocabs/" + itemID + ".xml");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/rdf+xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "vocabs/" + itemID + ".rdf");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/turtle")) {
				URI targetURIForRedirection = new URI(HOST_API + "vocabs/" + itemID + ".ttl");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/n3")) {
				URI targetURIForRedirection = new URI(HOST_API + "vocabs/" + itemID + ".n3");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/ld+json")) {
				URI targetURIForRedirection = new URI(HOST_API + "vocabs/" + itemID + ".jsonld");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else {
				URI targetURIForRedirection = new URI(HOST_HTMLPAGE);
				return Response.temporaryRedirect(targetURIForRedirection).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Logging.getMessageJSON(e, "info.labeling.rest.GenericResource")).
					header("Access-Control-Allow-Origin", "*").header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/{label}")
	public Response getLabel(@HeaderParam("Accept") String acceptHeader, @PathParam("label") String itemID) throws IOException, JDOMException,  ParserConfigurationException, TransformerException {
		try {
			String HOST_API = ConfigProperties.getPropertyParam("api");
			String HOST_HTMLPAGE = ConfigProperties.getPropertyParam("ls_detailhtml")
					.replace("$host", ConfigProperties.getPropertyParam("host"))
					.replace("$itemid", itemID)
					.replace("$item", "label");
			if (acceptHeader.startsWith("application/json")) {
				URI targetURIForRedirection = new URI(HOST_API + "labels/" + itemID + ".json");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/html")) {
				URI targetURIForRedirection = new URI(HOST_HTMLPAGE);
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "labels/" + itemID + ".xml");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/rdf+xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "labels/" + itemID + ".rdf");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/turtle")) {
				URI targetURIForRedirection = new URI(HOST_API + "labels/" + itemID + ".ttl");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/n3")) {
				URI targetURIForRedirection = new URI(HOST_API + "labels/" + itemID + ".n3");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/ld+json")) {
				URI targetURIForRedirection = new URI(HOST_API + "labels/" + itemID + ".jsonld");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else {
				URI targetURIForRedirection = new URI(HOST_HTMLPAGE);
				return Response.temporaryRedirect(targetURIForRedirection).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Logging.getMessageJSON(e, "info.labeling.rest.GenericResource")).
					header("Access-Control-Allow-Origin", "*").header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/agent/{agent}")
	public Response getAgent(@HeaderParam("Accept") String acceptHeader, @PathParam("agent") String itemID) throws IOException, JDOMException, ParserConfigurationException, TransformerException {
		try {
			String HOST_API = ConfigProperties.getPropertyParam("api");
			if (acceptHeader.startsWith("application/json")) {
				URI targetURIForRedirection = new URI(HOST_API + "agents/" + itemID + ".json");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "agents/" + itemID + ".xml");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/rdf+xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "agents/" + itemID + ".rdf");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/turtle")) {
				URI targetURIForRedirection = new URI(HOST_API + "agents/" + itemID + ".ttl");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/n3")) {
				URI targetURIForRedirection = new URI(HOST_API + "agents/" + itemID + ".n3");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/ld+json")) {
				URI targetURIForRedirection = new URI(HOST_API + "agents/" + itemID + ".jsonld");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else {
				URI targetURIForRedirection = new URI(HOST_API + "agents/" + itemID + ".jsonrdf");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Logging.getMessageJSON(e, "info.labeling.rest.GenericResource")).
					header("Access-Control-Allow-Origin", "*").header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/revision/{revision}")
	public Response getRevision(@HeaderParam("Accept") String acceptHeader, @PathParam("revision") String itemID) throws IOException, JDOMException, ParserConfigurationException, TransformerException {
		try {
			String HOST_API = ConfigProperties.getPropertyParam("api");
			if (acceptHeader.startsWith("application/json")) {
				URI targetURIForRedirection = new URI(HOST_API + "revisions/" + itemID + ".json");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "revisions/" + itemID + ".xml");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/rdf+xml")) {
				URI targetURIForRedirection = new URI(HOST_API + "revisions/" + itemID + ".rdf");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/turtle")) {
				URI targetURIForRedirection = new URI(HOST_API + "revisions/" + itemID + ".ttl");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("text/n3")) {
				URI targetURIForRedirection = new URI(HOST_API + "revisions/" + itemID + ".n3");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else if (acceptHeader.startsWith("application/ld+json")) {
				URI targetURIForRedirection = new URI(HOST_API + "revisions/" + itemID + ".jsonld");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			} else {
				URI targetURIForRedirection = new URI(HOST_API + "revisions/" + itemID + ".jsonrdf");
				return Response.temporaryRedirect(targetURIForRedirection).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Logging.getMessageJSON(e, "info.labeling.rest.GenericResource")).
					header("Access-Control-Allow-Origin", "*").header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}
