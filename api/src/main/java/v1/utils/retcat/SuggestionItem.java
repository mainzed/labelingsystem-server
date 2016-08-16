package v1.utils.retcat;

import java.util.HashMap;
import java.util.HashSet;

public class SuggestionItem {
	
	private String url = "";
	private String id = "";
	private String schemeTitle = "";
	private String schemeURI = "";
	private HashSet<String> labels = new HashSet();
	private HashSet<String> altLabels = new HashSet();
	private HashSet<String> descriptions = new HashSet();
	private HashSet<HashMap<String,String>> broaderTerms = new HashSet<HashMap<String,String>>();
	private HashSet<HashMap<String,String>> narrowerTerms = new HashSet<HashMap<String,String>>();
	private String type = "";
	private String quality = "";
	private String group = "";
	private String language = "";

	public SuggestionItem(String URL) {
		url = URL;
	}
	
	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public HashSet<HashMap<String, String>> getBroaderTerms() {
		return broaderTerms;
	}

	public void setBroaderTerm(HashMap<String, String> broader) {
		this.broaderTerms.add(broader);
	}

	public HashSet<HashMap<String, String>> getNarrowerTerms() {
		return narrowerTerms;
	}

	public void setNarrowerTerm(HashMap<String, String> narrower) {
		this.narrowerTerms.add(narrower);
	}

	public String getSchemeTitle() {
		return schemeTitle;
	}

	public void setSchemeTitle(String schemeTitle) {
		this.schemeTitle = schemeTitle;
	}

	public HashSet<String> getLabels() {
		return labels;
	}

	public void setLabel(String label) {
		this.labels.add(label);
	}
	
	public HashSet<String> getAltLabels() {
		return altLabels;
	}

	public void setAltLabel(String label) {
		this.altLabels.add(label);
	}

	public HashSet<String> getDescriptions() {
		return descriptions;
	}

	public void setDescription(String description) {
		this.descriptions.add(description);
	}

	public String getSchemeURI() {
		return schemeURI;
	}

	public void setSchemeURI(String schemeURI) {
		this.schemeURI = schemeURI;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
}
