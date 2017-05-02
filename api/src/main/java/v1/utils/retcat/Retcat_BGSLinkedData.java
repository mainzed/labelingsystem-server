package v1.utils.retcat;

import exceptions.ResourceNotAvailableException;
import exceptions.RetcatException;
import exceptions.SesameSparqlException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

public class Retcat_BGSLinkedData {

	public static Map<String, SuggestionItem> query(String searchword) {
		// no query API available, return empty object, use info with uri prefix
		return new HashMap<>();
	}

	public static JSONObject info(String url) throws MalformedURLException, IOException, ParseException, ParseException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, RetcatException {
		try {
			String dataURL = url.replace("/id/", "/doc/");
			dataURL += ".JSON";
			URL obj = new URL(dataURL);
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
			JSONObject conceptObject = (JSONObject) jsonObject.get(url);
			JSONArray prefLabel = (JSONArray) conceptObject.get("http://www.w3.org/2000/01/rdf-schema#label");
			JSONObject prefLabelObj = (JSONObject) prefLabel.get(0);
			jsonOut.put("label", prefLabelObj.get("value"));
			jsonOut.put("lang", prefLabelObj.get("lang"));
			JSONArray definition = (JSONArray) conceptObject.get("http://www.w3.org/2004/02/skos/core#definition");
			if (definition != null) {
				JSONObject definitionObj = (JSONObject) definition.get(0);
				jsonOut.put("description", definitionObj.get("value"));
			}
			JSONArray scheme = (JSONArray) conceptObject.get("http://www.w3.org/2004/02/skos/core#inScheme");
			if (scheme != null) {
				JSONObject schemeObj = (JSONObject) scheme.get(0);
				String schemeValue = (String) schemeObj.get("value");
				String[] schemeSplit = schemeValue.split("/");
				jsonOut.put("scheme", schemeSplit[schemeSplit.length - 1]);
			} else {
				jsonOut.put("scheme", "BGS Linked Data");
			}
			jsonOut.put("uri", url);
			// get retcat info
			String type = "bgs";
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
			// broader and narrower (not yet available)
			jsonOut.put("broaderTerms", new JSONArray());
			jsonOut.put("narrowerTerms", new JSONArray());
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
