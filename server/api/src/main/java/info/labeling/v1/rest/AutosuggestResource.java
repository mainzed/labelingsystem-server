package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.exceptions.Logging;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/autosuggests")
public class AutosuggestResource {

	private final String LIMIT = "20";

	@GET
	public Response getAPIpage() {
		return Response.noContent().build();
	}

	@GET
	@Path("/heritagedata")
	public Response getAPIpageHD() {
		return Response.noContent().build();
	}

	@GET
	@Path("/heritagedata/englishheritage")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsEH(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<http://purl.org/heritagedata/schemes/eh_com> || ?scheme=<http://purl.org/heritagedata/schemes/eh_evd> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmc>) "
					+ "} LIMIT " + LIMIT;
			URL obj = new URL(url);
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
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/heritagedata/historicengland")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsHE(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<http://purl.org/heritagedata/schemes/mda_obj> || ?scheme=<http://purl.org/heritagedata/schemes/eh_period> || ?scheme=<http://purl.org/heritagedata/schemes/agl_et> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmt2> || ?scheme=<http://purl.org/heritagedata/schemes/560> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tbm>) "
					+ "} LIMIT " + LIMIT;
			URL obj = new URL(url);
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
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/heritagedata/rcahms")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsRCAHMS(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<http://purl.org/heritagedata/schemes/2> || ?scheme=<http://purl.org/heritagedata/schemes/3> || ?scheme=http://purl.org/heritagedata/schemes/1>) "
					+ "} LIMIT " + LIMIT;
			URL obj = new URL(url);
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
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/heritagedata/rcahmw")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsRCAHMW(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<http://purl.org/heritagedata/schemes/11> || ?scheme=<http://purl.org/heritagedata/schemes/10> || ?scheme=<http://purl.org/heritagedata/schemes/12> || ?scheme=<http://purl.org/heritagedata/schemes/17> || ?scheme=<http://purl.org/heritagedata/schemes/19> || ?scheme=<http://purl.org/heritagedata/schemes/14> || ?scheme=<http://purl.org/heritagedata/schemes/15> || ?scheme=<http://purl.org/heritagedata/schemes/18> || ?scheme=<http://purl.org/heritagedata/schemes/20> || ?scheme=<http://purl.org/heritagedata/schemes/13> || ?scheme=<http://purl.org/heritagedata/schemes/21> || ?scheme=<http://purl.org/heritagedata/schemes/22>) "
					+ "} LIMIT " + LIMIT;
			URL obj = new URL(url);
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
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/getty")
	public Response getAPIpageGE() {
		return Response.noContent().build();
	}

	@GET
	@Path("/getty/aat")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsAAT(@QueryParam("query") String searchword) {
		try {
			String url = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?Subject ?prefLabel ?scopeNote { "
					+ "?Subject a skos:Concept. "
					+ "?Subject luc:term '" + searchword + "' . "
					+ "?Subject skos:inScheme aat: . "
					+ "?Subject gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ "OPTIONAL {?Subject skos:scopeNote [dct:language gvp_lang:en; rdf:value ?scopeNote]} . "
					+ " } ORDER BY ASC(LCASE(STR(?Term))) LIMIT " + LIMIT;
			URL obj = new URL(url);
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
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/getty/tgn")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsTGN(@QueryParam("query") String searchword) {
		try {
			String url = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?Subject ?prefLabel ?scopeNote { "
					+ "?Subject a skos:Concept. "
					+ "?Subject luc:term '" + searchword + "' . "
					+ "?Subject skos:inScheme tgn: . "
					+ "?Subject gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ "OPTIONAL {?Subject skos:scopeNote [dct:language gvp_lang:en; rdf:value ?scopeNote]} . "
					+ " } ORDER BY ASC(LCASE(STR(?Term))) LIMIT " + LIMIT;
			URL obj = new URL(url);
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
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/getty/ulan")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsULAN(@QueryParam("query") String searchword) {
		try {
			String url = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?Subject ?prefLabel ?scopeNote { "
					+ "?Subject a skos:Concept. "
					+ "?Subject luc:term '" + searchword + "' . "
					+ "?Subject skos:inScheme ulan: . "
					+ "?Subject gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ "OPTIONAL {?Subject skos:scopeNote [dct:language gvp_lang:en; rdf:value ?scopeNote]} . "
					+ " } ORDER BY ASC(LCASE(STR(?Term))) LIMIT " + LIMIT;
			URL obj = new URL(url);
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
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/dbpedia")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsDBPEDIA(@QueryParam("query") String searchword) {
		try {
			String url_string = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=" + searchword + "&MaxHits=" + LIMIT;
			URL url = new URL(url_string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Accept", "application/json");
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
			return Response.ok(response.toString()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "de.i3mainz.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}
