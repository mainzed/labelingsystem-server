package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.SuggestionItem;
import info.labeling.v1.utils.PropertiesLocal;
import info.labeling.v1.utils.RetcatItems;
import info.labeling.v1.utils.SQlite;
import info.labeling.v1.utils.SuggestionItem;
import info.labeling.v1.utils.Utils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Path("/retcat")
public class RetcatResource {

    private final String LIMIT = "20";

    /**
     * **************
     * RETCAT LISTS * **************
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRetcatList() {
        try {
            JSONObject jsonRETCAT = new JSONObject();
            JSONArray outArray = new JSONArray();
            // add items
            for (String[] itemArray : RetcatItems.getAllItems()) {
                JSONObject tmpRETCAT = new JSONObject();
                tmpRETCAT.put("name", itemArray[0]);
                tmpRETCAT.put("fulltextquery", itemArray[1]);
                tmpRETCAT.put("labelquery", itemArray[2]);
                tmpRETCAT.put("prefix", itemArray[3]);
                tmpRETCAT.put("short", itemArray[4]);
                tmpRETCAT.put("relations", itemArray[5]);
                outArray.add(tmpRETCAT);
            }
            // output
            jsonRETCAT.put("retcat", outArray);
            return Response.ok(jsonRETCAT).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/vocabulary/{vocabulary}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRetcatListByVocabulary(@PathParam("vocabulary") String vocabulary) {
        try {
            String newRetcatString = SQlite.getRetcatByVocabulary(vocabulary);
            String[] retcatItems = newRetcatString.split(",");
            // output json
            JSONObject jsonRETCAT = new JSONObject();
            JSONArray outArray = new JSONArray();
            // set data
            for (String item : retcatItems) {
                for (String[] itemArray : RetcatItems.getAllItems()) {
                    if (item.equals(itemArray[0])) {
                        JSONObject tmpRETCAT = new JSONObject();
                        tmpRETCAT.put("name", itemArray[0]);
                        tmpRETCAT.put("fulltextquery", itemArray[1]);
                        tmpRETCAT.put("labelquery", itemArray[2]);
                        tmpRETCAT.put("prefix", itemArray[3]);
                        tmpRETCAT.put("short", itemArray[4]);
                        tmpRETCAT.put("relations", itemArray[5]);
                        outArray.add(tmpRETCAT);
                    }
                }
            }
            jsonRETCAT.put("retcat", outArray);
            return Response.ok(jsonRETCAT).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @POST
    @Path("/vocabulary/{vocabulary}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response setRetcatForVocabulary(@PathParam("vocabulary") String vocabulary, String json) {
        try {
            // db string
            String retcatString = "";
            // parse json
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            JSONArray retcatArray = (JSONArray) jsonObject.get("retcat");
            for (Object itemObject : retcatArray) {
                JSONObject tmpObject = (JSONObject) itemObject;
                retcatString += tmpObject.get("name") + ",";
            }
            retcatString = retcatString.substring(0, retcatString.length() - 1);
            // sqlite db action
            SQlite.deleteRetcatEntry(vocabulary);
            SQlite.insertRetcatString(vocabulary, retcatString);
            String newRetcatString = SQlite.getRetcatByVocabulary(vocabulary);
            String[] retcatItems = newRetcatString.split(",");
            // output json
            JSONObject jsonRETCAT = new JSONObject();
            JSONArray outArray = new JSONArray();
            // set data
            for (String item : retcatItems) {
                for (String[] itemArray : RetcatItems.getAllItems()) {
                    if (item.equals(itemArray[0])) {
                        JSONObject tmpRETCAT = new JSONObject();
                        tmpRETCAT.put("name", itemArray[0]);
                        tmpRETCAT.put("fulltextquery", itemArray[1]);
                        tmpRETCAT.put("labelquery", itemArray[2]);
                        tmpRETCAT.put("prefix", itemArray[3]);
                        tmpRETCAT.put("short", itemArray[4]);
                        tmpRETCAT.put("relations", itemArray[5]);
                        outArray.add(tmpRETCAT);
                    }
                }
            }
            // output
            jsonRETCAT.put("retcat", outArray);
            return Response.ok(jsonRETCAT).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    /**
     * *************
     * RETCAT QUERY * *************
     */
    @GET
    @Path("/query/heritagedata/historicengland")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsHE(@QueryParam("query") String searchword) {
        try {
            String url = "http://heritagedata.org/live/sparql";
            String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                    + "?Subject skos:inScheme ?scheme . "
                    + "?Subject skos:prefLabel ?prefLabel . "
                    + "?scheme rdfs:label ?schemeTitle . "
                    + "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
                    + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm.} "
                    + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm .} "
                    + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                    + "FILTER(?scheme=<http://purl.org/heritagedata/schemes/mda_obj> || ?scheme=<http://purl.org/heritagedata/schemes/eh_period> || ?scheme=<http://purl.org/heritagedata/schemes/agl_et> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmt2> || ?scheme=<http://purl.org/heritagedata/schemes/560> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tbm> || ?scheme=<http://purl.org/heritagedata/schemes/eh_com> || ?scheme=<http://purl.org/heritagedata/schemes/eh_evd> || ?scheme=<http://purl.org/heritagedata/schemes/eh_tmc>) "
                    + "} LIMIT " + LIMIT;
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
            JSONObject jsonOut = new JSONObject();
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
                tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
                // get Scheme
                JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
                String schemeValue = (String) schemeObject.get("value");
                String schemeLang = (String) schemeObject.get("xml:lang");
                tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
                // get scopeNote
                JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
                if (scopeNoteObject != null) {
                    String scopeNoteValue = (String) scopeNoteObject.get("value");
                    String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                    tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
                }
                // get broader 
                String broaderVL = "";
                String broaderURI = "";
                JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
                if (broaderObject != null) {
                    String broaderValue = (String) broaderObject.get("value");
                    String broaderLang = (String) broaderObject.get("xml:lang");
                    broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
                }
                JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
                if (broaderURIObject != null) {
                    broaderURI = (String) broaderURIObject.get("value");
                }
                if (!broaderURI.equals("")) {
                    HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroader(hstmpBroader);
                }
                // get narrower 
                String narrowerVL = "";
                String narrowerURI = "";
                JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
                if (narrowerObject != null) {
                    String narrowerValue = (String) narrowerObject.get("value");
                    String narrowerLang = (String) narrowerObject.get("xml:lang");
                    narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
                }
                JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
                if (narrowerURIObject != null) {
                    narrowerURI = (String) narrowerURIObject.get("value");
                }
                if (!narrowerURI.equals("")) {
                    HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/heritagedata/rcahms")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsRCAHMS(@QueryParam("query") String searchword) {
        try {
            String url = "http://heritagedata.org/live/sparql";
            String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                    + "?Subject skos:inScheme ?scheme . "
                    + "?Subject skos:prefLabel ?prefLabel . "
                    + "?scheme rdfs:label ?schemeTitle . "
                    + "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
                    + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm.} "
                    + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm .} "
                    + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                    + "FILTER(?scheme=<http://purl.org/heritagedata/schemes/2> || ?scheme=<http://purl.org/heritagedata/schemes/3> || ?scheme=<http://purl.org/heritagedata/schemes/1>) "
                    + "} LIMIT " + LIMIT;
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
            JSONObject jsonOut = new JSONObject();
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
                tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
                // get Scheme
                JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
                String schemeValue = (String) schemeObject.get("value");
                String schemeLang = (String) schemeObject.get("xml:lang");
                tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
                // get scopeNote
                JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
                if (scopeNoteObject != null) {
                    String scopeNoteValue = (String) scopeNoteObject.get("value");
                    String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                    tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
                }
                // get broader 
                String broaderVL = "";
                String broaderURI = "";
                JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
                if (broaderObject != null) {
                    String broaderValue = (String) broaderObject.get("value");
                    String broaderLang = (String) broaderObject.get("xml:lang");
                    broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
                }
                JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
                if (broaderURIObject != null) {
                    broaderURI = (String) broaderURIObject.get("value");
                }
                if (!broaderURI.equals("")) {
                    HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroader(hstmpBroader);
                }
                // get narrower 
                String narrowerVL = "";
                String narrowerURI = "";
                JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
                if (narrowerObject != null) {
                    String narrowerValue = (String) narrowerObject.get("value");
                    String narrowerLang = (String) narrowerObject.get("xml:lang");
                    narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
                }
                JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
                if (narrowerURIObject != null) {
                    narrowerURI = (String) narrowerURIObject.get("value");
                }
                if (!narrowerURI.equals("")) {
                    HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/heritagedata/rcahmw")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsRCAHMW(@QueryParam("query") String searchword) {
        try {
            String url = "http://heritagedata.org/live/sparql";
            String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                    + "?Subject skos:inScheme ?scheme . "
                    + "?Subject skos:prefLabel ?prefLabel . "
                    + "?scheme rdfs:label ?schemeTitle . "
                    + "OPTIONAL { ?Subject skos:scopeNote ?scopeNote . } "
                    + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred skos:prefLabel ?BroaderPreferredTerm.} "
                    + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred skos:prefLabel ?NarrowerPreferredTerm .} "
                    + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                    + "FILTER(?scheme=<http://purl.org/heritagedata/schemes/11> || ?scheme=<http://purl.org/heritagedata/schemes/10> || ?scheme=<http://purl.org/heritagedata/schemes/12> || ?scheme=<http://purl.org/heritagedata/schemes/17> || ?scheme=<http://purl.org/heritagedata/schemes/19> || ?scheme=<http://purl.org/heritagedata/schemes/14> || ?scheme=<http://purl.org/heritagedata/schemes/15> || ?scheme=<http://purl.org/heritagedata/schemes/18> || ?scheme=<http://purl.org/heritagedata/schemes/20> || ?scheme=<http://purl.org/heritagedata/schemes/13> || ?scheme=<http://purl.org/heritagedata/schemes/21> || ?scheme=<http://purl.org/heritagedata/schemes/22>) "
                    + "} LIMIT " + LIMIT;
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
            JSONObject jsonOut = new JSONObject();
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
                tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
                // get Scheme
                JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
                String schemeValue = (String) schemeObject.get("value");
                String schemeLang = (String) schemeObject.get("xml:lang");
                tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
                // get scopeNote
                JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
                if (scopeNoteObject != null) {
                    String scopeNoteValue = (String) scopeNoteObject.get("value");
                    String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                    tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
                }
                // get broader 
                String broaderVL = "";
                String broaderURI = "";
                JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
                if (broaderObject != null) {
                    String broaderValue = (String) broaderObject.get("value");
                    String broaderLang = (String) broaderObject.get("xml:lang");
                    broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
                }
                JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
                if (broaderURIObject != null) {
                    broaderURI = (String) broaderURIObject.get("value");
                }
                if (!broaderURI.equals("")) {
                    HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroader(hstmpBroader);
                }
                // get narrower 
                String narrowerVL = "";
                String narrowerURI = "";
                JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
                if (narrowerObject != null) {
                    String narrowerValue = (String) narrowerObject.get("value");
                    String narrowerLang = (String) narrowerObject.get("xml:lang");
                    narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
                }
                JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
                if (narrowerURIObject != null) {
                    narrowerURI = (String) narrowerURIObject.get("value");
                }
                if (!narrowerURI.equals("")) {
                    HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/getty/aat")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsAAT(@QueryParam("query") String searchword) {
        try {
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
                    + " } ORDER BY ASC(LCASE(STR(?Term))) LIMIT " + LIMIT;
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
            JSONObject jsonOut = new JSONObject();
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
                tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
                // get Scheme
                JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
                String schemeValue = (String) schemeObject.get("value");
                tmpAutosuggest.setSchemeTitle(schemeValue);
                // get scopeNote
                JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
                if (scopeNoteObject != null) {
                    String scopeNoteValue = (String) scopeNoteObject.get("value");
                    String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                    tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
                }
                // get broader 
                String broaderVL = "";
                String broaderURI = "";
                JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
                if (broaderObject != null) {
                    String broaderValue = (String) broaderObject.get("value");
                    String broaderLang = (String) broaderObject.get("xml:lang");
                    broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
                }
                JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
                if (broaderURIObject != null) {
                    broaderURI = (String) broaderURIObject.get("value");
                }
                if (!broaderURI.equals("")) {
                    HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroader(hstmpBroader);
                }
                // get narrower 
                String narrowerVL = "";
                String narrowerURI = "";
                JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
                if (narrowerObject != null) {
                    String narrowerValue = (String) narrowerObject.get("value");
                    String narrowerLang = (String) narrowerObject.get("xml:lang");
                    narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
                }
                JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
                if (narrowerURIObject != null) {
                    narrowerURI = (String) narrowerURIObject.get("value");
                }
                if (!narrowerURI.equals("")) {
                    HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/getty/tgn")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsTGN(@QueryParam("query") String searchword) {
        try {
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
                    + " } ORDER BY ASC(LCASE(STR(?Term))) LIMIT " + LIMIT;
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
            JSONObject jsonOut = new JSONObject();
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
                    tmpAutosuggest.setDefinition(scopeNoteValue);
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
                    tmpAutosuggest.setBroader(hstmpBroader);
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
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/getty/ulan")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsULAN(@QueryParam("query") String searchword) {
        try {
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
                    + " } ORDER BY ASC(LCASE(STR(?Term))) LIMIT " + LIMIT;
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
            JSONObject jsonOut = new JSONObject();
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
                    tmpAutosuggest.setDefinition(scopeNoteValue);
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
                    tmpAutosuggest.setBroader(hstmpBroader);
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
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/dbpedia")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsDBPEDIA(@QueryParam("query") String searchword) {
        try {
            searchword = Utils.encodeURIComponent(searchword);
            String url_string = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=" + searchword + "&MaxHits=" + LIMIT;
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
            // init output
            JSONObject jsonOut = new JSONObject();
            JSONArray outArray = new JSONArray();
            // fill objects
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
            JSONArray resultsArray = (JSONArray) jsonObject.get("results");
            Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
            for (Object element : resultsArray) {
                JSONObject tmpElement = (JSONObject) element;
                String uriValue = (String) tmpElement.get("uri");
                autosuggests.put(uriValue, new SuggestionItem(uriValue));
                SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
                String labelValue = (String) tmpElement.get("label");
                tmpAutosuggest.setLabel(labelValue);
                String descriptionValue = (String) tmpElement.get("description");
                if (descriptionValue != null) {
                    tmpAutosuggest.setDefinition(descriptionValue);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("scheme", "DBpedia");
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                JSONArray broaderArrayNew = new JSONArray();
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                JSONArray narrrowerArrayNew = new JSONArray();
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/geonames")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsGEONAMES(@QueryParam("query") String searchword) {
        try {
            searchword = Utils.encodeURIComponent(searchword);
            String url_string = "http://api.geonames.org/searchJSON?q=" + searchword + "&maxRows=" + LIMIT + "&username=" + PropertiesLocal.getPropertyParam("geonames");
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
            // init output
            JSONObject jsonOut = new JSONObject();
            JSONArray outArray = new JSONArray();
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
                tmpAutosuggest.setDefinition(adminName1 + ", " + countryName + " [" + lat + " " + lon + "]");
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("scheme", "GeoNames");
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                JSONArray broaderArrayNew = new JSONArray();
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                JSONArray narrrowerArrayNew = new JSONArray();
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/pelagiospleiadesplaces")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsPELAGIOS(@QueryParam("query") String searchword) {
        try {
            searchword = Utils.encodeURIComponent(searchword);
            String url_string = "http://pelagios.org/peripleo/search?query=" + searchword + "&types=place&limit=" + LIMIT;
            URL url = new URL(url_string);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setRequestProperty("Accept", "application/json");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            // init output
            JSONObject jsonOut = new JSONObject();
            JSONArray outArray = new JSONArray();
            // fill objects
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
            JSONArray resultsArray = (JSONArray) jsonObject.get("items");
            Map<String, SuggestionItem> autosuggests = new HashMap<String, SuggestionItem>();
            for (Object element : resultsArray) {
                JSONObject tmpElement = (JSONObject) element;
                String uriValue = (String) tmpElement.get("identifier");
                if (uriValue.contains("pleiades.stoa.org")) {
                    autosuggests.put(uriValue, new SuggestionItem(uriValue));
                    SuggestionItem tmpAutosuggest = autosuggests.get(uriValue);
                    String labelValue = (String) tmpElement.get("title");
                    tmpAutosuggest.setLabel(labelValue);
                    String descriptionValue = (String) tmpElement.get("description");
                    if (descriptionValue != null) {
                        tmpAutosuggest.setDefinition(descriptionValue);
                    }
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("scheme", "Pelagios");
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                JSONArray broaderArrayNew = new JSONArray();
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                JSONArray narrrowerArrayNew = new JSONArray();
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/chronontology")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsCHRONONTOLOGY(@QueryParam("query") String searchword) {
        try {
            searchword = Utils.encodeURIComponent(searchword);
            String url_string = "http://chronontology.dainst.org/data/period?q=" + searchword + "&limit=" + LIMIT;
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
            JSONObject jsonOut = new JSONObject();
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
                    tmpAutosuggest.setDefinition(descriptionValue);
                }
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
                            tmpAutosuggest.setBroader(hstmpBroader);
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
                            tmpAutosuggest.setNarrower(hstmpNarrower);
                        }
                    }
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("scheme", "ChronOntology");
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/labelingsystem")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsLabelingSystemAll(@QueryParam("query") String searchword) {
        try {
            String url = "http://" + PropertiesLocal.getPropertyParam("host") + "/api/v1/sparql";
            String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> PREFIX dc: <http://purl.org/dc/elements/1.1/> "
                    + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                    + "?Subject skos:inScheme ?scheme . "
                    + "?scheme dc:title ?schemeTitle . "
                    + "?Subject skos:prefLabel ?prefLabel . "
                    + "OPTIONAL { ?Subject skos:note ?scopeNote . } "
                    + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred ls:preferredLabel ?BroaderPreferredTerm.} "
                    + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred ls:preferredLabel ?NarrowerPreferredTerm .} "
                    + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                    + "} LIMIT " + LIMIT;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
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
                tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
                // get Scheme
                JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
                String schemeValue = (String) schemeObject.get("value");
                String schemeLang = (String) schemeObject.get("xml:lang");
                tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
                // get scopeNote
                JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
                if (scopeNoteObject != null) {
                    String scopeNoteValue = (String) scopeNoteObject.get("value");
                    String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                    tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
                }
                // get broader 
                String broaderVL = "";
                String broaderURI = "";
                JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
                if (broaderObject != null) {
                    String broaderValue = (String) broaderObject.get("value");
                    String broaderLang = (String) broaderObject.get("xml:lang");
                    broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
                }
                JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
                if (broaderURIObject != null) {
                    broaderURI = (String) broaderURIObject.get("value");
                }
                if (!broaderURI.equals("")) {
                    HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroader(hstmpBroader);
                }
                // get narrower 
                String narrowerVL = "";
                String narrowerURI = "";
                JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
                if (narrowerObject != null) {
                    String narrowerValue = (String) narrowerObject.get("value");
                    String narrowerLang = (String) narrowerObject.get("xml:lang");
                    narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
                }
                JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
                if (narrowerURIObject != null) {
                    narrowerURI = (String) narrowerURIObject.get("value");
                }
                if (!narrowerURI.equals("")) {
                    HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
				// scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/query/labelingsystem/{vocabulary}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getResultsLabelingSystemVocabulary(@QueryParam("query") String searchword, @PathParam("vocabulary") String vocabulary) {
        try {
            String url = "http://" + PropertiesLocal.getPropertyParam("host") + "/api/v1/sparql";
            String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> PREFIX dc: <http://purl.org/dc/elements/1.1/> "
                    + "SELECT ?Subject ?prefLabel ?scopeNote ?BroaderPreferredTerm ?BroaderPreferred ?NarrowerPreferredTerm ?NarrowerPreferred ?schemeTitle WHERE { "
                    + "?Subject skos:inScheme ?scheme . "
                    + "?scheme dc:title ?schemeTitle . "
                    + "?Subject skos:prefLabel ?prefLabel . "
                    + "OPTIONAL { ?Subject skos:note ?scopeNote . } "
                    + "OPTIONAL {?Subject skos:broader ?BroaderPreferred . ?BroaderPreferred ls:preferredLabel ?BroaderPreferredTerm.} "
                    + "OPTIONAL {?Subject skos:narrower ?NarrowerPreferred . ?NarrowerPreferred ls:preferredLabel ?NarrowerPreferredTerm .} "
                    + "FILTER(regex(?prefLabel, '" + searchword + "', 'i') || regex(?scopeNote, '" + searchword + "', 'i')) "
                    + "FILTER(?scheme=<http://" + PropertiesLocal.getPropertyParam("host") + "/item/vocabulary/" + vocabulary + ">) "
                    + "} LIMIT " + LIMIT;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
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
                tmpAutosuggest.setLabel(labelValue + "@" + labelLang);
                // get Scheme
                JSONObject schemeObject = (JSONObject) tmpElement.get("schemeTitle");
                String schemeValue = (String) schemeObject.get("value");
                String schemeLang = (String) schemeObject.get("xml:lang");
                tmpAutosuggest.setSchemeTitle(schemeValue + "@" + schemeLang);
                // get scopeNote
                JSONObject scopeNoteObject = (JSONObject) tmpElement.get("scopeNote");
                if (scopeNoteObject != null) {
                    String scopeNoteValue = (String) scopeNoteObject.get("value");
                    String scopeNoteLang = (String) scopeNoteObject.get("xml:lang");
                    tmpAutosuggest.setDefinition(scopeNoteValue + "@" + scopeNoteLang);
                }
                // get broader 
                String broaderVL = "";
                String broaderURI = "";
                JSONObject broaderObject = (JSONObject) tmpElement.get("BroaderPreferredTerm");
                if (broaderObject != null) {
                    String broaderValue = (String) broaderObject.get("value");
                    String broaderLang = (String) broaderObject.get("xml:lang");
                    broaderVL = broaderValue.replace("<", "").replace(">", "") + "@" + broaderLang.replace("<", "").replace(">", "");
                }
                JSONObject broaderURIObject = (JSONObject) tmpElement.get("BroaderPreferred");
                if (broaderURIObject != null) {
                    broaderURI = (String) broaderURIObject.get("value");
                }
                if (!broaderURI.equals("")) {
                    HashMap<String, String> hstmpBroader = new HashMap<String, String>();
                    hstmpBroader.put(broaderURI, broaderVL);
                    tmpAutosuggest.setBroader(hstmpBroader);
                }
                // get narrower 
                String narrowerVL = "";
                String narrowerURI = "";
                JSONObject narrowerObject = (JSONObject) tmpElement.get("NarrowerPreferredTerm");
                if (narrowerObject != null) {
                    String narrowerValue = (String) narrowerObject.get("value");
                    String narrowerLang = (String) narrowerObject.get("xml:lang");
                    narrowerVL = narrowerValue.replace("<", "").replace(">", "") + "@" + narrowerLang.replace("<", "").replace(">", "");
                }
                JSONObject narrowerURIObject = (JSONObject) tmpElement.get("NarrowerPreferred");
                if (narrowerURIObject != null) {
                    narrowerURI = (String) narrowerURIObject.get("value");
                }
                if (!narrowerURI.equals("")) {
                    HashMap<String, String> hstmpNarrower = new HashMap<String, String>();
                    hstmpNarrower.put(narrowerURI, narrowerVL);
                    tmpAutosuggest.setNarrower(hstmpNarrower);
                }
            }
            // fill output json
            for (Map.Entry<String, SuggestionItem> entry : autosuggests.entrySet()) {
                SuggestionItem tmpAS = entry.getValue();
                JSONObject suggestionObject = new JSONObject();
                JSONObject suggestionObjectCollection = new JSONObject();
				// scheme
                JSONArray schemeArrayNew = new JSONArray();
                schemeArrayNew.add(tmpAS.getSchemeTitle());
                suggestionObjectCollection.put("scheme", schemeArrayNew);
                // label
                JSONArray labelArrayNew = new JSONArray();
                labelArrayNew.add(tmpAS.getLabel());
                suggestionObjectCollection.put("label", labelArrayNew);
                // definition
                JSONArray scopeNoteArrayNew = new JSONArray();
                if (!tmpAS.getDefinition().equals("")) {
                    scopeNoteArrayNew.add(tmpAS.getDefinition());
                }
                suggestionObjectCollection.put("definition", scopeNoteArrayNew);
                // broader
                Set broaderTerms = tmpAS.getBroader();
                JSONArray broaderArrayNew = new JSONArray();
                if (broaderTerms.size() > 0) {
                    for (Object element : broaderTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject broaderObjectTmp = new JSONObject();
                            broaderObjectTmp.put("uri", key);
                            broaderObjectTmp.put("label", value);
                            broaderArrayNew.add(broaderObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("broader", broaderArrayNew);
                // narrrower
                Set narrrowerTerms = tmpAS.getNarrower();
                JSONArray narrrowerArrayNew = new JSONArray();
                if (narrrowerTerms.size() > 0) {
                    for (Object element : narrrowerTerms) {
                        Map hm = (Map) element;
                        Iterator entries = hm.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            String key = (String) thisEntry.getKey();
                            String value = (String) thisEntry.getValue();
                            JSONObject narrrowerObjectTmp = new JSONObject();
                            narrrowerObjectTmp.put("uri", key);
                            narrrowerObjectTmp.put("label", value);
                            narrrowerArrayNew.add(narrrowerObjectTmp);
                        }
                    }
                }
                suggestionObjectCollection.put("narrrower", narrrowerArrayNew);
                // add information to output
                suggestionObject.put(tmpAS.getId(), suggestionObjectCollection);
                outArray.add(suggestionObject);
            }
            jsonOut.put("suggestions", outArray);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    /**
     * *************
     * RETCAT LABEL * *************
     */
    @GET
    @Path("/label/getty")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response geLabelGetty(@QueryParam("url") String url) {
        try {
            String sparqlendpoint = "http://vocab.getty.edu/sparql";
            String sparql = "SELECT ?prefLabel { "
                    + "<" + url + "> gvp:prefLabelGVP [xl:literalForm ?prefLabel]. "
                    + " }";
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
                jsonOut.put("label", labelValue + "@" + labelLang);
            }
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/label/heritagedata")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response geLabelHeritageData(@QueryParam("url") String url) {
        try {
            String sparqlendpoint = "http://heritagedata.org/live/sparql";
            String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
                    + "SELECT ?prefLabel WHERE { "
                    + "<" + url + "> skos:prefLabel ?prefLabel. "
                    + " }";
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
                jsonOut.put("label", labelValue + "@" + labelLang);
            }
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/label/labelingsystem")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response geLabelLabelingSystem(@QueryParam("url") String url) {
        try {
            String sparqlendpoint = "http://localhost:8084/api/v1/sparql";
            String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#>"
                    + "SELECT ?prefLabel { "
                    + "<" + url + "> ls:preferredLabel ?prefLabel. "
                    + " }";
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
                jsonOut.put("label", labelValue + "@" + labelLang);
            }
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/label/html")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response geLabelExtern(@QueryParam("url") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements titleTag = doc.select("title");
            JSONObject jsonOut = new JSONObject();
            String out = titleTag.text();
            if (url.startsWith("http://dbpedia.org/resource/")) {
                out = out.replace("About: ", "");
            }
            jsonOut.put("label", out);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/label/geonames")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response geLabelGeoNames(@QueryParam("url") String url) {
        try {
            url = url.replace("http://sws.geonames.org/", "");
            url = "http://api.geonames.org/get?geonameId=" + url + "&username=" + PropertiesLocal.getPropertyParam("geonames");
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
            // output
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("label", name);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/label/pelagiospleiadesplaces")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response geLabelPelagios(@QueryParam("url") String url) {
        try {
            url = Utils.encodeURIComponent(url);
            url = "http://pelagios.org/peripleo/places/" + url;
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
            String title = (String) jsonObject.get("title");
            // output
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("label", title);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/label/chronontology")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response geLabelChronontology(@QueryParam("url") String url) {
        try {
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
            // output
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("label", labelValue);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

}
