package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.exceptions.Logging;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Path("/v1/resdetail")
public class ResdetailResource {

	@GET
	@Path("/getty")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getPrefLabelGetty(@QueryParam("url") String url) {
		try {
			String sparqlendpoint = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?prefLabel { "
					+ "<" + url + "> gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ " }";
			URL obj = new URL(sparqlendpoint);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/sparql-results+json");
			String urlParameters = "query=" + sparql;
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// init output
			JSONObject jsonOut = new JSONObject();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject prefLabel = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) prefLabel.get("value");
				String labelLang = (String) prefLabel.get("xml:lang");
				jsonOut.put("label", labelValue + "@" + labelLang);
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.ResdetailResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/heritagedata")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getPrefLabelHeritageData(@QueryParam("url") String url) {
		try {
			String sparqlendpoint = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
					+ "SELECT ?prefLabel WHERE { "
					+ "<" + url + "> skos:prefLabel ?prefLabel. "
					+ " }";
			URL obj = new URL(sparqlendpoint);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/sparql-results+json");
			String urlParameters = "query=" + sparql;
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// init output
			JSONObject jsonOut = new JSONObject();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject prefLabel = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) prefLabel.get("value");
				String labelLang = (String) prefLabel.get("xml:lang");
				jsonOut.put("label", labelValue + "@" + labelLang);
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.ResdetailResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/labelingsystem")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getPrefLabelLabelingSystem(@QueryParam("url") String url) {
		try {
			String sparqlendpoint = "http://localhost:8084/api/v1/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#>"
					+ "SELECT ?prefLabel { "
					+ "<" + url + "> ls:preferredLabel ?prefLabel. "
					+ " }";
			URL obj = new URL(sparqlendpoint);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/sparql-results+json");
			String urlParameters = "query=" + sparql;
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// init output
			JSONObject jsonOut = new JSONObject();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject prefLabel = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) prefLabel.get("value");
				String labelLang = (String) prefLabel.get("xml:lang");
				jsonOut.put("label", labelValue + "@" + labelLang);
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.ResdetailResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/html")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getPrefLabelExtern(@QueryParam("url") String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			Elements titleTag = doc.select("title");
			JSONObject jsonOut = new JSONObject();
			String out = titleTag.text();
			if (url.startsWith("http://dbpedia.org/resource/")) {
				out = out.replace("About: ", "");
			}
			jsonOut.put("label", out);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.ResdetailResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}
