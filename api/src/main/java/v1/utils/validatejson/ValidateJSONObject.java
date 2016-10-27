package v1.utils.validatejson;

import exceptions.ValidateJSONObjectException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ValidateJSONObject {

	public static void validateAgent(String json) throws ParseException, ValidateJSONObjectException {
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
		// MENDATORY KEYS
		if (!jsonObject.containsKey("id")) {
			throw new ValidateJSONObjectException("id missing");
		}
		if (!jsonObject.containsKey("firstName")) {
			throw new ValidateJSONObjectException("firstName missing");
		}
		if (!jsonObject.containsKey("lastName")) {
			throw new ValidateJSONObjectException("lastName missing");
		}
		if (!jsonObject.containsKey("orcid")) {
			throw new ValidateJSONObjectException("orcid missing");
		}
		if (!jsonObject.containsKey("affiliation")) {
			throw new ValidateJSONObjectException("affiliation missing");
		}
		// GET UNSUPPORTED KEYS
		// MENDATORY
		if (jsonObject.containsKey("id")) {
			jsonObject.remove("id");
		}
		if (jsonObject.containsKey("firstName")) {
			jsonObject.remove("firstName");
		}
		if (jsonObject.containsKey("lastName")) {
			jsonObject.remove("lastName");
		}
		if (jsonObject.containsKey("orcid")) {
			jsonObject.remove("orcid");
		}
		if (jsonObject.containsKey("affiliation")) {
			jsonObject.remove("affiliation");
		}
		// OPTIONAL
		if (jsonObject.containsKey("title")) {
			jsonObject.remove("title");
		}
		if (!jsonObject.isEmpty()) {
			throw new ValidateJSONObjectException("found unsupported key");
		}
	}
	
	public static void validateVocabulary(String json) throws ParseException, ValidateJSONObjectException {
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
		// MENDATORY KEYS
		if (!jsonObject.containsKey("creator")) {
			throw new ValidateJSONObjectException("creator missing");
		}
		if (!jsonObject.containsKey("title")) {
			throw new ValidateJSONObjectException("title missing");
		}
		if (!jsonObject.containsKey("description")) {
			throw new ValidateJSONObjectException("description missing");
		}
		if (!jsonObject.containsKey("language")) {
			throw new ValidateJSONObjectException("language missing");
		}
		if (!jsonObject.containsKey("license")) {
			throw new ValidateJSONObjectException("license missing");
		}
		// GET UNSUPPORTED KEYS
		// MENDATORY
		if (jsonObject.containsKey("id")) {
			jsonObject.remove("id");
		}
		if (jsonObject.containsKey("creator")) {
			jsonObject.remove("creator");
		}
		if (jsonObject.containsKey("title")) {
			jsonObject.remove("title");
		}
		if (jsonObject.containsKey("description")) {
			jsonObject.remove("description");
		}
		if (jsonObject.containsKey("language")) {
			jsonObject.remove("language");
		}
		if (jsonObject.containsKey("license")) {
			jsonObject.remove("license");
		}
		// OPTIONAL
		if (jsonObject.containsKey("created")) {
			jsonObject.remove("created");
		}
		if (jsonObject.containsKey("released")) {
			jsonObject.remove("released");
		}
		if (jsonObject.containsKey("lastModified")) {
			jsonObject.remove("lastModified");
		}
		if (jsonObject.containsKey("statistics")) {
			jsonObject.remove("statistics");
		}
		if (jsonObject.containsKey("creatorInfo")) {
			jsonObject.remove("creatorInfo");
		}
		if (jsonObject.containsKey("releaseType")) {
			jsonObject.remove("releaseType");
		}
		if (!jsonObject.isEmpty()) {
			throw new ValidateJSONObjectException("found unsupported key");
		}
	}
	
	public static void validateLabel(String json) throws ParseException, ValidateJSONObjectException {
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
		// MENDATORY KEYS
		if (!jsonObject.containsKey("creator")) {
			throw new ValidateJSONObjectException("creator missing");
		}
		if (!jsonObject.containsKey("vocabID")) {
			throw new ValidateJSONObjectException("vocabID missing");
		}
		if (!jsonObject.containsKey("thumbnail")) {
			throw new ValidateJSONObjectException("thumbnail missing");
		}
		if (!jsonObject.containsKey("language")) {
			throw new ValidateJSONObjectException("language missing");
		}
		// GET UNSUPPORTED KEYS
		// MENDATORY
		if (jsonObject.containsKey("id")) {
			jsonObject.remove("id");
		}
		if (jsonObject.containsKey("creator")) {
			jsonObject.remove("creator");
		}
		if (jsonObject.containsKey("vocabID")) {
			jsonObject.remove("vocabID");
		}
		if (jsonObject.containsKey("thumbnail")) {
			jsonObject.remove("thumbnail");
		}
		if (jsonObject.containsKey("language")) {
			jsonObject.remove("language");
		}
		if (jsonObject.containsKey("license")) {
			jsonObject.remove("license");
		}
		// OPTIONAL
		if (jsonObject.containsKey("description")) {
			jsonObject.remove("description");
		}
		if (jsonObject.containsKey("translations")) {
			jsonObject.remove("translations");
		}
		if (jsonObject.containsKey("broader")) {
			jsonObject.remove("broader");
		}
		if (jsonObject.containsKey("narrower")) {
			jsonObject.remove("narrower");
		}
		if (jsonObject.containsKey("related")) {
			jsonObject.remove("related");
		}
		if (jsonObject.containsKey("broadMatch")) {
			jsonObject.remove("broadMatch");
		}
		if (jsonObject.containsKey("narrowMatch")) {
			jsonObject.remove("narrowMatch");
		}
		if (jsonObject.containsKey("relatedMatch")) {
			jsonObject.remove("relatedMatch");
		}
		if (jsonObject.containsKey("closeMatch")) {
			jsonObject.remove("closeMatch");
		}
		if (jsonObject.containsKey("exactMatch")) {
			jsonObject.remove("exactMatch");
		}
		if (jsonObject.containsKey("seeAlso")) {
			jsonObject.remove("seeAlso");
		}
		if (jsonObject.containsKey("created")) {
			jsonObject.remove("created");
		}
		if (jsonObject.containsKey("released")) {
			jsonObject.remove("released");
		}
		if (jsonObject.containsKey("lastModified")) {
			jsonObject.remove("lastModified");
		}
		if (jsonObject.containsKey("revisions")) {
			jsonObject.remove("revisions");
		}
		if (jsonObject.containsKey("equalConcepts")) {
			jsonObject.remove("equalConcepts");
		}
		if (jsonObject.containsKey("creatorInfo")) {
			jsonObject.remove("creatorInfo");
		}
		if (jsonObject.containsKey("releaseType")) {
			jsonObject.remove("releaseType");
		}
		if (!jsonObject.isEmpty()) {
			throw new ValidateJSONObjectException("found unsupported key");
		}
	}

}
