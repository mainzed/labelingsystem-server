package de.i3mainz.ls.rdfutils;

import de.i3mainz.ls.rdfutils.exceptions.NoInputException;
import de.i3mainz.ls.rdfutils.exceptions.SesameSparqlException;
import de.i3mainz.ls.Config.Config;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletOutputStream;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;

/**
 * CLASS to handle SPARQL queries and update to sesame triplestore
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 18.02.2015
 */
public class Sesame2714 {

    /**
     * send SPARQL UPDATE to sesame triplestore repository
     *
     * @param repositoryID
     * @param updateString
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws UpdateExecutionException
     * @throws de.i3mainz.ls.rdfutils.exceptions.SesameSparqlException
     */
    public static void SPARQLupdate(String repositoryID, String SesameServer, String updateString) throws RepositoryException, MalformedQueryException, UpdateExecutionException, SesameSparqlException {
        try {
            // GET NUMBER OF STATEMENTS
            int before = getNumberOfStatements(repositoryID, SesameServer);
            // SEND UPDATE
            Repository repo = new HTTPRepository(SesameServer, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            Update update = con.prepareUpdate(QueryLanguage.SPARQL, updateString);
            update.execute();
            con.close();
            // GET NUMBER OF STATEMENTS (CHECK)
            int after = getNumberOfStatements(repositoryID, SesameServer);
            if (before == after) {
                throw new NoInputException("NoInputException: [" + Sesame2714.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + "]");
            }
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        }
    }

    /**
     * send SPARQL QUERY to sesame triplestore repository
     *
     * @param repositoryID
     * @param SesameSever
     * @param queryString
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws de.i3mainz.ls.rdfutils.exceptions.SesameSparqlException
     */
    public static List<BindingSet> SPARQLquery(String repositoryID, String SesameSever, String queryString) throws RepositoryException, MalformedQueryException, QueryEvaluationException, SesameSparqlException {
		List<BindingSet> BindingList = new ArrayList<BindingSet>();
        try {
            Repository repo = new HTTPRepository(SesameSever, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = tupleQuery.evaluate();
            while (result.hasNext()) {
                BindingList.add(result.next());
            }
            result.close();
            con.close();
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        } finally {
            return BindingList;
        }
    }

    /**
     * get list of results by variable as ordered list
     *
     * @param result
     * @param var
     * @return
     * @throws QueryEvaluationException
     * @throws SesameSparqlException
     */
    public static List<String> getValuesFromBindingSet_ORDEREDLIST(List<BindingSet> result, String var) throws QueryEvaluationException, SesameSparqlException {
        List<String> ValueList = new ArrayList<String>();
        try {
            for (BindingSet result1 : result) {
                Value value = result1.getValue(var);
                if (value == null) {
                    ValueList.add(null);
                } else {
                    String valuestring = value.toString();
                    if (valuestring.startsWith("http://") || valuestring.contains("mailto") || valuestring.startsWith("https://")) {
                        valuestring = valuestring.substring(0, valuestring.length());
                    } else if (valuestring.contains("@")) {
                        valuestring = "\"" + valuestring.substring(1, valuestring.length());
                    } else if (valuestring.contains("^^<http://www.w3.org/2001/XMLSchema#string>")) {
                        valuestring = valuestring.replace("^^<http://www.w3.org/2001/XMLSchema#string>", "");
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    } else {
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    }
                    ValueList.add(valuestring);
                }
            }
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        } finally {
            return ValueList;
        }
    }

    /**
     * get list of results by variable as unique set
     *
     * @param result
     * @param var
     * @return
     * @throws QueryEvaluationException
     * @throws SesameSparqlException
     */
    public static HashSet<String> getValuesFromBindingSet_UNIQUESET(List<BindingSet> result, String var) throws QueryEvaluationException, SesameSparqlException {
        HashSet<String> ValueList = new HashSet<String>();
        try {
            for (BindingSet result1 : result) {
                Value value = result1.getValue(var);
                if (value == null) {
                    ValueList.add(null);
                } else {
                    String valuestring = value.toString();
                    if (valuestring.startsWith("http://") || valuestring.contains("mailto") || valuestring.startsWith("https://")) {
                        valuestring = valuestring.substring(0, valuestring.length());
                    } else if (valuestring.contains("@")) {
                        valuestring = "\"" + valuestring.substring(1, valuestring.length());
                    } else if (valuestring.contains("^^<http://www.w3.org/2001/XMLSchema#string>")) {
                        valuestring = valuestring.replace("^^<http://www.w3.org/2001/XMLSchema#string>", "");
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    } else {
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    }
                    ValueList.add(valuestring);
                }
            }
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        } finally {
            return ValueList;
        }
    }

    public static ServletOutputStream SPARQLqueryOutputFile(String repositoryID, String SesameSever, String queryString, String format, ServletOutputStream out) throws SesameSparqlException {
        try {
            Repository repo = new HTTPRepository(SesameSever, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            if ("xml".equals(format) || "XML".equals(format) || "Xml".equals(format)) {
                SPARQLResultsXMLWriter sparqlWriterXML = new SPARQLResultsXMLWriter(out);
                tupleQuery.evaluate(sparqlWriterXML);
            } else if ("json".equals(format) || "JSON".equals(format) || "Json".equals(format)) {
                SPARQLResultsJSONWriter sparqlWriterJSON = new SPARQLResultsJSONWriter(out);
                tupleQuery.evaluate(sparqlWriterJSON);
            } else if ("csv".equals(format) || "CSV".equals(format) || "Csv".equals(format)) {
                SPARQLResultsCSVWriter sparqlWriterCSV = new SPARQLResultsCSVWriter(out);
                tupleQuery.evaluate(sparqlWriterCSV);
            } else if ("tsv".equals(format) || "TSV".equals(format) || "Tsv".equals(format)) {
                SPARQLResultsTSVWriter sparqlWriterTSV = new SPARQLResultsTSVWriter(out);
                tupleQuery.evaluate(sparqlWriterTSV);
            } else {
                SPARQLResultsJSONWriter sparqlWriter = new SPARQLResultsJSONWriter(out);
                tupleQuery.evaluate(sparqlWriter);
            }
            con.close();
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        }
        return out;
    }
    
    public static OutputStream SPARQLqueryOutputFileOS(String repositoryID, String SesameSever, String queryString, String format, OutputStream out) throws SesameSparqlException {
        try {
            Repository repo = new HTTPRepository(SesameSever, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            if ("xml".equals(format) || "XML".equals(format) || "Xml".equals(format)) {
                SPARQLResultsXMLWriter sparqlWriterXML = new SPARQLResultsXMLWriter(out);
                tupleQuery.evaluate(sparqlWriterXML);
            } else if ("json".equals(format) || "JSON".equals(format) || "Json".equals(format)) {
                SPARQLResultsJSONWriter sparqlWriterJSON = new SPARQLResultsJSONWriter(out);
                tupleQuery.evaluate(sparqlWriterJSON);
            } else if ("csv".equals(format) || "CSV".equals(format) || "Csv".equals(format)) {
                SPARQLResultsCSVWriter sparqlWriterCSV = new SPARQLResultsCSVWriter(out);
                tupleQuery.evaluate(sparqlWriterCSV);
            } else if ("tsv".equals(format) || "TSV".equals(format) || "Tsv".equals(format)) {
                SPARQLResultsTSVWriter sparqlWriterTSV = new SPARQLResultsTSVWriter(out);
                tupleQuery.evaluate(sparqlWriterTSV);
            } else {
                SPARQLResultsJSONWriter sparqlWriter = new SPARQLResultsJSONWriter(out);
                tupleQuery.evaluate(sparqlWriter);
            }
            con.close();
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        }
        return out;
    }

    /**
     * get number of triples in labelingsystem repository
     *
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private static int getNumberOfStatements(String repositoryID, String SesameServer) throws MalformedURLException, IOException, SesameSparqlException {
        try {
            String size_url = SesameServer + "/repositories/" + repositoryID + "/size";
            URL obj = new URL(size_url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            int size = Integer.parseInt(response.toString());
            return size;
        } catch (Exception e) {
            throw new SesameSparqlException();
        }
    }

    public static void inputRDFfromJSONLDString(String repositoryID, String SesameServer, String JSONLD) throws SesameSparqlException {
        try {
            // GET NUMBER OF STATEMENTS
            int before = getNumberOfStatements(repositoryID, SesameServer);
            // SEND UPDATE
            Repository repo = new HTTPRepository(SesameServer, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            InputStream stream = new ByteArrayInputStream(JSONLD.getBytes(StandardCharsets.UTF_8));
            con.add(stream, "http://labeling.i3mainz.de#", RDFFormat.JSONLD);
            con.close();
            // GET NUMBER OF STATEMENTS (CHECK)
            int after = getNumberOfStatements(repositoryID, SesameServer);
            if (before == after) {
                throw new NoInputException("NoInputException: [" + Sesame2714.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + "]");
            }
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        }
    }

    public static void inputRDFfromRDFJSONString(String repositoryID, String SesameServer, String RDFJSON) throws SesameSparqlException {
        try {
            // GET NUMBER OF STATEMENTS
            int before = getNumberOfStatements(repositoryID, SesameServer);
            // SEND UPDATE
            Repository repo = new HTTPRepository(SesameServer, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            InputStream stream = new ByteArrayInputStream(RDFJSON.getBytes(StandardCharsets.UTF_8));
            con.add(stream, "http://dummy.org#", RDFFormat.RDFJSON);
            con.close();
            // GET NUMBER OF STATEMENTS (CHECK)
            int after = getNumberOfStatements(repositoryID, SesameServer);
            if (before == after) {
                throw new NoInputException("NoInputException: [" + Sesame2714.class.getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + "]");
            }
        } catch (Exception e) {
            throw new SesameSparqlException(e.getMessage());
        }
    }

}
