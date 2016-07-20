package info.labeling.v1.utils;

import java.util.HashMap;
import java.util.HashSet;

public class SuggestionItem {
	
	private String id = "";
	private String schemeTitle = "";
	private HashSet<String> label = new HashSet();
	private HashSet<String> definition = new HashSet();
	private HashSet<HashMap<String,String>> broader = new HashSet<HashMap<String,String>>();
	private HashSet<HashMap<String,String>> narrower = new HashSet<HashMap<String,String>>();

	public SuggestionItem(String ID) {
		id = ID;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public HashSet<HashMap<String, String>> getBroader() {
		return broader;
	}

	public void setBroader(HashMap<String, String> broader) {
		this.broader.add(broader);
	}

	public HashSet<HashMap<String, String>> getNarrower() {
		return narrower;
	}

	public void setNarrower(HashMap<String, String> narrower) {
		this.narrower.add(narrower);
	}

	public String getSchemeTitle() {
		return schemeTitle;
	}

	public void setSchemeTitle(String schemeTitle) {
		this.schemeTitle = schemeTitle;
	}

	public HashSet<String> getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label.add(label);
	}

	public HashSet<String> getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition.add(definition);
	}
	
	
	
}
