package info.labeling.v1.utils;

public class Utils {

    private static final String PREFIXSPARQLUPDATE = ""
            // ls vocabulary
            + "PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#> "
            // instances
            + "PREFIX ls_pro: <http://labeling.i3mainz.hs-mainz.de/item/project/> "
            + "PREFIX ls_voc: <http://labeling.i3mainz.hs-mainz.de/item/vocabulary/> "
            + "PREFIX ls_lab: <http://labeling.i3mainz.hs-mainz.de/item/label/> "
            + "PREFIX ls_age: <http://labeling.i3mainz.hs-mainz.de/item/agent/> "
            + "PREFIX ls_gui: <http://labeling.i3mainz.hs-mainz.de/item/gui/> "
            + "PREFIX ls_rev: <http://labeling.i3mainz.hs-mainz.de/item/revision/> "
            // other ontologies
            + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
            + "PREFIX dct: <http://purl.org/dc/terms/> "
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";

    public static String getPREFIXSPARQL() {
        return PREFIXSPARQLUPDATE;
    }

}