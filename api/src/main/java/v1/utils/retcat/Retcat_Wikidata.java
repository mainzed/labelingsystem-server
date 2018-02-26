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
import link.labeling.retcat.classes.RetcatItem;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Retcat_Wikidata {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException, link.labeling.retcat.exceptions.ResourceNotAvailableException {
		String url = "https://query.wikidata.org/bigdata/namespace/wdq/sparql";
		String sparql = "PREFIX schema: <http://schema.org/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT DISTINCT ?Subject ?item ?prefLabel ?desc { "
				+ "?Subject rdfs:label ?prefLabel . "
				+ "?Subject schema:description ?desc . "
				+ "FILTER(CONTAINS(LCASE(?prefLabel),'" + searchword + "')) "
				+ "FILTER(langMatches(lang(?prefLabel), \"EN\")) "
				+ "FILTER(langMatches(lang(?desc), \"EN\")) "
				+ "} LIMIT 20";
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
			tmpAutosuggest.setLanguage(labelLang);
			// get description
			JSONObject descriptionObject = (JSONObject) tmpElement.get("desc");
			if (descriptionObject != null) {
				String descriptionValue = (String) descriptionObject.get("value");
				tmpAutosuggest.setDescription(descriptionValue);
			}
			// get Scheme
			tmpAutosuggest.setSchemeTitle("wikidata");
			// get retcat info
			String type = "wikidata";
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
		return autosuggests;
	}

	public static JSONObject info(String url) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException, RetcatException {
		try {
			String sparqlendpoint = "https://query.wikidata.org/bigdata/namespace/wdq/sparql";
			String sparql = "PREFIX schema: <http://schema.org/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT DISTINCT ?prefLabel ?desc { "
					+ "<" + url + "> rdfs:label ?prefLabel . "
					+ "<" + url + "> schema:description ?desc . "
					+ "FILTER(langMatches(lang(?prefLabel), \"EN\")) "
					+ "FILTER(langMatches(lang(?desc), \"EN\")) "
					+ "} LIMIT 1";
			URL obj = new URL(sparqlendpoint);
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
			// output
			JSONObject jsonOut = new JSONObject();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				// get Label
				JSONObject labelObject = (JSONObject) tmpElement.get("prefLabel");
				String labelValue = (String) labelObject.get("value");
				String labelLang = (String) labelObject.get("xml:lang");
				jsonOut.put("label", labelValue);
				jsonOut.put("lang", labelLang);
				// get description
				JSONObject descriptionObject = (JSONObject) tmpElement.get("desc");
				if (descriptionObject != null) {
					String descriptionValue = (String) descriptionObject.get("value");
					jsonOut.put("description", descriptionValue);
				}
			}
			jsonOut.put("uri", url);
			// get retcat info
			String type = "wikidata";
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
			jsonOut.put("scheme", "wikidata");
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
