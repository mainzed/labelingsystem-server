package v1.utils.transformer;

import v1.utils.config.ConfigProperties;
import rdf.RDF;
import exceptions.ResourceNotAvailableException;
import exceptions.RevisionTypeException;
import exceptions.SesameSparqlException;
import exceptions.UniqueIdentifierException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import v1.utils.retcat.RetcatItem;

public class Transformer {

    public static String vocabulary_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject rdfObject = new JSONObject();
        JSONObject vocabularyObject = (JSONObject) new JSONParser().parse(json);
        // for patch
        JSONArray flushArray = (JSONArray) vocabularyObject.get("flush");
        if (flushArray != null && !flushArray.isEmpty()) {
            return vocabularyObject.toJSONString();
        } else {
            // change title
            JSONObject titleArray = (JSONObject) vocabularyObject.get("title");
            if (titleArray != null && !titleArray.isEmpty()) {
                vocabularyObject.remove("title");
                JSONArray titleArrayNew = new JSONArray();
                JSONObject titleObject = new JSONObject();
                titleObject.put("type", "literal");
                titleObject.put("value", titleArray.get("value"));
                titleObject.put("lang", titleArray.get("lang"));
                titleArrayNew.add(titleObject);
                vocabularyObject.put(rdf.getPrefixItem("dc:title"), titleArrayNew);
            }
            // change description
            JSONObject descriptionArray = (JSONObject) vocabularyObject.get("description");
            if (descriptionArray != null && !descriptionArray.isEmpty()) {
                vocabularyObject.remove("description");
                JSONArray descriptionArrayNew = new JSONArray();
                JSONObject descriptionObject = new JSONObject();
                descriptionObject.put("type", "literal");
                descriptionObject.put("value", descriptionArray.get("value"));
                descriptionObject.put("lang", descriptionArray.get("lang"));
                descriptionArrayNew.add(descriptionObject);
                vocabularyObject.put(rdf.getPrefixItem("dc:description"), descriptionArrayNew);
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
            // change theme
            List<String> themeStringList = new ArrayList<String>();
            JSONArray themeArray = (JSONArray) vocabularyObject.get("theme");
            if (themeArray != null && !themeArray.isEmpty()) {
                for (Object element : themeArray) {
                    themeStringList.add((String) element);
                }
                vocabularyObject.remove("theme");
                JSONArray themeArrayNew = new JSONArray();
                for (String element : themeStringList) {
                    JSONObject themeObject = new JSONObject();
                    themeObject.put("type", "uri");
                    themeObject.put("value", element);
                    themeArrayNew.add(themeObject);
                }
                vocabularyObject.put(rdf.getPrefixItem("dcat:theme"), themeArrayNew);
            }
            // change contributor
            List<String> contributorStringList = new ArrayList<String>();
            JSONArray contributorArray = (JSONArray) vocabularyObject.get("contributor");
            if (contributorArray != null && !contributorArray.isEmpty()) {
                for (Object element : contributorArray) {
                    contributorStringList.add((String) element);
                }
                vocabularyObject.remove("contributors");
                JSONArray contributorArrayNew = new JSONArray();
                JSONArray contributorArrayNew2 = new JSONArray();
                for (String element : contributorStringList) {
                    JSONObject contributorObject = new JSONObject();
                    contributorObject.put("type", "literal");
                    contributorObject.put("value", element);
                    contributorArrayNew.add(contributorObject);
                    JSONObject contributorObject2 = new JSONObject();
                    contributorObject2.put("type", "uri");
                    contributorObject2.put("value", rdf.getPrefixItem("ls_age:" + element));
                    contributorArrayNew2.add(contributorObject2);
                }
                vocabularyObject.put(rdf.getPrefixItem("dc:contributor"), contributorArrayNew);
                vocabularyObject.put(rdf.getPrefixItem("dct:contributor"), contributorArrayNew2);
            }
            // delete optional items
            vocabularyObject.remove(rdf.getPrefixItem("contributors"));
            vocabularyObject.remove(rdf.getPrefixItem("topConcept"));
            // delete not supported items
            vocabularyObject.remove(rdf.getPrefixItem("theme"));
            vocabularyObject.remove(rdf.getPrefixItem("creator"));
            vocabularyObject.remove(rdf.getPrefixItem("created"));
            vocabularyObject.remove(rdf.getPrefixItem("modifications"));
            vocabularyObject.remove(rdf.getPrefixItem("license"));
            vocabularyObject.remove(rdf.getPrefixItem("id"));
            vocabularyObject.remove("revisionIDs");
            vocabularyObject.remove("statusType");
            // add object
            rdfObject.put(rdf.getPrefixItem("ls_voc" + ":" + id), vocabularyObject);
            return rdfObject.toJSONString();
        }
    }

