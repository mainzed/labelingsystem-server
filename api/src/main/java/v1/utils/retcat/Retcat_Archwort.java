package v1.utils.retcat;

import exceptions.ResourceNotAvailableException;
import exceptions.RetcatException;
import exceptions.SesameSparqlException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Retcat_Archwort {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
		String url = "http://archwort.dainst.org/de/vocab/sparql.php";
		String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX dc: <http://purl.org/dc/elements/1.1/> "
				+ "SELECT ?Subject ?prefLabel ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
				+ "?Subject skos:inScheme ?scheme . "
				+ "?Subject skos:prefLabel ?prefLabel . "
				+ "?scheme dc:title ?schemeTitle . "
				+ "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
				+ "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
				+ "FILTER(regex(?prefLabel, '" + searchword + "', 'i')) "
				+ "}";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "application/sparql-results+json");
		String urlParameters = "query=" + sparql;
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
		writer.write(urlParameters);
		writer.close();
		wr.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
		String inputLine;
		StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
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
			subjectValue = subjectValue.replace("http://archwort.dainst.org/thesaurus/de/vocab/?tema=", "http://archwort.dainst.org/de/term/");
			uris.add(subjectValue);
		}
		// create list of autosuggest objects
		Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
		for (String element : uris) {
			autosuggests.put(element, new SuggestionItem(element));
		}
		int z = 0;
		// fill objects
		for (Object element : bindingsArray) {
			JSONObject tmpElement = (JSONObject) element;
			// get Subject
			JSONObject subject = (JSONObject) tmpElement.get("Subject");
			String subjectValue = (String) subject.get("value");
			subjectValue = subjectValue.replace("http://archwort.dainst.org/thesaurus/de/vocab/?tema=", "http://archwort.dainst.org/de/term/");
			// for every subject value get object from list and write values in it 
			SuggestionItem tmpAutosuggest = autosuggests.get(subjectValue);
			// get Label
			JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
			String labelValue = (String) labelObject.get("value");
			String labelLang = (String) labelObject.get("xml:lang");
			tmpAutosuggest.setLabel(labelValue);
			tmpAutosuggest.setLanguage(labelLang);
			// get Scheme
			JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
			String schemeValue = (String) schemeObject.get("value");
			tmpAutosuggest.setSchemeTitle(schemeValue);
			// get broader 
			String broaderVL = "";
			String broaderURI = "";
			String broaderLang = "";
			JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
			if (broaderObject != null) {
				String broaderValue = (String) broaderObject.get("value");
				broaderLang = (String) broaderObject.get("xml:lang");
				broaderVL = broaderValue.replace("<", "").replace(">", "");
			}
			JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
			if (broaderURIObject != null) {
				broaderURI = (String) broaderURIObject.get("value");
			}
			if (!broaderURI.equals("")) {
				HashMap<String, String> hstmpBroader = new HashMap<String, String>();
				broaderURI = broaderURI.replace("http://archwort.dainst.org/thesaurus/de/vocab/?tema=", "http://archwort.dainst.org/de/term/");
				hstmpBroader.put(broaderURI, broaderVL);
				tmpAutosuggest.setBroaderTerm(hstmpBroader);
			}
			// get narrower 
			String narrowerVL = "";
			String narrowerURI = "";
			String narrowerLang = "";
			JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
			if (narrowerObject != null) {
				String narrowerValue = (String) narrowerObject.get("value");
				narrowerLang = (String) narrowerObject.get("xml:lang");
				narrowerVL = narrowerValue.replace("<", "").replace(">", "");
			}
			JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
			if (narrowerURIObject != null) {
				narrowerURI = (String) narrowerURIObject.get("value");
			}
			if (!narrowerURI.equals("")) {
				HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
				narrowerURI = narrowerURI.replace("http://archwort.dainst.org/thesaurus/de/vocab/?tema=", "http://archwort.dainst.org/de/term/");
				hstmpNarrower.put(narrowerURI, narrowerVL);
				tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
			}
			// get retcat info
			String type = "archwort";
			String quality = "";
			String group = "";
			for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
				if (item.getType().equals(type)) {
					quality = item.getQuality();
					group = item.getGroup();
				}
			}
			tmpAutosuggest.setType(type);
			tmpAutosuggest.setQuality(quality);
			tmpAutosuggest.setGroup(group);
		}
		return autosuggests;
	}

	public static JSONObject info(String url) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException, RetcatException {
		try {
			String[] id = url.split("/");
			String newURL = "http://archwort.dainst.org/de/term/xml.php?jsonldTema=" + id[id.length - 1];
			URL obj = new URL(newURL);
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
			JSONObject jsonOut = new JSONObject();
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			// output
			JSONObject prefLabel = (JSONObject) jsonObject.get("skos:prefLabel");
			String lang = (String) prefLabel.get("@language");
			prefLabel.remove("@language");
			String label = prefLabel.toJSONString().replace("{\"@value=\":\"", "").replace("\"}", "");
			jsonOut.put("label", label);
			jsonOut.put("lang", lang);
			jsonOut.put("description", "");
			jsonOut.put("uri", url);
			// get retcat info
			String type = "archwort";
			String quality = "";
			String group = "";
			for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
				if (item.getType().equals(type)) {
					quality = item.getQuality();
					group = item.getGroup();
				}
			}
			jsonOut.put("type", type);
			jsonOut.put("quality", quality);
			jsonOut.put("group", group);
			jsonOut.put("scheme", "archwort");
			// broader and narrower
			JSONArray broaderTerms = new JSONArray();
			JSONArray narrowerTerms = new JSONArray();
			JSONArray broader = (JSONArray) jsonObject.get("skos:broader");
			JSONArray narrower = (JSONArray) jsonObject.get("skos:narrower");
			// broader
			if (broader != null) {
				for (Object broaderItem : broader) {
					String tmp = (String) broaderItem;
					String[] tmpArray = tmp.split("/");
					String broaderurl = "http://archwort.dainst.org/de/term/xml.php?jsonldTema=" + tmpArray[tmpArray.length - 1];
					URL obj2 = new URL(broaderurl);
					HttpURLConnection con2 = (HttpURLConnection) obj2.openConnection();
					con2.setRequestMethod("GET");
					BufferedReader in2 = new BufferedReader(new InputStreamReader(con2.getInputStream(), "UTF8"));
					String inputLine2;
					StringBuilder response2 = new StringBuilder();
					while ((inputLine2 = in2.readLine()) != null) {
						response2.append(inputLine2);
					}
					in.close();
					// parse json
					JSONObject broaderTmp = new JSONObject();
					JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(response2.toString());
					JSONObject prefLabel2 = (JSONObject) jsonObject2.get("skos:prefLabel");
					prefLabel2.remove("@language");
					String label2 = prefLabel2.toJSONString().replace("{\"@value=\":\"", "").replace("\"}", "");
					broaderTmp.put("label", label2);
					tmp = tmp.replace("http://archwort.dainst.org/thesaurus/de/vocab/?tema=", "http://archwort.dainst.org/de/term/");
					broaderTmp.put("uri", tmp);
					broaderTerms.add(broaderTmp);
				}
			}
			// narrower
			if (narrower != null) {
				for (Object narrowerItem : narrower) {
					String tmp = (String) narrowerItem;
					String[] tmpArray = tmp.split("/");
					String narrowerurl = "http://archwort.dainst.org/de/term/xml.php?jsonldTema=" + tmpArray[tmpArray.length - 1];
					URL obj2 = new URL(narrowerurl);
					HttpURLConnection con2 = (HttpURLConnection) obj2.openConnection();
					con2.setRequestMethod("GET");
					BufferedReader in2 = new BufferedReader(new InputStreamReader(con2.getInputStream(), "UTF8"));
					String inputLine2;
					StringBuilder response2 = new StringBuilder();
					while ((inputLine2 = in2.readLine()) != null) {
						response2.append(inputLine2);
					}
					in.close();
					// parse json
					JSONObject narrowerTmp = new JSONObject();
					JSONObject jsonObject2 = (JSONObject) new JSONParser().parse(response2.toString());
					JSONObject prefLabel2 = (JSONObject) jsonObject2.get("skos:prefLabel");
					prefLabel2.remove("@language");
					String label2 = prefLabel2.toJSONString().replace("{\"@value=\":\"", "").replace("\"}", "");
					narrowerTmp.put("label", label2);
					tmp = tmp.replace("http://archwort.dainst.org/thesaurus/de/vocab/?tema=", "http://archwort.dainst.org/de/term/");
					narrowerTmp.put("uri", tmp);
					narrowerTerms.add(narrowerTmp);
				}
			}
			jsonOut.put("broaderTerms", broaderTerms);
			jsonOut.put("narrowerTerms", narrowerTerms);
			if (jsonOut.get("label") != null && !jsonOut.get("label").equals("")) {
				return jsonOut;
			} else {
				throw new RetcatException("no label for this uri available");
			}
		} catch (Exception e) {
			return new JSONObject();
		}
	}

}
