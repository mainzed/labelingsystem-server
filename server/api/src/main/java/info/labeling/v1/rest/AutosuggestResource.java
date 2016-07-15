package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.Autosuggest;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@Path("/v1/autosuggests")
public class AutosuggestResource {

	private final String LIMIT = "20";

	@GET
	@Path("/heritagedata/historicengland")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsHE(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm.} "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm .} "
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroader(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/heritagedata/rcahms")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsRCAHMS(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm.} "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm .} "
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroader(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/heritagedata/rcahmw")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsRCAHMW(@QueryParam("query") String searchword) {
		try {
			String url = "http://heritagedata.org/live/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?Subject skos:prefLabel ?prefLabel . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm.} "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm .} "
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroader(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/getty/aat")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsAAT(@QueryParam("query") String searchword, @QueryParam("lang") String lang) {
		try {
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				tmpAutosuggest.setSchemeTitle(schemeValue);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroader(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/getty/tgn")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsTGN(@QueryParam("query") String searchword, @QueryParam("lang") String lang) {
		try {
			String url = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle{ "
					+ "?Subject a skos:Concept. "
					+ "?Subject luc:term '" + searchword + "' . "
					+ "?Subject skos:inScheme tgn: . "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme rdfs:label ?schemeTitle . "
					+ "?Subject gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					//+ "OPTIONAL {?Subject skos:scopeNote [dct:language gvp_lang:" + lang + "; rdf:value ?scopeNote]} . "
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
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
					tmpAutosuggest.setDefinition(scopeNoteValue);
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
					tmpAutosuggest.setBroader(hstmpBroader);
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
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/getty/ulan")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsULAN(@QueryParam("query") String searchword, @QueryParam("lang") String lang) {
		try {
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
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
					tmpAutosuggest.setDefinition(scopeNoteValue);
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
					tmpAutosuggest.setBroader(hstmpBroader);
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
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
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
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONArray outArray = new JSONArray();
			// fill objects
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONArray resultsArray = (JSONArray) jsonObject.get("results");
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (Object element : resultsArray) {
				JSONObject tmpElement = (JSONObject) element;
				String uriValue = (String) tmpElement.get("uri");
				autosuggests.put(uriValue, new Autosuggest(uriValue));
				Autosuggest tmpAutosuggest = autosuggests.get(uriValue);
				String labelValue = (String) tmpElement.get("label");
				tmpAutosuggest.setLabel(labelValue);
				String descriptionValue = (String) tmpElement.get("description");
				if (descriptionValue!=null) {
					tmpAutosuggest.setDefinition(descriptionValue);
				}
				JSONArray classesArray = (JSONArray) tmpElement.get("classes");
				if (classesArray != null) {
					for (Object item : classesArray) {
						JSONObject tmpObject = (JSONObject) item;
						String broaderValue = (String) tmpObject.get("label");
						String broaderURI = (String) tmpObject.get("uri");
						HashMap<String, String> hstmpBroader = new HashMap<String, String>();
						hstmpBroader.put(broaderURI, broaderValue);
						tmpAutosuggest.setBroader(hstmpBroader);
					}
				 }
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("scheme", "DBpedia");
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/labelingsystem")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsLabelingSystem(@QueryParam("query") String searchword) {
		try {
			String url = "http://localhost:8084/api/v1/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> PREFIX dc: <http://purl.org/dc/elements/1.1/> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme dc:title ?schemeTitle . "
					+ "?Subject skos:prefLabel ?prefLabels . "
					+ "?Subject ls:preferredLabel ?prefLabel . "
					+ "OPTIONAL { ?Subject skos:note ?scopeNote . } "
					+ "OPTIONAL { ?Subject skos:note ?scopeNotes . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred ls:preferredLabel ?BroaderPreferredTerm.} "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred ls:preferredLabel ?NarrowerPreferredTerm .} "
					+ "FILTER(regex(?prefLabels, '" + searchword + "', 'i') || regex(?scopeNotes, '" + searchword + "', 'i')) "
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroader(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}
	
	@GET
	@Path("/labelingsystem/{vocabulary}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getSPARQLresultsLabelingSystem(@QueryParam("query") String searchword, @PathParam("vocabulary") String vocabulary) {
		try {
			String url = "http://localhost:8084/api/v1/sparql";
			String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> PREFIX dc: <http://purl.org/dc/elements/1.1/> "
					+ "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
					+ "?Subject skos:inScheme ?scheme . "
					+ "?scheme dc:title ?schemeTitle . "
					+ "?Subject skos:prefLabel ?prefLabels . "
					//+ "?Subject ls:preferredLabel ?prefLabel . "
					+ "OPTIONAL { ?Subject skos:note ?scopeNote . } "
					+ "OPTIONAL { ?Subject skos:note ?scopeNotes . } "
					+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred ls:preferredLabel ?BroaderPreferredTerm.} "
					+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred ls:preferredLabel ?NarrowerPreferredTerm .} "
					+ "FILTER(regex(?prefLabels, '" + searchword + "', 'i') || regex(?scopeNotes, '" + searchword + "', 'i')) "
					+ "FILTER(?scheme=<http://"+PropertiesLocal.getPropertyParam("host")+"/item/vocabulary/"+vocabulary+">) "
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
			Map<String, Autosuggest> autosuggests = new HashMap<String, Autosuggest>();
			for (String element : uris) {
				autosuggests.put(element, new Autosuggest(element));
			}
			// fill objects
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Subject
				JSONObject subject = (JSONObject) tmpElement.get("Subject");
				String subjectValue = (String) subject.get("value");
				// for every subject value get object from list and write values in it 
				Autosuggest tmpAutosuggest = autosuggests.get(subjectValue);
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
				// get Scheme
				JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
				String schemeValue = (String) schemeObject.get("value");
				String schemeLang = (String) schemeObject.get("xml:lang");
				tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
				// get scopeNote
				JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
				if (scopeNoteObject != null) {
					String scopeNoteValue = (String) scopeNoteObject.get("value");
					String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
					tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
				}
				// get broader 
				String broaderVL = "";
				String broaderURI = "";
				JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (broaderObject != null) {
					String broaderValue = (String) broaderObject.get("value");
					String broaderLang = (String) broaderObject.get("xml:lang");
					broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
				}
				JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
				if (broaderURIObject != null) {
					broaderURI = (String) broaderURIObject.get("value");
				}
				if (!broaderURI.equals("")) {
					HashMap<String, String> hstmpBroader = new HashMap<String, String>();
					hstmpBroader.put(broaderURI, broaderVL);
					tmpAutosuggest.setBroader(hstmpBroader);
				}
				// get narrower 
				String narrowerVL = "";
				String narrowerURI = "";
				JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (narrowerObject != null) {
					String narrowerValue = (String) narrowerObject.get("value");
					String narrowerLang = (String) narrowerObject.get("xml:lang");
					narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
				}
				JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
				if (narrowerURIObject != null) {
					narrowerURI = (String) narrowerURIObject.get("value");
				}
				if (!narrowerURI.equals("")) {
					HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
					hstmpNarrower.put(narrowerURI, narrowerVL);
					tmpAutosuggest.setNarrower(hstmpNarrower);
				}
			}
			// fill output json
			for (Map.Entry<String, Autosuggest> entry : autosuggests.entrySet()) {
				Autosuggest tmpAS = entry.getValue();
				JSONObject suggestionObject = new JSONObject();
				JSONObject suggestionObjectCollection = new JSONObject();
				// label
				JSONArray labelArrayNew = new JSONArray();
				labelArrayNew.add(tmpAS.getLabel());
				suggestionObjectCollection.put("label", labelArrayNew);
				// scheme
				JSONArray schemeArrayNew = new JSONArray();
				schemeArrayNew.add(tmpAS.getSchemeTitle());
				suggestionObjectCollection.put("scheme", schemeArrayNew);
				// definition
				JSONArray scopeNoteArrayNew = new JSONArray();
				if (!tmpAS.getDefinition().equals("")) {
					scopeNoteArrayNew.add(tmpAS.getDefinition());
				}
				suggestionObjectCollection.put("definition", scopeNoteArrayNew);
				// broader
				Set broaderTerms = tmpAS.getBroader();
				JSONArray broaderArrayNew = new JSONArray();
				if (broaderTerms.size() > 0) {
					for (Object element : broaderTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject broaderObjectTmp = new JSONObject();
							broaderObjectTmp.put("uri", key);
							broaderObjectTmp.put("label", value);
							broaderArrayNew.add(broaderObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("broader", broaderArrayNew);
				// narrrower
				Set narrrowerTerms = tmpAS.getNarrower();
				JSONArray narrrowerArrayNew = new JSONArray();
				if (narrrowerTerms.size() > 0) {
					for (Object element : narrrowerTerms) {
						Map hm = (Map) element;
						Iterator entries = hm.entrySet().iterator();
						while (entries.hasNext()) {
							Entry thisEntry = (Entry) entries.next();
							String key = (String) thisEntry.getKey();
							String value = (String) thisEntry.getValue();
							JSONObject narrrowerObjectTmp = new JSONObject();
							narrrowerObjectTmp.put("uri", key);
							narrrowerObjectTmp.put("label", value);
							narrrowerArrayNew.add(narrrowerObjectTmp);
						}
					}
				}
				suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
				// add information to output
				suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
				outArray.add(suggestionObject);
			}
			jsonOut.put("autosuggest", outArray);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AutosuggestResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}
