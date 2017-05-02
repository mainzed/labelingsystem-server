package v1.utils.retcat;

import exceptions.ResourceNotAvailableException;
import exceptions.RetcatException;
import exceptions.SesameSparqlException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import v1.rest.RetcatResource;
import v1.utils.generalfuncs.GeneralFunctions;

public class Retcat_Dbpedia {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
		searchword = GeneralFunctions.encodeURIComponent(searchword);
		String url_string = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=" + searchword + "&MaxHits=" + RetcatResource.getLimit();
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
			// get retcat info
			String type = "dbpedia";
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
			String outputUrl = url;
			url = url.replace("resource", "data");
			url = url + ".json";
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
			JSONObject thisValue = (JSONObject) jsonObject.get(outputUrl);
			JSONArray nameArray = (JSONArray) thisValue.get("http://www.w3.org/2000/01/rdf-schema#label");
			String name = "";
			String lang = "";
			for (Object element : nameArray) {
				JSONObject tmpObj = (JSONObject) element;
				String langTmp = (String) tmpObj.get("lang");
				if (langTmp.equals("en")) {
					name = (String) tmpObj.get("value");
					lang = (String) tmpObj.get("lang");
				}
			}
			if (name.equals("")) {
				JSONObject tmpObj = (JSONObject) nameArray.get(0);
				name = (String) tmpObj.get("value");
				lang = (String) tmpObj.get("lang");
			}
			JSONArray abstractArray = (JSONArray) thisValue.get("http://dbpedia.org/ontology/abstract");
			String desc = "";
			if (abstractArray != null) {
				for (Object element : abstractArray) {
					JSONObject tmpObj = (JSONObject) element;
					String langTmp = (String) tmpObj.get("lang");
					if (langTmp.equals("en")) {
						desc = (String) tmpObj.get("value");
					}
				}
				if (desc.equals("")) {
					JSONObject tmpObj = (JSONObject) abstractArray.get(0);
					desc = (String) tmpObj.get("value");
				}
			}
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", name);
			jsonOut.put("lang", lang);
			// get retcat info
			String type = "dbpedia";
			String quality = "";
			String group = "";
			for (RetcatItem item : RetcatItems.getAllRetcatItems()) {
				if (item.getType().equals(type)) {
					quality = item.getQuality();
					group = item.getGroup();
					type = item.getType();
				}
			}
			jsonOut.put("type", type);
			jsonOut.put("quality", quality);
			jsonOut.put("group", group);
			jsonOut.put("description", desc);
			jsonOut.put("uri", outputUrl);
			jsonOut.put("scheme", "DBpedia");
			// broader and narrower
			JSONArray broaderTerms = new JSONArray();
			JSONArray narrowerTerms = new JSONArray();
			jsonOut.put("broaderTerms", broaderTerms);
			jsonOut.put("narrowerTerms", narrowerTerms);
			if (jsonOut.get("label") != null && !jsonOut.get("label").equals("")) {
				return jsonOut;
			} else {
				throw new RetcatException("no label for this uri available");
			}
		} catch (Exception e) {
			throw new RetcatException(e.toString());
		}
	}

}
