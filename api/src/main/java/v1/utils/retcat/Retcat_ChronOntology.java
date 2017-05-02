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

public class Retcat_ChronOntology {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
		searchword = GeneralFunctions.encodeURIComponent(searchword);
		String url_string = "http://chronontology.dainst.org/data/period?q=" + searchword + "&limit=" + RetcatResource.getLimit();
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
			JSONObject resourceObject = (JSONObject) tmpElement.get("resource");
			String uriValue = (String) tmpElement.get("@id");
			uriValue = "http://chronontology.dainst.org" + uriValue;
			autosuggests.put(uriValue, new SuggestionItem(uriValue));
			SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
			JSONArray names = (JSONArray) resourceObject.get("names");
			String labelValue = null;
			String labelLang = null;
			for (Object item : names) {
				JSONObject tmp = (JSONObject) item;
				if (tmp.get("lang").equals("en")) {
					labelLang = (String) tmp.get("lang");
					JSONArray content = (JSONArray) tmp.get("content");
					labelValue = (String) content.get(0);
				}
			}
			tmpAutosuggest.setLabel(labelValue);
			tmpAutosuggest.setLanguage(labelLang);
			String descriptionValue = (String) resourceObject.get("description");
			if (descriptionValue != null) {
				tmpAutosuggest.setDescription(descriptionValue);
			}
			tmpAutosuggest.setSchemeTitle("ChronOntology");
			// get retcat info
			String type = "chronontology";
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
			JSONArray names = (JSONArray) resourceObject.get("names");
			String labelValue = null;
			String labelLang = null;
			for (Object item : names) {
				JSONObject tmp = (JSONObject) item;
				if (tmp.get("lang").equals("en")) {
					labelLang = (String) tmp.get("lang");
					JSONArray content = (JSONArray) tmp.get("content");
					labelValue = (String) content.get(0);
				}
			}
			String descValue = "";
			if (resourceObject.get("description") != null) {
				descValue = resourceObject.get("description").toString();
			}
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", labelValue);
			jsonOut.put("lang", labelLang);
			// get retcat info
			String type = "chronontology";
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
			jsonOut.put("description", descValue);
			jsonOut.put("uri", outputUrl);
			jsonOut.put("scheme", "ChronOntology");
			jsonOut.put("broaderTerms", new JSONArray());
			jsonOut.put("narrowerTerms", new JSONArray());
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
