package v1.utils.retcat;

import exceptions.ResourceNotAvailableException;
import exceptions.SesameSparqlException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import v1.utils.generalfuncs.GeneralFunctions;

public class Retcat_HTML {

    public static Map<String, SuggestionItem> query(String url) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
        Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
        SuggestionItem tmpAutosuggest = new SuggestionItem(url);
        String urlEncoded = GeneralFunctions.encodeURIUmlaut(url);
        Document doc = Jsoup.connect(urlEncoded).get();
        Elements titleTag = doc.select("title");
        String titleStr = titleTag.text();
        tmpAutosuggest.setLabel(titleStr);
        tmpAutosuggest.setGroup("wayback");
        tmpAutosuggest.setQuality("low");
        tmpAutosuggest.setType("wayback");
        autosuggests.put(url, tmpAutosuggest);
        return autosuggests;
    }

    public static JSONObject info(String url) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, ParseException {
        String outputUrl = url;
        url = GeneralFunctions.encodeURIUmlaut(url);
        Document doc = Jsoup.connect(url).get();
        Elements titleTag = doc.select("title");
        JSONObject jsonOut = new JSONObject();
        String out = titleTag.text();
        jsonOut.put("label", out);
        jsonOut.put("lang", "");
        jsonOut.put("type", "wayback");
        jsonOut.put("quality", "low");
        jsonOut.put("group", "wayback");
        jsonOut.put("uri", outputUrl);
        JSONArray broader = new JSONArray();
        JSONArray narrower = new JSONArray();
        jsonOut.put("broaderTerms", broader);
        jsonOut.put("narrowerTerms", narrower);
        jsonOut.put("description", "");
        jsonOut.put("scheme", "");
        return jsonOut;
    }

}
