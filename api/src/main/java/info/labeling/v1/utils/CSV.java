package info.labeling.v1.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import de.i3mainz.ls.Config.Config;
import info.labeling.rdf.RDF;
import info.labeling.rdf.RDF4J_20M3;
import info.labeling.exceptions.ConfigException;
import info.labeling.exceptions.CsvExistanceException;
import info.labeling.exceptions.CsvLabelImportException;
import info.labeling.exceptions.CsvLanguageException;
import info.labeling.exceptions.SesameSparqlException;
import info.labeling.exceptions.UniqueIdentifierException;
import info.labeling.v1.rest.ImportcsvResource;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 * CLASS (implements Runnable) to import a CSV file to the triplestore
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.07.2016
 */
public class CSV implements Runnable {

	public static String JSON_STRING = "";
	private static List<String> TRIPLE_LIST = new ArrayList<String>();
	private static final int COLUMNS = 18;
	private static final int IDCOLUMN = 17;

	@Override
	public void run() {
		try {
			if (ImportcsvResource.validator) {
				// Input Test
				Input(ImportcsvResource.csvContent, ImportcsvResource.creator, ImportcsvResource.CONTEXT, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, true, true);
			} else {
				// Input Test
				String JSON = Input(ImportcsvResource.csvContent, ImportcsvResource.creator, ImportcsvResource.CONTEXT, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, true, false);
				if (JSON.contains("\"success\": \"true\"")) {
					// Real Input
					Input(ImportcsvResource.csvContent, ImportcsvResource.creator, ImportcsvResource.CONTEXT, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, false, false);
				}
			}
		} catch (Exception e) {
			Logger.getLogger(CSV.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	private static String createLabelTriples(String[] tokens, String creator, String context) throws IOException, UniqueIdentifierException, CsvLabelImportException, info.labeling.exceptions.ConfigException, info.labeling.exceptions.SesameSparqlException {
		String uuid = UniqueIdentifier.getUUID();
		try {
			//vocabulary[0];prefLabel[1];altLabel[2];scopeNote[3];definition[4];
			//broader[5];narrower[6];related[7];broadMatch[8];narrowMatch[9];relatedMatch[10];
			//closeMatch[11];exactMatch[12];seeAlso[13];isDefinedBy[14];sameAs[15];contributors[16];internalID[17]
			String vocabulary = tokens[0];
			String[] prefLabel = tokens[1].split(";");
			String[] altLabel = tokens[2].split(";");
			String[] scopeNote = tokens[3].split(";");
			String[] definition = tokens[4].split(";");
			//String prefLang = tokens[5];
			String[] broadMatch = tokens[8].split(";");
			String[] narrowMatch = tokens[9].split(";");
			String[] relatedMatch = tokens[10].split(";");
			String[] closeMatch = tokens[11].split(";");
			String[] exactMatch = tokens[12].split(";");
			String[] seeAlso = tokens[13].split(";");
			String[] isDefinedBy = tokens[14].split(";");
			String[] sameAs = tokens[15].split(";");
			String[] contributor = tokens[16].split(";");
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String label = "";
			// ls label
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("rdf:type") + "> ";
			label += "<" + rdf.getPrefixItem("ls:Label") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// skos concept
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("rdf:type") + "> ";
			label += "<" + rdf.getPrefixItem("skos:Concept") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// dcelements creator
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:creator") + "> ";
			label += "\"" + creator + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// dcterms creator
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:creator") + "> ";
			label += "<" + rdf.getPrefixItem("ls_age:" + creator) + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// dcelements contributor
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:contributor") + "> ";
			label += "\"" + creator + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// dcterms contributor
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:contributor") + "> ";
			label += "<" + rdf.getPrefixItem("ls_age:" + creator) + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// dcterms date
			Calendar cal = Calendar.getInstance();
			Date time = cal.getTime();
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			String d = formatter.format(time);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:created") + "> ";
			label += "\"" + d + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// dcterms licence
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:license") + "> ";
			label += "<" + "http://creativecommons.org/licenses/by/4.0/" + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// dcelements identifier
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:identifier") + "> ";
			label += "\"" + uuid + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// ls context
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("ls:hasContext") + "> ";
			label += "\"" + context + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// ls status type
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("ls:hasStatusType") + "> ";
			label += "<" + rdf.getPrefixItem("ls:Active") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// skos prefLabel (multiple, obligatory)
			for (int i = 0; i < prefLabel.length; i = i + 2) {
				if (i == 0) {
					// ls preferredLabel
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("ls:preferredLabel") + "> ";
					label += "\"" + prefLabel[i] + "\"@" + prefLabel[i + 1] + " ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
				label = "";
				label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
				label += "<" + rdf.getPrefixItem("skos:prefLabel") + "> ";
				label += "\"" + prefLabel[i] + "\"@" + prefLabel[i + 1] + " ";
				label += ". ";
				TRIPLE_LIST.add(label);
			}
			// skos altLabel (multiple, optional)
			if (altLabel.length >= 1 && !altLabel[0].equals("")) {
				for (int i = 0; i < altLabel.length; i = i + 2) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:altLabel") + "> ";
					label += "\"" + altLabel[i] + "\"@" + altLabel[i + 1] + " ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			// skos scopeNote (multiple, optional)
			if (scopeNote.length >= 1 && !scopeNote[0].equals("")) {
				for (int i = 0; i < scopeNote.length; i = i + 2) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:scopeNote") + "> ";
					label += "\"" + scopeNote[i] + "\"@" + scopeNote[i + 1] + " ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			// skos definition (multiple, optional)
			/*if (definition.length >= 1 && !definition[0].equals("")) {
                for (int i = 0; i < definition.length; i = i + 2) {
                    label = "";
                    label += "<" + rdf.getPrefixItem("ls_lab:"+uuid) + "> ";
                    label += "<" + rdf.getPrefixItem("skos:definition") + "> ";
                    label += "\"" + definition[i] + "\"@" + definition[i + 1] + " ";
                    label += ". ";
                    TRIPLE_LIST.add(label);
                }
            }*/
			// links to resources
			if (broadMatch.length >= 1 && !broadMatch[0].equals("")) {
				for (String broadMatch1 : broadMatch) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:broadMatch") + "> ";
					label += "<" + broadMatch1 + "> ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			if (narrowMatch.length >= 1 && !narrowMatch[0].equals("")) {
				for (String narrowMatch1 : narrowMatch) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:narrowMatch") + "> ";
					label += "<" + narrowMatch1 + "> ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			if (relatedMatch.length >= 1 && !relatedMatch[0].equals("")) {
				for (String relatedMatch1 : relatedMatch) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:relatedMatch") + "> ";
					label += "<" + relatedMatch1 + "> ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			if (closeMatch.length >= 1 && !closeMatch[0].equals("")) {
				for (String closeMatch1 : closeMatch) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:closeMatch") + "> ";
					label += "<" + closeMatch1 + "> ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			if (exactMatch.length >= 1 && !exactMatch[0].equals("")) {
				for (String exactMatch1 : exactMatch) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:exactMatch") + "> ";
					label += "<" + exactMatch1 + "> ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			if (seeAlso.length >= 1 && !seeAlso[0].equals("")) {
				for (String seeAlso1 : seeAlso) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("rdfs:seeAlso") + "> ";
					label += "<" + seeAlso1 + "> ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			/*if (isDefinedBy.length >= 1 && !isDefinedBy[0].equals("")) {
                for (String definedBy : isDefinedBy) {
                    label = "";
                   label += "<" + rdf.getPrefixItem("ls_lab:"+uuid) + "> ";
                    label += "<" + rdf.getPrefixItem("rdfs:isDefinedBy") + "> ";
                    label += "<" + definedBy + "> ";
                    label += ". ";
                    TRIPLE_LIST.add(label);
                }
            }*/
 /*if (sameAs.length >= 1 && !sameAs[0].equals("")) {
                for (String sameA : sameAs) {
                    label = "";
                    label += "<" + rdf.getPrefixItem("ls_lab:"+uuid) + "> ";
                    label += "<" + rdf.getPrefixItem("owl:sameAs") + "> ";
                    label += "<" + sameA + "> ";
                    label += ". ";
                    TRIPLE_LIST.add(label);
                }
            }*/
			if (contributor.length >= 1 && !contributor[0].equals("")) {
				for (String cont : contributor) {
					// dcelements contributor
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("dc:contributor") + "> ";
					label += "\"" + cont + "\" ";
					label += ". ";
					TRIPLE_LIST.add(label);
					// dcterms contributor
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("dct:contributor") + "> ";
					label += "<" + rdf.getPrefixItem("ls_age:" + cont) + "> ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			//connections
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("skos:inScheme") + "> ";
			label += "<" + rdf.getPrefixItem("ls_voc:" + vocabulary) + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			/////////////////////////
			// SET REVISION CREATE //
			/////////////////////////
			String revisionid = UniqueIdentifier.getUUID();
			// connect revision
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("skos:changeNote") + "> ";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// set revision
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("rdf:type") + "> ";
			label += "<" + rdf.getPrefixItem("ls:Revision") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("rdf:type") + "> ";
			label += "<" + rdf.getPrefixItem("prov:Activity") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("rdf:type") + "> ";
			label += "<" + rdf.getPrefixItem("prov:Modify") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:identifier") + "> ";
			label += "\"" + revisionid + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:creator") + "> ";
			label += "\"" + creator + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:creator") + "> ";
			label += "<" + rdf.getPrefixItem("ls_age:" + creator) + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:type") + "> ";
			label += "<" + rdf.getPrefixItem("ls:createRevision") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:description") + "> ";
			label += "\"" + "CreateRevision" + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_rev:" + revisionid) + "> ";
			label += "<" + rdf.getPrefixItem("prov:startedAtTime") + "> ";
			label += "\"" + d + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
		} catch (Exception ex) {
			throw new CsvLabelImportException("[ImportcsvResource.java | importLabel()] " + ex.toString());
		}
		return uuid;
	}

	private static boolean vocabularyExistenceCheck(String voc, String creator) throws IOException, JDOMException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ConfigException, CsvExistanceException, info.labeling.exceptions.ConfigException, info.labeling.exceptions.SesameSparqlException {
		try {
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = rdf.getPREFIXSPARQL();
			query += "SELECT * WHERE { "
					+ "?v a ls:Vocabulary . "
					+ "?v dc:creator \"" + creator + "\" . "
					+ "?v dc:identifier ?identifier. "
					+ "FILTER (?identifier=\"$identifier\") }";
			query = query.replace("$identifier", voc);
			List<BindingSet> voc_result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"),
					ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> voc_true = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(voc_result, "v");
			return voc_true.size() > 0;
		} catch (RepositoryException e) {
			throw new CsvExistanceException();
		} catch (MalformedQueryException e) {
			throw new CsvExistanceException();
		} catch (QueryEvaluationException e) {
			throw new CsvExistanceException();
		}
	}

	private static boolean doubleLanguageCheck(String languagestring) throws CsvLanguageException {
		try {
			boolean result = false;
			String[] ls = languagestring.split(";");
			HashSet set = new HashSet();
			if (ls.length > 1) {
				for (int i = 0; i < ls.length; i = i + 2) {
					if (set.contains(ls[i + 1])) {
						result = true;
						break;
					} else {
						set.add(ls[i + 1]);
					}
				}
			}
			return result;
		} catch (Exception e) {
			throw new CsvLanguageException();
		}
	}

	private static boolean languageCheck(String string) throws CsvLanguageException {
		boolean result = false;
		try {
			/*String[] LANGUAGES = {"de", "en", "fr", "it", "es", "pl", "la", "rm", "de-std", "de-orig", "fr-std", "fr-orig", "it-std", "it-orig", "la-std", "la-orig", "rm-std", "rm-orig"};
			for (int i = 0; i < LANGUAGES.length; i++) {
				if (string.equals(LANGUAGES[i])) {
					result = true;
					break;
				}
			}
			return result;*/
			return true;
		} catch (Exception e) {
			throw new CsvLanguageException();
		}
	}

	public static String Input(String csvContent, String creator, String context, String filename, String filelink,
			boolean validator, boolean check) throws CsvLabelImportException, UniqueIdentifierException, IOException {
		JSONObject outputJSONobject = new JSONObject(); // {}
		JSONArray errorArray = new JSONArray();
		String JSON_OUT = "";
		String ids = "";
		int errors = 0;
		int relationerrors = 0;
		int importedlabels = 0;
		int importedrelations = 0;
		boolean error = false;
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		try {
			TRIPLE_LIST.clear();
			Map<Integer, String> labels = new HashMap<Integer, String>();
			//vocabulary[0];prefLabel[1];altLabel[2];scopeNote[3];definition[4];prefLang[5];
			//broader[6];narrower[7];related[8];broadMatch[9];narrowMatch[10];relatedMatch[11];
			//closeMatch[12];exactMatch[13];seeAlso[14];isDefinedBy[15];sameAs[16];contributor[17];internalID[18]
			String[] csvLine = csvContent.split("\r\n");
			//check vocabulary id and stop import if sth. is wrong
			if (validator) {
				ImportcsvResource.action = "check file and vocabularies (check)...";
			} else {
				ImportcsvResource.action = "check file and vocabularies...";
			}
			for (int i = 0; i < csvLine.length; i++) {
				if (csvLine[i].equals("")) {
					errorArray.add("error: empty line found in line " + i);
					error = true;
					errors++;
				}
			}
			if (!error) {
				HashSet<String> vocabularyList = new HashSet<String>();
				for (int i = 1; i < csvLine.length; i++) {
					String[] tokens = csvLine[i].split("[\t]");
					vocabularyList.add(tokens[0]);
				}
				for (String voc : vocabularyList) {
					if (voc.equals("")) {
						error = true;
						errorArray.add("error: no required vocabulary found");
						errors++;
						break;
					}
					if (CSV.vocabularyExistenceCheck(voc, creator) == false) {
						error = true;
						errorArray.add("error: vocabulary not found");
						errors++;
						break;
					}
				}
			}
			if (!error) {
				// check and import labels 
				for (int i = 1; i < csvLine.length; i++) {
					ImportcsvResource.currentStep = ImportcsvResource.currentStep + 1;
					ImportcsvResource.status = ((double) ImportcsvResource.currentStep / (double) ImportcsvResource.maxSteps) * 100;
					if (validator) {
						ImportcsvResource.action = "check and import labels [line " + i + "] (check)...";
					} else {
						ImportcsvResource.action = "check and import labels [line " + i + "] ...";
					}
					try {
						String[] tokens = csvLine[i].split("[\t]");
						if (tokens.length == COLUMNS) {
							// prefLabel check
							if (tokens[1].equals("")) {
								error = true;
								errorArray.add("ignored: no required prefLabel in line " + i);
							} else {
								// ckeck if just one prefLabel for one language (same with scopeNote and definition)
								String[] elements = tokens[1].split(";");
								for (int j = 0; j < elements.length; j++) {
									if (j % 2 != 0) {
										if (CSV.languageCheck(elements[j])) {
										} else {
											error = true;
											errorArray.add("ignored: wrong prefLabel language in line " + i);
										}
									}
								}
								if (CSV.doubleLanguageCheck(tokens[1])) {
									error = true;
									errorArray.add("ignored: multiple prefLabel language in line " + i);
								}
							}
							if (tokens[1].split(";").length % 2 != 0) {
								error = true;
								errorArray.add("ignored: prefLabel language error in line " + i);
							}
							// altLabel check (optional, multiple languages allowed)
							if (!tokens[2].equals("")) {
								String[] elements = tokens[2].split(";");
								if (tokens[2].split(";").length % 2 != 0) {
									error = true;
									errorArray.add("ignored: altLabel language error in line " + i);
								}
							}
							// scopeNote check (optional)
							if (!tokens[3].equals("")) {
								String[] elements = tokens[3].split(";");
								if (tokens[3].split(";").length % 2 != 0) {
									error = true;
									errorArray.add("ignored: note language error in line " + i);
								}
								for (int j = 0; j < elements.length; j++) {
									if (j % 2 != 0) {
										if (CSV.languageCheck(elements[j])) {
										} else {
											error = true;
											errorArray.add("ignored: wrong note language in line " + i);
										}
									}
								}
								if (CSV.doubleLanguageCheck(tokens[3])) {
									error = true;
									errorArray.add("ignored: multiple note language in line " + i);
								}
							}
							// definition check (optional)
							if (!tokens[4].equals("")) {
								String[] elements = tokens[4].split(";");
								if (tokens[4].split(";").length % 2 != 0) {
									error = true;
									errorArray.add("ignored: definition language error in line " + i);
								}
								for (int j = 0; j < elements.length; j++) {
									if (j % 2 != 0) {
										if (CSV.languageCheck(elements[j])) {
										} else {
											error = true;
											errorArray.add("ignored: wrong definition language in line " + i);
										}
									}
								}
								if (CSV.doubleLanguageCheck(tokens[4])) {
									error = true;
									errorArray.add("ignored: multiple definition language in line " + i);
								}
							}
							// broadMatch check (optional)
							if (!tokens[8].equals("")) {
								String[] elements = tokens[8].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							// narrowMatch check (optional)
							if (!tokens[9].equals("")) {
								String[] elements = tokens[9].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							// relatedMatch check (optional)
							if (!tokens[10].equals("")) {
								String[] elements = tokens[10].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							// closeMatch check (optional)
							if (!tokens[11].equals("")) {
								String[] elements = tokens[11].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							// exactMatch check (optional)
							if (!tokens[12].equals("")) {
								String[] elements = tokens[12].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							// seeAlso check (optional)
							if (!tokens[13].equals("")) {
								String[] elements = tokens[13].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							// isDefinedBy check (optional)
							if (!tokens[14].equals("")) {
								String[] elements = tokens[14].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							// sameAs check (optional)
							if (!tokens[15].equals("")) {
								String[] elements = tokens[15].split(";");
								for (String element : elements) {
									if (!element.contains("http://")) {
										error = true;
										errorArray.add("ignored: no HTTP web resource in line " + i);
									}
								}
							}
							if (!error) {
								//import to triplestore or validate
								if (!tokens[IDCOLUMN].equals("")) {
									if (Integer.parseInt(tokens[IDCOLUMN]) < 1) {
										// hierarchy check
										errorArray.add("ignored: id smaller than 1 or not numeric in " + i);
									} else if (validator) {
										String labelID = CSV.createLabelTriples(tokens, creator, context);
										if (!labelID.equals("")) { // if label is imported
											labels.put(Integer.parseInt(tokens[IDCOLUMN]), labelID);
											importedlabels++;
										}
									} else {
										String labelID = CSV.createLabelTriples(tokens, creator, context);
										if (!labelID.equals("")) { // if label is imported
											labels.put(Integer.parseInt(tokens[IDCOLUMN]), labelID);
											importedlabels++;
											if (ids.equals("")) {
												ids += labelID;
											} else {
												ids += ";" + labelID;
											}
										}
									}
								}
							} else {
								errors++;
								error = true;
							}
						} else {
							errors++;
							error = true;
							errorArray.add("ignored: not enough columns (" + tokens.length + "istead of " + COLUMNS + ") in line " + i);
						}
					} catch (Exception e) {
						errorArray.add("ignored: " + e.toString() + " in line " + i);
					} finally {
						//error = false;
					}
				}
				// check and import relations
				for (int i = 1; i < csvLine.length; i++) {
					ImportcsvResource.currentStep = ImportcsvResource.currentStep + 1;
					ImportcsvResource.status = ((double) ImportcsvResource.currentStep / (double) ImportcsvResource.maxSteps) * 100;
					if (validator) {
						ImportcsvResource.action = "check and import relations (check)...";
					} else {
						ImportcsvResource.action = "check and import relations...";
					}
					String[] tokens = csvLine[i].split("[\t]");
					if (tokens.length == COLUMNS) {
						try {
							if (!tokens[5].equals("")) {
								String[] split = tokens[5].split(";");
								for (int ii = 0; ii < split.length; ii++) {
									if (Integer.parseInt(split[ii]) < 1) {
										errorArray.add("ignored: broader id smaller than 1 or not numeric in line " + i);
										relationerrors++;
									}
								}
							}
							if (!tokens[6].equals("")) {
								String[] split = tokens[6].split(";");
								for (int ii = 0; ii < split.length; ii++) {
									if (Integer.parseInt(split[ii]) < 1) {
										errorArray.add("ignored: narrower id smaller than 1 or not numeric in line " + i);
										relationerrors++;
									}
								}
							}
							if (!tokens[7].equals("")) {
								String[] split = tokens[7].split(";");
								for (int ii = 0; ii < split.length; ii++) {
									if (Integer.parseInt(split[ii]) < 1) {
										errorArray.add("ignored: related id smaller than 1 or not numeric in line " + i);
										relationerrors++;
									}
								}
							}
						} catch (Exception e) {
							errorArray.add("ignored: " + e.toString() + " in line " + i);
							relationerrors++;
						}
						// get label UUID
						String labelUUID = "";
						int internalID = Integer.parseInt(tokens[IDCOLUMN]);
						for (Object key : labels.keySet()) {
							Object value = labels.get(key);
							if (internalID == Integer.parseInt(key.toString())) {
								labelUUID = value.toString();
								break;
							}
						}
						// action of broader realation
						if (!tokens[5].equals("")) {
							try {
								String[] split = tokens[5].split(";");
								for (int ii = 0; ii < split.length; ii++) {
									String labelUUID_broader = "";
									int broaderID = Integer.parseInt(split[ii]);
									for (Object key : labels.keySet()) {
										Object value = labels.get(key);
										if (broaderID == Integer.parseInt(key.toString())) {
											labelUUID_broader = value.toString();
											break;
										}
									}
									if (!labelUUID_broader.equals("") && !labelUUID.equals("")) {
										try {
											String label = "";
											// broader
											label = "";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID) + "> ";
											label += "<" + rdf.getPrefixItem("skos:broader") + "> ";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID_broader) + "> ";
											label += ". ";
											TRIPLE_LIST.add(label);
											// narrower
											label = "";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID_broader) + "> ";
											label += "<" + rdf.getPrefixItem("skos:narrower") + "> ";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID) + "> ";
											label += ". ";
											TRIPLE_LIST.add(label);
											//if (validator) {
											//CSV.SPARQLupdateVALIDATOR("labelingsystem", label);
											//}
											importedrelations++;
										} catch (Exception ex) {
											throw new IllegalArgumentException("[ImportcsvResource.java | processRequest()] " + ex.toString());
										}
									} else {
										errorArray.add("ignored: wrong broader-relation id in " + i);
										relationerrors++;
									}
								}
							} catch (Exception e) {
								errorArray.add("ignored: " + e.toString() + " in line " + i);
								relationerrors++;
							}
						}
						if (!tokens[6].equals("")) { // action of narrower realation
							try {
								String[] split = tokens[6].split(";");
								for (int ii = 0; ii < split.length; ii++) {
									String labelUUID_narrower = "";
									int narrowerID = Integer.parseInt(split[ii]);
									for (Object key : labels.keySet()) {
										Object value = labels.get(key);
										if (narrowerID == Integer.parseInt(key.toString())) {
											labelUUID_narrower = value.toString();
											break;
										}
									}
									if (!labelUUID_narrower.equals("") && !labelUUID.equals("")) {
										try {
											String label = "";
											// narrower
											label = "";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID) + "> ";
											label += "<" + rdf.getPrefixItem("skos:narrower") + "> ";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID_narrower) + "> ";
											label += ". ";
											TRIPLE_LIST.add(label);
											// broader
											label = "";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID_narrower) + "> ";
											label += "<" + rdf.getPrefixItem("skos:broader") + "> ";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID) + "> ";
											label += ". ";
											TRIPLE_LIST.add(label);
											importedrelations++;
										} catch (Exception e) {
											throw new CsvLabelImportException(e.toString());
										}
									} else {
										errorArray.add("ignored: wrong narrower-relation id in " + i);
										relationerrors++;
									}
								}
							} catch (Exception e) {
								errorArray.add("ignored: " + e.toString() + " in line " + i);
								relationerrors++;
							}
						}
						if (!tokens[7].equals("")) { // action of related realation
							try {
								String[] split = tokens[7].split(";");
								for (int ii = 0; ii < split.length; ii++) {
									String labelUUID_related = "";
									int relatedID = Integer.parseInt(split[ii]);
									for (Object key : labels.keySet()) {
										Object value = labels.get(key);
										if (relatedID == Integer.parseInt(key.toString())) {
											labelUUID_related = value.toString();
											break;
										}
									}
									if (!labelUUID_related.equals("") && !labelUUID.equals("")) {
										try {
											// related
											String label = "";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID) + "> ";
											label += "<" + rdf.getPrefixItem("skos:related") + "> ";
											label += "<" + rdf.getPrefixItem("ls_lab:" + labelUUID_related) + "> ";
											label += ". ";
											TRIPLE_LIST.add(label);
											// if (validator) {
											//  CSV.SPARQLupdateVALIDATOR("labelingsystem", label);
											//}
											importedrelations++;
										} catch (Exception e) {
											throw new CsvLabelImportException("[ImportcsvResource.java] " + e.toString());
										}
									} else {
										errorArray.add("ignored: wrong related-relation id in " + i);
										relationerrors++;
									}
								}
							} catch (Exception e) {
								errorArray.add("ignored: " + e.toString() + " in line " + i);
								relationerrors++;
							}
						} // end action of broader realation
					} else {
						errorArray.add("ignored: not enough columns (" + tokens.length + " istead of " + COLUMNS + ") in line " + i);
						relationerrors++;
					} // end length query
				}
			} // end not error
			// write triple file and send to sesame
			if (!error) {
				ImportcsvResource.action = "create RDF file...";
				PrintWriter pw = new PrintWriter(ImportcsvResource.SERVER_UPLOAD_LOCATION_FOLDER + ImportcsvResource.FILENAME, "UTF-8");
				for (String TRIPLE_LIST_ITEM : TRIPLE_LIST) {
					pw.println(TRIPLE_LIST_ITEM);
				}
				pw.close();
				if (!validator) {
					RDF4J_20M3.SPARQLupdate(ConfigProperties.getPropertyParam("repository"),
							ConfigProperties.getPropertyParam("ts_server"), "LOAD <" + ImportcsvResource.FILELINK + ">");
				}
			}
		} catch (Exception e) {
			error = true;
			errors++;
			errorArray.add(e.toString());
		} finally {
			outputJSONobject.put("errors", errors);
			outputJSONobject.put("relationerrors", relationerrors);
			if (errors == 0 && relationerrors == 0) {
				outputJSONobject.put("success", "true");
				outputJSONobject.put("filelink", ImportcsvResource.FILELINK);
				System.out.println("filelink: " + ImportcsvResource.FILELINK);
				outputJSONobject.put("context", ImportcsvResource.CONTEXT);
				outputJSONobject.put("importedlabels", importedlabels);
				outputJSONobject.put("importedrelations", importedrelations);
				outputJSONobject.put("triples", TRIPLE_LIST.size());
			} else {
				outputJSONobject.put("success", "false");
			}
			outputJSONobject.put("messages", errorArray);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JSON_OUT = gson.toJson(outputJSONobject);
			if (error) {
				ImportcsvResource.status = 100.0;
			}
			ImportcsvResource.action = "done!"; // finish
			JSON_STRING = JSON_OUT;
			System.out.println("status: " + ImportcsvResource.status + " errors: " + errors + " relationerrors: " + relationerrors + " triples: " + TRIPLE_LIST.size() + " context: " + ImportcsvResource.CONTEXT);
			return JSON_OUT;
		}
	}

}
