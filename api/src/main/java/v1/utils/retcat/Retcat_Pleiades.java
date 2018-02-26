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
import link.labeling.retcat.classes.RetcatItem;
import link.labeling.retcat.classes.SuggestionItem;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import v1.rest.RetcatResource;
import v1.utils.generalfuncs.GeneralFunctions;

public class Retcat_Pleiades {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException, link.labeling.retcat.exceptions.ResourceNotAvailableException {
		searchword = GeneralFunctions.encodeURIComponent(searchword);
		String url_string = "http://pelagios.org/peripleo/search?query=" + searchword + "&types=place&limit=" + RetcatResource.getLimit();
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
				// get retcat info
				String type = "pleiades";
				String quality = "";
				String group = "";
				for (RetcatItem item : LocalRetcatItems.getAllRetcatItems()) {
					if (item.getType().equals(type)) {
						quality = item.getQuality();
						group = item.getGroup();
					}
				}
				tmpAutosuggest.setType(type);
				tmpAutosuggest.setQuality(quality);
				tmpAutosuggest.setGroup(group);
			}
		}
		return autosuggests;
	}

	public static JSONObject info(String url) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException, RetcatException {
		try {
			String outputUrl = url;
			url = GeneralFunctions.encodeURIComponent(url);
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
			String description = (String) jsonObject.get("description");
			// output
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("label", title);
			jsonOut.put("lang", "");
			// get retcat info
			String type = "pleiades";
			String quality = "";
			String group = "";
			for (RetcatItem item : LocalRetcatItems.getAllRetcatItems()) {
				if (item.getType().equals(type)) {
					quality = item.getQuality();
					group = item.getGroup();
				}
			}
			jsonOut.put("type", type);
			jsonOut.put("quality", quality);
			jsonOut.put("group", group);
			jsonOut.put("uri", outputUrl);
			jsonOut.put("scheme", "Pleides Places");
			JSONArray broader = new JSONArray();
			JSONArray narrower = new JSONArray();
			jsonOut.put("broaderTerms", broader);
			jsonOut.put("narrowerTerms", narrower);
			jsonOut.put("description", description);
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
