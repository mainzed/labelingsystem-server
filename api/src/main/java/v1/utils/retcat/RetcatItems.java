package v1.utils.retcat;

import rdf.RDF;
import exceptions.ResourceNotAvailableException;
import exceptions.SesameSparqlException;
import rdf.RDF4J_20M3;
import v1.utils.config.ConfigProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

public class RetcatItems {

	public static List<RetcatItem> getAllRetcatItems() throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
		List<RetcatItem> retcatList = new ArrayList();
		// LOCAL LABELING SYSTEM
		retcatList.add(new RetcatItem("Local Labeling System", 
				"The local Labeling System vocabularies.", 
				"/v1/retcat/query/labelingsystem", "/v1/retcat/label/labelingsystem", "//" + ConfigProperties.getPropertyParam("host"), "common reference thesauri (CH)", "ls", "div", "high"));
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
				retcatList.add(new RetcatItem(titles.get(i).split("@")[0].replace("\"", ""), descriptions.get(i).split("@")[0].replace("\"", ""), "/v1/retcat/query/labelingsystem/" + ids.get(i), "/v1/retcat/label/labelingsystem", "//" + ConfigProperties.getPropertyParam("host"), "common reference thesauri (CH)", "ls", "div","high"));
			}
		}*/
		// SKOS CONCEPTS
		// GETTY
		retcatList.add(new RetcatItem("Getty AAT", 
				"AAT is a structured vocabulary, including terms, descriptions, and other metadata for generic concepts related to art, architecture, conservation, archaeology, and other cultural heritage. Included are work types, styles, materials, techniques, and others.",
				"/v1/retcat/query/getty/aat", "/v1/retcat/label/getty", "//vocab.getty.edu", "common reference thesauri (CH)", "getty", "en", "high"));
		retcatList.add(new RetcatItem("Getty TGN", 
				"TGN is a structured vocabulary, including names, descriptions, and other metadata for extant and historical cities, empires, archaeological sites, and physical features important to research of art and architecture. TGN may be linked to GIS, maps, and other geographic resources.", 
				"/v1/retcat/query/getty/tgn", "/v1/retcat/label/getty", "//vocab.getty.edu", "common reference thesauri (CH)", "getty", "en", "high"));
		retcatList.add(new RetcatItem("Getty ULAN", 
				"ULAN is a structured vocabulary, including names, biographies, related people, and other metadata about artists, architects, firms, studios, museums, patrons, sitters, and other people and groups involved in the creation and study of art and architecture.", 
				"/v1/retcat/query/getty/ulan", "/v1/retcat/label/getty", "//vocab.getty.edu", "common reference thesauri (CH)", "getty", "en", "high"));
		// HERITAGE DATA
		retcatList.add(new RetcatItem("Heritage Data Historic England", 
				"FISH Archaeological Sciences Thesaurus + FISH Building Materials Thesaurus + FISH Event Types Thesaurus + FISH Archaeological Objects Thesaurus + FISH Maritime Craft Types Thesaurus FISH Thesaurus of Monument Types + Historic England Periods Authority File + Components + Evidence.", 
				"/v1/retcat/query/heritagedata/historicengland", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "common reference thesauri (CH)", "heritagedata", "en", "high"));
		retcatList.add(new RetcatItem("Heritage Data RCAHMS", 
				"Objects made by human activity + types of craft that survive as wrecks, or are documented as losses, in Scottish maritime waters + monument types relating to the archaeological and built heritage of Scotland. The terminology also includes Scottish Gaelic translations for some terms.", 
				"/v1/retcat/query/heritagedata/rcahms", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "common reference thesauri (CH)", "heritagedata", "en", "high"));
		retcatList.add(new RetcatItem("Heritage Data RCAHMW", 
				"A list of periods for use in Wales + classification of monument types in Wales by function.", 
				"/v1/retcat/query/heritagedata/rcahmw", "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "common reference thesauri (CH)", "heritagedata", "en", "high"));
		// CHRONONTOLOGY
		retcatList.add(new RetcatItem("ChronOntology", 
				"ChronOntology is a time gazetteer web service that assumes a role similar to that of place gazetteers but for temporal concepts and cultural periods as well a system for storing, managing, mapping and making accessible descriptions of temporal concepts.", 
				"/v1/retcat/query/chronontology", "/v1/retcat/label/chronontology", "//chronontology.dainst.org/period", "common reference thesauri (CH)", "chronontology", "en", "high"));
		// PLEIADES (from Pelagios Periopleo API)
		retcatList.add(new RetcatItem("Pleiades", 
				"Pleiades is a community-built gazetteer and graph of ancient places. It publishes authoritative information about ancient places and spaces, providing unique services for finding, displaying, and reusing that information under open license.", 
				"/v1/retcat/query/pelagiospleiadesplaces", "/v1/retcat/label/pelagiospleiadesplaces", "//pleiades.stoa.org/places", "common reference thesauri (CH)", "pleiades", "en", "high"));
		// SKOSMOS
		retcatList.add(new RetcatItem("FINTO - Finnish Thesaurus and Ontology Service", 
				"Finto is a Finnish thesaurus and ontology service, which enables both the publication and browsing of vocabularies. The service also offers interfaces for integrating the thesauri and ontologies into other applications and systems.", 
				"/v1/retcat/query/skosmos/finto", "/v1/retcat/label/skosmos/finto", "//www.yso.fi", "other reference thesauri", "finto", "en", "low"));
		retcatList.add(new RetcatItem("FAO - AGROVOC Multilingual agricultural thesaurus", 
				"AGROVOC is a controlled vocabulary covering all areas of interest of the Food and Agriculture Organization (FAO) of the United Nations, including food, nutrition, agriculture, fisheries, forestry, environment etc. It is published by FAO and edited by a community of experts.", 
				"/v1/retcat/query/skosmos/fao", "/v1/retcat/label/skosmos/fao", "//aims.fao.org", "other reference thesauri", "fao", "en", "low"));
		retcatList.add(new RetcatItem("UNESCO Thesaurus", 
				"TThe UNESCO Thesaurus is a controlled and structured list of terms used in subject analysis and retrieval of documents and publications in the fields of education, culture, natural sciences, social and human sciences, communication and information. Continuously enriched and updated, its multidisciplinary terminology reflects the evolution of UNESCO's programmes and activities.", 
				"/v1/retcat/query/skosmos/unesco", "/v1/retcat/label/skosmos/unesco", "//vocabularies.unesco.org", "other reference thesauri", "unesco", "en", "high"));
		// INTERPRET AS SKOS CONCEPT
		// DBPEDIA
		retcatList.add(new RetcatItem("DBpedia", 
				"DBpedia is a project aiming to extract structured content from the information created as part of the Wikipedia project. ", 
				"/v1/retcat/query/dbpedia", "/v1/retcat/label/html", "//dbpedia.org/resource", "additional information", "dbpedia", "de", "low"));
		// GEONAMES
		retcatList.add(new RetcatItem("GeoNames", 
				"GeoNames is a geographical database available and accessible through various web services, under a Creative Commons attribution license.", 
				"/v1/retcat/query/geonames", "/v1/retcat/label/geonames", "//sws.geonames.org", "additional information", "geonames", "en", "low"));
		return retcatList;
	}

}
