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

public class Retcat_Finto {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException, link.labeling.retcat.exceptions.ResourceNotAvailableException {
		searchword = GeneralFunctions.encodeURIComponent(searchword);
		String url_string = "http://finto.fi/rest/v1/search?query=*" + searchword + "*&lang=en&type=skos:Concept&fields=narrower%20broader%20scopeNote&vocab=allars%20koko%20ponduskategorier%20ysa%20yso%20juho%20jupo%20keko%20okm-tieteenala%20liito%20mero%20puho%20tsr%20afo%20kassu%20mesh%20tero%20maotao%20musa%20muso%20valo%20kauno%20kito%20kto&limit=" + RetcatResource.getLimit();
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
			JSONArray desArray = (JSONArray) tmpElement.get("skos:scopeNote");
			if (desArray != null) {
				for (Object e : desArray) {
					String descStr = e.toString();
					tmpAutosuggest.setDescription(descStr);
				}
			}
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
			// get retcat info
			String type = "finto";
			String quality = "";
			String group = "";
			for (RetcatItem item : LocalRetcatItems.getLocalCatalogue()) {
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
			String vocab = url.split("/")[4];
			url = GeneralFunctions.encodeURIComponent(url);
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
			jsonOut.put("lang", "");
			// get retcat info
			String type = "finto";
			String quality = "";
			String group = "en";
			for (RetcatItem item : LocalRetcatItems.getLocalCatalogue()) {
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
			jsonOut.put("scheme", vocab);
			// broader and narrower
			JSONArray broaderTerms = new JSONArray();
			JSONArray narrowerTerms = new JSONArray();
			// broader
			String urlB = "http://api.finto.fi/rest/v1/" + vocab + "/broader?lang=en&uri=" + url;
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
			String urlN = "http://api.finto.fi/rest/v1/" + vocab + "/narrower?lang=en&uri=" + url;
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
