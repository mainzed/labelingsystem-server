$(document).ready(function() {
    var PREFIXES = "PREFIX ls: <http://labeling.i3mainz.hs-mainz.de/vocab#>\n" +
        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
        "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
        "PREFIX dct: <http://purl.org/dc/terms/>\n" +
        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n\n";
    var QUERY_RESET = "SELECT * WHERE {\n" +
        "  ?s ?p ?o.\n" +
        "} LIMIT 100";
    var QUERY_TRIPLES = "SELECT DISTINCT (COUNT(?s) AS ?count) WHERE {\n" +
        "  ?s ?p ?o.\n" +
        "}";
    var QUERY_PROJECTS = "SELECT * WHERE {\n" +
        "  ?project a ls:Project.\n" +
        "}";
    var QUERY_VOCABULARIES = "SELECT * WHERE {\n" +
        "  ?vocabulary a ls:Vocabulary.\n" +
        "  ?vocabulary ls:sameAs ?concept.\n" +
        "}";
    var QUERY_LABELS = "SELECT ?label ?id ?prefLabel WHERE {\n" +
        "  ?l a ls:Label.\n" +
        "  ?l dc:creator ?creator.\n" +
        "  ?l ls:sameAs ?label.\n" +
        "  ?l ls:identifier ?id.\n" +
        "  ?l skos:prefLabel ?prefLabel .\n" +
        "  ?l ls:prefLang ?prefLang .\n" +
        "  FILTER(LANGMATCHES(LANG(?prefLabel), ?prefLang))\n" +
        "  FILTER(?creator=\"maxmustermann\")\n" +
        "}";
    var editor;

    // set sparql field and load default
    $("#sparqlfield").html("");
    $("#sparqlfield").html("<textarea id='sparql_eingabe'></textarea>");
    editor = CodeMirror.fromTextArea(document.getElementById("sparql_eingabe"), {
        mode: "application/sparql-query",
        matchBrackets: true
    });
    editor.setValue(PREFIXES + QUERY_RESET);

    sendSPARQLShowTable = function(url, query, format, callback, info) {
        $('#sparql_result').html('<b>Loading...</b> <img src="loading.gif" height="40">');
        query = encodeURIComponent(query);
        $.ajax({
            type: 'POST',
            url: url,
            dataType: "text",
            data: {
                query: query,
                format: format
            },
            error: function(jqXHR, textStatus, errorThrown) {
                $('#sparql_result').html("<hr /><center><b style='font-size:30pt'>Results</b><br><br><b>" + errorThrown + "</b></center>");
                console.error(errorThrown);
            },
            success: function(output) {
                // get HTML table from TSV
                var lines = output.split("\n");
                var html_str = "";
                html_str += "<hr /><p align='center'><b style='font-size:30pt'>Results</b>&nbsp;&nbsp;";
                html_str += '<span id="xmllink"></span><span id="jsonlink"></span><span id="csvlink"></span><span id="csvfile"></span></p>';
                html_str += "<table id='result' width='100%'>";
                for (var i = 0; i < lines.length - 1; i++) {
                    if (i % 2 > 0) {
                        html_str += "<tr style='background-color: #ffffff'>";
                    } else {
                        html_str += "<tr style='background-color: #eeeeee'>";
                    }
                    var line = lines[i].split("\t");
                    if (i == 0) {
                        html_str += "<th>id</th>"; // empty line
                        for (var j = 0; j < line.length; j++) {
                            html_str += "<th>" + line[j] + "</th>";
                        }
                    } else {
                        html_str += "<td><b>" + i.toString() + "</b></td>";
                        for (var j = 0; j < line.length; j++) {
                            var text = line[j].replace("<", "&lt;").replace(">", "&gt;");
                            var link = line[j].replace("<", "").replace(">", "");
                            if (text.indexOf("XMLSchema#integer") != -1) { // if result is a resource
                                var split = link.split("^^");
                                text = split[0];
                            } else if (text.indexOf("XMLSchema#string") != -1) { // if result is a resource
                                var split = link.split("^^");
                                text = split[0];
                            } else if (text.indexOf("http") != -1) { // if result is a resource
                                text = "<a href='" + link + "' target='_blank'>" + text + "</a>";
                            }
                            html_str += "<td>" + text + "</td>";
                        }
                    }
                    html_str += "</tr>";
                }
                html_str += "</table>";
                // set div/span with sparql table content
                $('#sparql_result').html("");
                $('#sparql_result').html(html_str);
                // set links to XML and JSON
                setXMLJSONlink();
            }
        });
    }

    clearresults = function() {
        $('#sparql_result').html("");
        $('#xmllink').html("");
        $('#jsonlink').html("");
        $('#csvlink').html("");
    }

    prefixes = function() {
        $("#sparqlfield").html("");
        $("#sparqlfield").html("<textarea id='sparql_eingabe'></textarea>");
        editor = CodeMirror.fromTextArea(document.getElementById("sparql_eingabe"), {
            mode: "application/sparql-query",
            matchBrackets: true
        });
        editor.setValue(PREFIXES + QUERY_RESET);
        $('#sparql_result').html("");
        $('#xmllink').html("");
        $('#jsonlink').html("");
        $('#csvlink').html("");
    }

    ex0 = function() {
        $("#sparqlfield").html("");
        $("#sparqlfield").html("<textarea id='sparql_eingabe'></textarea>");
        editor = CodeMirror.fromTextArea(document.getElementById("sparql_eingabe"), {
            mode: "application/sparql-query",
            matchBrackets: true
        });
        editor.setValue(PREFIXES + QUERY_TRIPLES);
        $('#sparql_result').html("");
        $('#xmllink').html("");
        $('#jsonlink').html("");
        $('#csvlink').html("");
    }

    ex1 = function() {
        $("#sparqlfield").html("");
        $("#sparqlfield").html("<textarea id='sparql_eingabe'></textarea>");
        editor = CodeMirror.fromTextArea(document.getElementById("sparql_eingabe"), {
            mode: "application/sparql-query",
            matchBrackets: true
        });
        editor.setValue(PREFIXES + QUERY_PROJECTS);
        $('#sparql_result').html("");
        $('#xmllink').html("");
        $('#jsonlink').html("");
        $('#csvlink').html("");
    }
    ex2 = function() {
        $("#sparqlfield").html("");
        $("#sparqlfield").html("<textarea id='sparql_eingabe'></textarea>");
        editor = CodeMirror.fromTextArea(document.getElementById("sparql_eingabe"), {
            mode: "application/sparql-query",
            matchBrackets: true
        });
        editor.setValue(PREFIXES + QUERY_VOCABULARIES);
        $('#sparql_result').html("");
        $('#xmllink').html("");
        $('#jsonlink').html("");
        $('#csvlink').html("");
    }
    ex3 = function() {
        $("#sparqlfield").html("");
        $("#sparqlfield").html("<textarea id='sparql_eingabe'></textarea>");
        editor = CodeMirror.fromTextArea(document.getElementById("sparql_eingabe"), {
            mode: "application/sparql-query",
            matchBrackets: true
        });
        editor.setValue(PREFIXES + QUERY_LABELS);
        $('#sparql_result').html("");
        $('#xmllink').html("");
        $('#jsonlink').html("");
        $('#csvlink').html("");
    }

    function setXMLJSONlink() {
        var m = encodeURIComponent(editor.getValue());
        var l = Config.SPARQL + "?query=" + m + "&format=xml&file=true";
        var h = "<a href='" + l + "' target='_blank'>SPARQL/XML</a> | ";
        $('#xmllink').html("as " + h);
        var l = Config.SPARQL + "?query=" + m + "&format=json&file=true";
        var h = "<a href='" + l + "' target='_blank'>SPARQL/JSON</a> | ";
        $('#jsonlink').html(h);
        var l = Config.SPARQL + "?query=" + m + "&format=csv&file=true";
        var h = "<a href='" + l + "' target='_blank'>CSV</a>";
        $('#csvlink').html(h);
    }

    // reset result area if query has changed
    editor.on('keyup', function() {
        //console.log(Math.random());
        $('#sparql_result').html("");
        $('#xmllink').html("");
        $('#jsonlink').html("");
        $('#csvlink').html("");
    });
});