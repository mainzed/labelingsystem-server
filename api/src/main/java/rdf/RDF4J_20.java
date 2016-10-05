package rdf;

import exceptions.NoInputException;
import exceptions.SparqlParseException;
import exceptions.SparqlQueryException;
import exceptions.SparqlUpdateException;
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
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

public class RDF4J_20 {

    public static List<BindingSet> SPARQLquery(String repositoryID, String rdf4jServer, String queryString) throws RepositoryException, MalformedQueryException, QueryEvaluationException, SparqlQueryException {
        List<BindingSet> BindingList = new ArrayList();
        try {
            Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
            repo.initialize();
            try (RepositoryConnection conn = repo.getConnection()) {
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    while (result.hasNext()) {
                        BindingList.add(result.next());
                    }
                }
            }
        } catch (Exception e) {
            throw new SparqlQueryException(e.getMessage());
        }
        return BindingList;
    }

    public static List<String> getValuesFromBindingSet_ORDEREDLIST(List<BindingSet> result, String var) throws QueryEvaluationException, SparqlParseException {
        List<String> ValueList = new ArrayList();
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
                    } else if (valuestring.contains("^^<http://www.w3.org/2001/XMLSchema#integer>")) {
                        valuestring = valuestring.replace("^^<http://www.w3.org/2001/XMLSchema#integer>", "");
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    } else if (valuestring.contains("^^<http://www.w3.org/2001/XMLSchema#double>")) {
                        valuestring = valuestring.replace("^^<http://www.w3.org/2001/XMLSchema#double>", "");
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    } else {
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    }
                    ValueList.add(valuestring);
                }
            }
        } catch (Exception e) {
            throw new SparqlParseException(e.getMessage());
        }
        return ValueList;
    }

    public static HashSet<String> getValuesFromBindingSet_UNIQUESET(List<BindingSet> result, String var) throws QueryEvaluationException, SparqlParseException {
        HashSet<String> ValueList = new HashSet();
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
                    } else if (valuestring.contains("^^<http://www.w3.org/2001/XMLSchema#integer>")) {
                        valuestring = valuestring.replace("^^<http://www.w3.org/2001/XMLSchema#integer>", "");
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    } else if (valuestring.contains("^^<http://www.w3.org/2001/XMLSchema#double>")) {
                        valuestring = valuestring.replace("^^<http://www.w3.org/2001/XMLSchema#double>", "");
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    } else {
                        valuestring = valuestring.substring(1, valuestring.length() - 1);
                    }
                    ValueList.add(valuestring);
                }
            }
        } catch (Exception e) {
            throw new SparqlParseException(e.getMessage());
        }
        return ValueList;

    }

    public static void SPARQLupdate(String repositoryID, String rdf4jServer, String updateString) throws RepositoryException, MalformedQueryException, UpdateExecutionException, NoInputException, SparqlUpdateException {
        try {
            // GET NUMBER OF STATEMENTS
            int before = getNumberOfStatements(repositoryID, rdf4jServer);
            // SEND UPDATE
            Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
            repo.initialize();
            try (RepositoryConnection con = repo.getConnection()) {
                Update update = con.prepareUpdate(QueryLanguage.SPARQL, updateString);
                update.execute();
            }
            // GET NUMBER OF STATEMENTS (CHECK)
            int after = getNumberOfStatements(repositoryID, rdf4jServer);
            if (before == after) {
                //throw new NoInputException();
            }
        } catch (Exception e) {
            if (e.toString().contains("NoInputException")) {
                throw new NoInputException(e.getMessage());
            } else {
                throw new SparqlUpdateException(e.getMessage());
            }
        }
    }

    private static int getNumberOfStatements(String repositoryID, String rdf4jServer) throws MalformedURLException, IOException, SparqlQueryException {
        try {
            String size_url = rdf4jServer + "/repositories/" + repositoryID + "/size";
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
            throw new SparqlQueryException(e.getMessage());
        }
    }

    public static void inputRDFfromJSONLDString(String repositoryID, String rdf4jServer, String JSONLD) throws SparqlUpdateException {
        try {
            // GET NUMBER OF STATEMENTS
            int before = getNumberOfStatements(repositoryID, rdf4jServer);
            // SEND UPDATE
            Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            InputStream stream = new ByteArrayInputStream(JSONLD.getBytes(StandardCharsets.UTF_8));
            con.add(stream, "http://dummy.org#", RDFFormat.JSONLD);
            con.close();
            // GET NUMBER OF STATEMENTS (CHECK)
            int after = getNumberOfStatements(repositoryID, rdf4jServer);
            if (before == after) {
                throw new NoInputException();
            }
        } catch (Exception e) {
            throw new SparqlUpdateException(e.getMessage());
        }
    }

    public static void inputRDFfromRDFJSONString(String repositoryID, String rdf4jServer, String RDFJSON) throws SparqlUpdateException {
        try {
            // GET NUMBER OF STATEMENTS
            int before = getNumberOfStatements(repositoryID, rdf4jServer);
            // SEND UPDATE
            Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            InputStream stream = new ByteArrayInputStream(RDFJSON.getBytes(StandardCharsets.UTF_8));
            con.add(stream, "http://dummy.org#", RDFFormat.RDFJSON);
            con.close();
            // GET NUMBER OF STATEMENTS (CHECK)
            int after = getNumberOfStatements(repositoryID, rdf4jServer);
            if (before == after) {
                throw new NoInputException();
            }
        } catch (Exception e) {
            throw new SparqlUpdateException(e.getMessage());
        }
    }

    public static ServletOutputStream SPARQLqueryOutputFile(String repositoryID, String rdf4jServer, String queryString, String format, ServletOutputStream out) throws SparqlQueryException {
        try {
            Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
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
            throw new SparqlQueryException(e.getMessage());
        }
        return out;
    }

    public static OutputStream SPARQLqueryOutputFileOS(String repositoryID, String rdf4jServer, String queryString, String format, OutputStream out) throws SparqlQueryException {
        try {
            Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
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
            throw new SparqlQueryException(e.getMessage());
        }
        return out;
    }

}
