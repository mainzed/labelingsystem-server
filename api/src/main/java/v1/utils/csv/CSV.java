package v1.utils.csv;

import rdf.RDF;
import rdf.RDF4J_20;
import exceptions.CsvLabelImportException;
import exceptions.UniqueIdentifierException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import v1.rest.ImportcsvResource;
import v1.utils.config.ConfigProperties;
import v1.utils.uuid.UniqueIdentifier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.eclipse.rdf4j.query.BindingSet;
import org.json.simple.parser.JSONParser;
import v1.rest.LanguagesResource;

public class CSV implements Runnable {

	public static String JSON_STRING = "";
	private static List<String> TRIPLE_LIST = new ArrayList();
	private static String vocabLanguage = "";
	private static String creator = "";
	private static boolean validation = false;

	@Override
	public void run() {
		try {
			if (ImportcsvResource.validator) {
				Input(ImportcsvResource.csvContent, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, true, true, ImportcsvResource.vocab);
			} else {
				Input(ImportcsvResource.csvContent, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, true, false, ImportcsvResource.vocab);
				if (validation) { // Real Input
					Input(ImportcsvResource.csvContent, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, false, false, ImportcsvResource.vocab);
				}
			}
		} catch (Exception e) {
			Logger.getLogger(CSV.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public static void startImport() {
		try {
			Input(ImportcsvResource.csvContent, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, true, false, ImportcsvResource.vocab);
			if (validation) {
				Input(ImportcsvResource.csvContent, ImportcsvResource.FILENAME, ImportcsvResource.FILELINK, false, false, ImportcsvResource.vocab);
			}
		} catch (Exception e) {
			Logger.getLogger(CSV.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public static String Input(String csvContent, String filename, String filelink, boolean validator, boolean check, String vocab)
			throws CsvLabelImportException, UniqueIdentifierException, IOException {
		JSONObject outputJSONobject = new JSONObject();
		JSONArray errorArray = new JSONArray();
		int errors = 0;
		int importedlabels = 0;
		boolean error = false;
		try {
			if (validator) {
				ImportcsvResource.action = "check file and vocabularies (validate)...";
			} else {
				ImportcsvResource.action = "check file and vocabularies...";
			}
			TRIPLE_LIST.clear();
			// thumbnail[0];description[1];translations[2];END[3]
			String[] csvLines = csvContent.split("\r\n");
			// check if vocabulary exists
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String query = rdf.getPREFIXSPARQL();
			query += "SELECT * WHERE { ?v a ls:Vocabulary . ?v dc:identifier \"" + vocab + "\" . }";
			List<BindingSet> voc_result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			List<String> voc_true = RDF4J_20.getValuesFromBindingSet_ORDEREDLIST(voc_result, "v");
			if (voc_true.size() == 0) {
				error = true;
				errorArray.add("error: vocabulary not found");
				errors++;
			}
			// get vocabulary language
			rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			query = rdf.getPREFIXSPARQL();
			query += "SELECT * WHERE { ?vocab dc:identifier \"" + vocab + "\". ?vocab dc:language ?lang. }";
			List<BindingSet> result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			HashSet<String> langList = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "lang");
			vocabLanguage = (String) langList.toArray()[0];
			if (vocabLanguage == null) {
				error = true;
				errorArray.add("error: no vocabulary language");
				errors++;
			}
			// get creator
			rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			query = rdf.getPREFIXSPARQL();
			query += "SELECT * WHERE { ?vocab dc:identifier \"" + vocab + "\". ?vocab dc:creator ?creator. }";
			result = RDF4J_20.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
			HashSet<String> creatorList = RDF4J_20.getValuesFromBindingSet_UNIQUESET(result, "creator");
			creator = (String) creatorList.toArray()[0];
			if (creator == null) {
				error = true;
				errorArray.add("error: no creator available");
				errors++;
			}
			// check for empty lines
			for (int i = 0; i < csvLines.length; i++) {
				if (csvLines[i].equals("")) {
					errorArray.add("error: empty line found in line " + (i + 1));
					error = true;
					errors++;
				}
			}
			// check for header names
			String[] header = csvLines[0].split("\t");
			if (header[0].contains("thumbnail") && header[1].contains("description") && header[2].contains("translations") && header[3].contains("end")) {
			} else {
				String headerStr = header[0] + "," + header[1] + "," + header[2] + "," + header[3];
				errorArray.add("error: wrong header-names in first line -> " + headerStr);
				error = true;
				errors++;
			}
			// check for line breaks
			if (csvLines.length < 2) {
				errorArray.add("error: linebreaks wrong");
				error = true;
				errors++;
			}
			// check for tabstop or wrong field-count
			for (int i = 0; i < csvLines.length; i++) {
				String[] tokens = csvLines[i].split("[\t]");
				if (tokens.length != 4) {
					errorArray.add("error: delimiter wrong or not enough fields (=4) in line " + (i + 1));
					error = true;
					errors++;
				}
			}
			// thumbnail check
			for (int i = 1; i < csvLines.length; i++) {
				String[] tokens = csvLines[i].split("[\t]");
				if (tokens[0].equals("")) {
					errorArray.add("error: no thumbnail in line " + i);
					error = true;
					errors++;
				}
			}
			// translation check
			for (int i = 1; i < csvLines.length; i++) {
				String[] tokens = csvLines[i].split("[\t]");
				if (!tokens[2].equals("") && !tokens[2].contains(";")) {
					errorArray.add("error: semocolon needed for translations in line " + (i + 1));
					error = true;
					errors++;
				}
				if (tokens[2].contains(";")) {
					if (tokens[2].split(";").length % 2 != 0) {
						errorArray.add("error: language or deliminiter error for translations in line " + (i + 1));
						error = true;
						errors++;
					}
				}
			}
			// language check for translations (same as thumbnail)
			for (int i = 1; i < csvLines.length; i++) {
				String[] tokens = csvLines[i].split("[\t]");
				for (int j = 0; j < tokens.length; j = j + 2) {
					if (tokens[j + 1].equals(vocabLanguage)) {
						errorArray.add("error: translation language is same as thumbnail langauge in line " + (i + 1));
						error = true;
						errors++;
					}
				}
			}
			// language check for translations (allowed)
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LanguagesResource.class.getClassLoader().getResource("languages.json").getFile()), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
			JSONArray jsonArray = (JSONArray) new JSONParser().parse(response.toString());
			List<String> languagesAllowed = new ArrayList();
			for (Object obj : jsonArray) {
				JSONObject lang = (JSONObject) obj;
				languagesAllowed.add((String) lang.get("value"));
			}
			for (int i = 0; i < csvLines.length; i++) {
				String[] tokens = csvLines[i].split("[\t]");
				String[] translations = tokens[2].split(";");
				List<String> languages = new ArrayList();
				for (int j = 0; j < translations.length; j++) {
					if (j % 2 != 0) {
						languages.add(translations[j]);
					}
				}
				for (String lang : languages) {
					if (!languagesAllowed.contains(lang)) {
						errorArray.add("error: a language for a translation not allowed in line " + (i + 1));
						error = true;
						errors++;
					}
				}
			}
			// replacements
			for (int i = 1; i < csvLines.length; i++) {
				String[] tokens = csvLines[i].split("[\t]");
				tokens[0] = tokens[0].replace("\"", "'");
				tokens[1] = tokens[1].replace("\"", "'");
				tokens[2] = tokens[2].replace("\"", "'");
				csvLines[i] = tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + "\t" + tokens[3] + "\r\n";
			}
			if (!error) {
				// create triples
				for (int i = 1; i < csvLines.length; i++) {
					ImportcsvResource.currentStep = ImportcsvResource.currentStep + 1;
					ImportcsvResource.status = ((double) ImportcsvResource.currentStep / (double) ImportcsvResource.maxSteps) * 100;
					if (validator) {
						ImportcsvResource.action = "check and import labels [line " + i + "] (check)...";
					} else {
						ImportcsvResource.action = "check and import labels [line " + i + "] ...";
					}
					try {
						String[] tokens = csvLines[i].split("[\t]");
						String labelID = CSV.createLabelTriples(tokens, creator);
						if (!labelID.equals("")) {
							importedlabels++;
						}
					} catch (Exception e) {
						errorArray.add("error: " + e.toString());
						error = true;
						errors++;
					}
				}
				// send triples to triplestore
				try {
					ImportcsvResource.action = "create RDF file...";
					PrintWriter pw = new PrintWriter(ImportcsvResource.SERVER_UPLOAD_LOCATION_FOLDER + ImportcsvResource.FILENAME, "UTF-8");
					for (String TRIPLE_LIST_ITEM : TRIPLE_LIST) {
						pw.println(TRIPLE_LIST_ITEM);
					}
					pw.close();
					if (!validator) {
						RDF4J_20.SPARQLupdate(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), "LOAD <" + ImportcsvResource.FILELINK + ">");
					}
				} catch (Exception e) {
					errorArray.add("error: " + e.toString());
					error = true;
					errors++;
				}
			}
		} catch (Exception e) {
			error = true;
		} finally {
			if (errors == 0) {
				outputJSONobject.put("importedlabels", importedlabels);
				outputJSONobject.put("triples", TRIPLE_LIST.size());
			} else {
				outputJSONobject.put("errors", errors);
				outputJSONobject.put("messages", errorArray);
			}
			System.out.println(ImportcsvResource.status);
			if (error) {
				ImportcsvResource.status = 100.0;
			}
			ImportcsvResource.action = "done!"; // finish
			String output = outputJSONobject.toString();
			System.out.println("status: " + ImportcsvResource.status + " errors: " + errors + " triples: " + TRIPLE_LIST.size());
			System.out.println(output);
			validation = true;
			JSON_STRING = output;
			return output;
		}
	}

	private static String createLabelTriples(String[] tokens, String creator) throws IOException, UniqueIdentifierException, CsvLabelImportException, exceptions.ConfigException, exceptions.SesameSparqlException {
		String uuid = UniqueIdentifier.getHashID();
		try {
			// thumbnail[0];description[1];translations[2];END[3]
			String thumbnail = tokens[0];
			String description = tokens[1];
			String[] translations = tokens[2].split(";");
			RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
			String label = "";
			// typing
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("rdf:type") + "> ";
			label += "<" + rdf.getPrefixItem("ls:Label") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("rdf:type") + "> ";
			label += "<" + rdf.getPrefixItem("skos:Concept") + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// identifier
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:identifier") + "> ";
			label += "\"" + uuid + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// creator
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:creator") + "> ";
			label += "\"" + creator + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:creator") + "> ";
			label += "<" + rdf.getPrefixItem("ls_age:" + creator) + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// inScheme
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("skos:inScheme") + "> ";
			label += "<" + rdf.getPrefixItem("ls_voc:" + ImportcsvResource.vocab) + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// thumbnail
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("ls:thumbnail") + "> ";
			label += "\"" + thumbnail + "\"@" + vocabLanguage + " ";
			label += ". ";
			TRIPLE_LIST.add(label);
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("skos:prefLabel") + "> ";
			label += "\"" + thumbnail + "\"@" + vocabLanguage + " ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// language
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:language") + "> ";
			label += "\"" + vocabLanguage + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// description
			if (!description.equals("") && description != null) {
				label = "";
				label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
				label += "<" + rdf.getPrefixItem("skos:scopeNote") + "> ";
				label += "\"" + description + "\"@" + vocabLanguage + " ";
				label += ". ";
				TRIPLE_LIST.add(label);
			}
			// translations
			if (translations.length >= 1 && !translations[0].equals("")) {
				for (int i = 0; i < translations.length; i = i + 2) {
					label = "";
					label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
					label += "<" + rdf.getPrefixItem("skos:prefLabel") + "> ";
					label += "\"" + translations[i] + "\"@" + translations[i + 1] + " ";
					label += ". ";
					TRIPLE_LIST.add(label);
				}
			}
			// date
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
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dc:modified") + "> ";
			label += "\"" + d + "\" ";
			label += ". ";
			TRIPLE_LIST.add(label);
			// licence
			label = "";
			label += "<" + rdf.getPrefixItem("ls_lab:" + uuid) + "> ";
			label += "<" + rdf.getPrefixItem("dct:license") + "> ";
			label += "<" + "http://creativecommons.org/licenses/by/4.0/" + "> ";
			label += ". ";
			TRIPLE_LIST.add(label);
		} catch (Exception e) {
			throw new CsvLabelImportException(e.toString());
		}
		return uuid;
	}

}
