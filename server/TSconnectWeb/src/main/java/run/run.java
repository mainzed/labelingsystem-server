package run;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.rdf4j.query.BindingSet;
import rdf.RDF4J_20M3;

public class run extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            // UPDATE
            String a = String.valueOf(System.currentTimeMillis());
            //String a = "a";
            RDF4J_20M3.SPARQLupdate("tmp", "http://143.93.114.135/rdf4j-server", "INSERT DATA { <http://subject.info> <http://predicate.info> \"" + a + "\" .}");
            // QUERY
            List<BindingSet> bs = RDF4J_20M3.SPARQLquery("tmp", "http://143.93.114.135/rdf4j-server", "SELECT * WHERE { ?s <http://predicate.info> ?o }");
            List<String> list = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(bs, "s");
            HashSet<String> hs = RDF4J_20M3.getValuesFromBindingSet_UNIQUESET(bs, "s");
            out.println(list.size());
            out.println("");
            out.println(hs.size());
            // UPDATE JSONRDF
            String jsonrdf = "{ \n"
                    + "  \"http://labeling.i3mainz.hs-mainz.de/item/vocabulary/7e34e500-53e8-48a3-b20c-aa6c8faee743\" : { \n"
                    + "    \"http://purl.org/dc/elements/1.1/creator\" : [ { \n"
                    + "      \"type\" : \"literal\" ,\n"
                    + "      \"value\" : \"demo\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/terms/contributor\" : [ { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://labeling.i3mainz.hs-mainz.de/item/agent/demo\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://labeling.i3mainz.hs-mainz.de/vocab#hasStatusType\" : [ { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://labeling.i3mainz.hs-mainz.de/vocab#Active\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/terms/license\" : [ { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://creativecommons.org/licenses/by/4.0/\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/elements/1.1/description\" : [ { \n"
                    + "      \"type\" : \"literal\" ,\n"
                    + "      \"value\" : \"Beispielvokabular mit Zeiten von der CAA 2015 in Siena\" ,\n"
                    + "      \"lang\" : \"de\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/elements/1.1/identifier\" : [ { \n"
                    + "      \"type\" : \"literal\" ,\n"
                    + "      \"value\" : \"7e34e500-53e8-48a3-b20c-aa6c8faee743\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://www.w3.org/2004/02/skos/core#changeNote\" : [ { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://labeling.i3mainz.hs-mainz.de/item/revision/d59ea762-27ce-429e-95ae-2713d77c012f\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" : [ { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://labeling.i3mainz.hs-mainz.de/vocab#Vocabulary\"\n"
                    + "    }\n"
                    + "    , { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://www.w3.org/2004/02/skos/core#ConceptScheme\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/elements/1.1/created\" : [ { \n"
                    + "      \"type\" : \"literal\" ,\n"
                    + "      \"value\" : \""+System.currentTimeMillis()+"\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://labeling.i3mainz.hs-mainz.de/vocab#hasReleaseType\" : [ { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://labeling.i3mainz.hs-mainz.de/vocab#Public\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/elements/1.1/contributor\" : [ { \n"
                    + "      \"type\" : \"literal\" ,\n"
                    + "      \"value\" : \"demo\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/elements/1.1/title\" : [ { \n"
                    + "      \"type\" : \"literal\" ,\n"
                    + "      \"value\" : \"Zeiten\" ,\n"
                    + "      \"lang\" : \"de\"\n"
                    + "    }\n"
                    + "     ] ,\n"
                    + "    \"http://purl.org/dc/terms/creator\" : [ { \n"
                    + "      \"type\" : \"uri\" ,\n"
                    + "      \"value\" : \"http://labeling.i3mainz.hs-mainz.de/item/agent/demo\"\n"
                    + "    }\n"
                    + "     ]\n"
                    + "  }\n"
                    + "}";
            RDF4J_20M3.inputRDFfromRDFJSONString("tmp", "http://143.93.114.135/rdf4j-server", jsonrdf);
            // QUERY
            List<BindingSet> bs2 = RDF4J_20M3.SPARQLquery("tmp", "http://143.93.114.135/rdf4j-server", "SELECT * WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o }");
            List<String> list2 = RDF4J_20M3.getValuesFromBindingSet_ORDEREDLIST(bs2, "o");
            HashSet<String> hs2 = RDF4J_20M3.getValuesFromBindingSet_UNIQUESET(bs2, "o");
            out.println(list2.size());
            out.println("");
            out.println(hs2.size());
        } catch (Exception e) {
            out.println(e.toString());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
