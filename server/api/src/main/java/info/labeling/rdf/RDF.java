package info.labeling.rdf;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import info.labeling.exceptions.RdfException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * CLASS for set up a RDF graph and export it
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 27.06.2015
 */
public class RDF {

    private Model model = null;
    private final String PREFIX_LABELINGSYSTEM = "http://labeling.i3mainz.hs-mainz.de/vocab#"; // !!! CHANGE !!!
    private final String PREFIX_SKOS = "http://www.w3.org/2004/02/skos/core#";
    private final String PREFIX_DCTERMS = "http://purl.org/dc/terms/";
    private final String PREFIX_DCELEMENTS = "http://purl.org/dc/elements/1.1/";
    private final String PREFIX_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    private final String PREFIX_OWL = "http://www.w3.org/2002/07/owl#";
    private final String PREFIX_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private final String PREFIX_FOAF = "http://xmlns.com/foaf/0.1/";
    private final String PREFIX_PROV = "http://www.w3.org/ns/prov#";
    private final String PREFIX_XSD = "http://www.w3.org/2001/XMLSchema#";
    private final String PREFIX_GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    private final String PREFIX_DCAT = "http://www.w3.org/ns/dcat#";
    private String Instance_VOCABULARY_ITEM = "http://$host/item/vocabulary/";
    private String Instance_LABEL_ITEM = "http://$host/item/label/";
    private String Instance_AGENT_ITEM = "http://$host/item/agent/";
    private String Instance_REVISION_ITEM = "http://$host/item/revision/";
	private String Instance_RETCATS_ITEM = "http://$host/item/retcats/";
    private String PREFIXSPARQL = ""
            // ls vocabulary
            + "PREFIX ls: <" + PREFIX_LABELINGSYSTEM + "> "
            // instances
            + "PREFIX ls_voc: <" + Instance_VOCABULARY_ITEM + "> "
            + "PREFIX ls_lab: <" + Instance_LABEL_ITEM + "> "
            + "PREFIX ls_age: <" + Instance_AGENT_ITEM + "> "
            + "PREFIX ls_rev: <" + Instance_REVISION_ITEM + "> "
			+ "PREFIX ls_ret: <" + Instance_RETCATS_ITEM + "> "
            // other ontologies
            + "PREFIX skos: <" + PREFIX_SKOS + "> "
            + "PREFIX rdf: <" + PREFIX_RDF + "> "
            + "PREFIX rdfs: <" + PREFIX_RDFS + "> "
            + "PREFIX dc: <" + PREFIX_DCELEMENTS + "> "
            + "PREFIX dct: <" + PREFIX_DCTERMS + "> "
            + "PREFIX owl: <" + PREFIX_OWL + "> "
            + "PREFIX xsd: <" + PREFIX_XSD + "> "
            + "PREFIX prov: <" + PREFIX_PROV + "> "
            + "PREFIX foaf: <" + PREFIX_FOAF + "> "
            + "PREFIX geo: <" + PREFIX_GEO + "> "
            + "PREFIX dcat: <" + PREFIX_DCAT + "> ";

    public RDF(String HOST) throws IOException {
        model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ls", PREFIX_LABELINGSYSTEM);
        model.setNsPrefix("skos", PREFIX_SKOS);
        model.setNsPrefix("dct", PREFIX_DCTERMS);
        model.setNsPrefix("dc", PREFIX_DCELEMENTS);
        model.setNsPrefix("rdfs", PREFIX_RDFS);
        model.setNsPrefix("owl", PREFIX_OWL);
        model.setNsPrefix("rdf", PREFIX_RDF);
        model.setNsPrefix("foaf", PREFIX_FOAF);
        model.setNsPrefix("prov", PREFIX_PROV);
        model.setNsPrefix("xsd", PREFIX_XSD);
        model.setNsPrefix("geo", PREFIX_GEO);
        model.setNsPrefix("dcat", PREFIX_DCAT);
        Instance_VOCABULARY_ITEM = Instance_VOCABULARY_ITEM.replace("$host", HOST);
        Instance_LABEL_ITEM = Instance_LABEL_ITEM.replace("$host", HOST);
        Instance_AGENT_ITEM = Instance_AGENT_ITEM.replace("$host", HOST);
        Instance_REVISION_ITEM = Instance_REVISION_ITEM.replace("$host", HOST);
		Instance_RETCATS_ITEM = Instance_RETCATS_ITEM.replace("$host", HOST);
        PREFIXSPARQL = PREFIXSPARQL.replace("$host", HOST);
    }

    public Model getModelObject() {
        return model;
    }

