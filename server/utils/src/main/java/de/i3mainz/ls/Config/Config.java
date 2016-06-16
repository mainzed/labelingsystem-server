package de.i3mainz.ls.Config;

import de.i3mainz.ls.rdfutils.exceptions.ConfigException;

/**
 * CLASS to set global settings
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 21.08.2015
 */
public class Config {

	// property file items (futire)
	private static final String PROPERTY_SERVER = "http://labeling.i3mainz.hs-mainz.de/";
	private static final String PROPERTY_TRIPLESTORE = "http://labeling.i3mainz.hs-mainz.de/";
	private static final String PROPERTY_INSTANCEHOST = "http://labeling.i3mainz.hs-mainz.de/";
	private static final String PROPERTY_TOMCAT_WEBAPPS = "/usr/share/apache-tomcat-7.0.50/webapps/";
	private static final String PROPERTY_APACHE_WWW = "/var/www/html/";
	private static final String PROPERTY_APACHE_ACCESS_LOG = "/var/log/httpd/access_log";
	private static final String PROPERTY_APACHE_ERROR_LOG = "/var/log/httpd/error_log";
	private static final String PROPERTY_TOMCAT_CATALINA_OUT = "/usr/share/apache-tomcat-7.0.50/logs/catalina.out";
	private static final String PROPERTY_LABELINGSYSTEM = "labelingsystem";
	private static final String PROPERTY_CONCEPTS = "concepts";

	// web 
	public final static String SERVER = PROPERTY_SERVER;
	public final static String TRIPLESTORE = PROPERTY_TRIPLESTORE;
	public final static String INSTANCEHOST = PROPERTY_INSTANCEHOST;
	public final static String TRIPLESTORE_SERVER = TRIPLESTORE + "openrdf-sesame";
	public final static String TRIPLESTORE_WORKBENCH = TRIPLESTORE + "openrdf-workbench";
	public final static String TRIPLESTORE_REPOSITORY_LS_EXPORT = TRIPLESTORE_SERVER + "/repositories/" + PROPERTY_LABELINGSYSTEM + "/statements?Accept=text/plain";
	public final static String TRIPLESTORE_REPOSITORY_LS_SIZE = TRIPLESTORE_SERVER + "/repositories/" + PROPERTY_LABELINGSYSTEM + "/size";
	// file system
	public final static String TOMCAT_WEBAPPS = PROPERTY_TOMCAT_WEBAPPS;
	public final static String APACHE_WWW = PROPERTY_APACHE_WWW;
	public final static String SHARE_FILESYSTEM = APACHE_WWW + "share/";
	// public final static String SHARE_FILESYSTEM = "C:\\temp\\ls\\"; // local
	// file system log files
	public final static String SHARE_WEB = SERVER + "share/";
	public final static String APACHE_ACCESS_LOG = PROPERTY_APACHE_ACCESS_LOG;
	public final static String APACHE_ERROR_LOG = PROPERTY_APACHE_ERROR_LOG;
	public final static String TOMCAT_CATALINA_OUT = PROPERTY_TOMCAT_CATALINA_OUT;

	// change prefixes here
	public final static String PREFIX_LABELINGSYSTEM = "http://labeling.i3mainz.hs-mainz.de/vocab#";
	public final static String PREFIX_SKOS = "http://www.w3.org/2004/02/skos/core#";
	public final static String PREFIX_DCTERMS = "http://purl.org/dc/terms/";
	public final static String PREFIX_DCELEMENTS = "http://purl.org/dc/elements/1.1/";
	public final static String PREFIX_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public final static String PREFIX_OWL = "http://www.w3.org/2002/07/owl#";
	public final static String PREFIX_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public final static String PREFIX_FOAF = "http://xmlns.com/foaf/0.1/";
	public final static String PREFIX_PROV = "http://www.w3.org/ns/prov#";

