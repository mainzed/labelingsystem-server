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

public class Retcat_Fao {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
		searchword = GeneralFunctions.encodeURIComponent(searchword);
		String url_string = "http://oek1.fao.org/skosmos/rest/v1/search?query=*" + searchword + "*&lang=en&type=skos:Concept&fields=narrower%20broader&limit=" + RetcatResource.getLimit();
		URL url = new URL(url_string);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
			String labelValue = (String) tmpElement.get("prefLabel");
			tmpAutosuggest.setLabel(labelValue);
			String vocabValue = (String) tmpElement.get("vocab");
			tmpAutosuggest.setSchemeTitle(vocabValue);
			JSONArray boraderArray = (JSONArray) tmpElement.get("broader");
			JSONArray narrowerArray = (JSONArray) tmpElement.get("narrower");
			tmpAutosuggest.setLanguage("en");
			// query for broader
			if (boraderArray != null) {
				for (Object item : boraderArray) {
					JSONObject tmpObject = (JSONObject) item;
					HashMap<String, String> hstmp = new HashMap();
					String uriValueTmp = (String) tmpObject.get("uri");
					//query for label
					String broaderUrl = GeneralFunctions.encodeURIComponent(uriValueTmp);
					broaderUrl = "http://oek1.fao.org/skosmos/rest/v1/agrovoc/label?lang=en&uri=" + broaderUrl;
					URL obj = new URL(broaderUrl);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
					con.setRequestMethod("GET");
					BufferedReader broaderIn = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
					String broaderInputLine;
					StringBuilder broaderResponse = new StringBuilder();
					while ((broaderInputLine = broaderIn.readLine()) != null) {
						broaderResponse.append(broaderInputLine);
					}
					broaderIn.close();
					// parse json
					JSONObject broaderJsonObject = (JSONObject) new JSONParser().parse(broaderResponse.toString());
					String broaderLabelValue = (String) broaderJsonObject.get("prefLabel");
					hstmp.put(uriValueTmp, broaderLabelValue);
					tmpAutosuggest.setBroaderTerm(hstmp);
				}
			}
			// query for narrower
			if (narrowerArray != null) {
				for (Object item : boraderArray) {
					JSONObject tmpObject = (JSONObject) item;
					HashMap<String, String> hstmp = new HashMap();
					String uriValueTmp = (String) tmpObject.get("uri");
					//query for label
					String narrowerUrl = GeneralFunctions.encodeURIComponent(uriValueTmp);
					narrowerUrl = "http://oek1.fao.org/skosmos/rest/v1/agrovoc/label?lang=en&uri=" + narrowerUrl;
					URL obj = new URL(narrowerUrl);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
					con.setRequestMethod("GET");
					BufferedReader narrowerIn = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
					String narrowerInputLine;
					StringBuilder narrowerResponse = new StringBuilder();
					while ((narrowerInputLine = narrowerIn.readLine()) != null) {
						narrowerResponse.append(narrowerInputLine);
					}
					narrowerIn.close();
					// parse json
					JSONObject broaderJsonObject = (JSONObject) new JSONParser().parse(narrowerResponse.toString());
					String narrowerLabelValue = (String) broaderJsonObject.get("prefLabel");
					hstmp.put(uriValueTmp, narrowerLabelValue);
					tmpAutosuggest.setNarrowerTerm(hstmp);
				}
			}
			// get retcat info
			String type = "fao";
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
			url = GeneralFunctions.encodeURIComponent(url);
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
			jsonOut.put("lang", "");
			// get retcat info
			String type = "fao";
			String quality = "en";
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
			jsonOut.put("description", "");
			jsonOut.put("uri", outputUrl);
			jsonOut.put("scheme", "agrovoc");
			// broader and narrower
			JSONArray broaderTerms = new JSONArray();
			JSONArray narrowerTerms = new JSONArray();
			// broader
			String urlB = "http://oek1.fao.org/skosmos/rest/v1/agrovoc/broader?lang=en&uri=" + url;
			URL objB = new URL(urlB);
			HttpURLConnection conB = (HttpURLConnection) objB.openConnection();
			conB.setRequestMethod("GET");
			BufferedReader inB = new BufferedReader(new InputStreamReader(conB.getInputStream(), "UTF8"));
			String inputLineB;
			StringBuilder responseB = new StringBuilder();
			while ((inputLineB = inB.readLine()) != null) {
				responseB.append(inputLineB);
			}
			inB.close();
			// parse json
			JSONObject jsonObjectB = (JSONObject) new JSONParser().parse(responseB.toString());
			JSONArray broaderArray = (JSONArray) jsonObjectB.get("broader");
			for (Object bo : broaderArray) {
				JSONObject tmp = (JSONObject) bo;
				JSONObject tmpObject = new JSONObject();
				String broaderURI = (String) tmp.get("uri");
				String broaderLabel = (String) tmp.get("prefLabel");
				tmpObject.put("label", broaderLabel);
				tmpObject.put("uri", broaderURI);
				broaderTerms.add(tmpObject);
			}
			// narrower
			String urlN = "http://oek1.fao.org/skosmos/rest/v1/agrovoc/narrower?lang=en&uri==" + url;
			URL objN = new URL(urlN);
			HttpURLConnection conN = (HttpURLConnection) objN.openConnection();
			conN.setRequestMethod("GET");
			BufferedReader inN = new BufferedReader(new InputStreamReader(conN.getInputStream(), "UTF8"));
			String inputLineN;
			StringBuilder responseN = new StringBuilder();
			while ((inputLineN = inN.readLine()) != null) {
				responseN.append(inputLineN);
			}
			inN.close();
			// parse json
			JSONObject jsonObjectN = (JSONObject) new JSONParser().parse(responseN.toString());
			JSONArray narrowerArray = (JSONArray) jsonObjectN.get("narrower");
			for (Object no : narrowerArray) {
				JSONObject tmp = (JSONObject) no;
				JSONObject tmpObject = new JSONObject();
				String narrowerURI = (String) tmp.get("uri");
				String narrowerLabel = (String) tmp.get("prefLabel");
				tmpObject.put("label", narrowerLabel);
				tmpObject.put("uri", narrowerURI);
				narrowerTerms.add(tmpObject);
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
