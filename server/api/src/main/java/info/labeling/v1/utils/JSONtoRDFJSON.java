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

public class JSONtoRDFJSON {

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

}