	// all PREFIXES for SPARQL query and update
	public final static String PREFIX_SPARQL = ""
			+ "PREFIX ls: <" + PREFIX_LABELINGSYSTEM + "> "
			+ "PREFIX skos: <" + PREFIX_SKOS + "> "
			+ "PREFIX dc: <" + PREFIX_DCELEMENTS + "> "
			+ "PREFIX dct: <" + PREFIX_DCTERMS + "> "
			+ "PREFIX rdfs: <" + PREFIX_RDFS + "> "
			+ "PREFIX owl: <" + PREFIX_OWL + "> "
			+ "PREFIX rdf: <" + PREFIX_RDF + "> "
			+ "PREFIX foaf: <" + PREFIX_FOAF + "> "
			+ "PREFIX prov: <" + PREFIX_PROV + "> ";

	// instances and rest path
	public final static String REST = SERVER + "rest/";
	public final static String Rest_VOCABS = REST + "vocabs/";
	public final static String Rest_REVISIONS = REST + "rev/";
	public final static String Rest_PROJECTS = REST + "projects/";
	public final static String Rest_LABELS = REST + "vocabs/$vocab/$label";
	public final static String Rest_GUIS = REST + "guis/";
	public final static String Rest_AGENTS = REST + "agents/";
	public final static String Rest_SPARQLENDPOINTS = REST + "sparqlendpoints/";
	public final static String Rest_PROPERTYSCHEME = REST + "pscheme/";
	public final static String Rest_PROPERTY = REST + "pscheme/$pscheme/$property";
	public final static String Rest_ANNO = REST + "anno/";

	public final static String LABELINGSERVER = SERVER + "labelingserver/";
	public final static String Instance_PROJECT = INSTANCEHOST + "project#";
	public final static String Instance_VOCABULARY = INSTANCEHOST + "vocabulary#";
	public final static String Instance_LABEL = INSTANCEHOST + "label#";
	public final static String Instance_LOG = INSTANCEHOST + "log#";
	public final static String Instance_SPARQLENDPOINT = INSTANCEHOST + "sparqlendpoint#";
	public final static String Instance_AGENT = INSTANCEHOST + "agent#";
	public final static String Instance_GUI = INSTANCEHOST + "gui#";
	public final static String Instance_ANNO = INSTANCEHOST + "anno#";
	public final static String Instance_REVISION = INSTANCEHOST + "rev#";
	
	public final static String Instance_PROJECT_LC = "http://localhost:8084/item/project/";
	public final static String Instance_VOCABULARY_LC = "http://localhost:8084/item/vocab/";
	public final static String Instance_LABEL_LC = "http://localhost:8084/item/label/";
	public final static String Instance_AGENT_LC = "http://localhost:8084/item/agent/";
	public final static String Instance_GUI_LC = "http://localhost:8084/item/gui/";
	public final static String Instance_REVISION_LC = "http://localhost:8084/item/revision/";
	
	public final static String Instance_PROJECT_ITEM = "http://labeling.i3mainz.hs-mainz.de/item/project/";
	public final static String Instance_VOCABULARY_ITEM = "http://labeling.i3mainz.hs-mainz.de/item/vocab/";
	public final static String Instance_LABEL_ITEM = "http://labeling.i3mainz.hs-mainz.de/item/label/";
	public final static String Instance_AGENT_ITEM = "http://labeling.i3mainz.hs-mainz.de/item/agent/";
	public final static String Instance_GUI_ITEM = "http://labeling.i3mainz.hs-mainz.de/item/gui/";
	public final static String Instance_REVISION_ITEM = "http://labeling.i3mainz.hs-mainz.de/item/revision/";

	// security properties
	public final static String LOGIN = "";
	public final static String PASSWORD = "";

	// revision terms
	public static final String[] REVISION = {"created", "deleted", "connected or disconnected", "published or hidden", "textual description modified", "relation modified"};

	// choose label licence
	public static final String LICENCE = "http://creativecommons.org/licenses/by/4.0/";

