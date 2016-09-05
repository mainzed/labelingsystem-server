package v1.utils.retcat;

import exceptions.ResourceNotAvailableException;
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
            String uriValue = (String) resourceObject.get("@id");
            uriValue = "http://chronontology.dainst.org" + uriValue;
            autosuggests.put(uriValue, new SuggestionItem(uriValue));
            SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
            String labelValue = resourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
            tmpAutosuggest.setLabel(labelValue);
            String descriptionValue = (String) resourceObject.get("description");
            if (descriptionValue != null) {
                tmpAutosuggest.setDescription(descriptionValue);
            }
            tmpAutosuggest.setSchemeTitle("ChronOntology");
            JSONArray isPartOfValue = (JSONArray) resourceObject.get("isPartOf");
            JSONArray hasPartValue = (JSONArray) resourceObject.get("hasPart");
            // query for broader
            if (isPartOfValue != null) {
                for (Object broaderItem : isPartOfValue) {
                    String itemString = broaderItem.toString();
                    String broaderurl = itemString.replace("/period", "/data/period");
                    // query for json
                    URL obj = new URL("http://chronontology.dainst.org" + broaderurl);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
                    StringBuilder broaderResponse = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        broaderResponse.append(inputLine);
                    }
                    in.close();
                    // parse json
                    JSONObject broaderjsonObject = (JSONObject) new JSONParser().parse(broaderResponse.toString());
                    JSONObject broaderResourceObject = (JSONObject) broaderjsonObject.get("resource");
                    String broaderUriValue = (String) broaderResourceObject.get("@id");
                    broaderUriValue = "http://chronontology.dainst.org" + broaderUriValue;
                    String broaderLabelValue = broaderResourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
                    if (!broaderUriValue.equals("")) {
                        HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                        hstmpBroader.put(broaderUriValue, broaderLabelValue);
                        tmpAutosuggest.setBroaderTerm(hstmpBroader);
                    }
                }
            }
            // query for narrower
            if (hasPartValue != null) {
                for (Object narrowerItem : hasPartValue) {
                    String itemString = narrowerItem.toString();
                    String narrowerurl = itemString.replace("/period", "/data/period");
                    // query for json
                    URL obj = new URL("http://chronontology.dainst.org" + narrowerurl);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
                    StringBuilder narrowerResponse = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        narrowerResponse.append(inputLine);
                    }
                    in.close();
                    // parse json
                    JSONObject narrowerjsonObject = (JSONObject) new JSONParser().parse(narrowerResponse.toString());
                    JSONObject narrowerResourceObject = (JSONObject) narrowerjsonObject.get("resource");
                    String narrowerUriValue = (String) narrowerResourceObject.get("@id");
                    narrowerUriValue = "http://chronontology.dainst.org" + narrowerUriValue;
                    String narrowerLabelValue = narrowerResourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
                    if (!narrowerUriValue.equals("")) {
                        HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                        hstmpNarrower.put(narrowerUriValue, narrowerLabelValue);
                        tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
                    }
                }
            }
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

    public static JSONObject info(String url) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
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
        String labelValue = resourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
        String descValue = "";
        if (resourceObject.get("description") != null) {
            descValue = resourceObject.get("description").toString();
        }
        // output
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("label", labelValue);
        jsonOut.put("lang", "");
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
        // broader und narrower
        JSONArray broader = new JSONArray();
        JSONArray narrower = new JSONArray();
        JSONArray isPartOfValue = (JSONArray) resourceObject.get("isPartOf");
        JSONArray hasPartValue = (JSONArray) resourceObject.get("hasPart");
        // query for broader
        if (isPartOfValue != null) {
            for (Object broaderItem : isPartOfValue) {
                String itemString = broaderItem.toString();
                String broaderurl = itemString.replace("/period", "/data/period");
                // query for json
                URL objB = new URL("http://chronontology.dainst.org" + broaderurl);
                HttpURLConnection conB = (HttpURLConnection) objB.openConnection();
                conB.setRequestMethod("GET");
                BufferedReader inB = new BufferedReader(new InputStreamReader(conB.getInputStream(), "UTF8"));
                StringBuilder broaderResponse = new StringBuilder();
                while ((inputLine = inB.readLine()) != null) {
                    broaderResponse.append(inputLine);
                }
                inB.close();
                // parse json
                JSONObject broaderjsonObject = (JSONObject) new JSONParser().parse(broaderResponse.toString());
                JSONObject broaderResourceObject = (JSONObject) broaderjsonObject.get("resource");
                String broaderUriValue = (String) broaderResourceObject.get("@id");
                broaderUriValue = "http://chronontology.dainst.org" + broaderUriValue;
                String broaderLabelValue = broaderResourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
                if (!broaderUriValue.equals("")) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("label", broaderLabelValue);
                    tmpObject.put("uri", broaderLabelValue);
                    broader.add(tmpObject);
                }
            }
        }
        // query for narrower
        if (hasPartValue != null) {
            for (Object narrowerItem : hasPartValue) {
                String itemString = narrowerItem.toString();
                String narrowerurl = itemString.replace("/period", "/data/period");
                // query for json
                URL objN = new URL("http://chronontology.dainst.org" + narrowerurl);
                HttpURLConnection conN = (HttpURLConnection) objN.openConnection();
                conN.setRequestMethod("GET");
                BufferedReader inN = new BufferedReader(new InputStreamReader(conN.getInputStream(), "UTF8"));
                StringBuilder narrowerResponse = new StringBuilder();
                while ((inputLine = inN.readLine()) != null) {
                    narrowerResponse.append(inputLine);
                }
                inN.close();
                // parse json
                JSONObject narrowerjsonObject = (JSONObject) new JSONParser().parse(narrowerResponse.toString());
                JSONObject narrowerResourceObject = (JSONObject) narrowerjsonObject.get("resource");
                String narrowerUriValue = (String) narrowerResourceObject.get("@id");
                narrowerUriValue = "http://chronontology.dainst.org" + narrowerUriValue;
                String narrowerLabelValue = narrowerResourceObject.get("prefLabel").toString().replace("}", "").split(":")[1].replace("\"", "");
                if (!narrowerUriValue.equals("")) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("label", narrowerLabelValue);
                    tmpObject.put("uri", narrowerLabelValue);
                    narrower.add(tmpObject);
                }
            }
        }
        jsonOut.put("broaderTerms", broader);
        jsonOut.put("narrowerTerms", narrower);
        return jsonOut;
    }

}
