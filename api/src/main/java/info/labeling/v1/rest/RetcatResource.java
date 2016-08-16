package info.labeling.v1.rest;

import info.labeling.exceptions.Logging;
import info.labeling.exceptions.ResourceNotAvailableException;
import info.labeling.rdf.RDF;
import info.labeling.rdf.RDF4J_20M3;
import info.labeling.v1.utils.ConfigProperties;
import v1.utils.retcat.RetcatItems;
import info.labeling.v1.utils.SQlite;
import v1.utils.retcat.SuggestionItem;
import info.labeling.v1.utils.Utils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.rdf4j.query.BindingSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import v1.utils.retcat.RetcatItem;

@Path("/retcat")
public class RetcatResource {

	private final String LIMIT = "20";

	@GET
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getRetcatList() {
		try {
			JSONArray outArray = new JSONArray();
			// add items
			for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
				JSONObject tmpRETCAT = new JSONObject();
				tmpRETCAT.put("name", item.getName());
				tmpRETCAT.put("descripion", item.getDescription());
				tmpRETCAT.put("queryURL", item.getQueryURL());
				tmpRETCAT.put("labelURL", item.getLabelURL());
				tmpRETCAT.put("prefix", item.getPrefix());
				tmpRETCAT.put("group", item.getGroup());
				tmpRETCAT.put("type", item.getType());
				tmpRETCAT.put("language", item.getLanguage());
				tmpRETCAT.put("quality", item.getQuality());
				outArray.add(tmpRETCAT);
			}
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/{retcat}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getRetcatDetails(@PathParam("retcat") String retcat) {
		try {
			JSONArray outArray = new JSONArray();
			for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
				if (item.getName().equals(retcat)) {
					JSONObject tmpRETCAT = new JSONObject();
					tmpRETCAT.put("name", item.getName());
					tmpRETCAT.put("descripion", item.getDescription());
					tmpRETCAT.put("queryURL", item.getQueryURL());
					tmpRETCAT.put("labelURL", item.getLabelURL());
					tmpRETCAT.put("prefix", item.getPrefix());
					tmpRETCAT.put("group", item.getGroup());
					tmpRETCAT.put("type", item.getType());
					tmpRETCAT.put("language", item.getLanguage());
					tmpRETCAT.put("quality", item.getQuality());
					outArray.add(tmpRETCAT);
				}
			}
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}
	
	@GET
	@Path("/info/qualities")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getRetcatQualities() {
		try {
			JSONArray outArray = new JSONArray();
			HashSet<String> qualityList = new HashSet();
			for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
				qualityList.add(item.getQuality());
			}
			for (String item: qualityList) {
				outArray.add(item);
			}
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}
	
	@GET
	@Path("/info/groups")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getRetcatGroups() {
		try {
			JSONArray outArray = new JSONArray();
			HashSet<String> groupList = new HashSet();
			for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
				groupList.add(item.getGroup());
			}
			for (String item: groupList) {
				outArray.add(item);
			}
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/vocabulary/{vocabulary}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getRetcatListByVocabulary(@PathParam("vocabulary") String vocabulary) {
		try {
			// get vocab name
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = rdf.getPREFIXSPARQL();
			query += "SELECT ?vocab WHERE { ?v a ls:Vocabulary. ?v dc:identifier ?id. ?v dc:title ?vocab. FILTER(?id=\"" + vocabulary + "\")}";
			List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> vocabname = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "vocab");
			String vocabretcat = vocabname.get(0).split("@")[0];
			vocabretcat = vocabretcat.substring(1, vocabretcat.length() - 1);
			// get retcat items
			String newRetcatString = SQlite.getRetcatByVocabulary(vocabulary);
			newRetcatString += "," + vocabretcat;
			String[] retcatItems = newRetcatString.split(",");
			// output json
			JSONArray outArray = new JSONArray();
			// set data
			for (String vocItem : retcatItems) {
				for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
					if (item.getName().equals(vocItem)) {
						JSONObject tmpRETCAT = new JSONObject();
						tmpRETCAT.put("name", item.getName());
						tmpRETCAT.put("descripion", item.getDescription());
						tmpRETCAT.put("queryURL", item.getQueryURL());
						tmpRETCAT.put("labelURL", item.getLabelURL());
						tmpRETCAT.put("prefix", item.getPrefix());
						tmpRETCAT.put("group", item.getGroup());
						tmpRETCAT.put("type", item.getType());
						tmpRETCAT.put("language", item.getLanguage());
						tmpRETCAT.put("quality", item.getQuality());
						outArray.add(tmpRETCAT);
					}
				}
			}
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@POST
	@Path("/vocabulary/{vocabulary}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response setRetcatForVocabulary(@PathParam("vocabulary") String vocabulary, String json) {
		try {
			// db string
			String retcatString = "";
			// parse json
			JSONArray retcatArray = (JSONArray) new JSONParser().parse(json);
			for (Object itemObject : retcatArray) {
				JSONObject tmpObject = (JSONObject) itemObject;
				retcatString += tmpObject.get("name") + ",";
			}
			retcatString = retcatString.substring(0, retcatString.length() - 1);
			// sqlite db action
			SQlite.deleteRetcatEntry(vocabulary);
			SQlite.insertRetcatString(vocabulary, retcatString);
			String newRetcatString = SQlite.getRetcatByVocabulary(vocabulary);
			String[] retcatItems = newRetcatString.split(",");
			// output json
			JSONArray outArray = new JSONArray();
			// set data
			for (String vocItem : retcatItems) {
				for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
					if (item.getName().equals(vocItem)) {
						JSONObject tmpRETCAT = new JSONObject();
						tmpRETCAT.put("name", item.getName());
						tmpRETCAT.put("descripion", item.getDescription());
						tmpRETCAT.put("queryURL", item.getQueryURL());
						tmpRETCAT.put("labelURL", item.getLabelURL());
						tmpRETCAT.put("prefix", item.getPrefix());
						tmpRETCAT.put("group", item.getGroup());
						tmpRETCAT.put("type", item.getType());
						tmpRETCAT.put("language", item.getLanguage());
						tmpRETCAT.put("quality", item.getQuality());
						outArray.add(tmpRETCAT);
					}
				}
			}
			// output
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/waybacklink")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getWaybackLink(@QueryParam("url") String url) {
		try {
			URL obj = new URL(ConfigProperties.getPropertyParam("waybackapi").replace("$url", url));
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			String urlParameters = "";
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
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("archived_snapshots");
			JSONObject resultsObject2 = (JSONObject) resultsObject.get("closest");
			String wburl = null;
			try {
				wburl = (String) resultsObject2.get("url");
			} catch (Exception e) {
				throw new NullPointerException("no url available");
			}
			jsonOut.put("url", wburl);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/heritagedata/historicengland")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsHE(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<http://purl.org/heritagedata/schemes/mda_obj> || ?scheme=<http://purl.org/heritagedata/schemes/eh_period> || ?scheme=<http://purl.org/heritagedata/schemes/agl_et> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmt2> || ?scheme=<http://purl.org/heritagedata/schemes/560> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tbm> || ?scheme=<http://purl.org/heritagedata/schemes/eh_com> || ?scheme=<http://purl.org/heritagedata/schemes/eh_evd> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmc>) "
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/heritagedata/rcahms")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsRCAHMS(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<http://purl.org/heritagedata/schemes/2> || ?scheme=<http://purl.org/heritagedata/schemes/3> || ?scheme=<http://purl.org/heritagedata/schemes/1>) "
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/heritagedata/rcahmw")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsRCAHMW(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
					//+ "FILTER(LANGMATCHES(LANG(?prefLabel), \"en\")) "
					//+ "FILTER(LANGMATCHES(LANG(?scopeNote), \"en\")) "
					//+ "FILTER(LANGMATCHES(LANG(?BroaderPreferredTerm), \"en\")) "
					//+ "FILTER(LANGMATCHES(LANG(?NarrowerPreferredTerm), \"en\")) "
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/getty/aat")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsAAT(@QueryParam("query") String searchword) {
		try {
			String lang = "en"; // language for scopeNote
			String url = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle { "
					+ "?Subject a skos:Concept. "
					+ "?Subject luc:term '" + searchword + "' . "
					+ "?Subject skos:inScheme aat: . "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "?Subject gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ "OPTIONAL {?Subject skos:scopeNote [dct:language gvp_lang:" + lang + "; rdf:value ?scopeNote]} . "
					+ "OPTIONAL {?Subject gvp:broaderPreferred ?BroaderPreferred . ?BroaderPreferred gvp:prefLabelGVP [xl:literalForm ?BroaderPreferredTerm].} . "
					+ "OPTIONAL {?NarrowerPreferred gvp:broaderPreferred ?Subject . ?NarrowerPreferred gvp:prefLabelGVP [xl:literalForm ?NarrowerPreferredTerm].} . "
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
			// init output
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/getty/tgn")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsTGN(@QueryParam("query") String searchword) {
		try {
			String url = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle{ "
					+ "?Subject a skos:Concept. "
					+ "?Subject luc:term '" + searchword + "' . "
					+ "?Subject skos:inScheme tgn: . "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "?Subject gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ "OPTIONAL {?Subject gvp:parentString ?scopeNote . } "
					+ "OPTIONAL {?Subject gvp:broaderPreferred ?BroaderPreferred . ?BroaderPreferred gvp:prefLabelGVP [xl:literalForm ?BroaderPreferredTerm].} . "
					+ "OPTIONAL {?NarrowerPreferred gvp:broaderPreferred ?Subject . ?NarrowerPreferred gvp:prefLabelGVP [xl:literalForm ?NarrowerPreferredTerm].} . "
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				//String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					//String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					//String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					//String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/getty/ulan")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsULAN(@QueryParam("query") String searchword) {
		try {
			String lang = "en"; // language for scopeNote
			String url = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle{ "
					+ "?Subject a skos:Concept. "
					+ "?Subject luc:term '" + searchword + "' . "
					+ "?Subject skos:inScheme ulan: . "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "?Subject gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ "OPTIONAL {?Subject skos:scopeNote [dct:language gvp_lang:" + lang + "; rdf:value ?scopeNote]} . "
					+ "OPTIONAL {?Subject gvp:broaderPreferred ?BroaderPreferred . ?BroaderPreferred gvp:prefLabelGVP [xl:literalForm ?BroaderPreferredTerm].} . "
					+ "OPTIONAL {?NarrowerPreferred gvp:broaderPreferred ?Subject . ?NarrowerPreferred gvp:prefLabelGVP [xl:literalForm ?NarrowerPreferredTerm].} . "
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				//String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					//String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					//String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					//String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/dbpedia")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsDBPEDIA(@QueryParam("query") String searchword) {
		try {
			searchword = Utils.encodeURIComponent(searchword);
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// fill objects
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONArray resultsArray = (JSONArray) jsonObject.get("results");
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (Object element : resultsArray) {
				JSONObject tmpElement = (JSONObject) element;
				String uriValue = (String) tmpElement.get("uri");
				autosuggests.put(uriValue, new SuggestionItem(uriValue));
				SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
				String labelValue = (String) tmpElement.get("label");
				tmpAutosuggest.setLabel(labelValue);
				String descriptionValue = (String) tmpElement.get("description");
				if (descriptionValue != null) {
					tmpAutosuggest.setDescription(descriptionValue);
				}
				tmpAutosuggest.setSchemeTitle("DBpedia");
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/geonames")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsGEONAMES(@QueryParam("query") String searchword) {
		try {
			searchword = Utils.encodeURIComponent(searchword);
			String url_string = "http://api.geonames.org/searchJSON?q=" + searchword + "&maxRows=" + LIMIT + "&username=" + ConfigProperties.getPropertyParam("geonames");
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// fill objects
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONArray resultsArray = (JSONArray) jsonObject.get("geonames");
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (Object element : resultsArray) {
				JSONObject tmpElement = (JSONObject) element;
				Long uriValue = (Long) tmpElement.get("geonameId");
				String uri = "http://sws.geonames.org/" + uriValue;
				autosuggests.put(uri, new SuggestionItem(uri));
				SuggestionItem tmpAutosuggest = autosuggests.get(uri);
				String labelValue = (String) tmpElement.get("name");
				tmpAutosuggest.setLabel(labelValue);
				String adminName1 = (String) tmpElement.get("adminName1");
				String countryName = (String) tmpElement.get("countryName");
				String lat = (String) tmpElement.get("lat");
				String lon = (String) tmpElement.get("lng");
				tmpAutosuggest.setDescription(adminName1 + ", " + countryName + " [" + lat + " " + lon + "]");
				tmpAutosuggest.setSchemeTitle("GeoNames");
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/pelagiospleiadesplaces")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsPELAGIOS(@QueryParam("query") String searchword) {
		try {
			searchword = Utils.encodeURIComponent(searchword);
			String url_string = "http://pelagios.org/peripleo/search?query=" + searchword + "&types=place&limit=" + LIMIT;
			URL url = new URL(url_string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//conn.setRequestProperty("Accept", "application/json");
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// fill objects
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONArray resultsArray = (JSONArray) jsonObject.get("items");
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (Object element : resultsArray) {
				JSONObject tmpElement = (JSONObject) element;
				String uriValue = (String) tmpElement.get("identifier");
				if (uriValue.contains("pleiades.stoa.org")) {
					autosuggests.put(uriValue, new SuggestionItem(uriValue));
					SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
					String labelValue = (String) tmpElement.get("title");
					tmpAutosuggest.setLabel(labelValue);
					String descriptionValue = (String) tmpElement.get("description");
					if (descriptionValue != null) {
						tmpAutosuggest.setDescription(descriptionValue);
					}
					tmpAutosuggest.setSchemeTitle("Pleides Places");
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/chronontology")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsCHRONONTOLOGY(@QueryParam("query") String searchword) {
		try {
			searchword = Utils.encodeURIComponent(searchword);
			String url_string = "http://chronontology.dainst.org/data/period?q=" + searchword + "&limit=" + LIMIT;
			URL url = new URL(url_string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// fill objects
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONArray resultsArray = (JSONArray) jsonObject.get("results");
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (Object element : resultsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject resourceObject = (JSONObject) tmpElement.get("resource");
				String uriValue = (String) resourceObject.get("@id");
				uriValue = "http://chronontology.dainst.org" + uriValue;
				autosuggests.put(uriValue, new SuggestionItem(uriValue));
				SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
				String labelValue = resourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
				tmpAutosuggest.setLabel(labelValue);
				String descriptionValue = (String) resourceObject.get("description");
				if (descriptionValue != null) {
					tmpAutosuggest.setDescription(descriptionValue);
				}
				tmpAutosuggest.setSchemeTitle("ChronOntology");
				JSONArray isPartOfValue = (JSONArray) resourceObject.get("isPartOf");
				JSONArray hasPartValue = (JSONArray) resourceObject.get("hasPart");
				// query for broader
				if (isPartOfValue != null) {
					for (Object broaderItem : isPartOfValue) {
						String itemString = broaderItem.toString();
						String broaderurl = itemString.replace("/period", "/data/period");
						// query for json
						URL obj = new URL("http://chronontology.dainst.org" + broaderurl);
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
						con.setRequestMethod("GET");
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
						StringBuilder broaderResponse = new StringBuilder();
						while ((inputLine = in.readLine()) != null) {
							broaderResponse.append(inputLine);
						}
						in.close();
						// parse json
						JSONObject broaderjsonObject = (JSONObject) new JSONParser().parse(broaderResponse.toString());
						JSONObject broaderResourceObject = (JSONObject) broaderjsonObject.get("resource");
						String broaderUriValue = (String) broaderResourceObject.get("@id");
						broaderUriValue = "http://chronontology.dainst.org" + broaderUriValue;
						String broaderLabelValue = broaderResourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
						if (!broaderUriValue.equals("")) {
							HashMap<String, String> hstmpBroader = new HashMap<String, String>();
							hstmpBroader.put(broaderUriValue, broaderLabelValue);
							tmpAutosuggest.setBroaderTerm(hstmpBroader);
						}
					}
				}
				// query for narrower
				if (hasPartValue != null) {
					for (Object narrowerItem : hasPartValue) {
						String itemString = narrowerItem.toString();
						String narrowerurl = itemString.replace("/period", "/data/period");
						// query for json
						URL obj = new URL("http://chronontology.dainst.org" + narrowerurl);
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
						con.setRequestMethod("GET");
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
						StringBuilder narrowerResponse = new StringBuilder();
						while ((inputLine = in.readLine()) != null) {
							narrowerResponse.append(inputLine);
						}
						in.close();
						// parse json
						JSONObject narrowerjsonObject = (JSONObject) new JSONParser().parse(narrowerResponse.toString());
						JSONObject narrowerResourceObject = (JSONObject) narrowerjsonObject.get("resource");
						String narrowerUriValue = (String) narrowerResourceObject.get("@id");
						narrowerUriValue = "http://chronontology.dainst.org" + narrowerUriValue;
						String narrowerLabelValue = narrowerResourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
						if (!narrowerUriValue.equals("")) {
							HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
							hstmpNarrower.put(narrowerUriValue, narrowerLabelValue);
							tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
						}
					}
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/labelingsystem")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsLabelingSystemAll(@QueryParam("query") String searchword) {
		try {
			String url = ConfigProperties.getPropertyParam("api") + "/v1/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> PREFIX dc: <http://purl.org/dc/elements/1.1/> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme dc:title ?schemeTitle . "
					+ "?scheme ls:hasReleaseType ls:Public . "
					+ "?Subject skos:prefLabel ?pl . "
					+ "?Subject ls:preferredLabel ?prefLabel . "
					+ "?Subject ls:hasStatusType ls:Active . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred ls:preferredLabel ?BroaderPreferredTerm.} "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred ls:preferredLabel ?NarrowerPreferredTerm .} "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "} LIMIT " + LIMIT;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
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
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/labelingsystem/{vocabulary}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsLabelingSystemVocabulary(@QueryParam("query") String searchword, @PathParam("vocabulary") String vocabulary) {
		try {
			String url = ConfigProperties.getPropertyParam("api") + "/v1/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> PREFIX dc: <http://purl.org/dc/elements/1.1/> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme dc:title ?schemeTitle . "
					+ "?Subject skos:prefLabel ?pl . "
					+ "?Subject ls:preferredLabel ?prefLabel . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred ls:preferredLabel ?BroaderPreferredTerm.} "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred ls:preferredLabel ?NarrowerPreferredTerm .} "
					+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<" + ConfigProperties.getPropertyParam("http_protocol") + "://" + ConfigProperties.getPropertyParam("host") + "/item/vocabulary/" + vocabulary + ">) "
					+ "} LIMIT " + LIMIT;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
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
			JSONArray outArray = new JSONArray();
			// parse SPARQL results json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObject.get("bindings");
			// create unique list of ids
			HashSet<String> uris = new HashSet<String>();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				uris.add(subjectValue);
			}
			// create list of autosuggest objects
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (String element : uris) {
				autosuggests.put(element, new SuggestionItem(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDescription(scopeNoteValue);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroaderTerm(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/skosmos/finto")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsSKOSMOS_FINTO(@QueryParam("query") String searchword) {
		try {
			searchword = Utils.encodeURIComponent(searchword);
			String url_string = "http://finto.fi/rest/v1/search?query=*" + searchword + "*&lang=en&type=skos:Concept&fields=narrower%20broader&vocab=allars%20koko%20ponduskategorier%20ysa%20yso%20juho%20jupo%20keko%20okm-tieteenala%20liito%20mero%20puho%20tsr%20afo%20kassu%20mesh%20tero%20maotao%20musa%20muso%20valo%20kauno%20kito%20kto&limit=" + LIMIT;
			URL url = new URL(url_string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
			// init output
			JSONArray outArray = new JSONArray();
			// fill objects
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONArray resultsArray = (JSONArray) jsonObject.get("results");
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (Object element : resultsArray) {
				JSONObject tmpElement = (JSONObject) element;
				String uriValue = (String) tmpElement.get("uri");
				autosuggests.put(uriValue, new SuggestionItem(uriValue));
				SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
				String labelValue = (String) tmpElement.get("prefLabel");
				tmpAutosuggest.setLabel(labelValue);
				String vocabValue = (String) tmpElement.get("vocab");
				tmpAutosuggest.setSchemeTitle(vocabValue);
				JSONArray boraderArray = (JSONArray) tmpElement.get("skos:broader");
				JSONArray narrowerArray = (JSONArray) tmpElement.get("skos:narrower");
				// query for broader
				if (boraderArray != null) {
					for (Object item : boraderArray) {
						JSONObject tmpObject = (JSONObject) item;
						HashMap<String, String> hstmp = new HashMap();
						String uriValueTmp = (String) tmpObject.get("uri");
						String labelValueTmp = (String) tmpObject.get("prefLabel");
						hstmp.put(uriValueTmp, labelValueTmp);
						tmpAutosuggest.setBroaderTerm(hstmp);
					}
				}
				// query for narrower
				if (narrowerArray != null) {
					for (Object item : boraderArray) {
						JSONObject tmpObject = (JSONObject) item;
						HashMap<String, String> hstmp = new HashMap();
						String uriValueTmp = (String) tmpObject.get("uri");
						String labelValueTmp = (String) tmpObject.get("prefLabel");
						hstmp.put(uriValueTmp, labelValueTmp);
						tmpAutosuggest.setNarrowerTerm(hstmp);
					}
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/query/skosmos/fao")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getResultsSKOSMOS_FAO(@QueryParam("query") String searchword) {
		try {
			searchword = Utils.encodeURIComponent(searchword);
			String url_string = "http://oek1.fao.org/skosmos/rest/v1/search?query=*" + searchword + "*&lang=en&type=skos:Concept&fields=narrower%20broader&limit=" + LIMIT;
			URL url = new URL(url_string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
			// init output
			JSONArray outArray = new JSONArray();
			// fill objects
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONArray resultsArray = (JSONArray) jsonObject.get("results");
			Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
			for (Object element : resultsArray) {
				JSONObject tmpElement = (JSONObject) element;
				String uriValue = (String) tmpElement.get("uri");
				autosuggests.put(uriValue, new SuggestionItem(uriValue));
				SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
				String labelValue = (String) tmpElement.get("prefLabel");
				tmpAutosuggest.setLabel(labelValue);
				String vocabValue = (String) tmpElement.get("vocab");
				tmpAutosuggest.setSchemeTitle(vocabValue);
				JSONArray boraderArray = (JSONArray) tmpElement.get("skos:broader");
				JSONArray narrowerArray = (JSONArray) tmpElement.get("skos:narrower");
				// query for broader
				if (boraderArray != null) {
					for (Object item : boraderArray) {
						JSONObject tmpObject = (JSONObject) item;
						HashMap<String, String> hstmp = new HashMap();
						String uriValueTmp = (String) tmpObject.get("uri");
						String labelValueTmp = (String) tmpObject.get("prefLabel");
						hstmp.put(uriValueTmp, labelValueTmp);
						tmpAutosuggest.setBroaderTerm(hstmp);
					}
				}
				// query for narrower
				if (narrowerArray != null) {
					for (Object item : boraderArray) {
						JSONObject tmpObject = (JSONObject) item;
						HashMap<String, String> hstmp = new HashMap();
						String uriValueTmp = (String) tmpObject.get("uri");
						String labelValueTmp = (String) tmpObject.get("prefLabel");
						hstmp.put(uriValueTmp, labelValueTmp);
						tmpAutosuggest.setNarrowerTerm(hstmp);
					}
				}
			}
			// fill output json
			outArray = fillOutputJSONforQuery(autosuggests);
			return Response.ok(outArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/getty")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelGetty(@QueryParam("url") String url) {
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
				jsonOut.put("type", "getty");
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/heritagedata")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelHeritageData(@QueryParam("url") String url) {
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
				jsonOut.put("type", "heritagedata");
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/labelingsystem")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelLabelingSystem(@QueryParam("url") String url) {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String sparqlendpoint = ConfigProperties.getPropertyParam("api") + "/v1/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#>"
					+ "SELECT * { "
					+ "OPTIONAL { <" + url + "> ls:preferredLabel ?prefLabel. } "
					+ "<" + url + "> ls:hasStatusType ?statusType. "
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
			if (!bindingsArray.isEmpty()) {
				for (Object element : bindingsArray) {
					JSONObject tmpElement = (JSONObject) element;
					JSONObject prefLabel = (JSONObject) tmpElement.get("prefLabel");
					String labelValue = "";
					String labelLang = "";
					String stValue = "";
					if (prefLabel != null) {
						labelValue = (String) prefLabel.get("value");
						labelLang = (String) prefLabel.get("xml:lang");
						jsonOut.put("label", labelValue + "@" + labelLang);
					} else {
						jsonOut.put("label", "");
					}
					JSONObject statusType = (JSONObject) tmpElement.get("statusType");
					stValue = (String) statusType.get("value");
					jsonOut.put("type", "ls" + "+" + stValue.replace(rdf.getPrefixItem("ls:"), ""));
				}
			} else {
				throw new ResourceNotAvailableException();
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/html")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelExtern(@QueryParam("url") String url, @QueryParam("type") String type) {
		try {
			url = Utils.encodeURIUmlaut(url);
			Document doc = Jsoup.connect(url).get();
			Elements titleTag = doc.select("title");
			JSONObject jsonOut = new JSONObject();
			String out = titleTag.text();
			if (url.startsWith("http://dbpedia.org/resource/")) {
				out = out.replace("About: ", "");
			}
			jsonOut.put("label", out);
			if (type != null) {
				jsonOut.put("type", type);
			} else {
				jsonOut.put("type", "wayback");
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/geonames")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelGeoNames(@QueryParam("url") String url) {
		try {
			url = url.replace("http://sws.geonames.org/", "");
			url = "http://api.geonames.org/get?geonameId=" + url + "&username=" + ConfigProperties.getPropertyParam("geonames");
			// query for xml
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			String urlParameters = "";
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
			// parse xml
			int startTagName = response.indexOf("<name>");
			int endTagName = response.indexOf("</name>");
			String name = response.substring(startTagName, endTagName).replace("<name>", "");
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", name);
			jsonOut.put("type", "geonames");
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/pelagiospleiadesplaces")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelPelagios(@QueryParam("url") String url) {
		try {
			url = Utils.encodeURIComponent(url);
			url = "http://pelagios.org/peripleo/places/" + url;
			// query for json
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// parse json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			String title = (String) jsonObject.get("title");
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", title);
			jsonOut.put("type", "pleiades");
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/chronontology")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelChronontology(@QueryParam("url") String url) {
		try {
			url = url.replace("/period", "/data/period");
			// query for json
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// parse json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resourceObject = (JSONObject) jsonObject.get("resource");
			String labelValue = resourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", labelValue);
			jsonOut.put("type", "chronontology");
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/skosmos/finto")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelSkosmosFinto(@QueryParam("url") String url) {
		try {
			// query for json
			String vocab = url.split("/")[4];
			url = Utils.encodeURIComponent(url);
			url = "http://api.finto.fi/rest/v1/" + vocab + "/label?lang=en&uri=" + url;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// parse json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			String labelValue = (String) jsonObject.get("prefLabel");
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", labelValue);
			jsonOut.put("type", "fao");
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/label/skosmos/fao")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response geLabelSkosmosFao(@QueryParam("url") String url) {
		try {
			// query for json
			url = Utils.encodeURIComponent(url);
			url = "http://oek1.fao.org/skosmos/rest/v1/agrovoc/label?lang=en&uri=" + url;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// parse json
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			String labelValue = (String) jsonObject.get("prefLabel");
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", labelValue);
			jsonOut.put("type", "fao");
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	private JSONArray fillOutputJSONforQuery(Map<String, SuggestionItem> autosuggests) {
		JSONArray outArray = new JSONArray();
		for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
			SuggestionItem tmpAS = entry.getValue();
			JSONObject suggestionObject = new JSONObject();
			// url
			suggestionObject.put("uri", tmpAS.getURL());
			// labels
			suggestionObject.put("label", tmpAS.getLabels().iterator().next());
			// scheme
			suggestionObject.put("scheme", tmpAS.getSchemeTitle());
			// descriptions
			if (tmpAS.getDescriptions().size() > 0) {
				suggestionObject.put("description", tmpAS.getDescriptions().iterator().next());
			} else {
				suggestionObject.put("description", "");
			}
			// broader
			Set broaderTerms = tmpAS.getBroaderTerms();
			JSONArray broaderArrayNew = new JSONArray();
			if (broaderTerms.size() > 0) {
				for (Object element : broaderTerms) {
					Map hm = (Map) element;
					Iterator entries = hm.entrySet().iterator();
					while (entries.hasNext()) {
						Map.Entry thisEntry = (Map.Entry) entries.next();
						String key = (String) thisEntry.getKey();
						String value = (String) thisEntry.getValue();
						JSONObject broaderObjectTmp = new JSONObject();
						broaderObjectTmp.put("uri", key);
						broaderObjectTmp.put("label", value);
						broaderArrayNew.add(broaderObjectTmp);
					}
				}
			}
			suggestionObject.put("broaderTerms", broaderArrayNew);
			// narrrower
			Set narrrowerTerms = tmpAS.getNarrowerTerms();
			JSONArray narrrowerArrayNew = new JSONArray();
			if (narrrowerTerms.size() > 0) {
				for (Object element : narrrowerTerms) {
					Map hm = (Map) element;
					Iterator entries = hm.entrySet().iterator();
					while (entries.hasNext()) {
						Map.Entry thisEntry = (Map.Entry) entries.next();
						String key = (String) thisEntry.getKey();
						String value = (String) thisEntry.getValue();
						JSONObject narrrowerObjectTmp = new JSONObject();
						narrrowerObjectTmp.put("uri", key);
						narrrowerObjectTmp.put("label", value);
						narrrowerArrayNew.add(narrrowerObjectTmp);
					}
				}
			}
			suggestionObject.put("narrrowerTerms", narrrowerArrayNew);
			// add information to output array
			outArray.add(suggestionObject);
		}
		return outArray;
	}

}
