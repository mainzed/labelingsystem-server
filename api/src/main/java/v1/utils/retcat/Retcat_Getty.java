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
import java.util.Iterator;
import java.util.Map;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Retcat_Getty {

	public static Map<String, SuggestionItem> queryAAT(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
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
				+ " } ORDER BY ASC(LCASE(STR(?Term)))";
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
			tmpAutosuggest.setLanguage(labelLang);
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
			// get retcat info
			String type = "getty";
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

	public static Map<String, SuggestionItem> queryTGN(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
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
				+ " } ORDER BY ASC(LCASE(STR(?Term)))";
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
			// get retcat info
			String type = "getty";
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

	public static Map<String, SuggestionItem> queryULAN(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
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
				+ " } ORDER BY ASC(LCASE(STR(?Term)))";
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
			// get retcat info
			String type = "getty";
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
			String sparqlendpoint = "http://vocab.getty.edu/sparql";
			String sparql = "SELECT * { "
					+ "<" + url + "> gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
					+ "<" + url + "> skos:inScheme ?scheme. "
					+ "?scheme rdfs:label ?schemeTitle. ";
			if (url.contains("aat") || url.contains("ulan")) {
				sparql += "OPTIONAL {<" + url + "> skos:scopeNote [dct:language gvp_lang:" + "en" + "; rdf:value ?scopeNote]} . ";
			} else if (url.contains("tgn")) {
				sparql += "OPTIONAL {<" + url + "> gvp:parentString ?scopeNote . } ";
			}
			sparql += "OPTIONAL {<" + url + "> gvp:broaderPreferred ?BroaderPreferred . ?BroaderPreferred gvp:prefLabelGVP [xl:literalForm ?BroaderPreferredTerm].} . ";
			sparql += "OPTIONAL {?NarrowerPreferred gvp:broaderPreferred <" + url + "> . ?NarrowerPreferred gvp:prefLabelGVP [xl:literalForm ?NarrowerPreferredTerm].} . ";
			sparql += " }";
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
				jsonOut.put("label", labelValue);
				jsonOut.put("lang", labelLang);
			}
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject scopeNote = (JSONObject) tmpElement.get("scopeNote");
				String descValue = "";
				if (scopeNote != null) {
					descValue = (String) scopeNote.get("value");
				}
				jsonOut.put("description", descValue);
			}
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject scopeNote = (JSONObject) tmpElement.get("schemeTitle");
				String descValue = (String) scopeNote.get("value");
				jsonOut.put("scheme", descValue);
			}
			HashMap<String, String> hmBroader = new HashMap();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject bpObj = (JSONObject) tmpElement.get("BroaderPreferred");
				JSONObject bptObj = (JSONObject) tmpElement.get("BroaderPreferredTerm");
				if (bpObj != null) {
					String bp = (String) bpObj.get("value");
					String bpt = (String) bptObj.get("value");
					hmBroader.put(bpt, bp);
				}
			}
			JSONArray tmpArrayBroader = new JSONArray();
			Iterator itB = hmBroader.entrySet().iterator();
			while (itB.hasNext()) {
				Map.Entry pair = (Map.Entry) itB.next();
				JSONObject tmpObject = new JSONObject();
				tmpObject.put("label", pair.getKey());
				tmpObject.put("uri", pair.getValue());
				tmpArrayBroader.add(tmpObject);
				itB.remove();
			}
			jsonOut.put("broaderTerms", tmpArrayBroader);
			HashMap<String, String> hmNarrower = new HashMap();
			for (Object element : bindingsArray) {
				JSONObject tmpElement = (JSONObject) element;
				JSONObject npObj = (JSONObject) tmpElement.get("NarrowerPreferred");
				JSONObject nptObj = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
				if (npObj != null) {
					String np = (String) npObj.get("value");
					String npt = (String) nptObj.get("value");
					hmNarrower.put(npt, np);
				}
			}
			JSONArray tmpArrayNarrower = new JSONArray();
			Iterator itN = hmNarrower.entrySet().iterator();
			while (itN.hasNext()) {
				Map.Entry pair = (Map.Entry) itN.next();
				JSONObject tmpObject = new JSONObject();
				tmpObject.put("label", pair.getKey());
				tmpObject.put("uri", pair.getValue());
				tmpArrayNarrower.add(tmpObject);
				itN.remove();
			}
			jsonOut.put("narrowerTerms", tmpArrayNarrower);
			// get retcat info
			String type = "getty";
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
			jsonOut.put("uri", url);
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
