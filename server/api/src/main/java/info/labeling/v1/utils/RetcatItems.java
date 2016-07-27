package info.labeling.v1.utils;

import info.labeling.rdf.RDF;
import info.labeling.rdf.Sesame2714;
import info.labeling.exceptions.ResourceNotAvailableException;
import info.labeling.exceptions.SesameSparqlException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

public class RetcatItems {

    private static final String relationsForSKOS = "broadMatch,narrowMatch,relatedMatch,closeMatch,exactMatch";
    private static final String relationsForRESOURCES = "seeAlso,definedBy,sameAs";
    private static final String relationsForMYVOCAB = "broader,narrower,related";
    
    public static List<String[]> getAllItems() throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException, ResourceNotAvailableException {
        List<String[]> retcatList = new ArrayList();
        // GETTY
        retcatList.add(new String[]{"Getty AAT", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/getty/aat", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relationsForSKOS});
        retcatList.add(new String[]{"Getty TGN", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/getty/tgn", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relationsForSKOS});
        retcatList.add(new String[]{"Getty ULAN", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/getty/ulan", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/getty", "//vocab.getty.edu", "getty", relationsForSKOS});
        // HERITAGE DATA
        retcatList.add(new String[]{"Heritage Data Historic England", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/heritagedata/historicengland", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relationsForSKOS});
        retcatList.add(new String[]{"Heritage Data RCAHMS", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/heritagedata/rcahms", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relationsForSKOS});
        retcatList.add(new String[]{"Heritage Data RCAHMW", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/heritagedata/rcahmw", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/heritagedata", "//purl.org/heritagedata/schemes", "heritagedata", relationsForSKOS});
        // CHRONONTOLOGY
        retcatList.add(new String[]{"ChronOntology", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/chronontology", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/chronontology", "//chronontology.dainst.org/period", "chronontology", relationsForSKOS});
        // DBPEDIA
        retcatList.add(new String[]{"DBpedia", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/dbpedia", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/html", "//dbpedia.org/resource", "dbpedia", relationsForRESOURCES});
        // GEONAMES
        retcatList.add(new String[]{"GeoNames", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/geonames", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/geonames", "//sws.geonames.org", "geonames", relationsForRESOURCES});
        // PLEIADES (from Pelagios Periopleo API)
        retcatList.add(new String[]{"Pleiades", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/pelagiospleiadesplaces", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/pelagiospleiadesplaces", "//pleiades.stoa.org/places", "pleiades", relationsForSKOS});
        // OWN VOCABULARY IN LOCAL LABELING SYSTEM
        retcatList.add(new String[]{"My Vocabulary", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/labelingsystem/:id", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/labelingsystem", "//" + PropertiesLocal.getPropertyParam("host"), "vocab", relationsForMYVOCAB});
        // LOCAL LABELING SYSTEM
        retcatList.add(new String[]{"Local Labeling System", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/labelingsystem", PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/labelingsystem", "//" + PropertiesLocal.getPropertyParam("host"), "ls", relationsForSKOS});
        // ADD LOCAL PUBLIC LABELING SYSTEM VOCABULARIES
        RDF rdf = new RDF(PropertiesLocal.getPropertyParam("host"));
        String prefixes = rdf.getPREFIXSPARQL();
        String query = prefixes + "SELECT * WHERE { ?s a ls:Vocabulary. ?s ls:hasReleaseType ls:Public. ?s dc:title ?title. ?s dc:identifier ?id. }";
        List<BindingSet> result = Sesame2714.SPARQLquery(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()), PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()), query);
        List<String> uris = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "s");
        List<String> titles = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "title");
        List<String> ids = Sesame2714.getValuesFromBindingSet_ORDEREDLIST(result, "id");
        if (!result.isEmpty()) {
            for (int i = 0; i < uris.size(); i++) {
                retcatList.add(new String[]{titles.get(i).split("@")[0].replace("\"", ""), PropertiesLocal.getPropertyParam("api") + "/v1/retcat/query/" + ids.get(i), PropertiesLocal.getPropertyParam("api") + "/v1/retcat/label/labelingsystem", "//" + PropertiesLocal.getPropertyParam("host"), "ls", relationsForSKOS});
            }
        }
        return retcatList;
    }

}
