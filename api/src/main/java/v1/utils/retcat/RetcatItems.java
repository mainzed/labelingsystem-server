package v1.utils.retcat;

import info.labeling.rdf.RDF;
import info.labeling.exceptions.ResourceNotAvailableException;
import info.labeling.exceptions.SesameSparqlException;
import info.labeling.rdf.RDF4J_20M3;
import info.labeling.v1.utils.ConfigProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

public class RetcatItems {

	private static final String relTypeSKOS = "concept";
	private static final String relTypeWayback = "wayback";

	public static List<String[]> getAllItems() throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
		List<String[]> retcatList = new ArrayList();

		// LABELING SYSTEM
		// LOCAL LABELING SYSTEM
		retcatList.add(new String[]{"Local Labeling System", ConfigProperties.getPropertyParam("api") + "/v1/retcat/query/labelingsystem", ConfigProperties.getPropertyParam("api") + "/v1/retcat/label/labelingsystem", "//" + ConfigProperties.getPropertyParam("host"), "ls", relTypeSKOS, ""});
		// ADD LOCAL PUBLIC LABELING SYSTEM VOCABULARIES
		RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String query = prefixes + "SELECT * WHERE { ?s a ls:Vocabulary. ?s ls:hasReleaseType ls:Public. ?s dc:title ?title. ?s dc:identifier ?id. }";
		List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
		List<String> uris = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "s");
		List<String> titles = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "title");
		List<String> ids = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "id");
		if (!result.isEmpty()) {
			for (int i = 0; i < uris.size(); i++) {
				retcatList.add(new String[]{titles.get(i).split("@")[0].replace("\"", ""), "/v1/retcat/query/labelingsystem/" + ids.get(i), "/v1/retcat/label/labelingsystem", "//" + ConfigProperties.getPropertyParam("host"), "ls", relTypeSKOS, titles.get(i).split("@")[1].replace("\"", "")});
			}
		}

		// SKOS CONCEPTS
		// GETTY
		retcatList.add(new String[]{"Getty AAT", "/v1/retcat/query/getty/aat", "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relTypeSKOS, "en"});
		retcatList.add(new String[]{"Getty TGN", "/v1/retcat/query/getty/tgn", "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relTypeSKOS, "en"});
		retcatList.add(new String[]{"Getty ULAN", "/v1/retcat/query/getty/ulan", "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relTypeSKOS, "en"});
		// HERITAGE DATA
		retcatList.add(new String[]{"Heritage Data Historic England", "/v1/retcat/query/heritagedata/historicengland", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relTypeSKOS, "en"});
		retcatList.add(new String[]{"Heritage Data RCAHMS", "/v1/retcat/query/heritagedata/rcahms", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relTypeSKOS, "en"});
		retcatList.add(new String[]{"Heritage Data RCAHMW", "/v1/retcat/query/heritagedata/rcahmw", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relTypeSKOS, "en"});
		// CHRONONTOLOGY
		retcatList.add(new String[]{"ChronOntology", "/v1/retcat/query/chronontology", "/v1/retcat/label/chronontology", "//chronontology.dainst.org/period", "chronontology", relTypeSKOS, "en"});
		// PLEIADES (from Pelagios Periopleo API)
		retcatList.add(new String[]{"Pleiades", "/v1/retcat/query/pelagiospleiadesplaces", "/v1/retcat/label/pelagiospleiadesplaces", "//pleiades.stoa.org/places", "pleiades", relTypeSKOS, "en"});
		// SKOSMOS
		retcatList.add(new String[]{"FINTO - Finnish Thesaurus and Ontology Service", "/v1/retcat/query/skosmos/finto", "/v1/retcat/label/skosmos/finto", "//www.yso.fi", "finto", relTypeSKOS, "en"});
		retcatList.add(new String[]{"FAO - AGROVOC Multilingual agricultural thesaurus", "/v1/retcat/query/skosmos/fao", "/v1/retcat/label/skosmos/fao", "//aims.fao.org", "fao", relTypeSKOS, "en"});

		// INTERPRET AS SKOS CONCEPT
		// DBPEDIA
		retcatList.add(new String[]{"DBpedia", "/v1/retcat/query/dbpedia", "/v1/retcat/label/html", "//dbpedia.org/resource", "dbpedia", relTypeSKOS, "de"});
		// GEONAMES
		retcatList.add(new String[]{"GeoNames", "/v1/retcat/query/geonames", "/v1/retcat/label/geonames", "//sws.geonames.org", "geonames", relTypeSKOS, "en"});
		return retcatList;
	}

	public static List<RetcatItem> getAllRetcatItems() throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
		List<RetcatItem> retcatList = new ArrayList();
		// LOCAL LABELING SYSTEM
		retcatList.add(new RetcatItem("Local Labeling System", "", ConfigProperties.getPropertyParam("api") + "/v1/retcat/query/labelingsystem", ConfigProperties.getPropertyParam("api") + "/v1/retcat/label/labelingsystem", "//" + ConfigProperties.getPropertyParam("host"), "ls", relTypeSKOS, ""));
		// ADD LOCAL PUBLIC LABELING SYSTEM VOCABULARIES
		/*RDF rdf = new RDF(ConfigProperties.getPropertyParam("host"));
		String prefixes = rdf.getPREFIXSPARQL();
		String query = prefixes + "SELECT * WHERE { ?s a ls:Vocabulary. ?s ls:hasReleaseType ls:Public. ?s dc:title ?title. ?s dc:identifier ?id. }";
		List<BindingSet> result = RDF4J_20M3.SPARQLquery(ConfigProperties.getPropertyParam("repository"), ConfigProperties.getPropertyParam("ts_server"), query);
		List<String> uris = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "s");
		List<String> titles = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "title");
		List<String> descriptions = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "description");
		List<String> ids = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(result, "id");
		if (!result.isEmpty()) {
			for (int i = 0; i < uris.size(); i++) {
				retcatList.add(new RetcatItem(titles.get(i).split("@")[0].replace("\"", ""), descriptions.get(i).split("@")[0].replace("\"", ""), "/v1/retcat/query/labelingsystem/" + ids.get(i), "/v1/retcat/label/labelingsystem", "//" + ConfigProperties.getPropertyParam("host"), "ls", relTypeSKOS, titles.get(i).split("@")[1].replace("\"", "")));
			}
		}*/
		// SKOS CONCEPTS
		// GETTY
		retcatList.add(new RetcatItem("Getty AAT", "", "/v1/retcat/query/getty/aat", "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relTypeSKOS, "en"));
		retcatList.add(new RetcatItem("Getty TGN", "", "/v1/retcat/query/getty/tgn", "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relTypeSKOS, "en"));
		retcatList.add(new RetcatItem("Getty ULAN", "", "/v1/retcat/query/getty/ulan", "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relTypeSKOS, "en"));
		// HERITAGE DATA
		retcatList.add(new RetcatItem("Heritage Data Historic England", "", "/v1/retcat/query/heritagedata/historicengland", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relTypeSKOS, "en"));
		retcatList.add(new RetcatItem("Heritage Data RCAHMS", "", "/v1/retcat/query/heritagedata/rcahms", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relTypeSKOS, "en"));
		retcatList.add(new RetcatItem("Heritage Data RCAHMW", "", "/v1/retcat/query/heritagedata/rcahmw", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relTypeSKOS, "en"));
		// CHRONONTOLOGY
		retcatList.add(new RetcatItem("ChronOntology", "", "/v1/retcat/query/chronontology", "/v1/retcat/label/chronontology", "//chronontology.dainst.org/period", "chronontology", relTypeSKOS, "en"));
		// PLEIADES (from Pelagios Periopleo API)
		retcatList.add(new RetcatItem("Pleiades", "", "/v1/retcat/query/pelagiospleiadesplaces", "/v1/retcat/label/pelagiospleiadesplaces", "//pleiades.stoa.org/places", "pleiades", relTypeSKOS, "en"));
		// SKOSMOS
		retcatList.add(new RetcatItem("FINTO - Finnish Thesaurus and Ontology Service", "", "/v1/retcat/query/skosmos/finto", "/v1/retcat/label/skosmos/finto", "//www.yso.fi", "finto", relTypeSKOS, "en"));
		retcatList.add(new RetcatItem("FAO - AGROVOC Multilingual agricultural thesaurus", "", "/v1/retcat/query/skosmos/fao", "/v1/retcat/label/skosmos/fao", "//aims.fao.org", "fao", relTypeSKOS, "en"));
		// INTERPRET AS SKOS CONCEPT
		// DBPEDIA
		retcatList.add(new RetcatItem("DBpedia", "", "/v1/retcat/query/dbpedia", "/v1/retcat/label/html", "//dbpedia.org/resource", "dbpedia", relTypeSKOS, "de"));
		// GEONAMES
		retcatList.add(new RetcatItem("GeoNames", "", "/v1/retcat/query/geonames", "/v1/retcat/label/geonames", "//sws.geonames.org", "geonames", relTypeSKOS, "en"));
		return retcatList;
	}

}
