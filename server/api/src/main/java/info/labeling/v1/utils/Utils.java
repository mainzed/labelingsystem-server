package info.labeling.v1.utils;

import de.i3mainz.ls.rdfutils.RDF;
import de.i3mainz.ls.rdfutils.exceptions.ConfigException;
import java.io.IOException;

public class Utils {

	public static String getAllElementsForItemID(String item, String itemid) throws ConfigException, IOException {
		RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String query = prefixes + "SELECT * WHERE { ";
		query += item + ":" + itemid + " ?p ?o. } ";
		query += "ORDER BY ASC(?p)";
		return query;
	}

	public static String getAllLabelsForVocabulary(String itemid) throws ConfigException, IOException {
		RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String query = prefixes + "SELECT * WHERE { ";
		query += "?s skos:inScheme ?voc . ";
		query += "?s dc:identifier ?id . ";
		query += "FILTER (?voc=ls_voc:" + itemid+") }";
		return query;
	}

	public static String getSPARQLqueryElementsForRetcatsItem(String itemid) throws ConfigException, IOException {
		RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String query = prefixes + "SELECT ?var ?query ?url WHERE { ";
		query += "?retcat dc:identifier ?identifier. ";
		query += "?retcat ls:retcatsquery ?query. ";
		query += "?retcat ls:retcatsvar ?var. ";
		query += "?retcat dcat:accessURL ?url. ";
		query += "FILTER (?identifier=\"$identifier\") }";
		query = query.replace("$identifier", itemid);
		return query;
	}

}