    public String getPrefixItem(String shortDesc) {
        if (shortDesc.startsWith("ls:")) {
            return shortDesc.replace("ls:", PREFIX_LABELINGSYSTEM);
        } else if (shortDesc.startsWith("skos:")) {
            return shortDesc.replace("skos:", PREFIX_SKOS);
        } else if (shortDesc.startsWith("dct:")) {
            return shortDesc.replace("dct:", PREFIX_DCTERMS);
        } else if (shortDesc.startsWith("dc:")) {
            return shortDesc.replace("dc:", PREFIX_DCELEMENTS);
        } else if (shortDesc.startsWith("rdfs:")) {
            return shortDesc.replace("rdfs:", PREFIX_RDFS);
        } else if (shortDesc.startsWith("owl:")) {
            return shortDesc.replace("owl:", PREFIX_OWL);
        } else if (shortDesc.startsWith("rdf:")) {
            return shortDesc.replace("rdf:", PREFIX_RDF);
        } else if (shortDesc.startsWith("foaf:")) {
            return shortDesc.replace("foaf:", PREFIX_FOAF);
        } else if (shortDesc.startsWith("prov:")) {
            return shortDesc.replace("prov:", PREFIX_PROV);
        } else if (shortDesc.startsWith("xsd:")) {
            return shortDesc.replace("xsd:", PREFIX_XSD);
        } else if (shortDesc.startsWith("geo:")) {
            return shortDesc.replace("geo:", PREFIX_GEO);
        } else if (shortDesc.startsWith("dcat:")) {
            return shortDesc.replace("dcat:", PREFIX_DCAT);
        } else if (shortDesc.startsWith("ls_voc:")) {
            return shortDesc.replace("ls_voc:", Instance_VOCABULARY_ITEM);
        } else if (shortDesc.startsWith("ls_lab:")) {
            return shortDesc.replace("ls_lab:", Instance_LABEL_ITEM);
        } else if (shortDesc.startsWith("ls_age:")) {
            return shortDesc.replace("ls_age:", Instance_AGENT_ITEM);
        } else if (shortDesc.startsWith("ls_rev:")) {
            return shortDesc.replace("ls_rev:", Instance_REVISION_ITEM);
        } else if (shortDesc.startsWith("ls_ret:")) {
            return shortDesc.replace("ls_ret:", Instance_RETCATS_ITEM);
        } else {
            return shortDesc;
        }
    }

    public String getPREFIXSPARQL() {
        return PREFIXSPARQL;
    }

    /**
     * set triple with literal
     *
     * @param subject
     * @param predicate
     * @param object
     * @throws de.i3mainz.ls.rdfutils.exceptions.RdfException
     */
    public void setModelLiteral(String subject, String predicate, String object) throws RdfException {
        try {
            Resource s = model.createResource(getPrefixItem(subject));
            Property p = model.createProperty(getPrefixItem(predicate));
            Literal o = model.createLiteral(object);
            model.add(s, p, o);
        } catch (Exception e) {
            throw new RdfException("[" + RDF.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
        }
    }

    /**
     * set triple with literal and language
     *
     * @param subject
     * @param predicate
     * @param object
     * @param lang
     * @throws de.i3mainz.ls.rdfutils.exceptions.RdfException
     */
    public void setModelLiteralLanguage(String subject, String predicate, String object, String lang) throws RdfException {
        try {
            Resource s = model.createResource(getPrefixItem(subject));
            Property p = model.createProperty(getPrefixItem(predicate));
            Literal o = model.createLiteral(object, lang);
            model.add(s, p, o);
        } catch (Exception e) {
            throw new RdfException("[" + RDF.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
        }
    }

    /**
     * set triple with uri
     *
     * @param subject
     * @param predicate
     * @param object
     * @throws de.i3mainz.ls.rdfutils.exceptions.RdfException
     */
    public void setModelURI(String subject, String predicate, String object) throws RdfException {
        try {
            Resource s = model.createResource(getPrefixItem(subject));
            Property p = model.createProperty(getPrefixItem(predicate));
            Resource o = model.createResource(getPrefixItem(object));
            model.add(s, p, o);
        } catch (Exception e) {
            throw new RdfException("[" + RDF.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
        }
    }

    /**
     * set triple and create model statement automaticly
     *
     * @param subject
     * @param predicate
     * @param object
     * @throws RdfException
     */
    public void setModelTriple(String subject, String predicate, String object) throws RdfException {
        try {
            if (object.startsWith("http://") || object.contains("mailto")) {
                setModelURI(subject, predicate, object);
            } else if (object.contains("@")) {
                String literalLanguage[] = object.split("@");
                setModelLiteralLanguage(subject, predicate, literalLanguage[0].replaceAll("\"", ""), literalLanguage[1]);
            } else {
                setModelLiteral(subject, predicate, object.replaceAll("\"", ""));
            }
        } catch (Exception e) {
            throw new RdfException("[" + RDF.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
        }
    }

    /**
     * get RDF model as RDF/XML
     *
     * @return
     * @throws de.i3mainz.ls.rdfutils.exceptions.RdfException
     */
    public String getModel() throws RdfException {
        try {
            JenaJSONLD.init();
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            model.write(o, "RDF/XML");
            model.removeAll();
            return o.toString("UTF-8");
        } catch (Exception e) {
            throw new RdfException("[" + RDF.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
        }
    }

    /**
     * get RDF model in several formats
     * [Turtle,N-Triples,RDF/XML,RDF/JSON,TriG,NQuads]
     *
     * @param format
     * @return
     * @throws UnsupportedEncodingException
     * @throws de.i3mainz.ls.rdfutils.exceptions.RdfException
     */
    public String getModel(String format) throws UnsupportedEncodingException, RdfException {
        // https://jena.apache.org/documentation/io/rdf-output.html#jena_model_write_formats
        try {
            JenaJSONLD.init();
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            model.write(o, format);
            model.removeAll();
            return o.toString("UTF-8");
        } catch (Exception e) {
            throw new RdfException("[" + RDF.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + e + "]");
        }
    }

}
