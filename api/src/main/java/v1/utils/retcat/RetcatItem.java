package v1.utils.retcat;

public class RetcatItem {
	
	private String name = "";
	private String description = "";
	private String queryURL = "";
	private String labelURL = "";
	private String prefix = "";
	private String group = "";
	private String type = "";
	private String language = "";
	private String quality = "";

	public RetcatItem(String name, String description, String queryURL, String labelURL, String prefix, String group, String type, String language, String quality) {
		this.name = name;
		this.description = description;
		this.queryURL = queryURL;
		this.labelURL = labelURL;
		this.prefix = prefix;
		this.group = group;
		this.type = type;
		this.language = language;
		this.quality = quality;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getQueryURL() {
		return queryURL;
	}

	public void setQueryURL(String queryURL) {
		this.queryURL = queryURL;
	}

	public String getLabelURL() {
		return labelURL;
	}

	public void setLabelURL(String labelURL) {
		this.labelURL = labelURL;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}
	
}
