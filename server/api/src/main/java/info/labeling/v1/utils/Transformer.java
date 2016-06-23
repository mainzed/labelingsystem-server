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

	public static JSONObject vocabulary_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
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
				vocabularyObject.put(rdf.getPrefixItem("description"), arrayNew);
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
				vocabularyObject.put(rdf.getPrefixItem("retcats"), arrayNew);
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
				vocabularyObject.put(rdf.getPrefixItem("releaseType"), arrayNew);
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
				vocabularyObject.put(rdf.getPrefixItem("statusType"), arrayNew);
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
			vocabularyObject.put(rdf.getPrefixItem("theme"), arrayTheme);
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
				vocabularyObject.put(rdf.getPrefixItem("created"), arrayNew);
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
				vocabularyObject.put(rdf.getPrefixItem("license"), arrayNew);
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
			vocabularyObject.put(rdf.getPrefixItem("topConcept"), arrayTopConcept);
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
			vocabularyObject.put(rdf.getPrefixItem("modified"), arrayModify);
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

}
