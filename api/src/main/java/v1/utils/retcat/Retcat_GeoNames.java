package v1.utils.retcat;

import exceptions.ResourceNotAvailableException;
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
import v1.rest.RetcatResource;
import v1.utils.config.ConfigProperties;
import v1.utils.generalfuncs.GeneralFunctions;

public class Retcat_GeoNames {

    public static Map<String, SuggestionItem> query(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
        searchword = GeneralFunctions.encodeURIComponent(searchword);
        String url_string = "http://api.geonames.org/searchJSON?q=" + searchword + "&maxRows=" + RetcatResource.getLimit() + "&username=" + ConfigProperties.getPropertyParam("geonames");
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
        JSONArray resultsArray = (JSONArray) jsonObject.get("geonames");
        Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
        for (Object element : resultsArray) {
            JSONObject tmpElement = (JSONObject) element;
            Long uriValue = (Long) tmpElement.get("geonameId");
            String uri = "http://sws.geonames.org/" + uriValue;
            autosuggests.put(uri, new SuggestionItem(uri));
            SuggestionItem tmpAutosuggest = autosuggests.get(uri);
            String labelValue = (String) tmpElement.get("name");
            tmpAutosuggest.setLabel(labelValue);
            String adminName1 = (String) tmpElement.get("adminName1");
            String countryName = (String) tmpElement.get("countryName");
            String lat = (String) tmpElement.get("lat");
            String lon = (String) tmpElement.get("lng");
            tmpAutosuggest.setDescription(adminName1 + ", " + countryName);
            tmpAutosuggest.setSchemeTitle("GeoNames");
            // get retcat info
            String type = "geonames";
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

    public static JSONObject info(String url) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
        String outputUrl = url;
        url = url.replace("http://sws.geonames.org/", "");
        url = "http://api.geonames.org/get?geonameId=" + url + "&username=" + ConfigProperties.getPropertyParam("geonames");
        // query for xml
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
        // parse xml
        int startTagName = response.indexOf("<name>");
        int endTagName = response.indexOf("</name>");
        String name = response.substring(startTagName, endTagName).replace("<name>", "");
        int startTagAN1 = response.indexOf("<adminName1>");
        int endTagAN1 = response.indexOf("</adminName1>");
        String an1 = response.substring(startTagAN1, endTagAN1).replace("<adminName1>", "");
        int startTagCN = response.indexOf("<countryName>");
        int endTagCN = response.indexOf("</countryName>");
        String cn = response.substring(startTagCN, endTagCN).replace("<countryName>", "");
        int startTagLAT = response.indexOf("<lat>");
        int endTagLAT = response.indexOf("</lat>");
        String lat = response.substring(startTagLAT, endTagLAT).replace("<lat>", "");
        int startTagLON = response.indexOf("<lng>");
        int endTagLON = response.indexOf("</lng>");
        String lon = response.substring(startTagLON, endTagLON).replace("<lng>", "");
        String desc = an1 + ", " + cn;
        // output
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("label", name);
        jsonOut.put("lang", "");
        // get retcat info
        String type = "geonames";
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
        jsonOut.put("uri", outputUrl);
        jsonOut.put("scheme", "GeoNames");
        JSONArray broader = new JSONArray();
        JSONArray narrower = new JSONArray();
        jsonOut.put("broaderTerms", broader);
        jsonOut.put("narrowerTerms", narrower);
        jsonOut.put("description", desc);
        return jsonOut;
    }

}
