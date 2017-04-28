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

public class Retcat_HeritageData {

    public static Map<String, SuggestionItem> queryHE(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
        String url = "http://heritagedata.org/live/sparql";
        String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                + "?Subject skos:inScheme ?scheme . "
                + "?Subject skos:prefLabel ?prefLabel . "
                + "?scheme rdfs:label ?schemeTitle . "
                + "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
                + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
                + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
                + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                + "FILTER(?scheme=<http://purl.org/heritagedata/schemes/mda_obj> || ?scheme=<http://purl.org/heritagedata/schemes/eh_period> || ?scheme=<http://purl.org/heritagedata/schemes/agl_et> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmt2> || ?scheme=<http://purl.org/heritagedata/schemes/560> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tbm> || ?scheme=<http://purl.org/heritagedata/schemes/eh_com> || ?scheme=<http://purl.org/heritagedata/schemes/eh_evd> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmc>) "
                + "}";
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
            if (labelLang.equals("en")) {
                tmpAutosuggest.setLabel(labelValue);
                tmpAutosuggest.setLanguage(labelLang);
            }
            // get Scheme
            JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
            String schemeValue = (String) schemeObject.get("value");
            String schemeLang = (String) schemeObject.get("xml:lang");
            tmpAutosuggest.setSchemeTitle(schemeValue);
            // get scopeNote
            JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
            if (scopeNoteObject != null) {
                String scopeNoteValue = (String) scopeNoteObject.get("value");
                String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                if (scopeNoteLang.equals("en")) {
                    tmpAutosuggest.setDescription(scopeNoteValue);
                }
            }
            // get broader 
            String broaderVL = "";
            String broaderURI = "";
            String broaderLang = "";
            JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
            if (broaderObject != null) {
                String broaderValue = (String) broaderObject.get("value");
                broaderLang = (String) broaderObject.get("xml:lang");
                broaderVL = broaderValue.replace("<", "").replace(">", "");
            }
            JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
            if (broaderURIObject != null) {
                broaderURI = (String) broaderURIObject.get("value");
            }
            if (!broaderURI.equals("")) {
                HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                if (broaderLang.equals("en")) {
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroaderTerm(hstmpBroader);
                }
            }
            // get narrower 
            String narrowerVL = "";
            String narrowerURI = "";
            String narrowerLang = "";
            JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
            if (narrowerObject != null) {
                String narrowerValue = (String) narrowerObject.get("value");
                narrowerLang = (String) narrowerObject.get("xml:lang");
                narrowerVL = narrowerValue.replace("<", "").replace(">", "");
            }
            JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
            if (narrowerURIObject != null) {
                narrowerURI = (String) narrowerURIObject.get("value");
            }
            if (!narrowerURI.equals("")) {
                HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                if (narrowerLang.equals("en")) {
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
                }
            }
            // get retcat info
            String type = "heritagedata";
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

    public static Map<String, SuggestionItem> queryRCAHMS(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
        String url = "http://heritagedata.org/live/sparql";
        String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                + "?Subject skos:inScheme ?scheme . "
                + "?Subject skos:prefLabel ?prefLabel . "
                + "?scheme rdfs:label ?schemeTitle . "
                + "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
                + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
                + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
                + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                + "FILTER(?scheme=<http://purl.org/heritagedata/schemes/2> || ?scheme=<http://purl.org/heritagedata/schemes/3> || ?scheme=<http://purl.org/heritagedata/schemes/1>) "
                + "}";
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
            String labelLang = (String) labelObject.get("xml:lang");
            if (labelLang.equals("en")) {
                tmpAutosuggest.setLabel(labelValue);
                tmpAutosuggest.setLanguage(labelLang);
            }
            // get Scheme
            JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
            String schemeValue = (String) schemeObject.get("value");
            String schemeLang = (String) schemeObject.get("xml:lang");
            tmpAutosuggest.setSchemeTitle(schemeValue);
            // get scopeNote
            JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
            if (scopeNoteObject != null) {
                String scopeNoteValue = (String) scopeNoteObject.get("value");
                String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                if (scopeNoteLang.equals("en")) {
                    tmpAutosuggest.setDescription(scopeNoteValue);
                }
            }
            // get broader 
            String broaderVL = "";
            String broaderURI = "";
            String broaderLang = "";
            JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
            if (broaderObject != null) {
                String broaderValue = (String) broaderObject.get("value");
                broaderLang = (String) broaderObject.get("xml:lang");
                broaderVL = broaderValue.replace("<", "").replace(">", "");
            }
            JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
            if (broaderURIObject != null) {
                broaderURI = (String) broaderURIObject.get("value");
            }
            if (!broaderURI.equals("")) {
                HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                if (broaderLang.equals("en")) {
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroaderTerm(hstmpBroader);
                }
            }
            // get narrower 
            String narrowerVL = "";
            String narrowerURI = "";
            String narrowerLang = "";
            JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
            if (narrowerObject != null) {
                String narrowerValue = (String) narrowerObject.get("value");
                narrowerLang = (String) narrowerObject.get("xml:lang");
                narrowerVL = narrowerValue.replace("<", "").replace(">", "");
            }
            JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
            if (narrowerURIObject != null) {
                narrowerURI = (String) narrowerURIObject.get("value");
            }
            if (!narrowerURI.equals("")) {
                HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                if (narrowerLang.equals("en")) {
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
                }
            }
            // get retcat info
            String type = "heritagedata";
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

    public static Map<String, SuggestionItem> queryRCAHMW(String searchword) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
        String url = "http://heritagedata.org/live/sparql";
        String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                + "?Subject skos:inScheme ?scheme . "
                + "?Subject skos:prefLabel ?prefLabel . "
                + "?scheme rdfs:label ?schemeTitle . "
                + "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
                + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
                + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
                + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                + "FILTER(?scheme=<http://purl.org/heritagedata/schemes/11> || ?scheme=<http://purl.org/heritagedata/schemes/10> || ?scheme=<http://purl.org/heritagedata/schemes/12> || ?scheme=<http://purl.org/heritagedata/schemes/17> || ?scheme=<http://purl.org/heritagedata/schemes/19> || ?scheme=<http://purl.org/heritagedata/schemes/14> || ?scheme=<http://purl.org/heritagedata/schemes/15> || ?scheme=<http://purl.org/heritagedata/schemes/18> || ?scheme=<http://purl.org/heritagedata/schemes/20> || ?scheme=<http://purl.org/heritagedata/schemes/13> || ?scheme=<http://purl.org/heritagedata/schemes/21> || ?scheme=<http://purl.org/heritagedata/schemes/22>) "
                + "}";
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
            String labelLang = (String) labelObject.get("xml:lang");
            if (labelLang.equals("en")) {
                tmpAutosuggest.setLabel(labelValue);
                tmpAutosuggest.setLanguage(labelLang);
            }
            // get Scheme
            JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
            String schemeValue = (String) schemeObject.get("value");
            String schemeLang = (String) schemeObject.get("xml:lang");
            tmpAutosuggest.setSchemeTitle(schemeValue);
            // get scopeNote
            JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
            if (scopeNoteObject != null) {
                String scopeNoteValue = (String) scopeNoteObject.get("value");
                String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                if (scopeNoteLang.equals("en")) {
                    tmpAutosuggest.setDescription(scopeNoteValue);
                }
            }
            // get broader 
            String broaderVL = "";
            String broaderURI = "";
            String broaderLang = "";
            JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
            if (broaderObject != null) {
                String broaderValue = (String) broaderObject.get("value");
                broaderLang = (String) broaderObject.get("xml:lang");
                broaderVL = broaderValue.replace("<", "").replace(">", "");
            }
            JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
            if (broaderURIObject != null) {
                broaderURI = (String) broaderURIObject.get("value");
            }
            if (!broaderURI.equals("")) {
                HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                if (broaderLang.equals("en")) {
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroaderTerm(hstmpBroader);
                }
            }
            // get narrower 
            String narrowerVL = "";
            String narrowerURI = "";
            String narrowerLang = "";
            JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
            if (narrowerObject != null) {
                String narrowerValue = (String) narrowerObject.get("value");
                narrowerLang = (String) narrowerObject.get("xml:lang");
                narrowerVL = narrowerValue.replace("<", "").replace(">", "");
            }
            JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
            if (narrowerURIObject != null) {
                narrowerURI = (String) narrowerURIObject.get("value");
            }
            if (!narrowerURI.equals("")) {
                HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                if (narrowerLang.equals("en")) {
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrowerTerm(hstmpNarrower);
                }
            }
            // get retcat info
            String type = "heritagedata";
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
        String sparqlendpoint = "http://heritagedata.org/live/sparql";
        String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT * WHERE { "
                + "<" + url + "> skos:prefLabel ?prefLabel. "
                + "<" + url + "> skos:inScheme ?scheme . "
                + "?scheme rdfs:label ?schemeTitle . "
                + "OPTIONAL { <" + url + "> skos:scopeNote ?scopeNote . } "
                + "OPTIONAL {<" + url + "> skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm. } "
                + "OPTIONAL {<" + url + "> skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm . } "
                + "}";
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
            if (labelLang.equals("en")) {
                jsonOut.put("label", labelValue);
                jsonOut.put("lang", labelLang);
            }
        }
        for (Object element : bindingsArray) {
            JSONObject tmpElement = (JSONObject) element;
            JSONObject scopeNote = (JSONObject) tmpElement.get("scopeNote");
            String descValue = "";
            String descLang = "";
            if (scopeNote != null) {
                descValue = (String) scopeNote.get("value");
                descLang = (String) scopeNote.get("xml:lang");
            }
            if (descLang.equals("en")) {
                jsonOut.put("description", descValue);
            }
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
                String bptLang = (String) bptObj.get("xml:lang");
                if (bptLang.equals("en")) {
                    hmBroader.put(bpt, bp);
                }
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
                String nptLang = (String) nptObj.get("xml:lang");
                if (nptLang.equals("en")) {
                    hmNarrower.put(npt, np);
                }
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
        String type = "heritagedata";
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
    }

}
