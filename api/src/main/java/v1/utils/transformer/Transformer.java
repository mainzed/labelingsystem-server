package v1.utils.transformer;

import v1.utils.config.ConfigProperties;
import rdf.RDF;
import exceptions.ResourceNotAvailableException;
import exceptions.RevisionTypeException;
import exceptions.SesameSparqlException;
import exceptions.TransformRdfToApiJsonException;
import exceptions.UniqueIdentifierException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.eclipse.rdf4j.query.BindingSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import rdf.RDF4J_20;
import v1.utils.retcat.RetcatItem;

public class Transformer {

    public static String vocabulary_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject rdfObject = new JSONObject();
        JSONObject vocabularyObject = (JSONObject) new JSONParser().parse(json);
        // change language
        String language = (String) vocabularyObject.get("language");
        if (language != null) {
            vocabularyObject.remove("language");
            JSONArray languageArrayNew = new JSONArray();
            JSONObject languageObject = new JSONObject();
            languageObject.put("type", "literal");
            languageObject.put("value", language);
            languageArrayNew.add(languageObject);
            vocabularyObject.put(rdf.getPrefixItem("dc:language"), languageArrayNew);
        }
        // change title
        String title = (String) vocabularyObject.get("title");
        if (title != null) {
            vocabularyObject.remove("title");
            JSONArray titleArrayNew = new JSONArray();
            JSONObject titleObject = new JSONObject();
            titleObject.put("type", "literal");
            titleObject.put("value", title);
            titleObject.put("lang", language);
            titleArrayNew.add(titleObject);
            vocabularyObject.put(rdf.getPrefixItem("dc:title"), titleArrayNew);
        }
        // change description
        String description = (String) vocabularyObject.get("description");
        if (description != null) {
            vocabularyObject.remove("description");
            JSONArray descriptionArrayNew = new JSONArray();
            JSONObject descriptionObject = new JSONObject();
            descriptionObject.put("type", "literal");
            descriptionObject.put("value", description);
            descriptionObject.put("lang", language);
            descriptionArrayNew.add(descriptionObject);
            vocabularyObject.put(rdf.getPrefixItem("dc:description"), descriptionArrayNew);
        }
        // change license
        String license = (String) vocabularyObject.get("license");
        if (license != null) {
            vocabularyObject.remove("license");
            JSONArray licenseArrayNew = new JSONArray();
            JSONObject licenseObject = new JSONObject();
            licenseObject.put("type", "uri");
            licenseObject.put("value", license);
            licenseArrayNew.add(licenseObject);
            vocabularyObject.put(rdf.getPrefixItem("dct:license"), licenseArrayNew);
        } else { // set public domain as default
            JSONArray licenseArrayNew = new JSONArray();
            JSONObject licenseObject = new JSONObject();
            licenseObject.put("type", "uri");
            licenseObject.put("value", "http://creativecommons.org/publicdomain/zero/1.0/legalcode");
            licenseArrayNew.add(licenseObject);
            vocabularyObject.put(rdf.getPrefixItem("dct:license"), licenseArrayNew);
        }
		// change rights
        String rights = (String) vocabularyObject.get("rights");
        if (rights != null) {
            vocabularyObject.remove("rights");
            JSONObject tmpObject = new JSONObject();
            tmpObject.put("type", "literal");
            tmpObject.put("value", rights);
            JSONArray arrayNew = new JSONArray();
            arrayNew.add(tmpObject);
            vocabularyObject.put(rdf.getPrefixItem("dc:rights"), arrayNew);
        }
        // change releasetype
        String releaseString = (String) vocabularyObject.get("releaseType");
        if (releaseString != null && !releaseString.isEmpty()) {
            vocabularyObject.remove("releaseType");
            JSONArray releaseArrayNew = new JSONArray();
            JSONObject releaseObject = new JSONObject();
            releaseObject.put("type", "uri");
            if (releaseString.equals("draft")) {
                releaseObject.put("value", rdf.getPrefixItem("ls:Draft"));
            } else {
                releaseObject.put("value", rdf.getPrefixItem("ls:Public"));
            }
            releaseArrayNew.add(releaseObject);
            vocabularyObject.put(rdf.getPrefixItem("ls:hasReleaseType"), releaseArrayNew);
        }
        Calendar calender = Calendar.getInstance();
        Date date = calender.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateiso = formatter.format(date);
        if (releaseString != null && !releaseString.isEmpty()) {
            if (releaseString.equals("public")) {
                JSONArray releaseArrayNew = new JSONArray();
                JSONObject releaseObject = new JSONObject();
                releaseObject.put("type", "literal");
                releaseObject.put("value", dateiso);
                releaseArrayNew.add(releaseObject);
                vocabularyObject.put(rdf.getPrefixItem("ls:released"), releaseArrayNew);
            }
        }
        // delete items
        vocabularyObject.remove("id");
        vocabularyObject.remove("creator");
        vocabularyObject.remove("contributors");
        vocabularyObject.remove("title");
        vocabularyObject.remove("description");
        vocabularyObject.remove("created");
        vocabularyObject.remove("license");
		vocabularyObject.remove("rights");
        vocabularyObject.remove("lastModified");
        vocabularyObject.remove("releaseType");
        vocabularyObject.remove("statistics");
        vocabularyObject.remove("creatorInfo");
        // add object
        rdfObject.put(rdf.getPrefixItem("ls_voc" + ":" + id), vocabularyObject);
        return rdfObject.toJSONString();
    }

    public static JSONObject vocabulary_GET(String json, String id, String fields, String statisticsBool, String creatorInfoBool) throws IOException, UniqueIdentifierException, ParseException, TransformRdfToApiJsonException {
        JSONObject vocabularyObject = null;
        try {
            //init
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            // parse json
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            jsonObject.put("vocab", jsonObject.remove(rdf.getPrefixItem("ls_voc" + ":" + id)));
            // get items
            vocabularyObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("vocab"));
            // change dc:identifier
            JSONArray identifierArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:identifier"));
            if (identifierArray != null && !identifierArray.isEmpty()) {
                for (Object element : identifierArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dc:identifier"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    vocabularyObject.put("id", value);
                }
            }
            // change dc:creator
            JSONArray creatorArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:creator"));
            if (creatorArray != null && !creatorArray.isEmpty()) {
                for (Object element : creatorArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dc:creator"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    vocabularyObject.put("creator", value);
                }
            }
            // change dc:title
            JSONArray titleArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:title"));
            String vocabLang = "";
            if (titleArray != null && !titleArray.isEmpty()) {
                for (Object element : titleArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dc:title"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    vocabLang = (String) obj.get("lang");
                    vocabularyObject.put("title", value);
                }
            }
            // change dc:description
            JSONArray descriptionArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:description"));
            if (descriptionArray != null && !descriptionArray.isEmpty()) {
                for (Object element : descriptionArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dc:description"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    vocabularyObject.put("description", value);
                }
            }
            // change dc:language
            JSONArray languageArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:language"));
            if (languageArray != null && !languageArray.isEmpty()) {
                for (Object element : languageArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dc:language"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    vocabularyObject.put("language", value);
                }
            } else {
                vocabularyObject.put("language", vocabLang);
            }
            // change dc:created
            JSONArray createdArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:created"));
            if (createdArray != null && !createdArray.isEmpty()) {
                for (Object element : createdArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dc:created"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    if (fields == null || fields.contains("created")) {
                        vocabularyObject.put("created", value);
                        vocabularyObject.put("lastModified", value);
                    }
                }
            }
            // change dct:license
            JSONArray licenseArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dct:license"));
            if (licenseArray != null && !licenseArray.isEmpty()) {
                for (Object element : licenseArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dct:license"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    if (fields == null || fields.contains("license")) {
                        vocabularyObject.put("license", value);
                    }
                }
            }
			// change dc:rights
            JSONArray rightsArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:rights"));
            if (rightsArray != null && !rightsArray.isEmpty()) {
                for (Object element : rightsArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("dc:rights"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    if (fields == null || fields.contains("rights")) {
                        vocabularyObject.put("rights", value);
                    }
                }
            }
            // change dc:modified
            JSONArray modifiedArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:modified"));
            if (modifiedArray != null && !modifiedArray.isEmpty()) {
                vocabularyObject.remove(rdf.getPrefixItem("dc:modified"));
                List<String> listModify = new ArrayList();
                for (Object element : modifiedArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    listModify.add(value);
                }
                // set last modified
                Collections.sort(listModify);
                vocabularyObject.remove("lastModified");
                vocabularyObject.put("lastModified", listModify.get(listModify.size() - 1));
            }
            // change ls:hasReleaseType
            JSONArray releaseTypeArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:hasReleaseType"));
            if (releaseTypeArray != null && !releaseTypeArray.isEmpty()) {
                for (Object element : releaseTypeArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("ls:hasReleaseType"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    if (value.contains("Draft")) {
                        value = "draft";
                    } else {
                        value = "public";
                    }
                    if (fields == null || fields.contains("releaseType")) {
                        vocabularyObject.put("releaseType", value);
                    }
                }
            }
            // change ls:released
            JSONArray releasedArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:released"));
            if (releasedArray != null && !releasedArray.isEmpty()) {
                for (Object element : releasedArray) {
                    vocabularyObject.remove(rdf.getPrefixItem("ls:released"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    if (fields == null || fields.contains("released")) {
                        vocabularyObject.put("released", value);
                    }
                }
            }
            // delete items
            vocabularyObject.remove(rdf.getPrefixItem("rdf:type"));
            vocabularyObject.remove(rdf.getPrefixItem("dc:identifier"));
            vocabularyObject.remove(rdf.getPrefixItem("dct:creator"));
            vocabularyObject.remove(rdf.getPrefixItem("dc:creator"));
            vocabularyObject.remove(rdf.getPrefixItem("dct:contributor"));
            vocabularyObject.remove(rdf.getPrefixItem("dc:contributor"));
            vocabularyObject.remove(rdf.getPrefixItem("dc:title"));
            vocabularyObject.remove(rdf.getPrefixItem("dc:description"));
            vocabularyObject.remove(rdf.getPrefixItem("dc:created"));
            vocabularyObject.remove(rdf.getPrefixItem("dct:license"));
			vocabularyObject.remove(rdf.getPrefixItem("dc:rights"));
            vocabularyObject.remove(rdf.getPrefixItem("dc:modified"));
            vocabularyObject.remove(rdf.getPrefixItem("ls:hasReleaseType"));
            vocabularyObject.remove(rdf.getPrefixItem("ls:released"));
            // deposits
            vocabularyObject.remove(rdf.getPrefixItem("skos:changeNote"));
            vocabularyObject.remove(rdf.getPrefixItem("skos:hasTopConcept"));
            vocabularyObject.remove(rdf.getPrefixItem("dcat:theme"));
            vocabularyObject.remove(rdf.getPrefixItem("ls:hasStatusType"));
            vocabularyObject.remove(rdf.getPrefixItem("ls:sameAs"));
            // statistics
            if (statisticsBool != null) {
                if (statisticsBool.equals("true")) {
                    String query = "PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX dc: <http://purl.org/dc/elements/1.1/> SELECT ?l ?p ?o WHERE { ?l skos:inScheme ?v . ?v dc:identifier ?id . ?l ?p ?o. FILTER (?id = \"" + id + "\") }";
                    List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
                    List<String> subjects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "l");
                    List<String> predicates = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "p");
                    List<String> objects = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "o");
                    int internalLinks = 0;
                    int externalLinks = 0;
                    int waybackLinks = 0;
                    for (String item : predicates) {
                        if (item.equals(rdf.getPrefixItem("skos:broader")) || item.equals(rdf.getPrefixItem("skos:narrower")) || item.equals(rdf.getPrefixItem("skos:related"))) {
                            internalLinks++;
                        } else if (item.equals(rdf.getPrefixItem("skos:broadMatch"))
                                || item.equals(rdf.getPrefixItem("skos:narrowMatch"))
                                || item.equals(rdf.getPrefixItem("skos:relatedMatch"))
                                || item.equals(rdf.getPrefixItem("skos:closeMatch"))
                                || item.equals(rdf.getPrefixItem("skos:exactMatch"))) {
                            externalLinks++;
                        } else if (item.equals(rdf.getPrefixItem("rdfs:seeAlso"))) {
                            waybackLinks++;
                        }
                    }
                    int labelCount = 0;
                    int descriptions = 0;
                    int prefLabels = 0;
                    List<String> listModify = new ArrayList();
                    for (int i = 0; i < subjects.size(); i++) {
                        if (objects.get(i).equals(rdf.getPrefixItem("ls:Label"))) {
                            labelCount++;
                        }
                        if (predicates.get(i).equals(rdf.getPrefixItem("skos:scopeNote"))) {
                            descriptions++;
                        }
                        if (predicates.get(i).equals(rdf.getPrefixItem("skos:prefLabel"))) {
                            prefLabels++;
                        }
                        if (predicates.get(i).equals(rdf.getPrefixItem("dc:modified")) || predicates.get(i).equals(rdf.getPrefixItem("dc:created"))) {
                            listModify.add(objects.get(i));
                        }
                    }
                    JSONObject statistics = new JSONObject();
                    JSONObject links = new JSONObject();
                    JSONObject labels = new JSONObject();
                    JSONObject descriptive = new JSONObject();
                    links.put("external", externalLinks);
                    links.put("internal", internalLinks);
                    links.put("count", externalLinks + internalLinks);
                    statistics.put("links", links);
                    labels.put("count", labelCount);
                    statistics.put("labels", labels);
                    descriptive.put("wayback", waybackLinks);
                    descriptive.put("descriptions", descriptions);
                    if (prefLabels - labelCount < 0) {
                        descriptive.put("translations", 0);
                    } else {
                        descriptive.put("translations", prefLabels - labelCount);
                    }
                    statistics.put("descriptive", descriptive);
                    if (listModify.size() > 0) {
                        Collections.sort(listModify);
                        statistics.put("lastModifyAction", listModify.get(listModify.size() - 1));
                    } else {
                        statistics.put("lastModifiedLabel", "no content");
                    }
                    vocabularyObject.put(rdf.getPrefixItem("statistics"), statistics);
                }
            }
            // set creator info
            if (creatorInfoBool != null) {
                if (creatorInfoBool.equals("true")) {
                    String url = ConfigProperties.getPropertyParam("api") + "/v1/agents/" + vocabularyObject.get("creator") + ".json";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Accept-Encoding", "json");
                    if (con.getResponseCode() == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        JSONObject simillarConcept = (JSONObject) new JSONParser().parse(response.toString());
                        vocabularyObject.put("creatorInfo", simillarConcept);
                    }
                }
            }
        } catch (Exception e) {
            int errorLine = -1;
            for (StackTraceElement element : e.getStackTrace()) {
                errorLine = element.getLineNumber();
                if (element.getClassName().equals(Transformer.class.getName())) {
                    break;
                }
            }
            throw new TransformRdfToApiJsonException(e.toString() + " in line " + String.valueOf(errorLine));
        }
        // return
        return vocabularyObject;
    }

    public static JSONObject revision_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException, TransformRdfToApiJsonException {
        JSONObject revisionObject = null;
        try {
            //init
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            // parse json
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            jsonObject.put("revision", jsonObject.remove(rdf.getPrefixItem("ls_rev" + ":" + id)));
            // get items
            revisionObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("revision"));
            // change dc:identifier
            JSONArray identifierArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("dc:identifier"));
            for (Object element : identifierArray) {
                revisionObject.remove(rdf.getPrefixItem("dc:identifier"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                revisionObject.put("id", value);
            }
            // change ls:action
            JSONArray actionArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("ls:action"));
            if (actionArray != null && !actionArray.isEmpty()) {
                for (Object element : actionArray) {
                    revisionObject.remove(rdf.getPrefixItem("ls:action"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    revisionObject.put("action", value);
                }
            }
            // change ls:objectType
            JSONArray objectTypeArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("ls:objectType"));
            if (objectTypeArray != null && !objectTypeArray.isEmpty()) {
                for (Object element : objectTypeArray) {
                    revisionObject.remove(rdf.getPrefixItem("ls:objectType"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    revisionObject.put("objectType", value);
                }
            }
            // change ls:valueBefore
            JSONArray valueBeforeArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("ls:valueBefore"));
            if (valueBeforeArray != null && !valueBeforeArray.isEmpty()) {
                for (Object element : valueBeforeArray) {
                    revisionObject.remove(rdf.getPrefixItem("ls:valueBefore"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    revisionObject.put("valueBefore", value);
                }
            }
            // change ls:valueAfter
            JSONArray valueAfterArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("ls:valueAfter"));
            if (valueAfterArray != null && !valueAfterArray.isEmpty()) {
                for (Object element : valueAfterArray) {
                    revisionObject.remove(rdf.getPrefixItem("ls:valueAfter"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    revisionObject.put("valueAfter", value);
                }
            }
            // change prov:startedAtTime
            JSONArray createdArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("prov:startedAtTime"));
            if (createdArray != null && !createdArray.isEmpty()) {
                for (Object element : createdArray) {
                    revisionObject.remove(rdf.getPrefixItem("prov:startedAtTime"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    revisionObject.put("date", value);
                }
            }
            // delete items 
            revisionObject.remove(rdf.getPrefixItem("rdf:type"));
            revisionObject.remove(rdf.getPrefixItem("dc:identifier"));
            revisionObject.remove(rdf.getPrefixItem("dct:creator"));
            revisionObject.remove(rdf.getPrefixItem("dc:creator"));
            revisionObject.remove(rdf.getPrefixItem("dc:description"));
            revisionObject.remove(rdf.getPrefixItem("prov:startedAtTime"));
            revisionObject.remove(rdf.getPrefixItem("ls:action"));
            revisionObject.remove(rdf.getPrefixItem("ls:objectType"));
            revisionObject.remove(rdf.getPrefixItem("ls:valueBefore"));
            revisionObject.remove(rdf.getPrefixItem("ls:valueAfter"));
        } catch (Exception e) {
            int errorLine = -1;
            for (StackTraceElement element : e.getStackTrace()) {
                errorLine = element.getLineNumber();
                if (element.getClassName().equals(Transformer.class.getName())) {
                    break;
                }
            }
            throw new TransformRdfToApiJsonException(e.toString() + " in line " + String.valueOf(errorLine));
        }
        // return
        return revisionObject;
    }

    public static JSONObject empty_JSON(String item) throws IOException, UniqueIdentifierException, ParseException {
        JSONObject jsonObject = new JSONObject();
        return jsonObject;
    }

    public static String agent_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject rdfObject = new JSONObject();
        JSONObject agentObject = (JSONObject) new JSONParser().parse(json);
        // change title
        String titleString = (String) agentObject.get("title");
        if (titleString != null && !titleString.isEmpty()) {
            agentObject.remove("title");
            JSONArray titleArrayNew = new JSONArray();
            JSONObject titleObject = new JSONObject();
            titleObject.put("type", "literal");
            titleObject.put("value", titleString);
            titleArrayNew.add(titleObject);
            agentObject.put(rdf.getPrefixItem("foaf:title"), titleArrayNew);
        }
        // change firstName
        String firstNameString = (String) agentObject.get("firstName");
        if (firstNameString != null && !firstNameString.isEmpty()) {
            agentObject.remove("firstName");
            JSONArray firstNameArrayNew = new JSONArray();
            JSONObject firstNameObject = new JSONObject();
            firstNameObject.put("type", "literal");
            firstNameObject.put("value", firstNameString);
            firstNameArrayNew.add(firstNameObject);
            agentObject.put(rdf.getPrefixItem("foaf:firstName"), firstNameArrayNew);
        }
        // change lastName
        String lastNameString = (String) agentObject.get("lastName");
        if (lastNameString != null && !lastNameString.isEmpty()) {
            agentObject.remove("lastName");
            JSONArray lastNameArrayNew = new JSONArray();
            JSONObject lastNameObject = new JSONObject();
            lastNameObject.put("type", "literal");
            lastNameObject.put("value", lastNameString);
            lastNameArrayNew.add(lastNameObject);
            agentObject.put(rdf.getPrefixItem("foaf:lastName"), lastNameArrayNew);
        }
        // change orcid
        String orcidString = (String) agentObject.get("orcid");
        if (orcidString != null && !orcidString.isEmpty()) {
            agentObject.remove("orcid");
            JSONArray orcidArrayNew = new JSONArray();
            JSONObject orcidObject = new JSONObject();
            orcidObject.put("type", "literal");
            orcidObject.put("value", orcidString);
            orcidArrayNew.add(orcidObject);
            agentObject.put(rdf.getPrefixItem("dct:publisher"), orcidArrayNew);
        }
        // change affiliation
        String affiliationString = (String) agentObject.get("affiliation");
        if (affiliationString != null && !affiliationString.isEmpty()) {
            agentObject.remove("affiliation");
            JSONArray affiliationArrayNew = new JSONArray();
            JSONObject affiliationObject = new JSONObject();
            affiliationObject.put("type", "literal");
            affiliationObject.put("value", affiliationString);
            affiliationArrayNew.add(affiliationObject);
            agentObject.put(rdf.getPrefixItem("dct:isPartOf"), affiliationArrayNew);
        }
        // delete items
        agentObject.remove("id");
        agentObject.remove("title");
        agentObject.remove("firstName");
        agentObject.remove("lastName");
        agentObject.remove("affiliation");
        agentObject.remove("orcid");
        // add object
        rdfObject.put(rdf.getPrefixItem("ls_age" + ":" + id), agentObject);
        return rdfObject.toJSONString();
    }

    public static JSONObject agent_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException, TransformRdfToApiJsonException {
        JSONObject agentObject = null;
        try {
            //init
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            // parse json
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            jsonObject.put("agent", jsonObject.remove(rdf.getPrefixItem("ls_age" + ":" + id)));
            // get items
            agentObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("agent"));
            // change dc:identifier
            JSONArray identifierArray = (JSONArray) agentObject.get(rdf.getPrefixItem("dc:identifier"));
            if (identifierArray != null && !identifierArray.isEmpty()) {
                for (Object element : identifierArray) {
                    agentObject.remove(rdf.getPrefixItem("dc:identifier"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    agentObject.put(rdf.getPrefixItem("id"), value);
                }
            }
            // change foaf:title
            JSONArray titleArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:title"));
            if (titleArray != null && !titleArray.isEmpty()) {
                for (Object element : titleArray) {
                    agentObject.remove(rdf.getPrefixItem("foaf:title"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    agentObject.put(rdf.getPrefixItem("title"), value);
                }
            }
            // change foaf:firstName
            JSONArray firstNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:firstName"));
            if (firstNameArray != null && !firstNameArray.isEmpty()) {
                for (Object element : firstNameArray) {
                    agentObject.remove(rdf.getPrefixItem("foaf:mbox"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    agentObject.put(rdf.getPrefixItem("firstName"), value);
                }
            }
            // change foaf:lastName
            JSONArray lastNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:lastName"));
            if (lastNameArray != null && !lastNameArray.isEmpty()) {
                for (Object element : lastNameArray) {
                    agentObject.remove(rdf.getPrefixItem("foaf:lastName"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    agentObject.put(rdf.getPrefixItem("lastName"), value);
                }
            }
            // change dct:publisher
            JSONArray publisherArray = (JSONArray) agentObject.get(rdf.getPrefixItem("dct:publisher"));
            if (publisherArray != null && !publisherArray.isEmpty()) {
                for (Object element : publisherArray) {
                    agentObject.remove(rdf.getPrefixItem("dct:publisher"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    agentObject.put(rdf.getPrefixItem("orcid"), value);
                }
            }
            // change dct:isPartOf
            JSONArray partOfArray = (JSONArray) agentObject.get(rdf.getPrefixItem("dct:isPartOf"));
            if (partOfArray != null && !partOfArray.isEmpty()) {
                for (Object element : partOfArray) {
                    agentObject.remove(rdf.getPrefixItem("dct:isPartOf"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    agentObject.put(rdf.getPrefixItem("affiliation"), value);
                }
            }
            // delete items
            agentObject.remove(rdf.getPrefixItem("rdf:type"));
            agentObject.remove(rdf.getPrefixItem("dc:identifier"));
            agentObject.remove(rdf.getPrefixItem("foaf:title"));
            agentObject.remove(rdf.getPrefixItem("foaf:firstName"));
            agentObject.remove(rdf.getPrefixItem("foaf:lastName"));
            agentObject.remove(rdf.getPrefixItem("dct:publisher"));
            agentObject.remove(rdf.getPrefixItem("dct:isPartOf"));
            // deposits
            agentObject.remove(rdf.getPrefixItem("ls:sameAs"));
            agentObject.remove(rdf.getPrefixItem("foaf:accountName"));
            agentObject.remove(rdf.getPrefixItem("ls:inGroup"));
            agentObject.remove(rdf.getPrefixItem("foaf:mbox"));
            agentObject.remove(rdf.getPrefixItem("foaf:homepage"));
            agentObject.remove(rdf.getPrefixItem("foaf:img"));
            agentObject.remove(rdf.getPrefixItem("geo:lat"));
            agentObject.remove(rdf.getPrefixItem("geo:lon"));
        } catch (Exception e) {
            int errorLine = -1;
            for (StackTraceElement element : e.getStackTrace()) {
                errorLine = element.getLineNumber();
                if (element.getClassName().equals(Transformer.class.getName())) {
                    break;
                }
            }
            throw new TransformRdfToApiJsonException(e.toString() + " in line " + String.valueOf(errorLine));
        }
        // return
        return agentObject;
    }

    public static String label_POST(String json, String id, String creator) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject rdfObject = new JSONObject();
        JSONObject labelObject = (JSONObject) new JSONParser().parse(json);
        // change language
        String language = (String) labelObject.get("language");
        if (language != null) {
            labelObject.remove("language");
            JSONArray languageArrayNew = new JSONArray();
            JSONObject languageObject = new JSONObject();
            languageObject.put("type", "literal");
            languageObject.put("value", language);
            languageArrayNew.add(languageObject);
            labelObject.put(rdf.getPrefixItem("dc:language"), languageArrayNew);
        }
        // change thumbnail
        String thumbnail = (String) labelObject.get("thumbnail");
        JSONArray prefLabelArray = new JSONArray();
        if (thumbnail != null) {
            labelObject.remove("thumbnail");
            JSONObject tmpObject = new JSONObject();
            tmpObject.put("type", "literal");
            tmpObject.put("value", thumbnail);
            tmpObject.put("lang", language);
            JSONArray arrayNew = new JSONArray();
            arrayNew.add(tmpObject);
            labelObject.put(rdf.getPrefixItem("ls:thumbnail"), arrayNew);
            // add thumbnail to preflabels
            prefLabelArray.add(tmpObject);
        }
        // change prefLabel
        JSONArray translationsArray = (JSONArray) labelObject.get("translations");
        if (translationsArray != null && !translationsArray.isEmpty()) {
            labelObject.remove("translations");
            for (Object element : translationsArray) {
                JSONObject thisObject = (JSONObject) element;
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "literal");
                tmpObject.put("value", thisObject.get("value"));
                tmpObject.put("lang", thisObject.get("lang"));
                prefLabelArray.add(tmpObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:prefLabel"), prefLabelArray);
        }
        // change description
        String description = (String) labelObject.get("description");
        if (description != null) {
            labelObject.remove("description");
            JSONObject tmpObject = new JSONObject();
            tmpObject.put("type", "literal");
            tmpObject.put("value", description);
            tmpObject.put("lang", language);
            JSONArray arrayNew = new JSONArray();
            arrayNew.add(tmpObject);
            labelObject.put(rdf.getPrefixItem("skos:scopeNote"), arrayNew);
        }
        // change related
        JSONArray relatedArray = (JSONArray) labelObject.get("related");
        List<String> relatedStringList = new ArrayList<String>();
        if (relatedArray != null && !relatedArray.isEmpty()) {
            for (Object element : relatedArray) {
                if (element.equals("null")) {
                    relatedStringList.add("http://dummy.net");
                } else {
                    relatedStringList.add((String) element);
                }
            }
            labelObject.remove("related");
            JSONArray arrayNew = new JSONArray();
            for (String element : relatedStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", rdf.getPrefixItem("ls_lab:" + element));
                arrayNew.add(tmpObject);
                // related symmetric
                JSONObject tmpLabelObject = new JSONObject();
                JSONArray arrayNew2 = new JSONArray();
                JSONObject tmpObject2 = new JSONObject();
                tmpObject2.put("type", "uri");
                tmpObject2.put("value", rdf.getPrefixItem("ls_lab" + ":" + id));
                arrayNew2.add(tmpObject2);
                tmpLabelObject.put(rdf.getPrefixItem("skos:related"), arrayNew2);
                rdfObject.put(rdf.getPrefixItem("ls_lab:" + element), tmpLabelObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:related"), arrayNew);
        }
        // change broader
        JSONArray broaderArray = (JSONArray) labelObject.get("broader");
        List<String> broaderStringList = new ArrayList<String>();
        if (broaderArray != null && !broaderArray.isEmpty()) {
            for (Object element : broaderArray) {
                if (element.equals("null")) {
                    broaderStringList.add("http://dummy.net");
                } else {
                    broaderStringList.add((String) element);
                }
            }
            labelObject.remove("broader");
            JSONArray arrayNew = new JSONArray();
            for (String element : broaderStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", rdf.getPrefixItem("ls_lab:" + element));
                arrayNew.add(tmpObject);
                // narrower
                JSONObject tmpLabelObject = new JSONObject();
                JSONArray arrayNew2 = new JSONArray();
                JSONObject tmpObject2 = new JSONObject();
                tmpObject2.put("type", "uri");
                tmpObject2.put("value", rdf.getPrefixItem("ls_lab" + ":" + id));
                arrayNew2.add(tmpObject2);
                tmpLabelObject.put(rdf.getPrefixItem("skos:narrower"), arrayNew2);
                rdfObject.put(rdf.getPrefixItem("ls_lab:" + element), tmpLabelObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:broader"), arrayNew);
        }
        // change narrower
        JSONArray narrowerArray = (JSONArray) labelObject.get("narrower");
        List<String> narrowerStringList = new ArrayList<String>();
        if (narrowerArray != null && !narrowerArray.isEmpty()) {
            for (Object element : narrowerArray) {
                if (element.equals("null")) {
                    narrowerStringList.add("http://dummy.net");
                } else {
                    narrowerStringList.add((String) element);
                }
            }
            labelObject.remove("narrower");
            JSONArray arrayNew = new JSONArray();
            for (String element : narrowerStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", rdf.getPrefixItem("ls_lab:" + element));
                arrayNew.add(tmpObject);
                // broader
                JSONObject tmpLabelObject = new JSONObject();
                JSONArray arrayNew2 = new JSONArray();
                JSONObject tmpObject2 = new JSONObject();
                tmpObject2.put("type", "uri");
                tmpObject2.put("value", rdf.getPrefixItem("ls_lab" + ":" + id));
                arrayNew2.add(tmpObject2);
                tmpLabelObject.put(rdf.getPrefixItem("skos:broader"), arrayNew2);
                rdfObject.put(rdf.getPrefixItem("ls_lab:" + element), tmpLabelObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:narrower"), arrayNew);
        }
        // change closeMatch
        JSONArray closeMatchArray = (JSONArray) labelObject.get("closeMatch");
        List<String> closeMatchStringList = new ArrayList<String>();
        if (closeMatchArray != null && !closeMatchArray.isEmpty()) {
            for (Object element : closeMatchArray) {
                JSONObject tmpjson = (JSONObject) element;
                closeMatchStringList.add((String) tmpjson.get("uri"));
            }
            labelObject.remove("closeMatch");
            JSONArray arrayNew = new JSONArray();
            for (String element : closeMatchStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", element);
                arrayNew.add(tmpObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:closeMatch"), arrayNew);
        }
        // change exactMatch
        JSONArray exactMatchArray = (JSONArray) labelObject.get("exactMatch");
        List<String> exactMatchStringList = new ArrayList<String>();
        if (exactMatchArray != null && !exactMatchArray.isEmpty()) {
            for (Object element : exactMatchArray) {
                JSONObject tmpjson = (JSONObject) element;
                exactMatchStringList.add((String) tmpjson.get("uri"));
            }
            labelObject.remove("exactMatch");
            JSONArray arrayNew = new JSONArray();
            for (String element : exactMatchStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", element);
                arrayNew.add(tmpObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:exactMatch"), arrayNew);
        }
        // change relatedMatch
        JSONArray relatedMatchArray = (JSONArray) labelObject.get("relatedMatch");
        List<String> relatedMatchStringList = new ArrayList<String>();
        if (relatedMatchArray != null && !relatedMatchArray.isEmpty()) {
            for (Object element : relatedMatchArray) {
                JSONObject tmpjson = (JSONObject) element;
                relatedMatchStringList.add((String) tmpjson.get("uri"));
            }
            labelObject.remove("relatedMatch");
            JSONArray arrayNew = new JSONArray();
            for (String element : relatedMatchStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", element);
                arrayNew.add(tmpObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:relatedMatch"), arrayNew);
        }
        // change narrowMatch
        JSONArray narrowMatchArray = (JSONArray) labelObject.get("narrowMatch");
        List<String> narrowMatchStringList = new ArrayList<String>();
        if (narrowMatchArray != null && !narrowMatchArray.isEmpty()) {
            for (Object element : narrowMatchArray) {
                JSONObject tmpjson = (JSONObject) element;
                narrowMatchStringList.add((String) tmpjson.get("uri"));
            }
            labelObject.remove("narrowMatch");
            JSONArray arrayNew = new JSONArray();
            for (String element : narrowMatchStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", element);
                arrayNew.add(tmpObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:narrowMatch"), arrayNew);
        }
        // change broadMatch
        JSONArray broadMatchArray = (JSONArray) labelObject.get("broadMatch");
        List<String> broadMatchStringList = new ArrayList<String>();
        if (broadMatchArray != null && !broadMatchArray.isEmpty()) {
            for (Object element : broadMatchArray) {
                JSONObject tmpjson = (JSONObject) element;
                broadMatchStringList.add((String) tmpjson.get("uri"));
            }
            labelObject.remove("broadMatch");
            JSONArray arrayNew = new JSONArray();
            for (String element : broadMatchStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", element);
                arrayNew.add(tmpObject);
            }
            labelObject.put(rdf.getPrefixItem("skos:broadMatch"), arrayNew);
        }
        // change seeAlso
        JSONArray seeAlsoArray = (JSONArray) labelObject.get("seeAlso");
        List<String> seeAlsoStringList = new ArrayList<String>();
        if (seeAlsoArray != null && !seeAlsoArray.isEmpty()) {
            for (Object element : seeAlsoArray) {
                JSONObject tmpjson = (JSONObject) element;
                seeAlsoStringList.add((String) tmpjson.get("uri"));
            }
            labelObject.remove("seeAlso");
            JSONArray arrayNew = new JSONArray();
            for (String element : seeAlsoStringList) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", element);
                arrayNew.add(tmpObject);
            }
            labelObject.put(rdf.getPrefixItem("rdfs:seeAlso"), arrayNew);
        }
        // delete items
        labelObject.remove("id");
        labelObject.remove("creator");
        labelObject.remove("contributors");
        labelObject.remove("vocabID");
        labelObject.remove("translations");
        labelObject.remove("thumbnail");
        labelObject.remove("description");
        labelObject.remove("created");
        labelObject.remove("license");
		labelObject.remove("rights");
        labelObject.remove("modifications");
        labelObject.remove("lastModified");
        labelObject.remove("revisionIDs");
        labelObject.remove("releaseType");
        labelObject.remove("related");
        labelObject.remove("broader");
        labelObject.remove("narrower");
        labelObject.remove("closeMatch");
        labelObject.remove("exactMatch");
        labelObject.remove("relatedMatch");
        labelObject.remove("narrowMatch");
        labelObject.remove("broadMatch");
        labelObject.remove("seeAlso");
        labelObject.remove("equalConcepts");
        labelObject.remove("revisions");
        labelObject.remove("released");
        labelObject.remove("creatorInfo");
        // add object
        rdfObject.put(rdf.getPrefixItem("ls_lab" + ":" + id), labelObject);
        return rdfObject.toJSONString();
    }

    public static JSONObject label_GET(String json, String id, String fields, List<RetcatItem> retcatlist, String equalConceptsBool, String revisionsBool, String creatorInfoBool) throws IOException, UniqueIdentifierException, ParseException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException, TransformRdfToApiJsonException {
        JSONObject labelObject = null;
        try {
            //init
            RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
            // parse json
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            jsonObject.put("label", jsonObject.remove(rdf.getPrefixItem("ls_lab" + ":" + id)));
            // get items
            labelObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("label"));
            // change dc:identifier
            JSONArray identifierArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:identifier"));
            if (identifierArray != null && !identifierArray.isEmpty()) {
                for (Object element : identifierArray) {
                    labelObject.remove(rdf.getPrefixItem("dc:identifier"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    labelObject.put(rdf.getPrefixItem("id"), value);
                }
            }
            // change dc:creator
            JSONArray creatorArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:creator"));
            if (creatorArray != null && !creatorArray.isEmpty()) {
                for (Object element : creatorArray) {
                    labelObject.remove(rdf.getPrefixItem("dc:creator"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    labelObject.put(rdf.getPrefixItem("creator"), value);
                }
            }
            // change skos:inScheme
            JSONArray vocabArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:inScheme"));
            if (vocabArray != null && !vocabArray.isEmpty()) {
                for (Object element : vocabArray) {
                    labelObject.remove(rdf.getPrefixItem("skos:inScheme"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    String arrayNew;
                    if (value.contains("vocabulary/")) {
                        arrayNew = value.split("vocabulary/")[1];
                    } else {
                        arrayNew = value;
                    }
                    if (fields == null || fields.contains("vocabID")) {
                        labelObject.put(rdf.getPrefixItem("vocabID"), arrayNew);
                    }
                }
            }
            // change ls:thumbnail
            JSONArray thumbnailArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:thumbnail"));
            String thumbnail = "";
            String language = "";
            if (thumbnailArray != null && !thumbnailArray.isEmpty()) {
                labelObject.remove(rdf.getPrefixItem("ls:thumbnail"));
                for (Object element : thumbnailArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    String lang = (String) obj.get("lang");
                    thumbnail = value + "@" + lang;
                    language = lang;
                    labelObject.put("thumbnail", value);
                    labelObject.put("language", lang);
                }
            }
            // change skos:scopeNote
            JSONArray scopeNoteArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:scopeNote"));
            if (scopeNoteArray != null && !scopeNoteArray.isEmpty()) {
                labelObject.remove(rdf.getPrefixItem("skos:scopeNote"));
                for (Object element : scopeNoteArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    labelObject.put("description", value);
                }
            }
            // change skos:prefLabel
            JSONArray prefLabelArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:prefLabel"));
            if (prefLabelArray != null && !prefLabelArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:prefLabel"));
                for (Object element : prefLabelArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    String lang = (String) obj.get("lang");
                    JSONObject objTmp = new JSONObject();
                    objTmp.put("value", value);
                    objTmp.put("lang", lang);
                    String prefLabel = value + "@" + lang;
                    if (!thumbnail.equals(prefLabel)) {
                        arrayNew.add(objTmp);
                    }
                }
                if (arrayNew.size() > 0) {
                    labelObject.put(rdf.getPrefixItem("translations"), arrayNew);
                }
            }
            // change dc:language
            JSONArray languageArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:language"));
            if (languageArray != null && !languageArray.isEmpty()) {
                for (Object element : languageArray) {
                    labelObject.remove(rdf.getPrefixItem("dc:language"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    labelObject.put("language", value);
                }
            } else {
                labelObject.put("language", language);
            }
            // change skos:related
            JSONArray relatedArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:related"));
            if (relatedArray != null && !relatedArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:related"));
                for (Object element : relatedArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    value = value.replace(rdf.getPrefixItem("ls_lab:"), "");
                    arrayNew.add(value);
                }
                if (fields == null || fields.contains("related")) {
                    labelObject.put(rdf.getPrefixItem("related"), arrayNew);
                }
            }
            // change skos:broader
            JSONArray broaderArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:broader"));
            if (broaderArray != null && !broaderArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:broader"));
                for (Object element : broaderArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    value = value.replace(rdf.getPrefixItem("ls_lab:"), "");
                    arrayNew.add(value);
                }
                if (fields == null || fields.contains("broader")) {
                    labelObject.put(rdf.getPrefixItem("broader"), arrayNew);
                }
            }
            // change skos:narrower
            JSONArray narrowerArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:narrower"));
            if (narrowerArray != null && !narrowerArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:narrower"));
                for (Object element : narrowerArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    value = value.replace(rdf.getPrefixItem("ls_lab:"), "");
                    arrayNew.add(value);
                }
                if (fields == null || fields.contains("narrower")) {
                    labelObject.put(rdf.getPrefixItem("narrower"), arrayNew);
                }
            }
            // change skos:closeMatch
            JSONArray closeMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:closeMatch"));
            if (closeMatchArray != null && !closeMatchArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:closeMatch"));
                for (Object element : closeMatchArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("uri", value);
                    // get retcat info
                    boolean match = false;
                    for (RetcatItem item : retcatlist) {
                        if (value.contains(item.getPrefix())) {
                            match = true;
                            tmpObject.put("type", item.getType());
                        }
                    }
                    if (!match) {
                        tmpObject.put("type", "wayback");
                    }
                    arrayNew.add(tmpObject);
                }
                if (fields == null || fields.contains("closeMatch")) {
                    labelObject.put(rdf.getPrefixItem("closeMatch"), arrayNew);
                }
            }
            // change skos:exactMatch
            JSONArray exactMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:exactMatch"));
            List<String> equalConcepts = new ArrayList();
            if (exactMatchArray != null && !exactMatchArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:exactMatch"));
                for (Object element : exactMatchArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("uri", value);
                    if (value.contains("//" + ConfigProperties.getPropertyParam("host"))) {
                        equalConcepts.add(value);
                    }
                    // get retcat info
                    boolean match = false;
                    for (RetcatItem item : retcatlist) {
                        if (value.contains(item.getPrefix())) {
                            match = true;
                            tmpObject.put("type", item.getType());
                        }
                    }
                    if (!match) {
                        tmpObject.put("type", "wayback");
                    }
                    arrayNew.add(tmpObject);
                }
                if (fields == null || fields.contains("exactMatch")) {
                    labelObject.put(rdf.getPrefixItem("exactMatch"), arrayNew);
                }
            }
            // change skos:relatedMatch
            JSONArray relatedMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:relatedMatch"));
            if (relatedMatchArray != null && !relatedMatchArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:relatedMatch"));
                for (Object element : relatedMatchArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("uri", value);
                    // get retcat info
                    boolean match = false;
                    for (RetcatItem item : retcatlist) {
                        if (value.contains(item.getPrefix())) {
                            match = true;
                            tmpObject.put("type", item.getType());
                        }
                    }
                    if (!match) {
                        tmpObject.put("type", "wayback");
                    }
                    arrayNew.add(tmpObject);
                }
                if (fields == null || fields.contains("relatedMatch")) {
                    labelObject.put(rdf.getPrefixItem("relatedMatch"), arrayNew);
                }
            }
            // change skos:narrowMatch
            JSONArray narrowMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:narrowMatch"));
            if (narrowMatchArray != null && !narrowMatchArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:narrowMatch"));
                for (Object element : narrowMatchArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("uri", value);
                    // get retcat info
                    boolean match = false;
                    for (RetcatItem item : retcatlist) {
                        if (value.contains(item.getPrefix())) {
                            match = true;
                            tmpObject.put("type", item.getType());
                        }
                    }
                    if (!match) {
                        tmpObject.put("type", "wayback");
                    }
                    arrayNew.add(tmpObject);
                }
                if (fields == null || fields.contains("altLabel")) {
                    labelObject.put(rdf.getPrefixItem("narrowMatch"), arrayNew);
                }
            }
            // change skos:broadMatch
            JSONArray broadMatchArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:broadMatch"));
            if (broadMatchArray != null && !broadMatchArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("skos:broadMatch"));
                for (Object element : broadMatchArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("uri", value);
                    // get retcat info
                    boolean match = false;
                    for (RetcatItem item : retcatlist) {
                        if (value.contains(item.getPrefix())) {
                            match = true;
                            tmpObject.put("type", item.getType());
                        }
                    }
                    if (!match) {
                        tmpObject.put("type", "wayback");
                    }
                    arrayNew.add(tmpObject);
                }
                if (fields == null || fields.contains("broadMatch")) {
                    labelObject.put(rdf.getPrefixItem("broadMatch"), arrayNew);
                }
            }
            // change rdfs:seeAlso
            JSONArray seeAlsoArray = (JSONArray) labelObject.get(rdf.getPrefixItem("rdfs:seeAlso"));
            if (seeAlsoArray != null && !seeAlsoArray.isEmpty()) {
                JSONArray arrayNew = new JSONArray();
                labelObject.remove(rdf.getPrefixItem("rdfs:seeAlso"));
                for (Object element : seeAlsoArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("uri", value);
                    tmpObject.put("type", "wayback");
                    arrayNew.add(tmpObject);
                }
                if (fields == null || fields.contains("seeAlso")) {
                    labelObject.put(rdf.getPrefixItem("seeAlso"), arrayNew);
                }
            }
            // change dc:created
            JSONArray createdArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:created"));
            if (createdArray != null && !createdArray.isEmpty()) {
                for (Object element : createdArray) {
                    labelObject.remove(rdf.getPrefixItem("dc:created"));
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    if (fields == null || fields.contains("created")) {
                        labelObject.put(rdf.getPrefixItem("created"), value);
                        labelObject.put("lastModified", value);
                    }
                }
            }
            // change dc:modified
            JSONArray modifiedArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:modified"));
            if (modifiedArray != null && !modifiedArray.isEmpty()) {
                labelObject.remove(rdf.getPrefixItem("dc:modified"));
                List<String> listModify = new ArrayList();
                for (Object element : modifiedArray) {
                    JSONObject obj = (JSONObject) element;
                    String value = (String) obj.get("value");
                    listModify.add(value);
                }
                // set last modified
                Collections.sort(listModify);
                labelObject.remove("lastModified");
                labelObject.put("lastModified", listModify.get(listModify.size() - 1));
            }
            // set released, license and rights
            RDF rdf3 = new RDF(ConfigProperties.getPropertyParam("host"));
            String queryRL = rdf3.getPREFIXSPARQL();
            queryRL += "SELECT * WHERE { ls_lab:" + id + " skos:inScheme ?scheme. OPTIONAL { ?scheme dct:license ?license. } OPTIONAL { ?scheme ls:released ?released } OPTIONAL { ?scheme dc:rights ?rights } } ";
            List<BindingSet> resultRL = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), queryRL);
            List<String> license = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(resultRL, "license");
            List<String> released = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(resultRL, "released");
			List<String> rights = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(resultRL, "rights");
            if (license != null && license.size() > 0) {
				if (license.get(0) != null) {
					labelObject.put(rdf.getPrefixItem("license"), license.get(0));
				}
			}
            if (released != null && released.size() > 0) {
				if (released.get(0) != null) {
					labelObject.put(rdf.getPrefixItem("released"), released.get(0));
				}
			}
			if (rights != null && rights.size() > 0) {
				if (rights.get(0) != null) {
					labelObject.put(rdf.getPrefixItem("rights"), rights.get(0));
				}
			}
            // set revisions
            labelObject.remove(rdf.getPrefixItem("skos:changeNote"));
            if (revisionsBool != null) {
                if (revisionsBool.equals("true")) {
                    RDF rdf2 = new RDF(ConfigProperties.getPropertyParam("host"));
                    String query = rdf2.getPREFIXSPARQL();
                    query += "SELECT * WHERE { "
                            + "ls_lab:" + id + " skos:changeNote ?rev. "
                            + "?rev ls:action ?action. "
                            + "?rev ls:objectType ?objectType. "
                            + "?rev ls:valueBefore ?valueBefore. "
                            + "?rev ls:valueAfter ?valueAfter. "
                            + "?rev prov:startedAtTime ?date. "
                            + "} ORDER BY DESC(?date)";
                    List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
                    List<String> rev = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "rev");
                    List<String> action = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "action");
                    List<String> objectType = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "objectType");
                    List<String> valueBefore = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "valueBefore");
                    List<String> valueAfter = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "valueAfter");
                    List<String> date = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "date");
                    JSONArray revisionsArray = new JSONArray();
                    for (int i = 0; i < rev.size(); i++) {
                        JSONObject tmpObject = new JSONObject();
                        tmpObject.put("action", action.get(i));
                        tmpObject.put("objectType", objectType.get(i));
                        tmpObject.put("valueBefore", valueBefore.get(i).replace("^^<http://www.w3.org/2001/XMLSchema#string>", ""));
                        tmpObject.put("valueAfter", valueAfter.get(i).replace("^^<http://www.w3.org/2001/XMLSchema#string>", ""));
                        tmpObject.put("date", date.get(i));
                        revisionsArray.add(tmpObject);
                    }
                    if (revisionsArray != null && !revisionsArray.isEmpty()) {
                        labelObject.put("revisions", revisionsArray);
                    }
                }
            }
            // set equal concepts
            if (equalConceptsBool != null) {
                // get bidirectional concepts
                RDF rdf2 = new RDF(ConfigProperties.getPropertyParam("host"));
                String query = rdf2.getPREFIXSPARQL();
                query += "SELECT * WHERE { ?em skos:exactMatch ls_lab:" + id + ". } ";
                List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
                List<String> em = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(result, "em");
                for (String value : em) {
                    equalConcepts.add(value);
                }
                // get concept json objects
                if (equalConcepts.size() > 0 && equalConceptsBool.equals("true")) {
                    JSONArray equalArray = new JSONArray();
                    for (String concept : equalConcepts) {
                        String[] conceptSplit = concept.split("/");
                        String url = ConfigProperties.getPropertyParam("api") + "/v1/labels/" + conceptSplit[conceptSplit.length - 1] + ".json?equalConcepts=false";
                        URL obj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                        con.setRequestMethod("GET");
                        con.setRequestProperty("Accept-Encoding", "json");
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        JSONObject simillarConcept = (JSONObject) new JSONParser().parse(response.toString());
                        equalArray.add(simillarConcept);
                    }
                    labelObject.put("equalConcepts", equalArray);
                }
            }
            // set creator info
            if (creatorInfoBool != null) {
                if (creatorInfoBool.equals("true")) {
                    String url = ConfigProperties.getPropertyParam("api") + "/v1/agents/" + labelObject.get("creator") + ".json";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Accept-Encoding", "json");
                    if (con.getResponseCode() == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        JSONObject simillarConcept = (JSONObject) new JSONParser().parse(response.toString());
                        labelObject.put("creatorInfo", simillarConcept);
                    }
                }
            }
            // delete items
            labelObject.remove(rdf.getPrefixItem("rdf:type"));
            labelObject.remove(rdf.getPrefixItem("dc:identifier"));
            labelObject.remove(rdf.getPrefixItem("dct:creator"));
            labelObject.remove(rdf.getPrefixItem("dc:creator"));
            labelObject.remove(rdf.getPrefixItem("dct:contributor"));
            labelObject.remove(rdf.getPrefixItem("dc:contributor"));
            labelObject.remove(rdf.getPrefixItem("skos:inScheme"));
            labelObject.remove(rdf.getPrefixItem("ls:thumbnail"));
            labelObject.remove(rdf.getPrefixItem("skos:prefLabel"));
            labelObject.remove(rdf.getPrefixItem("skos:scopeNote"));
            labelObject.remove(rdf.getPrefixItem("skos:broader"));
            labelObject.remove(rdf.getPrefixItem("skos:narrower"));
            labelObject.remove(rdf.getPrefixItem("skos:related"));
            labelObject.remove(rdf.getPrefixItem("skos:broadMatch"));
            labelObject.remove(rdf.getPrefixItem("skos:narrowMatch"));
            labelObject.remove(rdf.getPrefixItem("skos:relatedMatch"));
            labelObject.remove(rdf.getPrefixItem("skos:closeMatch"));
            labelObject.remove(rdf.getPrefixItem("skos:exactMatch"));
            labelObject.remove(rdf.getPrefixItem("rdfs:seeAlso"));
            labelObject.remove(rdf.getPrefixItem("dc:created"));
            labelObject.remove(rdf.getPrefixItem("dct:license"));
            labelObject.remove(rdf.getPrefixItem("dc:modified"));
            labelObject.remove(rdf.getPrefixItem("ls:releasedAt"));
            // deposits
            labelObject.remove(rdf.getPrefixItem("ls:sameAs"));
            labelObject.remove(rdf.getPrefixItem("ls:hasStatusType"));
            labelObject.remove(rdf.getPrefixItem("ls:hasContext"));
            labelObject.remove(rdf.getPrefixItem("skos:altLabel"));
            labelObject.remove(rdf.getPrefixItem("skos:changeNote"));
            labelObject.remove(rdf.getPrefixItem("ls:hasReleaseType"));
        } catch (Exception e) {
            int errorLine = -1;
            for (StackTraceElement element : e.getStackTrace()) {
                errorLine = element.getLineNumber();
                if (element.getClassName().equals(Transformer.class.getName())) {
                    break;
                }
            }
            throw new TransformRdfToApiJsonException(e.toString() + " in line " + String.valueOf(errorLine));
        }
        // return
        return labelObject;
    }

    public static HashMap<String, String> getLabelDifference(String json_old, String json_new) throws ParseException, RevisionTypeException {
        HashMap<String, String> hm = new HashMap();
        try {
            // jsons
            JSONObject oldObject = (JSONObject) new JSONParser().parse(json_old);
            JSONObject newObject = (JSONObject) new JSONParser().parse(json_new);
            // objects
            String oldDescription = (String) oldObject.get("description");
            String newDescription = (String) newObject.get("description");
            JSONArray oldTranslations = (JSONArray) oldObject.get("translations");
            JSONArray newTranslations = (JSONArray) newObject.get("translations");
            JSONArray oldBroader = (JSONArray) oldObject.get("broader");
            JSONArray newBroader = (JSONArray) newObject.get("broader");
            JSONArray oldNarrower = (JSONArray) oldObject.get("narrower");
            JSONArray newNarrower = (JSONArray) newObject.get("narrower");
            JSONArray oldRelated = (JSONArray) oldObject.get("related");
            JSONArray newRelated = (JSONArray) newObject.get("related");
            JSONArray oldBroadMatch = (JSONArray) oldObject.get("broadMatch");
            JSONArray newBroadMatch = (JSONArray) newObject.get("broadMatch");
            JSONArray oldNarrowMatch = (JSONArray) oldObject.get("narrowMatch");
            JSONArray newNarrowMatch = (JSONArray) newObject.get("narrowMatch");
            JSONArray oldRelatedMatch = (JSONArray) oldObject.get("relatedMatch");
            JSONArray newRelatedMatch = (JSONArray) newObject.get("relatedMatch");
            JSONArray oldCloseMatch = (JSONArray) oldObject.get("closeMatch");
            JSONArray newCloseMatch = (JSONArray) newObject.get("closeMatch");
            JSONArray oldExactMatch = (JSONArray) oldObject.get("exactMatch");
            JSONArray newExactMatch = (JSONArray) newObject.get("exactMatch");
            JSONArray oldSeeAlso = (JSONArray) oldObject.get("seeAlso");
            JSONArray newSeeAlso = (JSONArray) newObject.get("seeAlso");
            int z = 0;
            z = 1;
            // description
            if (oldDescription == null && newDescription != null) {
                hm.put("objectType", "description");
                hm.put("action", "add");
                hm.put("valueBefore", "");
                hm.put("valueAfter", newDescription);
            } else if (oldDescription != null && newDescription == null) {
                hm.put("objectType", "description");
                hm.put("action", "delete");
                hm.put("valueBefore", oldDescription);
                hm.put("valueAfter", "");
            } else if (oldDescription != newDescription) {
                hm.put("objectType", "description");
                hm.put("action", "modify");
                hm.put("valueBefore", oldDescription);
                hm.put("valueAfter", newDescription);
            }
            z = 1;
            // translation
            if (oldTranslations != null && newTranslations != null) { // not empty
                if (!oldTranslations.equals(newTranslations)) {
                    if (oldTranslations.size() == newTranslations.size()) { // value changed
                        hm.put("objectType", "translation");
                        hm.put("action", "modify");
                        JSONArray oldTranslationsTmp = oldTranslations;
                        JSONArray newTranslationsTmp = newTranslations;
                        oldTranslationsTmp.removeAll(newTranslationsTmp);
                        for (Object item : oldTranslationsTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueBefore", tmp.get("value") + " (" + tmp.get("lang") + ")");
                        }
                        for (Object item : newTranslationsTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueAfter", tmp.get("value") + " (" + tmp.get("lang") + ")");
                        }
                    } else if (oldTranslations.size() > newTranslations.size()) { // deleted
                        hm.put("objectType", "translation");
                        hm.put("action", "delete");
                        for (Object item : oldTranslations) {
                            if (!newTranslations.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", tmp.get("value") + " (" + tmp.get("lang") + ")");
                                hm.put("valueAfter", "");
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "translation");
                        hm.put("action", "add");
                        for (Object item : newTranslations) {
                            if (!oldTranslations.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp.get("value") + " (" + tmp.get("lang") + ")");
                                break;
                            }
                        }
                    }
                }
            } else if (oldTranslations == null && newTranslations != null) { // value to empty
                for (Object item : newTranslations) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "translation");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp.get("value") + " (" + tmp.get("lang") + ")");
                }
            } else if (oldTranslations != null && newTranslations == null) { // empty to value
                for (Object item : oldTranslations) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "translation");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp.get("value") + " (" + tmp.get("lang") + ")");
                    hm.put("valueAfter", "");
                }
            }
            z = 1;
            // broader
            if (oldBroader != null && newBroader != null) { // not empty
                if (!oldBroader.equals(newBroader)) {
                    if (oldBroader.size() == newBroader.size()) { // value changed
                        hm.put("objectType", "broader");
                        hm.put("action", "modify");
                        JSONArray oldBroaderTmp = oldBroader;
                        JSONArray newBroaderTmp = newBroader;
                        oldBroaderTmp.removeAll(newBroaderTmp);
                        for (Object item : oldBroader) {
                            String tmp = (String) item;
                            hm.put("valueBefore", tmp);
                            hm.put("bidirectional", tmp);
                            hm.put("bidirectional-del", tmp);
                        }
                        for (Object item : newBroader) {
                            String tmp = (String) item;
                            hm.put("valueAfter", tmp);
                            hm.put("bidirectional-add", tmp);
                        }
                    } else if (oldBroader.size() > newBroader.size()) { // deleted
                        hm.put("objectType", "broader");
                        hm.put("action", "delete");
                        for (Object item : oldBroader) {
                            if (!newBroader.contains(item)) {
                                String tmp = (String) item;
                                hm.put("valueBefore", tmp);
                                hm.put("valueAfter", "");
                                hm.put("bidirectional", tmp);
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "broader");
                        hm.put("action", "add");
                        for (Object item : newBroader) {
                            if (!oldBroader.contains(item)) {
                                String tmp = (String) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp);
                                hm.put("bidirectional", tmp);
                                break;
                            }
                        }
                    }
                }
            } else if (oldBroader == null && newBroader != null) { // value to empty
                for (Object item : newBroader) {
                    String tmp = (String) item;
                    hm.put("objectType", "broader");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp);
                    hm.put("bidirectional", tmp);
                }
            } else if (oldBroader != null && newBroader == null) { // empty to value
                for (Object item : oldBroader) {
                    String tmp = (String) item;
                    hm.put("objectType", "broader");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp);
                    hm.put("valueAfter", "");
                    hm.put("bidirectional", tmp);
                }
            }
            z = 1;
            // narrower
            if (oldNarrower != null && newNarrower != null) { // not empty
                if (!oldNarrower.equals(newNarrower)) {
                    if (oldNarrower.size() == newNarrower.size()) { // value changed
                        hm.put("objectType", "narrower");
                        hm.put("action", "modify");
                        JSONArray oldNarrowerTmp = oldNarrower;
                        JSONArray newNarrowerTmp = newNarrower;
                        oldNarrowerTmp.removeAll(newNarrowerTmp);
                        for (Object item : oldNarrower) {
                            String tmp = (String) item;
                            hm.put("valueBefore", tmp);
                            hm.put("bidirectional", tmp);
                            hm.put("bidirectional-del", tmp);
                        }
                        for (Object item : newNarrower) {
                            String tmp = (String) item;
                            hm.put("valueAfter", tmp);
                            hm.put("bidirectional-add", tmp);
                        }
                    } else if (oldNarrower.size() > newNarrower.size()) { // deleted
                        hm.put("objectType", "narrower");
                        hm.put("action", "delete");
                        for (Object item : oldNarrower) {
                            if (!newNarrower.contains(item)) {
                                String tmp = (String) item;
                                hm.put("valueBefore", tmp);
                                hm.put("valueAfter", "");
                                hm.put("bidirectional", tmp);
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "narrower");
                        hm.put("action", "add");
                        for (Object item : newNarrower) {
                            if (!oldNarrower.contains(item)) {
                                String tmp = (String) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp);
                                hm.put("bidirectional", tmp);
                                break;
                            }
                        }
                    }
                }
            } else if (oldNarrower == null && newNarrower != null) { // value to empty
                for (Object item : newNarrower) {
                    String tmp = (String) item;
                    hm.put("objectType", "narrower");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp);
                    hm.put("bidirectional", tmp);
                }
            } else if (oldNarrower != null && newNarrower == null) { // empty to value
                for (Object item : oldNarrower) {
                    String tmp = (String) item;
                    hm.put("objectType", "narrower");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp);
                    hm.put("valueAfter", "");
                    hm.put("bidirectional", tmp);
                }
            }
            z = 1;
            // related
            if (oldRelated != null && newRelated != null) { // not empty
                if (!oldRelated.equals(newRelated)) {
                    if (oldRelated.size() == newRelated.size()) { // value changed
                        hm.put("objectType", "related");
                        hm.put("action", "modify");
                        JSONArray oldRelatedTmp = oldRelated;
                        JSONArray newRelatedTmp = newRelated;
                        oldRelatedTmp.removeAll(newRelatedTmp);
                        for (Object item : oldRelated) {
                            String tmp = (String) item;
                            hm.put("valueBefore", tmp);
                            hm.put("bidirectional", tmp);
                            hm.put("bidirectional-del", tmp);
                        }
                        for (Object item : newRelated) {
                            String tmp = (String) item;
                            hm.put("valueAfter", tmp);
                            hm.put("bidirectional-add", tmp);
                        }
                    } else if (oldRelated.size() > newRelated.size()) { // deleted
                        hm.put("objectType", "related");
                        hm.put("action", "delete");
                        for (Object item : oldRelated) {
                            if (!newRelated.contains(item)) {
                                String tmp = (String) item;
                                hm.put("valueBefore", tmp);
                                hm.put("valueAfter", "");
                                hm.put("bidirectional", tmp);
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "related");
                        hm.put("action", "add");
                        for (Object item : newRelated) {
                            if (!oldRelated.contains(item)) {
                                String tmp = (String) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp);
                                hm.put("bidirectional", tmp);
                                break;
                            }
                        }
                    }
                }
            } else if (oldRelated == null && newRelated != null) { // value to empty
                for (Object item : newRelated) {
                    String tmp = (String) item;
                    hm.put("objectType", "related");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp);
                    hm.put("bidirectional", tmp);
                }
            } else if (oldRelated != null && newRelated == null) { // empty to value
                for (Object item : oldRelated) {
                    String tmp = (String) item;
                    hm.put("objectType", "related");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp);
                    hm.put("valueAfter", "");
                    hm.put("bidirectional", tmp);
                }
            }
            z = 1;
            // broadMatch
            if (oldBroadMatch != null && newBroadMatch != null) { // not empty
                if (!oldBroadMatch.equals(newBroadMatch)) {
                    if (oldBroadMatch.size() == newBroadMatch.size()) { // value changed
                        hm.put("objectType", "broadMatch");
                        hm.put("action", "modify");
                        JSONArray oldBroadMatchTmp = oldBroadMatch;
                        JSONArray newBroadMatchTmp = newBroadMatch;
                        oldBroadMatchTmp.removeAll(newBroadMatchTmp);
                        for (Object item : oldBroadMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                        for (Object item : newBroadMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                    } else if (oldBroadMatch.size() > newBroadMatch.size()) { // deleted
                        hm.put("objectType", "broadMatch");
                        hm.put("action", "delete");
                        for (Object item : oldBroadMatch) {
                            if (!newBroadMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                hm.put("valueAfter", "");
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "broadMatch");
                        hm.put("action", "add");
                        for (Object item : newBroadMatch) {
                            if (!oldBroadMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                break;
                            }
                        }
                    }
                }
            } else if (oldBroadMatch == null && newBroadMatch != null) { // value to empty
                for (Object item : newBroadMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "broadMatch");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                }
            } else if (oldBroadMatch != null && newBroadMatch == null) { // empty to value
                for (Object item : oldBroadMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "broadMatch");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                    hm.put("valueAfter", "");
                }
            }
            z = 1;
            // narrowMatch
            if (oldNarrowMatch != null && newNarrowMatch != null) { // not empty
                if (!oldNarrowMatch.equals(newNarrowMatch)) {
                    if (oldNarrowMatch.size() == newNarrowMatch.size()) { // value changed
                        hm.put("objectType", "narrowMatch");
                        hm.put("action", "modify");
                        JSONArray oldNarrowMatchTmp = oldNarrowMatch;
                        JSONArray newNarrowMatchTmp = newNarrowMatch;
                        oldNarrowMatchTmp.removeAll(newNarrowMatchTmp);
                        for (Object item : oldNarrowMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                        for (Object item : newNarrowMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                    } else if (oldNarrowMatch.size() > newNarrowMatch.size()) { // deleted
                        hm.put("objectType", "narrowMatch");
                        hm.put("action", "delete");
                        for (Object item : oldNarrowMatch) {
                            if (!newNarrowMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                hm.put("valueAfter", "");
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "narrowMatch");
                        hm.put("action", "add");
                        for (Object item : newNarrowMatch) {
                            if (!oldNarrowMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                break;
                            }
                        }
                    }
                }
            } else if (oldNarrowMatch == null && newNarrowMatch != null) { // value to empty
                for (Object item : newNarrowMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "narrowMatch");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                }
            } else if (oldNarrowMatch != null && newNarrowMatch == null) { // empty to value
                for (Object item : oldNarrowMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "narrowMatch");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                    hm.put("valueAfter", "");
                }
            }
            z = 1;
            // relatedMatch
            if (oldRelatedMatch != null && newRelatedMatch != null) { // not empty
                if (!oldRelatedMatch.equals(newRelatedMatch)) {
                    if (oldRelatedMatch.size() == newRelatedMatch.size()) { // value changed
                        hm.put("objectType", "relatedMatch");
                        hm.put("action", "modify");
                        JSONArray oldRelatedMatchTmp = oldRelatedMatch;
                        JSONArray newRelatedMatchTmp = newRelatedMatch;
                        oldRelatedMatchTmp.removeAll(newRelatedMatchTmp);
                        for (Object item : oldRelatedMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                        for (Object item : newRelatedMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                    } else if (oldRelatedMatch.size() > newRelatedMatch.size()) { // deleted
                        hm.put("objectType", "relatedMatch");
                        hm.put("action", "delete");
                        for (Object item : oldRelatedMatch) {
                            if (!newRelatedMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                hm.put("valueAfter", "");
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "relatedMatch");
                        hm.put("action", "add");
                        for (Object item : newRelatedMatch) {
                            if (!oldRelatedMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                break;
                            }
                        }
                    }
                }
            } else if (oldRelatedMatch == null && newRelatedMatch != null) { // value to empty
                for (Object item : newRelatedMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "relatedMatch");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                }
            } else if (oldRelatedMatch != null && newRelatedMatch == null) { // empty to value
                for (Object item : oldRelatedMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "relatedMatch");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                    hm.put("valueAfter", "");
                }
            }
            z = 1;
            // closeMatch
            if (oldCloseMatch != null && newCloseMatch != null) { // not empty
                if (!oldCloseMatch.equals(newCloseMatch)) {
                    if (oldCloseMatch.size() == newCloseMatch.size()) { // value changed
                        hm.put("objectType", "closeMatch");
                        hm.put("action", "modify");
                        JSONArray oldCloseMatchTmp = oldCloseMatch;
                        JSONArray newCloseMatchTmp = newCloseMatch;
                        oldCloseMatchTmp.removeAll(newCloseMatchTmp);
                        for (Object item : oldCloseMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                        for (Object item : newCloseMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                    } else if (oldCloseMatch.size() > newCloseMatch.size()) { // deleted
                        hm.put("objectType", "closeMatch");
                        hm.put("action", "delete");
                        for (Object item : oldCloseMatch) {
                            if (!newCloseMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                hm.put("valueAfter", "");
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "closeMatch");
                        hm.put("action", "add");
                        for (Object item : newCloseMatch) {
                            if (!oldCloseMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                break;
                            }
                        }
                    }
                }
            } else if (oldCloseMatch == null && newCloseMatch != null) { // value to empty
                for (Object item : newCloseMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "closeMatch");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                }
            } else if (oldCloseMatch != null && newCloseMatch == null) { // empty to value
                for (Object item : oldCloseMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "closeMatch");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                    hm.put("valueAfter", "");
                }
            }
            z = 1;
            // exactMatch
            if (oldExactMatch != null && newExactMatch != null) { // not empty
                if (!oldExactMatch.equals(newExactMatch)) {
                    if (oldExactMatch.size() == newExactMatch.size()) { // value changed
                        hm.put("objectType", "exactMatch");
                        hm.put("action", "modify");
                        JSONArray oldExactMatchTmp = oldExactMatch;
                        JSONArray newExactMatchTmp = newExactMatch;
                        oldExactMatchTmp.removeAll(newExactMatchTmp);
                        for (Object item : oldExactMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                        for (Object item : newExactMatchTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                    } else if (oldExactMatch.size() > newExactMatch.size()) { // deleted
                        hm.put("objectType", "exactMatch");
                        hm.put("action", "delete");
                        for (Object item : oldExactMatch) {
                            if (!newExactMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                hm.put("valueAfter", "");
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "exactMatch");
                        hm.put("action", "add");
                        for (Object item : newExactMatch) {
                            if (!oldExactMatch.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                break;
                            }
                        }
                    }
                }
            } else if (oldExactMatch == null && newExactMatch != null) { // value to empty
                for (Object item : newExactMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "exactMatch");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                    String tmpUriSplit[] = tmp.get("uri").toString().split("/");
                    hm.put("bidirectional", tmpUriSplit[tmpUriSplit.length - 1]);
                }
            } else if (oldExactMatch != null && newExactMatch == null) { // empty to value
                for (Object item : oldExactMatch) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "exactMatch");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                    hm.put("valueAfter", "");
                    String tmpUriSplit[] = tmp.get("uri").toString().split("/");
                    hm.put("bidirectional", tmpUriSplit[tmpUriSplit.length - 1]);
                }
            }
            z = 1;
            // seeAlso
            if (oldSeeAlso != null && newSeeAlso != null) { // not empty
                if (!oldSeeAlso.equals(newSeeAlso)) {
                    if (oldSeeAlso.size() == newSeeAlso.size()) { // value changed
                        hm.put("objectType", "seeAlso");
                        hm.put("action", "modify");
                        JSONArray oldSeeAlsoTmp = oldSeeAlso;
                        JSONArray newSeeAlsoTmp = newSeeAlso;
                        oldSeeAlsoTmp.removeAll(newSeeAlsoTmp);
                        for (Object item : oldSeeAlsoTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                        for (Object item : newSeeAlsoTmp) {
                            JSONObject tmp = (JSONObject) item;
                            hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                        }
                    } else if (oldSeeAlso.size() > newSeeAlso.size()) { // deleted
                        hm.put("objectType", "seeAlso");
                        hm.put("action", "delete");
                        for (Object item : oldSeeAlso) {
                            if (!newSeeAlso.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                hm.put("valueAfter", "");
                                break;
                            }
                        }
                    } else { // added
                        hm.put("objectType", "seeAlso");
                        hm.put("action", "add");
                        for (Object item : newSeeAlso) {
                            if (!oldSeeAlso.contains(item)) {
                                JSONObject tmp = (JSONObject) item;
                                hm.put("valueBefore", "");
                                hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                                break;
                            }
                        }
                    }
                }
            } else if (oldSeeAlso == null && newSeeAlso != null) { // value to empty
                for (Object item : newSeeAlso) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "seeAlso");
                    hm.put("action", "add");
                    hm.put("valueBefore", "");
                    hm.put("valueAfter", tmp.get("uri") + " (" + tmp.get("type") + ")");
                }
            } else if (oldSeeAlso != null && newSeeAlso == null) { // empty to value
                for (Object item : oldSeeAlso) {
                    JSONObject tmp = (JSONObject) item;
                    hm.put("objectType", "seeAlso");
                    hm.put("action", "delete");
                    hm.put("valueBefore", tmp.get("uri") + " (" + tmp.get("type") + ")");
                    hm.put("valueAfter", "");
                }
            }
            z = 1;
            return hm;
        } catch (Exception e) {
            int errorLine = -1;
            for (StackTraceElement element : e.getStackTrace()) {
                errorLine = element.getLineNumber();
                if (element.getClassName().equals(Transformer.class.getName())) {
                    break;
                }
            }
            return hm;
        }
    }

}
