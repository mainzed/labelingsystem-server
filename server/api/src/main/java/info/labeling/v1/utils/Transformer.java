package info.labeling.v1.utils;

import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.exceptions.UniqueIdentifierException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Transformer {

    public static String vocabulary_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        // change id
        jsonObject.put(rdf.getPrefixItem("ls_voc" + ":" + id), jsonObject.remove("vocab"));
        JSONObject vocabularyObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_voc" + ":" + id));
        // for patch
        JSONArray flushArray = (JSONArray) vocabularyObject.get("flush");
        if (flushArray != null && !flushArray.isEmpty()) {
            return jsonObject.toJSONString();
        } else {
            // change title
            String[] titleStringArray = null;
            JSONArray titleArray = (JSONArray) vocabularyObject.get("title");
            if (titleArray != null && !titleArray.isEmpty()) {
                for (Object element : titleArray) {
                    String titleString = (String) element;
                    titleStringArray = titleString.split("@");
                }
                vocabularyObject.remove("title");
                JSONArray titleArrayNew = new JSONArray();
                JSONObject titleObject = new JSONObject();
                titleObject.put("type", "literal");
                titleObject.put("value", titleStringArray[0]);
                titleObject.put("lang", titleStringArray[1]);
                titleArrayNew.add(titleObject);
                vocabularyObject.put(rdf.getPrefixItem("dc:title"), titleArrayNew);
            }
            // change description
            String[] descriptionStringArray = null;
            JSONArray descriptionArray = (JSONArray) vocabularyObject.get("description");
            if (descriptionArray != null && !descriptionArray.isEmpty()) {
                for (Object element : descriptionArray) {
                    String descriptionString = (String) element;
                    descriptionStringArray = descriptionString.split("@");
                }
                vocabularyObject.remove("description");
                JSONArray descriptionArrayNew = new JSONArray();
                JSONObject descriptionObject = new JSONObject();
                descriptionObject.put("type", "literal");
                descriptionObject.put("value", descriptionStringArray[0]);
                descriptionObject.put("lang", descriptionStringArray[1]);
                descriptionArrayNew.add(descriptionObject);
                vocabularyObject.put(rdf.getPrefixItem("dc:description"), descriptionArrayNew);
            }
            // change retcats
            String retcatsString = null;
            JSONArray retcatsArray = (JSONArray) vocabularyObject.get("retcats");
            if (retcatsArray != null && !retcatsArray.isEmpty()) {
                for (Object element : retcatsArray) {
                    retcatsString = (String) element;
                }
                vocabularyObject.remove("retcats");
                JSONArray retcatsArrayNew = new JSONArray();
                JSONObject retcatsObject = new JSONObject();
                retcatsObject.put("type", "literal");
                retcatsObject.put("value", retcatsString);
                retcatsArrayNew.add(retcatsObject);
                vocabularyObject.put(rdf.getPrefixItem("ls:isRetcatsItem"), retcatsArrayNew);
            }
            // change releasetype
            String releaseString = null;
            JSONArray releaseArray = (JSONArray) vocabularyObject.get("releaseType");
            if (releaseArray != null && !releaseArray.isEmpty()) {
                for (Object element : releaseArray) {
                    releaseString = (String) element;
                }
                vocabularyObject.remove("releaseType");
                JSONArray releaseArrayNew = new JSONArray();
                JSONObject releaseObject = new JSONObject();
                releaseObject.put("type", "uri");
                if (releaseString.equals("hidden")) {
                    releaseObject.put("value", rdf.getPrefixItem("ls:Hidden"));
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
                vocabularyObject.remove("contributor");
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
            // change topConcept
            List<String> topConceptStringList = new ArrayList<String>();
            JSONArray topConceptArray = (JSONArray) vocabularyObject.get("topConcept");
            if (topConceptArray != null && !topConceptArray.isEmpty()) {
                for (Object element : topConceptArray) {
                    topConceptStringList.add((String) element);
                }
                vocabularyObject.remove("topConcept");
                JSONArray topConceptArrayNew = new JSONArray();
                for (String element : topConceptStringList) {
                    JSONObject topConceptObject = new JSONObject();
                    topConceptObject.put("type", "uri");
                    topConceptObject.put("value", element);
                    topConceptArrayNew.add(topConceptObject);
                }
                vocabularyObject.put(rdf.getPrefixItem("skos:hasTopConcept"), topConceptArrayNew);
            }
            // delete optional items
            vocabularyObject.remove(rdf.getPrefixItem("contributor"));
            vocabularyObject.remove(rdf.getPrefixItem("topConcept"));
            // delete not supported items
            vocabularyObject.remove(rdf.getPrefixItem("creator"));
            vocabularyObject.remove(rdf.getPrefixItem("created"));
            vocabularyObject.remove(rdf.getPrefixItem("modified"));
            vocabularyObject.remove(rdf.getPrefixItem("license"));
            vocabularyObject.remove(rdf.getPrefixItem("id"));
            vocabularyObject.remove(rdf.getPrefixItem("statusType"));
            return jsonObject.toJSONString();
        }
    }

    public static JSONObject vocabulary_GET(String json, String id, String fields) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value + "@" + lang);
                vocabularyObject.put(rdf.getPrefixItem("title"), arrayNew);
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value + "@" + lang);
                if (fields == null || fields.contains("description")) {
                    vocabularyObject.put(rdf.getPrefixItem("description"), arrayNew);
                }
            }
        }
        // change ls:isRetcatsItem
        JSONArray retcatsArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:isRetcatsItem"));
        if (retcatsArray != null && !retcatsArray.isEmpty()) {
            for (Object element : retcatsArray) {
                vocabularyObject.remove(rdf.getPrefixItem("ls:isRetcatsItem"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("retcats")) {
                    vocabularyObject.put(rdf.getPrefixItem("retcats"), arrayNew);
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
                if (value.contains("Hidden")) {
                    value = "hidden";
                } else {
                    value = "public";
                }
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("releaseType")) {
                    vocabularyObject.put(rdf.getPrefixItem("releaseType"), arrayNew);
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
                } else {
                    value = "deleted";
                }
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("statusType")) {
                    vocabularyObject.put(rdf.getPrefixItem("statusType"), arrayNew);
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                vocabularyObject.put(rdf.getPrefixItem("creator"), arrayNew);
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
            vocabularyObject.put(rdf.getPrefixItem("contributor"), arrayContributor);
        }
        // change dc:created
        JSONArray createdArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:created"));
        if (createdArray != null && !createdArray.isEmpty()) {
            for (Object element : createdArray) {
                vocabularyObject.remove(rdf.getPrefixItem("dc:created"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("created")) {
                    vocabularyObject.put(rdf.getPrefixItem("created"), arrayNew);
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("license")) {
                    vocabularyObject.put(rdf.getPrefixItem("license"), arrayNew);
                }
            }
        }
        // change dc:identifier
        JSONArray identifierArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:identifier"));
        for (Object element : identifierArray) {
            vocabularyObject.remove(rdf.getPrefixItem("dc:identifier"));
            JSONObject obj = (JSONObject) element;
            String value = (String) obj.get("value");
            JSONArray arrayNew = new JSONArray();
            arrayNew.add(value);
            vocabularyObject.put(rdf.getPrefixItem("id"), arrayNew);
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
            if (fields == null || fields.contains("topConcept")) {
                vocabularyObject.put(rdf.getPrefixItem("topConcept"), arrayTopConcept);
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
            if (fields == null || fields.contains("modified")) {
                vocabularyObject.put(rdf.getPrefixItem("modified"), arrayModify);
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
        return jsonObject;
    }

    public static JSONObject revision_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
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
            JSONArray arrayNew = new JSONArray();
            arrayNew.add(value);
            revisionObject.put(rdf.getPrefixItem("id"), arrayNew);
        }
        // change dc:creator
        JSONArray creatorArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("dc:creator"));
        if (creatorArray != null && !creatorArray.isEmpty()) {
            for (Object element : creatorArray) {
                revisionObject.remove(rdf.getPrefixItem("dc:creator"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                revisionObject.put(rdf.getPrefixItem("creator"), arrayNew);
            }
        }
        // change prov:startedAtTime
        JSONArray createdArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("prov:startedAtTime"));
        if (createdArray != null && !createdArray.isEmpty()) {
            for (Object element : createdArray) {
                revisionObject.remove(rdf.getPrefixItem("prov:startedAtTime"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                revisionObject.put(rdf.getPrefixItem("date"), arrayNew);
            }
        }
        // change dc:description
        JSONArray descriptionArray = (JSONArray) revisionObject.get(rdf.getPrefixItem("dc:description"));
        if (descriptionArray != null && !descriptionArray.isEmpty()) {
            for (Object element : descriptionArray) {
                revisionObject.remove(rdf.getPrefixItem("dc:description"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                revisionObject.put(rdf.getPrefixItem("type"), arrayNew);
            }
        }
        // delete items 
        revisionObject.remove(rdf.getPrefixItem("rdf:type"));
        revisionObject.remove(rdf.getPrefixItem("dct:creator"));
        revisionObject.remove(rdf.getPrefixItem("dct:type"));
        // return
        return jsonObject;
    }

    public static JSONObject empty_JSON(String item) throws IOException, UniqueIdentifierException, ParseException {
        JSONObject jsonObject = new JSONObject();
        JSONArray emptyArray = new JSONArray();
        jsonObject.put(item, emptyArray);
        return jsonObject;
    }

    public static String agent_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        // change id
        jsonObject.put(rdf.getPrefixItem("ls_age" + ":" + id), jsonObject.remove("agent"));
        JSONObject agentObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_age" + ":" + id));
        // for patch
        JSONArray flushArray = (JSONArray) agentObject.get("flush");
        if (flushArray != null && !flushArray.isEmpty()) {
            return jsonObject.toJSONString();
        } else {
            // change email
            String emailString = null;
            JSONArray emailArray = (JSONArray) agentObject.get("email");
            if (emailArray != null && !emailArray.isEmpty()) {
                for (Object element : emailArray) {
                    emailString = (String) element;
                }
                agentObject.remove("email");
                JSONArray emailArrayNew = new JSONArray();
                JSONObject emailObject = new JSONObject();
                emailObject.put("type", "uri");
                emailObject.put("value", "mailto:" + emailString);
                emailArrayNew.add(emailObject);
                agentObject.put(rdf.getPrefixItem("foaf:mbox"), emailArrayNew);
            }
            // change firstName
            String firstNameString = null;
            JSONArray firstNameArray = (JSONArray) agentObject.get("firstName");
            if (firstNameArray != null && !firstNameArray.isEmpty()) {
                for (Object element : firstNameArray) {
                    firstNameString = (String) element;
                }
                agentObject.remove("firstName");
                JSONArray firstNameArrayNew = new JSONArray();
                JSONObject firstNameObject = new JSONObject();
                firstNameObject.put("type", "literal");
                firstNameObject.put("value", firstNameString);
                firstNameArrayNew.add(firstNameObject);
                agentObject.put(rdf.getPrefixItem("foaf:firstName"), firstNameArrayNew);
            }
            // change lastName
            String lastNameString = null;
            JSONArray lastNameArray = (JSONArray) agentObject.get("lastName");
            if (lastNameArray != null && !lastNameArray.isEmpty()) {
                for (Object element : lastNameArray) {
                    lastNameString = (String) element;
                }
                agentObject.remove("lastName");
                JSONArray lastNameArrayNew = new JSONArray();
                JSONObject lastNameObject = new JSONObject();
                lastNameObject.put("type", "literal");
                lastNameObject.put("value", lastNameString);
                lastNameArrayNew.add(lastNameObject);
                agentObject.put(rdf.getPrefixItem("foaf:lastName"), lastNameArrayNew);
            }
            // change homepage
            String homepageString = null;
            JSONArray homepageArray = (JSONArray) agentObject.get("homepage");
            if (homepageArray != null && !homepageArray.isEmpty()) {
                for (Object element : homepageArray) {
                    homepageString = (String) element;
                }
                agentObject.remove("homepage");
                JSONArray homepageArrayNew = new JSONArray();
                JSONObject homepageObject = new JSONObject();
                homepageObject.put("type", "literal");
                homepageObject.put("value", homepageString);
                homepageArrayNew.add(homepageObject);
                agentObject.put(rdf.getPrefixItem("foaf:homepage"), homepageArrayNew);
            }
            // change img
            String imgString = null;
            JSONArray imgArray = (JSONArray) agentObject.get("img");
            if (imgArray != null && !imgArray.isEmpty()) {
                for (Object element : imgArray) {
                    imgString = (String) element;
                }
                agentObject.remove("img");
                JSONArray imgArrayNew = new JSONArray();
                JSONObject imgObject = new JSONObject();
                imgObject.put("type", "uri");
                imgObject.put("value", imgString);
                imgArrayNew.add(imgObject);
                agentObject.put(rdf.getPrefixItem("foaf:img"), imgArrayNew);
            }
            // change lat
            String latString = null;
            JSONArray latArray = (JSONArray) agentObject.get("lat");
            if (latArray != null && !latArray.isEmpty()) {
                for (Object element : latArray) {
                    latString = (String) element;
                }
                agentObject.remove("lat");
                JSONArray latArrayNew = new JSONArray();
                JSONObject latObject = new JSONObject();
                latObject.put("type", "literal");
                latObject.put("value", latString);
                latArrayNew.add(latObject);
                agentObject.put(rdf.getPrefixItem("geo:lat"), latArrayNew);
            }
            // change lon
            String lonString = null;
            JSONArray lonArray = (JSONArray) agentObject.get("lon");
            if (lonArray != null && !lonArray.isEmpty()) {
                for (Object element : lonArray) {
                    lonString = (String) element;
                }
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
            agentObject.remove(rdf.getPrefixItem("name"));
            agentObject.remove(rdf.getPrefixItem("email"));
            agentObject.remove(rdf.getPrefixItem("firstName"));
            agentObject.remove(rdf.getPrefixItem("lastName"));
            // delete optional items
            agentObject.remove(rdf.getPrefixItem("homepage"));
            agentObject.remove(rdf.getPrefixItem("img"));
            agentObject.remove(rdf.getPrefixItem("lat"));
            agentObject.remove(rdf.getPrefixItem("lon"));
            return jsonObject.toJSONString();
        }
    }

    public static JSONObject agent_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("id"), arrayNew);
            }
        }
        // change foaf:accountName
        JSONArray accountNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:accountName"));
        if (accountNameArray != null && !accountNameArray.isEmpty()) {
            for (Object element : accountNameArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:accountName"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("name"), arrayNew);
            }
        }
        // change foaf:mbox
        JSONArray mboxArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:mbox"));
        if (mboxArray != null && !mboxArray.isEmpty()) {
            for (Object element : mboxArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:mbox"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("email"), arrayNew);
            }
        }
        // change foaf:firstName
        JSONArray firstNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:firstName"));
        if (firstNameArray != null && !firstNameArray.isEmpty()) {
            for (Object element : firstNameArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:mbox"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("firstName"), arrayNew);
            }
        }
        // change foaf:firstName
        JSONArray lastNameArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:lastName"));
        if (lastNameArray != null && !lastNameArray.isEmpty()) {
            for (Object element : lastNameArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:lastName"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("lastName"), arrayNew);
            }
        }
        // change foaf:homepage
        JSONArray homepageArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:homepage"));
        if (homepageArray != null && !homepageArray.isEmpty()) {
            for (Object element : homepageArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:homepage"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("homepage"), arrayNew);
            }
        }
        // change foaf:img
        JSONArray imgArray = (JSONArray) agentObject.get(rdf.getPrefixItem("foaf:img"));
        if (imgArray != null && !imgArray.isEmpty()) {
            for (Object element : imgArray) {
                agentObject.remove(rdf.getPrefixItem("foaf:img"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("img"), arrayNew);
            }
        }
        // change geo:lat
        JSONArray latArray = (JSONArray) agentObject.get(rdf.getPrefixItem("geo:lat"));
        if (latArray != null && !latArray.isEmpty()) {
            for (Object element : latArray) {
                agentObject.remove(rdf.getPrefixItem("geo:lat"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("lat"), arrayNew);
            }
        }
        // change geo:lon
        JSONArray lonArray = (JSONArray) agentObject.get(rdf.getPrefixItem("geo:lon"));
        if (lonArray != null && !lonArray.isEmpty()) {
            for (Object element : lonArray) {
                agentObject.remove(rdf.getPrefixItem("geo:lon"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                agentObject.put(rdf.getPrefixItem("lon"), arrayNew);
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
        return jsonObject;
    }

    public static String label_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        // change id
        jsonObject.put(rdf.getPrefixItem("ls_lab" + ":" + id), jsonObject.remove("label"));
        JSONObject labelObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_lab" + ":" + id));
        // for patch
        JSONArray flushArray = (JSONArray) labelObject.get("flush");
        if (flushArray != null && !flushArray.isEmpty()) {
            return jsonObject.toJSONString();
        } else {
            // change prefLabel
            JSONArray prefLabelArray = (JSONArray) labelObject.get("prefLabel");
            List<String> prefLabelStringList = new ArrayList<String>();
            if (prefLabelArray != null && !prefLabelArray.isEmpty()) {
                for (Object element : prefLabelArray) {
                    prefLabelStringList.add((String) element);
                }
                labelObject.remove("prefLabel");
                JSONArray arrayNew = new JSONArray();
                for (String element : prefLabelStringList) {
                    String[] tmpStringArray = element.split("@");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", tmpStringArray[0]);
                    tmpObject.put("lang", tmpStringArray[1]);
                    arrayNew.add(tmpObject);
                }
                labelObject.put(rdf.getPrefixItem("skos:prefLabel"), arrayNew);
            }
            // change preferredLabel
            JSONArray preferredLabelArray = (JSONArray) labelObject.get("preferredLabel");
            if (preferredLabelArray != null && !preferredLabelArray.isEmpty()) {
                String[] preferredLabelStringArray = null;
                for (Object element : preferredLabelArray) {
                    String tmpString = (String) element;
                    preferredLabelStringArray = tmpString.split("@");
                }
                labelObject.remove("preferredLabel");
                JSONArray arrayNew = new JSONArray();
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "literal");
                tmpObject.put("value", preferredLabelStringArray[0]);
                tmpObject.put("lang", preferredLabelStringArray[1]);
                arrayNew.add(tmpObject);
                labelObject.put(rdf.getPrefixItem("ls:preferredLabel"), arrayNew);
            }
            // change preferredLabel
            JSONArray preferredLangArray = (JSONArray) labelObject.get("preferredLang");
            if (preferredLangArray != null && !preferredLangArray.isEmpty()) {
                String preferredLabelString = null;
                for (Object element : preferredLangArray) {
                    String tmpString = (String) element;
                    preferredLabelString = tmpString;
                }
                labelObject.remove("preferredLang");
                JSONArray arrayNew = new JSONArray();
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "literal");
                tmpObject.put("value", preferredLabelString);
                arrayNew.add(tmpObject);
                labelObject.put(rdf.getPrefixItem("ls:preferredLang"), arrayNew);
            }
            // change vocab
            JSONArray vocabArray = (JSONArray) labelObject.get("vocab");
            if (vocabArray != null && !vocabArray.isEmpty()) {
                String vocabString = null;
                for (Object element : vocabArray) {
                    String tmpString = (String) element;
                    vocabString = tmpString;
                }
                labelObject.remove("vocab");
                JSONArray arrayNew = new JSONArray();
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "uri");
                tmpObject.put("value", vocabString);
                arrayNew.add(tmpObject);
                labelObject.put(rdf.getPrefixItem("skos:inScheme"), arrayNew);
            }
            // change altLabel
            JSONArray altLabelArray = (JSONArray) labelObject.get("altLabel");
            List<String> altLabelStringList = new ArrayList<String>();
            if (altLabelArray != null && !altLabelArray.isEmpty()) {
                for (Object element : altLabelArray) {
                    altLabelStringList.add((String) element);
                }
                labelObject.remove("altLabel");
                JSONArray arrayNew = new JSONArray();
                for (String element : altLabelStringList) {
                    String[] tmpStringArray = element.split("@");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", tmpStringArray[0]);
                    tmpObject.put("lang", tmpStringArray[1]);
                    arrayNew.add(tmpObject);
                }
                labelObject.put(rdf.getPrefixItem("skos:altLabel"), arrayNew);
            }
            // change note
            JSONArray noteArray = (JSONArray) labelObject.get("note");
            List<String> noteStringList = new ArrayList<String>();
            if (noteArray != null && !noteArray.isEmpty()) {
                for (Object element : noteArray) {
                    noteStringList.add((String) element);
                }
                labelObject.remove("note");
                JSONArray arrayNew = new JSONArray();
                for (String element : noteStringList) {
                    String[] tmpStringArray = element.split("@");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", tmpStringArray[0]);
                    tmpObject.put("lang", tmpStringArray[1]);
                    arrayNew.add(tmpObject);
                }
                labelObject.put(rdf.getPrefixItem("skos:note"), arrayNew);
            }
            // change definition
            JSONArray definitionArray = (JSONArray) labelObject.get("definition");
            List<String> definitionStringList = new ArrayList<String>();
            if (definitionArray != null && !definitionArray.isEmpty()) {
                for (Object element : definitionArray) {
                    definitionStringList.add((String) element);
                }
                labelObject.remove("definition");
                JSONArray arrayNew = new JSONArray();
                for (String element : definitionStringList) {
                    String[] tmpStringArray = element.split("@");
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", tmpStringArray[0]);
                    tmpObject.put("lang", tmpStringArray[1]);
                    arrayNew.add(tmpObject);
                }
                labelObject.put(rdf.getPrefixItem("skos:definition"), arrayNew);
            }
            // change context
            JSONArray contextArray = (JSONArray) labelObject.get("context");
            if (contextArray != null && !contextArray.isEmpty()) {
                String contextString = null;
                for (Object element : contextArray) {
                    String tmpString = (String) element;
                    contextString = tmpString;
                }
                labelObject.remove("context");
                JSONArray arrayNew = new JSONArray();
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("type", "literal");
                tmpObject.put("value", contextString);
                arrayNew.add(tmpObject);
                labelObject.put(rdf.getPrefixItem("ls:hasContext"), arrayNew);
            }
            // change contributor
            List<String> contributorStringList = new ArrayList<String>();
            JSONArray contributorArray = (JSONArray) labelObject.get("contributor");
            if (contributorArray != null && !contributorArray.isEmpty()) {
                for (Object element : contributorArray) {
                    contributorStringList.add((String) element);
                }
                labelObject.remove("contributor");
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
                    tmpObject.put("value", element);
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
                    tmpObject.put("value", element);
                    arrayNew.add(tmpObject);
                    // narrower
                    JSONObject tmpLabelObject = new JSONObject();
                    JSONArray arrayNew2 = new JSONArray();
                    JSONObject tmpObject2 = new JSONObject();
                    tmpObject2.put("type", "uri");
                    tmpObject2.put("value", rdf.getPrefixItem("ls_lab" + ":" + id));
                    arrayNew2.add(tmpObject2);
                    tmpLabelObject.put(rdf.getPrefixItem("skos:narrower"), arrayNew2);
                    jsonObject.put(element, tmpLabelObject);
                }
                labelObject.put(rdf.getPrefixItem("skos:broader"), arrayNew);
            }
            // change narrower
            JSONArray narrowerArray = (JSONArray) labelObject.get("narrower");
            List<String> narrowerStringList = new ArrayList<String>();
            if (narrowerArray != null && !narrowerArray.isEmpty()) {
                for (Object element : narrowerArray) {
                    narrowerStringList.add((String) element);
                }
                labelObject.remove("narrower");
                JSONArray arrayNew = new JSONArray();
                for (String element : narrowerStringList) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "uri");
                    tmpObject.put("value", element);
                    arrayNew.add(tmpObject);
                    // broader
                    JSONObject tmpLabelObject = new JSONObject();
                    JSONArray arrayNew2 = new JSONArray();
                    JSONObject tmpObject2 = new JSONObject();
                    tmpObject2.put("type", "uri");
                    tmpObject2.put("value", rdf.getPrefixItem("ls_lab" + ":" + id));
                    arrayNew2.add(tmpObject2);
                    tmpLabelObject.put(rdf.getPrefixItem("skos:broader"), arrayNew2);
                    jsonObject.put(element, tmpLabelObject);
                }
                labelObject.put(rdf.getPrefixItem("skos:narrower"), arrayNew);
            }
            // change closeMatch
            JSONArray closeMatchArray = (JSONArray) labelObject.get("closeMatch");
            List<String> closeMatchStringList = new ArrayList<String>();
            if (closeMatchArray != null && !closeMatchArray.isEmpty()) {
                for (Object element : closeMatchArray) {
                    closeMatchStringList.add((String) element);
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
                    exactMatchStringList.add((String) element);
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
                    relatedMatchStringList.add((String) element);
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
                    narrowMatchStringList.add((String) element);
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
                    broadMatchStringList.add((String) element);
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
                    seeAlsoStringList.add((String) element);
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
            // change definedBy
            JSONArray definedByArray = (JSONArray) labelObject.get("definedBy");
            List<String> definedByStringList = new ArrayList<String>();
            if (definedByArray != null && !definedByArray.isEmpty()) {
                for (Object element : definedByArray) {
                    definedByStringList.add((String) element);
                }
                labelObject.remove("definedBy");
                JSONArray arrayNew = new JSONArray();
                for (String element : definedByStringList) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "uri");
                    tmpObject.put("value", element);
                    arrayNew.add(tmpObject);
                }
                labelObject.put(rdf.getPrefixItem("rdfs:isDefinedBy"), arrayNew);
            }
            // change definedBy
            JSONArray sameAsArray = (JSONArray) labelObject.get("sameAs");
            List<String> sameAsStringList = new ArrayList<String>();
            if (sameAsArray != null && !sameAsArray.isEmpty()) {
                for (Object element : sameAsArray) {
                    sameAsStringList.add((String) element);
                }
                labelObject.remove("sameAs");
                JSONArray arrayNew = new JSONArray();
                for (String element : sameAsStringList) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "uri");
                    tmpObject.put("value", element);
                    arrayNew.add(tmpObject);
                }
                labelObject.put(rdf.getPrefixItem("owl:sameAs"), arrayNew);
            }
            // delete items
            labelObject.remove(rdf.getPrefixItem("vocab"));
            labelObject.remove(rdf.getPrefixItem("creator"));
            labelObject.remove(rdf.getPrefixItem("contributor"));
            labelObject.remove(rdf.getPrefixItem("id"));
            labelObject.remove(rdf.getPrefixItem("license"));
            labelObject.remove(rdf.getPrefixItem("prefLabel"));
            labelObject.remove(rdf.getPrefixItem("altLabel"));
            labelObject.remove(rdf.getPrefixItem("note"));
            labelObject.remove(rdf.getPrefixItem("definition"));
            labelObject.remove(rdf.getPrefixItem("preferredLabel"));
            labelObject.remove(rdf.getPrefixItem("preferredLang"));
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
            labelObject.remove(rdf.getPrefixItem("definedBy"));
            labelObject.remove(rdf.getPrefixItem("sameAs"));
            labelObject.remove(rdf.getPrefixItem("created"));
            labelObject.remove(rdf.getPrefixItem("modified"));
            return jsonObject.toJSONString();
        }
    }

    public static JSONObject label_GET(String json, String id, String fields) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("vocab")) {
                    labelObject.put(rdf.getPrefixItem("vocab"), arrayNew);
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                labelObject.put(rdf.getPrefixItem("creator"), arrayNew);
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
            labelObject.put(rdf.getPrefixItem("contributor"), arrayNew);
        }
        // change dc:identifier
        JSONArray identifierArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:identifier"));
        if (identifierArray != null && !identifierArray.isEmpty()) {
            for (Object element : identifierArray) {
                labelObject.remove(rdf.getPrefixItem("dc:identifier"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                labelObject.put(rdf.getPrefixItem("id"), arrayNew);
            }
        }
        // change dct:license
        JSONArray licenseArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dct:license"));
        if (licenseArray != null && !licenseArray.isEmpty()) {
            for (Object element : licenseArray) {
                labelObject.remove(rdf.getPrefixItem("dct:license"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("license")) {
                    labelObject.put(rdf.getPrefixItem("license"), arrayNew);
                }
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
                arrayNew.add(value + "@" + lang);
            }
            labelObject.put(rdf.getPrefixItem("prefLabel"), arrayNew);
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
                arrayNew.add(value + "@" + lang);
            }
            if (fields == null || fields.contains("altLabel")) {
                labelObject.put(rdf.getPrefixItem("altLabel"), arrayNew);
            }
        }
        // change skos:note
        JSONArray noteArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:note"));
        if (noteArray != null && !noteArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            labelObject.remove(rdf.getPrefixItem("skos:note"));
            for (Object element : noteArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                arrayNew.add(value + "@" + lang);
            }
            if (fields == null || fields.contains("note")) {
                labelObject.put(rdf.getPrefixItem("note"), arrayNew);
            }
        }
        // change skos:definition
        JSONArray definitionArray = (JSONArray) labelObject.get(rdf.getPrefixItem("skos:definition"));
        if (definitionArray != null && !definitionArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            labelObject.remove(rdf.getPrefixItem("skos:definition"));
            for (Object element : definitionArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                arrayNew.add(value + "@" + lang);
            }
            if (fields == null || fields.contains("definition")) {
                labelObject.put(rdf.getPrefixItem("definition"), arrayNew);
            }
        }
        // change ls:preferredLabel
        JSONArray preferredLabelArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:preferredLabel"));
        if (preferredLabelArray != null && !preferredLabelArray.isEmpty()) {
            for (Object element : preferredLabelArray) {
                labelObject.remove(rdf.getPrefixItem("ls:preferredLabel"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                String lang = (String) obj.get("lang");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value + "@" + lang);
                if (fields == null || fields.contains("preferredLabel")) {
                    labelObject.put(rdf.getPrefixItem("preferredLabel"), arrayNew);
                }
            }
        }
        // change ls:preferredLang
        JSONArray preferredLangArray = (JSONArray) labelObject.get(rdf.getPrefixItem("ls:preferredLang"));
        if (preferredLangArray != null && !preferredLangArray.isEmpty()) {
            for (Object element : preferredLangArray) {
                labelObject.remove(rdf.getPrefixItem("ls:preferredLang"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("preferredLang")) {
                    labelObject.put(rdf.getPrefixItem("preferredLang"), arrayNew);
                }
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
                } else {
                    value = "deleted";
                }
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("statusType")) {
                    labelObject.put(rdf.getPrefixItem("statusType"), arrayNew);
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
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("context")) {
                    labelObject.put(rdf.getPrefixItem("context"), arrayNew);
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
                arrayNew.add(value);
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
                arrayNew.add(value);
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
                arrayNew.add(value);
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
                arrayNew.add(value);
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
                arrayNew.add(value);
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
                arrayNew.add(value);
            }
            if (fields == null || fields.contains("seeAlso")) {
                labelObject.put(rdf.getPrefixItem("seeAlso"), arrayNew);
            }
        }
        // change rdfs:isDefinedBy
        JSONArray definedByArray = (JSONArray) labelObject.get(rdf.getPrefixItem("rdfs:isDefinedBy"));
        if (definedByArray != null && !definedByArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            labelObject.remove(rdf.getPrefixItem("rdfs:isDefinedBy"));
            for (Object element : definedByArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayNew.add(value);
            }
            if (fields == null || fields.contains("definedBy")) {
                labelObject.put(rdf.getPrefixItem("definedBy"), arrayNew);
            }
        }
        // change owl:sameAs
        JSONArray sameAsArray = (JSONArray) labelObject.get(rdf.getPrefixItem("owl:sameAs"));
        if (sameAsArray != null && !sameAsArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            labelObject.remove(rdf.getPrefixItem("owl:sameAs"));
            for (Object element : sameAsArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayNew.add(value);
            }
            if (fields == null || fields.contains("sameAs")) {
                labelObject.put(rdf.getPrefixItem("sameAs"), arrayNew);
            }
        }
        // change dc:created
        JSONArray createdArray = (JSONArray) labelObject.get(rdf.getPrefixItem("dc:created"));
        if (createdArray != null && !createdArray.isEmpty()) {
            for (Object element : createdArray) {
                labelObject.remove(rdf.getPrefixItem("dc:created"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                if (fields == null || fields.contains("created")) {
                    labelObject.put(rdf.getPrefixItem("created"), arrayNew);
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
            if (fields == null || fields.contains("modified")) {
                labelObject.put(rdf.getPrefixItem("modified"), arrayModify);
            }
        }
        // delete items
        labelObject.remove(rdf.getPrefixItem("dct:creator"));
        labelObject.remove(rdf.getPrefixItem("dct:contributor"));
        labelObject.remove(rdf.getPrefixItem("rdf:type"));
        labelObject.remove(rdf.getPrefixItem("skos:changeNote"));
        labelObject.remove(rdf.getPrefixItem("ls:sameAs"));
        // return
        return jsonObject;
    }
	
	public static String retcat_POST(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        // change id
        jsonObject.put(rdf.getPrefixItem("ls_ret" + ":" + id), jsonObject.remove("retcat"));
        JSONObject retcatObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_ret" + ":" + id));
        // for patch
        JSONArray flushArray = (JSONArray) retcatObject.get("flush");
        if (flushArray != null && !flushArray.isEmpty()) {
            return jsonObject.toJSONString();
        } else {
			// change description
            JSONArray descriptionArray = (JSONArray) retcatObject.get("description");
            if (descriptionArray != null && !descriptionArray.isEmpty()) {
                String newString = null;
				for (Object element : descriptionArray) {
                    newString = (String) element;
                }
                retcatObject.remove("description");
                JSONArray arrayNew = new JSONArray();
                JSONObject objectNew = new JSONObject();
                objectNew.put("type", "literal");
                objectNew.put("value", newString);
                arrayNew.add(objectNew);
                retcatObject.put(rdf.getPrefixItem("dc:description"), arrayNew);
            }
			// change creator
            JSONArray creatorArray = (JSONArray) retcatObject.get("creator");
            if (creatorArray != null && !creatorArray.isEmpty()) {
                String newString = null;
				for (Object element : creatorArray) {
                    newString = (String) element;
                }
                retcatObject.remove("description");
                JSONArray arrayNew = new JSONArray();
                JSONObject objectNew = new JSONObject();
                objectNew.put("type", "uri");
                objectNew.put("value", newString);
                arrayNew.add(objectNew);
                retcatObject.put(rdf.getPrefixItem("dct:publisher"), arrayNew);
            }
			// change theme
            JSONArray themeArray = (JSONArray) retcatObject.get("theme");
            if (themeArray != null && !themeArray.isEmpty()) {
                List<String> themeStringList = new ArrayList<String>();
				for (Object element : themeArray) {
                    themeStringList.add((String) element);
                }
                retcatObject.remove("theme");
                JSONArray arrayNew = new JSONArray();
                for (String element : themeStringList) {
                    JSONObject tmpObject = new JSONObject();
                    tmpObject.put("type", "literal");
                    tmpObject.put("value", element);
                    arrayNew.add(tmpObject);
                }
                retcatObject.put(rdf.getPrefixItem("dcat:theme"), arrayNew);
            }
			// change license
            JSONArray licenseArray = (JSONArray) retcatObject.get("license");
            if (licenseArray != null && !licenseArray.isEmpty()) {
                String newString = null;
				for (Object element : licenseArray) {
                    newString = (String) element;
                }
                retcatObject.remove("license");
                JSONArray arrayNew = new JSONArray();
                JSONObject objectNew = new JSONObject();
                objectNew.put("type", "uri");
                objectNew.put("value", newString);
                arrayNew.add(objectNew);
                retcatObject.put(rdf.getPrefixItem("dct:license"), arrayNew);
            }
			// change endpoint
            JSONArray endpointArray = (JSONArray) retcatObject.get("endpoint");
            if (endpointArray != null && !endpointArray.isEmpty()) {
                String newString = null;
				for (Object element : endpointArray) {
                    newString = (String) element;
                }
                retcatObject.remove("endpoint");
                JSONArray arrayNew = new JSONArray();
                JSONObject objectNew = new JSONObject();
                objectNew.put("type", "uri");
                objectNew.put("value", newString);
                arrayNew.add(objectNew);
                retcatObject.put(rdf.getPrefixItem("dcat:accessURL"), arrayNew);
            }
            // change query
            JSONArray queryArray = (JSONArray) retcatObject.get("query");
            if (queryArray != null && !queryArray.isEmpty()) {
                String newString = null;
				for (Object element : queryArray) {
                    newString = (String) element;
                }
                retcatObject.remove("query");
                JSONArray arrayNew = new JSONArray();
                JSONObject objectNew = new JSONObject();
                objectNew.put("type", "literal");
                objectNew.put("value", newString);
                arrayNew.add(objectNew);
                retcatObject.put(rdf.getPrefixItem("ls:retcatsquery"), arrayNew);
            }
			// change var
            JSONArray varArray = (JSONArray) retcatObject.get("var");
            if (varArray != null && !varArray.isEmpty()) {
                String newString = null;
				for (Object element : varArray) {
                    newString = (String) element;
                }
                retcatObject.remove("var");
                JSONArray arrayNew = new JSONArray();
                JSONObject objectNew = new JSONObject();
                objectNew.put("type", "literal");
                objectNew.put("value", newString);
                arrayNew.add(objectNew);
                retcatObject.put(rdf.getPrefixItem("ls:retcatsvar"), arrayNew);
            }
            // delete items
            retcatObject.remove(rdf.getPrefixItem("id"));
            retcatObject.remove(rdf.getPrefixItem("title"));
            retcatObject.remove(rdf.getPrefixItem("description"));
            retcatObject.remove(rdf.getPrefixItem("creator"));
            retcatObject.remove(rdf.getPrefixItem("theme"));
            retcatObject.remove(rdf.getPrefixItem("license"));
            retcatObject.remove(rdf.getPrefixItem("endpoint"));
            retcatObject.remove(rdf.getPrefixItem("query"));
            retcatObject.remove(rdf.getPrefixItem("var"));
            return jsonObject.toJSONString();
        }
    }
	
	public static JSONObject retcat_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
        //init
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        // parse json
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        jsonObject.put("retcat", jsonObject.remove(rdf.getPrefixItem("ls_ret" + ":" + id)));
        // get items
        JSONObject retcatObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("retcat"));
        // change dc:identifier
        JSONArray identifierArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dc:identifier"));
        if (identifierArray != null && !identifierArray.isEmpty()) {
            for (Object element : identifierArray) {
                retcatObject.remove(rdf.getPrefixItem("dc:identifier"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("id"), arrayNew);
            }
        }
        // change dc:title
        JSONArray titleArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dc:title"));
        if (titleArray != null && !titleArray.isEmpty()) {
            for (Object element : titleArray) {
                retcatObject.remove(rdf.getPrefixItem("dc:title"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("title"), arrayNew);
            }
        }
        // change dc:description
        JSONArray descriptionArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dc:description"));
        if (descriptionArray != null && !descriptionArray.isEmpty()) {
            for (Object element : descriptionArray) {
                retcatObject.remove(rdf.getPrefixItem("dc:description"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("description"), arrayNew);
            }
        }
        // change dct:publisher
        JSONArray publisherArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dct:publisher"));
        if (publisherArray != null && !publisherArray.isEmpty()) {
            for (Object element : publisherArray) {
                retcatObject.remove(rdf.getPrefixItem("dct:publisher"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("creator"), arrayNew);
            }
        }
		// change dcat:theme
        JSONArray themeArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dcat:theme"));
        if (themeArray != null && !themeArray.isEmpty()) {
            JSONArray arrayNew = new JSONArray();
            retcatObject.remove(rdf.getPrefixItem("dcat:theme"));
            for (Object element : themeArray) {
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                arrayNew.add(value);
            }
			retcatObject.put(rdf.getPrefixItem("theme"), arrayNew);
        }
        // change dct:license
        JSONArray licenseArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dct:license"));
        if (licenseArray != null && !licenseArray.isEmpty()) {
            for (Object element : licenseArray) {
                retcatObject.remove(rdf.getPrefixItem("dct:license"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("license"), arrayNew);
            }
        }
        // change dcat:accessURL
        JSONArray endpointArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("dcat:accessURL"));
        if (endpointArray != null && !endpointArray.isEmpty()) {
            for (Object element : endpointArray) {
                retcatObject.remove(rdf.getPrefixItem("dcat:accessURL"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("endpoint"), arrayNew);
            }
        }
        // change ls:retcatsquery
        JSONArray queryArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("ls:retcatsquery"));
        if (queryArray != null && !queryArray.isEmpty()) {
            for (Object element : queryArray) {
                retcatObject.remove(rdf.getPrefixItem("ls:retcatsquery"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("query"), arrayNew);
            }
        }
        // change ls:retcatsvar
        JSONArray varArray = (JSONArray) retcatObject.get(rdf.getPrefixItem("ls:retcatsvar"));
        if (varArray != null && !varArray.isEmpty()) {
            for (Object element : varArray) {
                retcatObject.remove(rdf.getPrefixItem("ls:retcatsvar"));
                JSONObject obj = (JSONObject) element;
                String value = (String) obj.get("value");
                JSONArray arrayNew = new JSONArray();
                arrayNew.add(value);
                retcatObject.put(rdf.getPrefixItem("var"), arrayNew);
            }
        }
        // delete items
        retcatObject.remove(rdf.getPrefixItem("rdf:type"));
        retcatObject.remove(rdf.getPrefixItem("ls:sameAs"));
        retcatObject.remove(rdf.getPrefixItem("dc:identifier"));
        retcatObject.remove(rdf.getPrefixItem("foaf:accountName"));
        retcatObject.remove(rdf.getPrefixItem("foaf:mbox"));
        retcatObject.remove(rdf.getPrefixItem("foaf:firstName"));
        retcatObject.remove(rdf.getPrefixItem("foaf:lastName"));
        retcatObject.remove(rdf.getPrefixItem("foaf:homepage"));
        retcatObject.remove(rdf.getPrefixItem("foaf:img"));
        retcatObject.remove(rdf.getPrefixItem("geo:lat"));
        retcatObject.remove(rdf.getPrefixItem("geo:lon"));
        // return
        return jsonObject;
    }

}
