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
		jsonObject.put(rdf.getPrefixItem("ls_voc" + ":" + id), jsonObject.remove("vocabulary"));
		JSONObject vocabularyObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("ls_voc" + ":" + id));
		// change title
		String[] titleStringArray = null;
		JSONArray titleArray = (JSONArray) vocabularyObject.get("title");
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
		// change description
		String[] descriptionStringArray = null;
		JSONArray descriptionArray = (JSONArray) vocabularyObject.get("description");
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
		vocabularyObject.put(rdf.getPrefixItem("dct:description"), descriptionArrayNew);
		// change retcats
		String retcatsString = null;
		JSONArray retcatsArray = (JSONArray) vocabularyObject.get("retcats");
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
		// change releasetype
		String releaseString = null;
		JSONArray releaseArray = (JSONArray) vocabularyObject.get("releaseType");
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
		// change theme
		List<String> themeStringList = new ArrayList<String>();
		JSONArray themeArray = (JSONArray) vocabularyObject.get("theme");
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
		return jsonObject.toJSONString();
	}
	
	public static JSONObject vocabulary_GET(String json, String id) throws IOException, UniqueIdentifierException, ParseException {
		//init
		RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
		// parse json
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
		jsonObject.put("vocabulary", jsonObject.remove(rdf.getPrefixItem("ls_voc" + ":" + id)));
		// get items
		JSONObject vocabularyObject = (JSONObject) jsonObject.get(rdf.getPrefixItem("vocabulary"));
		// change dc:title
		JSONArray titleArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:title"));
		for (Object element : titleArray) {
			vocabularyObject.remove(rdf.getPrefixItem("dc:title"));
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			String lang = (String) obj.get("lang");
			JSONArray arrayNew = new JSONArray();
			arrayNew.add(value+"@"+lang);
			vocabularyObject.put(rdf.getPrefixItem("title"), arrayNew);
		}
		// change dct:description
		JSONArray descriptionArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dct:description"));
		for (Object element : descriptionArray) {
			vocabularyObject.remove(rdf.getPrefixItem("dct:description"));
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			String lang = (String) obj.get("lang");
			JSONArray arrayNew = new JSONArray();
			arrayNew.add(value+"@"+lang);
			vocabularyObject.put(rdf.getPrefixItem("description"), arrayNew);
		}
		// change ls:isRetcatsItem
		JSONArray retcatsArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:isRetcatsItem"));
		for (Object element : retcatsArray) {
			vocabularyObject.remove(rdf.getPrefixItem("ls:isRetcatsItem"));
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			JSONArray arrayNew = new JSONArray();
			arrayNew.add(value);
			vocabularyObject.put(rdf.getPrefixItem("retcats"), arrayNew);
		}
		// change ls:hasReleaseType
		JSONArray releaseTypeArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:hasReleaseType"));
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
		// change dcat:theme
		JSONArray themeArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dcat:theme"));
		vocabularyObject.remove(rdf.getPrefixItem("dcat:theme"));
		JSONArray arrayTheme = new JSONArray();
		for (Object element : themeArray) {
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			arrayTheme.add(value);
		}
		vocabularyObject.put(rdf.getPrefixItem("theme"), arrayTheme);
		// change dc:creator
		JSONArray creatorArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:creator"));
		for (Object element : creatorArray) {
			vocabularyObject.remove(rdf.getPrefixItem("dc:creator"));
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			JSONArray arrayNew = new JSONArray();
			arrayNew.add(value);
			vocabularyObject.put(rdf.getPrefixItem("creator"), arrayNew);
		}
		// change dc:contributor
		JSONArray contributorArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:contributor"));
		vocabularyObject.remove(rdf.getPrefixItem("dc:contributor"));
		JSONArray arrayContributor = new JSONArray();
		for (Object element : contributorArray) {
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			arrayContributor.add(value);
		}
		vocabularyObject.put(rdf.getPrefixItem("contributor"), arrayContributor);
		// change dc:created
		JSONArray createdArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dc:created"));
		for (Object element : createdArray) {
			vocabularyObject.remove(rdf.getPrefixItem("dc:created"));
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			JSONArray arrayNew = new JSONArray();
			arrayNew.add(value);
			vocabularyObject.put(rdf.getPrefixItem("created"), arrayNew);
		}
		// change dct:license
		JSONArray licenseArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("dct:license"));
		for (Object element : licenseArray) {
			vocabularyObject.remove(rdf.getPrefixItem("dct:license"));
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			JSONArray arrayNew = new JSONArray();
			arrayNew.add(value);
			vocabularyObject.put(rdf.getPrefixItem("license"), arrayNew);
		}
		// change ls:identifier
		JSONArray identifierArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("ls:identifier"));
		for (Object element : identifierArray) {
			vocabularyObject.remove(rdf.getPrefixItem("ls:identifier"));
			JSONObject obj = (JSONObject) element;
			String value = (String) obj.get("value");
			JSONArray arrayNew = new JSONArray();
			arrayNew.add(value);
			vocabularyObject.put(rdf.getPrefixItem("id"), arrayNew);
		}
		// OPTIONAL VALUES
		// change skos:hasTopConcept
		JSONArray topConceptArray = (JSONArray) vocabularyObject.get(rdf.getPrefixItem("skos:hasTopConcept"));
		if (topConceptArray != null) {
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
		if (modifiedArray != null) {
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
		vocabularyObject.remove(rdf.getPrefixItem("dct:date"));
		vocabularyObject.remove(rdf.getPrefixItem("dct:identifier"));
		vocabularyObject.remove(rdf.getPrefixItem("dct:creator"));
		vocabularyObject.remove(rdf.getPrefixItem("dct:contributor"));
		vocabularyObject.remove(rdf.getPrefixItem("rdf:type"));
		vocabularyObject.remove(rdf.getPrefixItem("skos:changeNote"));
		vocabularyObject.remove(rdf.getPrefixItem("ls:sameAs"));
		// return
		return jsonObject;
	}

}
