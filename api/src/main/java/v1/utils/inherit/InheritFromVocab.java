package v1.utils.inherit;

public class InheritFromVocab {
	
	private String id = "";
	private String license = "";
	private String released = "";
	private String owner = "";

	public InheritFromVocab(String id, String license, String released, String owner) {
		this.id = id;
		this.released = released;
		this.license = license;
		this.owner = owner;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getReleased() {
		return released;
	}

	public void setReleased(String released) {
		this.released = released;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