	// supported preference languages
	public static final String[] LANGUAGES = {"de", "en", "fr", "it", "es", "pl", "la", "rm", "de-std", "de-orig", "fr-std", "fr-orig", "it-std", "it-orig", "la-std", "la-orig", "rm-std", "rm-orig"};

	/**
	 * get instance URI (options: type[project;vocabulary;label] and brackets
	 * [true;false])
	 *
	 * @param type
	 * @param item
	 * @param brackets
	 * @return
	 * @throws de.i3mainz.ls.rdfutils.exceptions.ConfigException
	 */
	public static String getInstance(String type, String item, boolean brackets) throws ConfigException {
		try {
			if ("project".equals(type)) {
				if (brackets) {
					return "<" + Instance_PROJECT + item + ">";
				} else {
					return Instance_PROJECT + item;
				}
			} else if ("vocabulary".equals(type)) {
				if (brackets) {
					return "<" + Instance_VOCABULARY + item + ">";
				} else {
					return Instance_VOCABULARY + item;
				}
			} else if ("label".equals(type)) {
				if (brackets) {
					return "<" + Instance_LABEL + item + ">";
				} else {
					return Instance_LABEL + item;
				}
			} else if ("log".equals(type)) {
				if (brackets) {
					return "<" + Instance_LOG + item + ">";
				} else {
					return Instance_LOG + item;
				}
			} else if ("sparqlendpoint".equals(type)) {
				if (brackets) {
					return "<" + Instance_SPARQLENDPOINT + item + ">";
				} else {
					return Instance_SPARQLENDPOINT + item;
				}
			} else if ("agent".equals(type)) {
				if (brackets) {
					return "<" + Instance_AGENT + item + ">";
				} else {
					return Instance_AGENT + item;
				}
			} else if ("gui".equals(type)) {
				if (brackets) {
					return "<" + Instance_GUI + item + ">";
				} else {
					return Instance_GUI + item;
				}
			} else if ("anno".equals(type)) {
				if (brackets) {
					return "<" + Instance_ANNO + item + ">";
				} else {
					return Instance_ANNO + item;
				}
			} else if ("revision".equals(type)) {
				if (brackets) {
					return "<" + Instance_REVISION + item + ">";
				} else {
					return Instance_REVISION + item;
				}
			} else {
				return "error";
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}
	
	public static String getInstanceItem(String type, String item, boolean brackets) throws ConfigException {
		try {
			if ("project".equals(type)) {
				if (brackets) {
					return "<" + Instance_PROJECT_ITEM + item + ">";
				} else {
					return Instance_PROJECT_ITEM + item;
				}
			} else if ("vocabulary".equals(type)) {
				if (brackets) {
					return "<" + Instance_VOCABULARY_ITEM + item + ">";
				} else {
					return Instance_VOCABULARY_ITEM + item;
				}
			} else if ("label".equals(type)) {
				if (brackets) {
					return "<" + Instance_LABEL_ITEM + item + ">";
				} else {
					return Instance_LABEL_ITEM + item;
				}
			} else if ("agent".equals(type)) {
				if (brackets) {
					return "<" + Instance_AGENT_ITEM + item + ">";
				} else {
					return Instance_AGENT_ITEM + item;
				}
			} else if ("gui".equals(type)) {
				if (brackets) {
					return "<" + Instance_GUI_ITEM + item + ">";
				} else {
					return Instance_GUI_ITEM + item;
				}
			} else if ("revision".equals(type)) {
				if (brackets) {
					return "<" + Instance_REVISION_ITEM + item + ">";
				} else {
					return Instance_REVISION_ITEM + item;
				}
			} else {
				return "error";
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}

	/**
	 * get REST URI of Vocabularies (options: brackets[true;false])
	 *
	 * @param brackets
	 * @return
	 * @throws ConfigException
	 */
	public static String RestVocabularies(boolean brackets) throws ConfigException {
		try {
			if (brackets) {
				return "<" + Rest_VOCABS + ">";
			} else {
				return Rest_VOCABS;
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}

	/**
	 * get REST URI of Vocabulary (options: brackets[true;false] and
	 * download[true;false])
	 *
	 * @param vocabulary
	 * @param brackets
	 * @param download
	 * @return
	 * @throws de.i3mainz.ls.rdfutils.exceptions.ConfigException
	 */
	public static String RestVocabulary(String vocabulary, boolean brackets, boolean download) throws ConfigException {
		try {
			if (brackets) {
				if (download) {
					return "<" + Rest_VOCABS + vocabulary + ".skos>";
				} else {
					return "<" + Rest_VOCABS + vocabulary + "/>";
				}
			} else {
				if (download) {
					return Rest_VOCABS + vocabulary + ".skos";
				} else {
					return Rest_VOCABS + vocabulary + "/";
				}
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}

	/**
	 * get REST URI of Label (options: brackets[true;false] and format[rdf;ttl])
	 *
	 * @param vocabulary
	 * @param label
	 * @param brackets
	 * @param format
	 * @return
	 * @throws de.i3mainz.ls.rdfutils.exceptions.ConfigException
	 */
	public static String RestLabel(String vocabulary, String label, boolean brackets, String format) throws ConfigException {
		try {
			if (brackets) {
				if (format != null) {
					return "<" + Rest_VOCABS + vocabulary + "/" + label + "." + format + ">";
				} else {
					return "<" + Rest_VOCABS + vocabulary + "/" + label + "/>";
				}
			} else {
				if (format != null) {
					return Rest_VOCABS + vocabulary + "/" + label + "." + format;
				} else {
					return Rest_VOCABS + vocabulary + "/" + label + "/";
				}
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}

	/**
	 * get ontology ITEM
	 *
	 * @param ontology (ls;skos;dcterms;rdfs)
	 * @param item
	 * @param brackets (true;false)
	 * @return
	 * @throws de.i3mainz.ls.rdfutils.exceptions.ConfigException
	 */
	public static String getPrefixItemOfOntology(String ontology, String item, boolean brackets) throws ConfigException {
		try {
			if (brackets) {
				switch (ontology) {
					case "ls":
						return "<" + PREFIX_LABELINGSYSTEM + item + ">";
					case "skos":
						return "<" + PREFIX_SKOS + item + ">";
					case "dcterms":
						return "<" + PREFIX_DCTERMS + item + ">";
					case "rdfs":
						return "<" + PREFIX_RDFS + item + ">";
					case "owl":
						return "<" + PREFIX_OWL + item + ">";
					case "dcelements":
						return "<" + PREFIX_DCELEMENTS + item + ">";
					case "rdf":
						return "<" + PREFIX_RDF + item + ">";
					case "foaf":
						return "<" + PREFIX_FOAF + item + ">";
					case "prov":
						return "<" + PREFIX_PROV + item + ">";
					default:
						return "<" + ontology + item + ">";
				}
			} else {
				switch (ontology) {
					case "ls":
						return PREFIX_LABELINGSYSTEM + item;
					case "skos":
						return PREFIX_SKOS + item;
					case "dcterms":
						return PREFIX_DCTERMS + item;
					case "rdfs":
						return PREFIX_RDFS + item;
					case "owl":
						return PREFIX_OWL + item;
					case "dcelements":
						return PREFIX_DCELEMENTS + item;
					case "rdf":
						return PREFIX_RDF + item;
					case "foaf":
						return PREFIX_FOAF + item;
					case "prov":
						return PREFIX_PROV + item;
					default:
						return ontology + item;
				}
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}

	/**
	 * get REST URI of items
	 *
	 * @param type
	 * @param item
	 * @param brackets
	 * @param format
	 * @return
	 * @throws de.i3mainz.ls.rdfutils.exceptions.ConfigException
	 */
	public static String getRestURI(String type, String item, boolean brackets, String format) throws ConfigException {
		try {
			String items[] = item.split(";");
			String formatsuffix = "";
			if (format != null) {
				formatsuffix = "." + format;
			}
			if (brackets) {
				switch (type) {
					case "project":
						return "<" + Rest_PROJECTS + item + formatsuffix + ">";
					case "vocabulary":
						return "<" + Rest_VOCABS + item + formatsuffix + ">";
					case "label":
						return "<" + Rest_LABELS.replace("$vocab", items[0]).replace("$label", items[1]) + formatsuffix + ">";
					case "propertyscheme":
						return "<" + Rest_PROPERTYSCHEME.replace("$pscheme", items[0]).replace("$property", items[1]) + formatsuffix + ">";
					case "property":
						return "<" + Rest_PROPERTY + item + formatsuffix + ">";
					case "agent":
						return "<" + Rest_AGENTS + item + formatsuffix + ">";
					case "gui":
						return "<" + Rest_GUIS + item + formatsuffix + ">";
					case "anno":
						return "<" + Rest_ANNO + item + formatsuffix + ">";
					case "sparqlendpoint":
						return "<" + Rest_SPARQLENDPOINTS + item + formatsuffix + ">";
					case "revision":
						return "<" + Rest_REVISIONS + item + formatsuffix + ">";
					default:
						return null;
				}
			} else {
				switch (type) {
					case "project":
						return Rest_PROJECTS + item + formatsuffix;
					case "vocabulary":
						return Rest_VOCABS + item + formatsuffix;
					case "label":
						return Rest_LABELS.replace("$vocab", items[0]).replace("$label", items[1]) + formatsuffix;
					case "propertyscheme":
						return Rest_PROPERTYSCHEME.replace("$pscheme", items[0]).replace("$property", items[1]) + formatsuffix;
					case "property":
						return Rest_PROPERTY + item + formatsuffix;
					case "agent":
						return Rest_AGENTS + item + formatsuffix;
					case "gui":
						return Rest_GUIS + item + formatsuffix;
					case "anno":
						return Rest_ANNO + item + formatsuffix;
					case "sparqlendpoint":
						return Rest_SPARQLENDPOINTS + item + formatsuffix;
					case "revision":
						return Rest_REVISIONS + item + formatsuffix;
					default:
						return null;
				}
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}

	/**
	 *
	 * @param string
	 * @param lang
	 * @param language
	 * @return
	 */
	public static String getString(String string, boolean lang, String language) {
		if (lang) {
			return "\"" + string + "\"@" + language;
		} else {
			return "\"" + string + "\"";
		}
	}
	
	public static String getInstanceLocalhost(String type, String item, boolean brackets) throws ConfigException {
		try {
			if ("project".equals(type)) {
				if (brackets) {
					return "<" + Instance_PROJECT_LC + item + ">";
				} else {
					return Instance_PROJECT_LC + item;
				}
			} else if ("vocabulary".equals(type)) {
				if (brackets) {
					return "<" + Instance_VOCABULARY_LC + item + ">";
				} else {
					return Instance_VOCABULARY_LC + item;
				}
			} else if ("label".equals(type)) {
				if (brackets) {
					return "<" + Instance_LABEL_LC + item + ">";
				} else {
					return Instance_LABEL_LC + item;
				}
			} else if ("agent".equals(type)) {
				if (brackets) {
					return "<" + Instance_AGENT_LC + item + ">";
				} else {
					return Instance_AGENT_LC + item;
				}
			} else if ("gui".equals(type)) {
				if (brackets) {
					return "<" + Instance_GUI_LC + item + ">";
				} else {
					return Instance_GUI_LC + item;
				}
			} else if ("revision".equals(type)) {
				if (brackets) {
					return "<" + Instance_REVISION_LC + item + ">";
				} else {
					return Instance_REVISION_LC + item;
				}
			} else {
				return "error";
			}
		} catch (Exception e) {
			throw new ConfigException("[" + Config.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
		}
	}

}
