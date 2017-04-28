package v1.utils.retcat;

import exceptions.ResourceNotAvailableException;
import exceptions.RetcatException;
import exceptions.SesameSparqlException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
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
import v1.utils.config.ConfigProperties;
import v1.utils.generalfuncs.GeneralFunctions;

public class Retcat_PersonDB {

	public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
		searchword = GeneralFunctions.encodeURIComponent(searchword);
		String PERSONDBHOST = "http://"+ ConfigProperties.getPropertyParam("host") +"/persondb";
        String url_string = PERSONDBHOST + "/search?query=" + searchword;
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
        JSONArray jsonArray = (JSONArray) new JSONParser().parse(response.toString());
        Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
        for (Object element : jsonArray) {
            JSONObject tmpElement = (JSONObject) element;
            String uriValue = (String) tmpElement.get("id");
            String uri = PERSONDBHOST + "/persons/" + uriValue;
            autosuggests.put(uri, new SuggestionItem(uri));
            SuggestionItem tmpAutosuggest = autosuggests.get(uri);
            String firstName = (String) tmpElement.get("firstName");
			String lastName = (String) tmpElement.get("lastName");
			String labelValue = firstName + " " + lastName;
            tmpAutosuggest.setLabel(labelValue);
            String affilliation = (String) tmpElement.get("affilliation");
            tmpAutosuggest.setDescription(affilliation);
            tmpAutosuggest.setSchemeTitle("Person Database");
            // get retcat info
            String type = "persondb";
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
        // query for json
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        String urlParameters = "";
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
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
        // output
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("label", jsonObject.get("firstName") + " " + jsonObject.get("lastName"));
        jsonOut.put("lang", "");
        // get retcat info
        String type = "persondb";
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
        jsonOut.put("scheme", "Person Database");
        JSONArray broader = new JSONArray();
        JSONArray narrower = new JSONArray();
        jsonOut.put("broaderTerms", broader);
        jsonOut.put("narrowerTerms", narrower);
        jsonOut.put("description", jsonObject.get("affilliation"));
        if (jsonOut.get("label") != null && !jsonOut.get("label").equals("")) {
            return jsonOut;
        } else {
            throw new RetcatException("no label for this uri available");
        }
    }

}