    public static JSONObject vocabulary_GET(String json, String id, String fields) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        jsonObject.put("vocab", jsonObject.remove(rdf.getPrefixItem("ls_voc" + ":" + id)));
        // get items
        JSONObject vocabularyObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("vocab"));
        // change dc:title
        JSONArray titleArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:title"));
        if (titleArray != null && !titleArray.isEmpty()) {
            for (Object element : titleArray) {
                vocabularyObject.remove(rdf.getPrefixItem("dc:title"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                JSONObject objTmp = new JSONObject();
                objTmp.put("value", value);
                objTmp.put("lang", lang);
                vocabularyObject.put(rdf.getPrefixItem("title"), objTmp);
            }
        }
        // change dct:description
        JSONArray descriptionArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:description"));
        if (descriptionArray != null && !descriptionArray.isEmpty()) {
            for (Object element : descriptionArray) {
                vocabularyObject.remove(rdf.getPrefixItem("dc:description"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                JSONObject objTmp = new JSONObject();
                objTmp.put("value", value);
                objTmp.put("lang", lang);
                if (fields == null || fields.contains("description")) {
                    vocabularyObject.put(rdf.getPrefixItem("description"), objTmp);
                }
            }
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
                    vocabularyObject.put(rdf.getPrefixItem("releaseType"), value);
                }
            }
        }
        // change ls:hasStatusType
        JSONArray statusTypeArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:hasStatusType"));
        if (statusTypeArray != null && !statusTypeArray.isEmpty()) {
            for (Object element : statusTypeArray) {
                vocabularyObject.remove(rdf.getPrefixItem("ls:hasStatusType"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                if (value.contains("Active")) {
                    value = "active";
                } else if (value.contains("Deprecated")) {
                    value = "deprecated";
                } else {
                    value = "deleted";
                }
                if (fields == null || fields.contains("statusType")) {
                    vocabularyObject.put(rdf.getPrefixItem("statusType"), value);
                }
            }
        }
        // change dcat:theme
        JSONArray themeArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dcat:theme"));
        if (themeArray != null && !themeArray.isEmpty()) {
            vocabularyObject.remove(rdf.getPrefixItem("dcat:theme"));
            JSONArray arrayTheme = new JSONArray();
            for (Object element : themeArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayTheme.add(value);
            }
            if (fields == null || fields.contains("theme")) {
                vocabularyObject.put(rdf.getPrefixItem("theme"), arrayTheme);
            }
        }
        // change dc:creator
        JSONArray creatorArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:creator"));
        if (creatorArray != null && !creatorArray.isEmpty()) {
            for (Object element : creatorArray) {
                vocabularyObject.remove(rdf.getPrefixItem("dc:creator"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                vocabularyObject.put(rdf.getPrefixItem("creator"), value);
            }
        }
        // change dc:contributor
        JSONArray contributorArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:contributor"));
        if (contributorArray != null && !contributorArray.isEmpty()) {
            vocabularyObject.remove(rdf.getPrefixItem("dc:contributor"));
            JSONArray arrayContributor = new JSONArray();
            for (Object element : contributorArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayContributor.add(value);
            }
            vocabularyObject.put(rdf.getPrefixItem("contributors"), arrayContributor);
        }
        // change dc:created
        JSONArray createdArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:created"));
        if (createdArray != null && !createdArray.isEmpty()) {
            for (Object element : createdArray) {
                vocabularyObject.remove(rdf.getPrefixItem("dc:created"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                if (fields == null || fields.contains("created")) {
                    vocabularyObject.put(rdf.getPrefixItem("created"), value);
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
                    vocabularyObject.put(rdf.getPrefixItem("license"), value);
                }
            }
        }
        // change dc:identifier
        JSONArray identifierArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:identifier"));
        for (Object element : identifierArray) {
            vocabularyObject.remove(rdf.getPrefixItem("dc:identifier"));
            JSONObject obj = (JSONObject) element;
            String value = (String) obj.get("value");
            vocabularyObject.put(rdf.getPrefixItem("id"), value);
        }
        // OPTIONAL VALUES
        // change skos:hasTopConcept
        JSONArray topConceptArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("skos:hasTopConcept"));
        if (topConceptArray != null && !topConceptArray.isEmpty()) {
            vocabularyObject.remove(rdf.getPrefixItem("skos:hasTopConcept"));
            JSONArray arrayTopConcept = new JSONArray();
            for (Object element : topConceptArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayTopConcept.add(value);
            }
            if (fields == null || fields.contains("topConcepts")) {
                vocabularyObject.put(rdf.getPrefixItem("topConcepts"), arrayTopConcept);
            }
        }
        // change dc:modified
        JSONArray modifiedArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:modified"));
        if (modifiedArray != null && !modifiedArray.isEmpty()) {
            vocabularyObject.remove(rdf.getPrefixItem("dc:modified"));
            JSONArray arrayModify = new JSONArray();
            for (Object element : modifiedArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayModify.add(value);
            }
            if (fields == null || fields.contains("modifications")) {
                vocabularyObject.put(rdf.getPrefixItem("modifications"), arrayModify);
            }
        }
        // change skos:changeNote
        JSONArray revisionsArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("skos:changeNote"));
        if (revisionsArray != null && !revisionsArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            for (Object element : revisionsArray) {
                vocabularyObject.remove(rdf.getPrefixItem("skos:changeNote"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                if (value.contains("revision/")) {
                    arrayNew.add(value.split("revision/")[1]);
                } else {
                    arrayNew.add(value);
                }
            }
            if (fields == null || fields.contains("revisionIDs")) {
                vocabularyObject.put(rdf.getPrefixItem("revisionIDs"), arrayNew);
            }
        }
        // delete items
        vocabularyObject.remove(rdf.getPrefixItem("dc:identifier"));
        vocabularyObject.remove(rdf.getPrefixItem("dct:creator"));
        vocabularyObject.remove(rdf.getPrefixItem("dct:contributor"));
        vocabularyObject.remove(rdf.getPrefixItem("rdf:type"));
        vocabularyObject.remove(rdf.getPrefixItem("skos:changeNote"));
        vocabularyObject.remove(rdf.getPrefixItem("ls:sameAs"));
        // return
        return vocabularyObject;
    }

    public static JSONObject revision_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        jsonObject.put("revision", jsonObject.remove(rdf.getPrefixItem("ls_rev" + ":" + id)));
        // get items
        JSONObject revisionObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("revision"));
        // change dc:identifier
        JSONArray identifierArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("dc:identifier"));
        for (Object element : identifierArray) {
            revisionObject.remove(rdf.getPrefixItem("dc:identifier"));
            JSONObject obj = (JSONObject) element;
            String value = (String) obj.get("value");
            revisionObject.put(rdf.getPrefixItem("id"), value);
        }
        // change dc:creator
        JSONArray creatorArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("dc:creator"));
        if (creatorArray != null && !creatorArray.isEmpty()) {
            for (Object element : creatorArray) {
                revisionObject.remove(rdf.getPrefixItem("dc:creator"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                revisionObject.put(rdf.getPrefixItem("creator"), value);
            }
        }
        // change prov:startedAtTime
        JSONArray createdArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("prov:startedAtTime"));
        if (createdArray != null && !createdArray.isEmpty()) {
            for (Object element : createdArray) {
                revisionObject.remove(rdf.getPrefixItem("prov:startedAtTime"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                revisionObject.put(rdf.getPrefixItem("date"), value);
            }
        }
        // change dc:description
        JSONArray descriptionArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("dct:description"));
        if (descriptionArray != null && !descriptionArray.isEmpty()) {
            for (Object element : descriptionArray) {
                revisionObject.remove(rdf.getPrefixItem("dct:description"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                revisionObject.put(rdf.getPrefixItem("type"), value);
            }
        }
        // delete items 
        revisionObject.remove(rdf.getPrefixItem("rdf:type"));
        revisionObject.remove(rdf.getPrefixItem("dct:creator"));
        revisionObject.remove(rdf.getPrefixItem("dct:type"));
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
        // for patch
        JSONArray flushArray = (JSONArray) agentObject.get("flush");
        if (flushArray != null && !flushArray.isEmpty()) {
            return agentObject.toJSONString();
        } else {
            // change email
            String emailString = (String) agentObject.get("email");
            if (emailString != null && !emailString.isEmpty()) {
                agentObject.remove("email");
                JSONArray emailArrayNew = new JSONArray();
                JSONObject emailObject = new JSONObject();
                emailObject.put("type", "uri");
                emailObject.put("value", emailString);
                emailArrayNew.add(emailObject);
                agentObject.put(rdf.getPrefixItem("foaf:mbox"), emailArrayNew);
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
            // change homepage
            String homepageString = (String) agentObject.get("homepage");
            if (homepageString != null && !homepageString.isEmpty()) {
                agentObject.remove("homepage");
                JSONArray homepageArrayNew = new JSONArray();
                JSONObject homepageObject = new JSONObject();
                homepageObject.put("type", "literal");
                homepageObject.put("value", homepageString);
                homepageArrayNew.add(homepageObject);
                agentObject.put(rdf.getPrefixItem("foaf:homepage"), homepageArrayNew);
            }
            // change img
            String imgString = (String) agentObject.get("img");
            if (imgString != null && !imgString.isEmpty()) {
                agentObject.remove("img");
                JSONArray imgArrayNew = new JSONArray();
                JSONObject imgObject = new JSONObject();
                imgObject.put("type", "uri");
                imgObject.put("value", imgString);
                imgArrayNew.add(imgObject);
                agentObject.put(rdf.getPrefixItem("foaf:img"), imgArrayNew);
            }
            // change lat
            Double latString = (Double) agentObject.get("lat");
            if (latString != null) {
                agentObject.remove("lat");
                JSONArray latArrayNew = new JSONArray();
                JSONObject latObject = new JSONObject();
                latObject.put("type", "literal");
                latObject.put("value", latString);
                latArrayNew.add(latObject);
                agentObject.put(rdf.getPrefixItem("geo:lat"), latArrayNew);
            }
            // change lon
            Double lonString = (Double) agentObject.get("lon");
            if (lonString != null) {
                agentObject.remove("lon");
                JSONArray lonArrayNew = new JSONArray();
                JSONObject lonObject = new JSONObject();
                lonObject.put("type", "literal");
                lonObject.put("value", lonString);
                lonArrayNew.add(lonObject);
                agentObject.put(rdf.getPrefixItem("geo:lon"), lonArrayNew);
            }
            // delete items
            agentObject.remove(rdf.getPrefixItem("id"));
            agentObject.remove(rdf.getPrefixItem("group"));
            agentObject.remove(rdf.getPrefixItem("name"));
            agentObject.remove(rdf.getPrefixItem("email"));
            agentObject.remove(rdf.getPrefixItem("firstName"));
            agentObject.remove(rdf.getPrefixItem("lastName"));
            // delete optional items
            agentObject.remove(rdf.getPrefixItem("homepage"));
            agentObject.remove(rdf.getPrefixItem("img"));
            agentObject.remove(rdf.getPrefixItem("lat"));
            agentObject.remove(rdf.getPrefixItem("lon"));
            // add object
            rdfObject.put(rdf.getPrefixItem("ls_age" + ":" + id), agentObject);
            return rdfObject.toJSONString();
        }
    }

    public static JSONObject agent_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        jsonObject.put("agent", jsonObject.remove(rdf.getPrefixItem("ls_age" + ":" + id)));
        // get items
        JSONObject agentObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("agent"));
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
        // change ls:inGroup
        JSONArray groupArray = (JSONArray) agentObject.get(rdf.getPrefixItem("ls:inGroup"));
        if (groupArray != null && !groupArray.isEmpty()) {
            for (Object element : groupArray) {
                agentObject.remove(rdf.getPrefixItem("ls:inGroup"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                agentObject.put(rdf.getPrefixItem("group"), value);
            }
        }
        // change foaf:accountName
        JSONArray accountNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:accountName"));
        if (accountNameArray != null && !accountNameArray.isEmpty()) {
            for (Object element : accountNameArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:accountName"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                agentObject.put(rdf.getPrefixItem("name"), value);
            }
        }
        // change foaf:mbox
        JSONArray mboxArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:mbox"));
        if (mboxArray != null && !mboxArray.isEmpty()) {
            for (Object element : mboxArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:mbox"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                agentObject.put(rdf.getPrefixItem("email"), value);
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
        // change foaf:firstName
        JSONArray lastNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:lastName"));
        if (lastNameArray != null && !lastNameArray.isEmpty()) {
            for (Object element : lastNameArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:lastName"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                agentObject.put(rdf.getPrefixItem("lastName"), value);
            }
        }
        // change foaf:homepage
        JSONArray homepageArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:homepage"));
        if (homepageArray != null && !homepageArray.isEmpty()) {
            for (Object element : homepageArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:homepage"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                agentObject.put(rdf.getPrefixItem("homepage"), value);
            }
        }
        // change foaf:img
        JSONArray imgArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:img"));
        if (imgArray != null && !imgArray.isEmpty()) {
            for (Object element : imgArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:img"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                agentObject.put(rdf.getPrefixItem("img"), value);
            }
        }
        // change geo:lat
        JSONArray latArray = (JSONArray) agentObject.get(rdf.getPrefixItem("geo:lat"));
        if (latArray != null && !latArray.isEmpty()) {
            for (Object element : latArray) {
                agentObject.remove(rdf.getPrefixItem("geo:lat"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                Double valueDbl = Double.parseDouble(value);
                agentObject.put(rdf.getPrefixItem("lat"), valueDbl);
            }
        }
        // change geo:lon
        JSONArray lonArray = (JSONArray) agentObject.get(rdf.getPrefixItem("geo:lon"));
        if (lonArray != null && !lonArray.isEmpty()) {
            for (Object element : lonArray) {
                agentObject.remove(rdf.getPrefixItem("geo:lon"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                Double valueDbl = Double.parseDouble(value);
                agentObject.put(rdf.getPrefixItem("lon"), valueDbl);
            }
        }
        // delete items
        agentObject.remove(rdf.getPrefixItem("rdf:type"));
        agentObject.remove(rdf.getPrefixItem("ls:sameAs"));
        agentObject.remove(rdf.getPrefixItem("dc:identifier"));
        agentObject.remove(rdf.getPrefixItem("foaf:accountName"));
        agentObject.remove(rdf.getPrefixItem("foaf:mbox"));
        agentObject.remove(rdf.getPrefixItem("foaf:firstName"));
        agentObject.remove(rdf.getPrefixItem("foaf:lastName"));
        agentObject.remove(rdf.getPrefixItem("foaf:homepage"));
        agentObject.remove(rdf.getPrefixItem("foaf:img"));
        agentObject.remove(rdf.getPrefixItem("geo:lat"));
        agentObject.remove(rdf.getPrefixItem("geo:lon"));
        // return
        return agentObject;
    }

    public static String label_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject rdfObject = new JSONObject();
        JSONObject labelObject = (JSONObject) new JSONParser().parse(json);
        // for patch
        JSONArray flushArray = (JSONArray) labelObject.get("flush");
        if (flushArray != null && !flushArray.isEmpty()) {
            return labelObject.toJSONString();
        } else {
            // change prefLabel
            JSONArray prefLabelArray = (JSONArray) labelObject.get("prefLabels");
            if (prefLabelArray != null && !prefLabelArray.isEmpty()) {
                labelObject.remove("prefLabels");
                JSONArray arrayNew = new JSONArray();
                for (Object element : prefLabelArray) {
                    JSONObject thisObject = (JSONObject) element;
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", thisObject.get("value"));
                    tmpObject.put("lang", thisObject.get("lang"));
                    arrayNew.add(tmpObject);
                    Boolean thumbnail = (Boolean) thisObject.get("isThumbnail");
                    if (thumbnail) {
                        JSONArray tmpArray = new JSONArray();
                        JSONObject tmpObject2 = new JSONObject();
                        tmpObject2.put("type", "literal");
                        tmpObject2.put("value", thisObject.get("value"));
                        tmpObject2.put("lang", thisObject.get("lang"));
                        tmpArray.add(tmpObject2);
                        labelObject.put(rdf.getPrefixItem("ls:preferredLabel"), tmpArray);
                    }
                }
                labelObject.put(rdf.getPrefixItem("skos:prefLabel"), arrayNew);
            }
            // change vocabID
            String vocabArray = (String) labelObject.get("vocabID");
            if (vocabArray != null && !vocabArray.isEmpty()) {
                labelObject.remove("vocabID");
                JSONArray arrayNew = new JSONArray();
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", rdf.getPrefixItem("ls_voc:" + vocabArray));
                arrayNew.add(tmpObject);
                labelObject.put(rdf.getPrefixItem("skos:inScheme"), arrayNew);
            }
            // change altLabel
            JSONArray altLabelArray = (JSONArray) labelObject.get("altLabels");
            if (altLabelArray != null && !altLabelArray.isEmpty()) {
                labelObject.remove("altLabels");
                JSONArray arrayNew = new JSONArray();
                for (Object element : altLabelArray) {
                    JSONObject thisObject = (JSONObject) element;
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", thisObject.get("value"));
                    tmpObject.put("lang", thisObject.get("lang"));
                    arrayNew.add(tmpObject);
                }
                labelObject.put(rdf.getPrefixItem("skos:altLabel"), arrayNew);
            }
            // change scopeNote
            JSONObject scopeNoteArray = (JSONObject) labelObject.get("scopeNote");
            if (scopeNoteArray != null && !scopeNoteArray.isEmpty()) {
                labelObject.remove("scopeNote");
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "literal");
                tmpObject.put("value", scopeNoteArray.get("value"));
                tmpObject.put("lang", scopeNoteArray.get("lang"));
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(tmpObject);
                labelObject.put(rdf.getPrefixItem("skos:scopeNote"), arrayNew);
            }
            // change context
            String contextArray = (String) labelObject.get("context");
            if (contextArray != null && !contextArray.isEmpty()) {
                labelObject.remove("context");
                JSONArray arrayNew = new JSONArray();
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "literal");
                tmpObject.put("value", contextArray);
                arrayNew.add(tmpObject);
                labelObject.put(rdf.getPrefixItem("ls:hasContext"), arrayNew);
            }
            // change contributor
            List<String> contributorStringList = new ArrayList<String>();
            JSONArray contributorArray = (JSONArray) labelObject.get("contributors");
            if (contributorArray != null && !contributorArray.isEmpty()) {
                for (Object element : contributorArray) {
                    contributorStringList.add((String) element);
                }
                labelObject.remove("contributors");
                JSONArray arrayNew = new JSONArray();
                JSONArray arrayNew2 = new JSONArray();
                for (String element : contributorStringList) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", element);
                    arrayNew.add(tmpObject);
                    JSONObject tmpObject2 = new JSONObject();
                    tmpObject2.put("type", "uri");
                    tmpObject2.put("value", rdf.getPrefixItem("ls_age:" + element));
                    arrayNew2.add(tmpObject2);
                }
                labelObject.put(rdf.getPrefixItem("dc:contributor"), arrayNew);
                labelObject.put(rdf.getPrefixItem("dct:contributor"), arrayNew2);
            }
            // change related
            JSONArray relatedArray = (JSONArray) labelObject.get("related");
            List<String> relatedStringList = new ArrayList<String>();
            if (relatedArray != null && !relatedArray.isEmpty()) {
                for (Object element : relatedArray) {
                    relatedStringList.add((String) element);
                }
                labelObject.remove("related");
                JSONArray arrayNew = new JSONArray();
                for (String element : relatedStringList) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "uri");
                    tmpObject.put("value", rdf.getPrefixItem("ls_lab:" + element));
                    arrayNew.add(tmpObject);
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
                    closeMatchStringList.add((String) tmpjson.get("url"));
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
                    exactMatchStringList.add((String) tmpjson.get("url"));
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
                    relatedMatchStringList.add((String) tmpjson.get("url"));
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
                    narrowMatchStringList.add((String) tmpjson.get("url"));
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
                    broadMatchStringList.add((String) tmpjson.get("url"));
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
                    seeAlsoStringList.add((String) tmpjson.get("url"));
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
            labelObject.remove(rdf.getPrefixItem("vocabID"));
            labelObject.remove(rdf.getPrefixItem("creator"));
            labelObject.remove(rdf.getPrefixItem("contributors"));
            labelObject.remove(rdf.getPrefixItem("id"));
            labelObject.remove(rdf.getPrefixItem("license"));
            labelObject.remove(rdf.getPrefixItem("prefLabels"));
            labelObject.remove(rdf.getPrefixItem("altLabels"));
            labelObject.remove(rdf.getPrefixItem("statusType"));
            labelObject.remove(rdf.getPrefixItem("context"));
            labelObject.remove(rdf.getPrefixItem("related"));
            labelObject.remove(rdf.getPrefixItem("broader"));
            labelObject.remove(rdf.getPrefixItem("narrower"));
            labelObject.remove(rdf.getPrefixItem("closeMatch"));
            labelObject.remove(rdf.getPrefixItem("exactMatch"));
            labelObject.remove(rdf.getPrefixItem("relatedMatch"));
            labelObject.remove(rdf.getPrefixItem("narrowMatch"));
            labelObject.remove(rdf.getPrefixItem("broadMatch"));
            labelObject.remove(rdf.getPrefixItem("seeAlso"));
            labelObject.remove(rdf.getPrefixItem("created"));
            labelObject.remove(rdf.getPrefixItem("modifications"));
            labelObject.remove(rdf.getPrefixItem("revisionIDs"));
            // add object
            rdfObject.put(rdf.getPrefixItem("ls_lab" + ":" + id), labelObject);
            return rdfObject.toJSONString();
        }
    }

    public static JSONObject label_GET(String json, String id, String fields, List<RetcatItem> retcatlist) throws IOException, UniqueIdentifierException, ParseException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
        //init
        RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        jsonObject.put("label", jsonObject.remove(rdf.getPrefixItem("ls_lab" + ":" + id)));
        // get items
        JSONObject labelObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("label"));
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
        // change dc:contrubutor
        JSONArray contributorArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:contributor"));
        if (contributorArray != null && !contributorArray.isEmpty()) {
            labelObject.remove(rdf.getPrefixItem("dc:contributor"));
            JSONArray arrayNew = new JSONArray();
            for (Object element : contributorArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayNew.add(value);
            }
            labelObject.put(rdf.getPrefixItem("contributors"), arrayNew);
        }
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
        // change dct:license
        JSONArray licenseArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dct:license"));
        if (licenseArray != null && !licenseArray.isEmpty()) {
            for (Object element : licenseArray) {
                labelObject.remove(rdf.getPrefixItem("dct:license"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                if (fields == null || fields.contains("license")) {
                    labelObject.put(rdf.getPrefixItem("license"), value);
                }
            }
        }
        // get thumbnail
        JSONArray preferredLabelArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:preferredLabel"));
        String thumbnail = "";
        if (preferredLabelArray != null && !preferredLabelArray.isEmpty()) {
            for (Object element : preferredLabelArray) {
                labelObject.remove(rdf.getPrefixItem("ls:preferredLabel"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                thumbnail = value + "@" + lang;
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
                if (thumbnail.equals(value + "@" + lang)) {
                    objTmp.put("isThumbnail", true);
                } else {
                    objTmp.put("isThumbnail", false);
                }
                arrayNew.add(objTmp);
            }
            labelObject.put(rdf.getPrefixItem("prefLabels"), arrayNew);
        }
        // change skos:altLabel
        JSONArray altLabelArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:altLabel"));
        if (altLabelArray != null && !altLabelArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            labelObject.remove(rdf.getPrefixItem("skos:altLabel"));
            for (Object element : altLabelArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                JSONObject objTmp = new JSONObject();
                objTmp.put("value", value);
                objTmp.put("lang", lang);
                arrayNew.add(objTmp);
            }
            labelObject.put(rdf.getPrefixItem("altLabels"), arrayNew);
        }
        // change skos:scopeNote
        JSONArray scopeNoteArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:scopeNote"));
        if (scopeNoteArray != null && !scopeNoteArray.isEmpty()) {
            labelObject.remove(rdf.getPrefixItem("skos:scopeNote"));
            JSONObject objTmp = new JSONObject();
            for (Object element : scopeNoteArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                objTmp.put("value", value);
                objTmp.put("lang", lang);
            }
            if (fields == null || fields.contains("scopeNote")) {
                labelObject.put(rdf.getPrefixItem("scopeNote"), objTmp);
            }
        }
        // change ls:hasStatusType
        JSONArray statusTypeArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:hasStatusType"));
        if (statusTypeArray != null && !statusTypeArray.isEmpty()) {
            for (Object element : statusTypeArray) {
                labelObject.remove(rdf.getPrefixItem("ls:hasStatusType"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                if (value.contains("Active")) {
                    value = "active";
                } else if (value.contains("Deprecated")) {
                    value = "deprecated";
                } else {
                    value = "deleted";
                }
                if (fields == null || fields.contains("statusType")) {
                    labelObject.put(rdf.getPrefixItem("statusType"), value);
                }
            }
        }
        // change ls:context
        JSONArray contextArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:hasContext"));
        if (contextArray != null && !contextArray.isEmpty()) {
            for (Object element : contextArray) {
                labelObject.remove(rdf.getPrefixItem("ls:hasContext"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                if (fields == null || fields.contains("context")) {
                    labelObject.put(rdf.getPrefixItem("context"), value);
                }
            }
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
                tmpObject.put("url", value);
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
        if (exactMatchArray != null && !exactMatchArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            labelObject.remove(rdf.getPrefixItem("skos:exactMatch"));
            for (Object element : exactMatchArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("url", value);
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
                tmpObject.put("url", value);
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
                tmpObject.put("url", value);
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
                tmpObject.put("url", value);
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
                tmpObject.put("url", value);
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
                }
            }
        }
        // change dc:modified
        JSONArray modifiedArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:modified"));
        if (modifiedArray != null && !modifiedArray.isEmpty()) {
            labelObject.remove(rdf.getPrefixItem("dc:modified"));
            JSONArray arrayModify = new JSONArray();
            for (Object element : modifiedArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayModify.add(value);
            }
            if (fields == null || fields.contains("modifications")) {
                labelObject.put(rdf.getPrefixItem("modifications"), arrayModify);
            }
        }
        // change skos:changeNote
        JSONArray revisionsArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:changeNote"));
        if (revisionsArray != null && !revisionsArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            for (Object element : revisionsArray) {
                labelObject.remove(rdf.getPrefixItem("skos:changeNote"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                if (value.contains("revision/")) {
                    arrayNew.add(value.split("revision/")[1]);
                } else {
                    arrayNew.add(value);
                }
            }
            if (fields == null || fields.contains("revisionIDs")) {
                labelObject.put(rdf.getPrefixItem("revisionIDs"), arrayNew);
            }
        }
        // delete items
        labelObject.remove(rdf.getPrefixItem("dct:creator"));
        labelObject.remove(rdf.getPrefixItem("dct:contributor"));
        labelObject.remove(rdf.getPrefixItem("rdf:type"));
        labelObject.remove(rdf.getPrefixItem("skos:changeNote"));
        labelObject.remove(rdf.getPrefixItem("ls:sameAs"));
        // return
        return labelObject;
    }

    public static String vocabularyDifference(String json_old, String json_new) throws ParseException, RevisionTypeException {
        String revisionTypes = "";
        try {
            JSONObject oldObject = (JSONObject) new JSONParser().parse(json_old);
            JSONObject newObject = (JSONObject) new JSONParser().parse(json_new);
            /*
        SUPPORTED:
        title{value,lang} (DescriptionRevision)
        description{value,lang} (DescriptionRevision)
        releaseType (SystemRevision)
        TODO:
        contributor[value] (ShareRevision)
        theme[value] (DescriptionRevision)
             */
            // title [mendatory]
            JSONObject oldTitle = (JSONObject) oldObject.get("title");
            String oldTitleValue = (String) oldTitle.get("value");
            String oldTitleLang = (String) oldTitle.get("lang");
            JSONObject newTitle = (JSONObject) newObject.get("title");
            if (newTitle != null) {
                String newTitleValue = (String) newTitle.get("value");
                String newTitleLang = (String) newTitle.get("lang");
                if (!oldTitleValue.equals(newTitleValue) || !oldTitleLang.equals(newTitleLang)) {
                    revisionTypes += "DescriptionRevision,";
                }
            }
            // description [mendatory]
            JSONObject oldDescription = (JSONObject) oldObject.get("description");
            String oldDescriptionValue = (String) oldDescription.get("value");
            String oldDescriptionLang = (String) oldDescription.get("lang");
            JSONObject newDescription = (JSONObject) newObject.get("description");
            if (newDescription != null) {
                String newDescriptionValue = (String) newDescription.get("value");
                String newDescriptionLang = (String) newDescription.get("lang");
                if (!oldDescriptionValue.equals(newDescriptionValue) || !oldDescriptionLang.equals(newDescriptionLang)) {
                    revisionTypes += "DescriptionRevision,";
                }
            }
            // releaseType [mendatory]
            String oldReleaseType = (String) oldObject.get("releaseType");
            String newReleaseType = (String) newObject.get("releaseType");
            if (newReleaseType != null) {
                if (!oldReleaseType.equals(newReleaseType)) {
                    revisionTypes += "SystemRevision,";
                }
            }
        } catch (Exception e) {
            throw new RevisionTypeException(e.toString());
        }
        return revisionTypes.substring(0, revisionTypes.length() - 1);
    }

    public static String labelDifference(String json_old, String json_new) throws ParseException, RevisionTypeException {

        String revisionTypes = "";
        try {
            JSONObject oldObject = (JSONObject) new JSONParser().parse(json_old);
            JSONObject newObject = (JSONObject) new JSONParser().parse(json_new);
            /*
        SUPPORTED:
        prefLabels[{lang,value,isThumbnail}] (DescriptionRevision)
        altLabels[{lang,value}] (DescriptionRevision)
        scopeNote{lang,value} (DescriptionRevision)
        broader[id] (LinkingRevision)
        narrower[id] (LinkingRevision)
        related[id] (LinkingRevision)
        ...
        TODO:
        preferredLabel (SystemRevision)
        context[value] (SystemRevision)
        contributor[value] (ShareRevision)
             */
            // scopeNote [optional]
            JSONObject oldScopeNote = (JSONObject) oldObject.get("scopeNote");
            String oldScopeNoteValue = "";
            String oldScopeNoteLang = "";
            if (oldScopeNote != null) {
                oldScopeNoteValue = (String) oldScopeNote.get("value");
                oldScopeNoteLang = (String) oldScopeNote.get("lang");
            }
            JSONObject newScopeNote = (JSONObject) newObject.get("scopeNote");
            String newScopeNoteValue = "";
            String newScopeNoteLang = "";
            if (oldScopeNote != null) {
                newScopeNoteValue = (String) newScopeNote.get("value");
                newScopeNoteLang = (String) newScopeNote.get("lang");
            }
            if (oldScopeNote != null && newScopeNote != null) {
                if (!oldScopeNoteValue.equals(newScopeNoteValue) || !oldScopeNoteLang.equals(newScopeNoteLang)) {
                    revisionTypes += "DescriptionRevision,";
                }
            } else if (oldScopeNote == null && newScopeNote != null) {
                revisionTypes += "DescriptionRevision,";
            } else if (oldScopeNote != null && newScopeNote == null) {
                revisionTypes += "DescriptionRevision,";
            }
            // prefLabel [mendatory]
            JSONArray oldPrefLabel = (JSONArray) oldObject.get("prefLabels");
            JSONArray newPrefLabel = (JSONArray) newObject.get("prefLabels");
            // prefLabel value
            String oldPrefLabels = "";
            for (Object item : oldPrefLabel) {
                JSONObject tmpObject = (JSONObject) item;
                oldPrefLabels = tmpObject.get("value") + ",";
            }
            List<String> newPrefLabels = new ArrayList();
            for (Object item : newPrefLabel) {
                JSONObject tmpObject = (JSONObject) item;
                newPrefLabels.add((String) tmpObject.get("value"));
            }
            // prefLabel count and text value
            if (oldPrefLabel.size() != newPrefLabel.size()) {
                revisionTypes += "DescriptionRevision,";
            } else {
                for (String item : newPrefLabels) {
                    if (!oldPrefLabels.contains(item)) {
                        revisionTypes += "DescriptionRevision,";
                    }
                }
            }
            // altLabel [optional]
            JSONArray oldAltLabel = (JSONArray) oldObject.get("altLabels");
            JSONArray newAltLabel = (JSONArray) newObject.get("altLabels");
            // altLabel value
            String oldAltLabels = "";
            List<String> newAltLabels = new ArrayList();
            if (oldAltLabel != null && newAltLabel != null) {
                for (Object item : oldAltLabel) {
                    JSONObject tmpObject = (JSONObject) item;
                    oldAltLabels = tmpObject.get("value") + ",";
                }
                for (Object item : newAltLabel) {
                    JSONObject tmpObject = (JSONObject) item;
                    newAltLabels.add((String) tmpObject.get("value"));
                }
            } else if (oldAltLabel == null && newAltLabel != null) {
                revisionTypes += "DescriptionRevision,";
            } else if (oldAltLabel != null && newAltLabel == null) {
                revisionTypes += "DescriptionRevision,";
            }
            // altLabel count and text value
            if (oldAltLabel != null && newAltLabel != null) {
                if (oldAltLabel.size() != newAltLabel.size()) {
                    revisionTypes += "DescriptionRevision,";
                } else {
                    for (String item : newAltLabels) {
                        if (!oldAltLabels.contains(item)) {
                            revisionTypes += "DescriptionRevision,";
                        }
                    }
                }
            }
            // relations
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
            // set revisions using size attribute
            if (oldBroader != null && newBroader != null) {
                if (oldBroader.size() != newBroader.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldBroader == null && newBroader != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldBroader != null && newBroader == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldNarrower != null && newNarrower != null) {
                if (oldNarrower.size() != newNarrower.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldNarrower == null && newNarrower != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldNarrower != null && newNarrower == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldRelated != null && newRelated != null) {
                if (oldRelated.size() != newRelated.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldRelated == null && newRelated != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldRelated != null && newRelated == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldBroadMatch != null && newBroadMatch != null) {
                if (oldBroadMatch.size() != newBroadMatch.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldBroadMatch == null && newBroadMatch != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldBroadMatch != null && newBroadMatch == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldNarrowMatch != null && newNarrowMatch != null) {
                if (oldNarrowMatch.size() != newNarrowMatch.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldNarrowMatch == null && newNarrowMatch != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldNarrowMatch != null && newNarrowMatch == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldRelatedMatch != null && newRelatedMatch != null) {
                if (oldRelatedMatch.size() != newRelatedMatch.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldRelatedMatch == null && newRelatedMatch != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldRelatedMatch != null && newRelatedMatch == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldCloseMatch != null && newCloseMatch != null) {
                if (oldCloseMatch.size() != newCloseMatch.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldCloseMatch == null && newCloseMatch != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldCloseMatch != null && newCloseMatch == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldExactMatch != null && newExactMatch != null) {
                if (oldExactMatch.size() != newExactMatch.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldExactMatch == null && newExactMatch != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldExactMatch != null && newExactMatch == null) {
                revisionTypes += "LinkingRevision,";
            }
            if (oldSeeAlso != null && newSeeAlso != null) {
                if (oldSeeAlso.size() != newSeeAlso.size()) {
                    revisionTypes += "LinkingRevision,";
                }
            } else if (oldSeeAlso == null && newSeeAlso != null) {
                revisionTypes += "LinkingRevision,";
            } else if (oldSeeAlso != null && newSeeAlso == null) {
                revisionTypes += "LinkingRevision,";
            }
        } catch (Exception e) {
            throw new RevisionTypeException(e.toString());
        }
        // all revision types
        return revisionTypes.substring(0, revisionTypes.length() - 1);
    }

}
