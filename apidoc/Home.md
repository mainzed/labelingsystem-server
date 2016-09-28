# Version 1.0

The Labeling System API provides programmatic access to the Labeling System functionality and content.
Version 1 of the API is limited to the essentials of the workbench (Version ??) functionality.

The API is [REST API](http://en.wikipedia.org/wiki/Representational_State_Transfer "RESTful")
and uses a SQlite database for user authentication purposes.
Currently, return format for all endpoints is [JSON](http://json.org/ "JSON").

***

## Endpoints

### API path

    {host}/api/v1/

### Index of Contents

* [Vocabulary resources](#vocabulary-resources)
* [Label resources](#label-resources)
* [Agent resources](#agent-resources)
* [Revision resources](#revision-resources)
* [Reference-Thesauri-Catalog (ReTCat) functions](#reference-thesauri-catalog-retcat-functions)
* [Search functions](#search-functions)
* [Authentication functions](#authentication)
* [System functions](#system-functions)

### Vocabulary resources

- **[<code>GET</code> vocabs](GET_vocabs)**
- **[<code>GET</code> vocabs/:id](GET_vocabs_id)**
- **[<code>GET</code> vocabs/:id.json](GET_vocabs_id_json)**
- **[<code>GET</code> vocabs/:id.xml](GET_vocabs_id_xml)**
- **[<code>GET</code> vocabs/:id.rdf](GET_vocabs_id_rdf)**
- **[<code>GET</code> vocabs/:id.ttl](GET_vocabs_id_ttl)**
- **[<code>GET</code> vocabs/:id.n3](GET_vocabs_id_n3)**
- **[<code>GET</code> vocabs/:id.jsonrdf](GET_vocabs_id_jsonrdf)**
- **[<code>GET</code> vocabs/:id.jsonld](GET_vocabs_id_jsonld)**
- **[<code>GET</code> vocabs/:id.skos](GET_vocabs_id_skos)**
- **[<code>POST</code> vocabs](POST_vocabs)**
- **[<code>PUT</code> vocabs/:id](PUT_vocabs_id)**
- **[<code>DELETE</code> vocabs/:id](DELETE_vocabs_id)**

### Label resources

- **[<code>GET</code> labels](GET_labels)**
- **[<code>GET</code> labels/:id](GET_labels_id)**
- **[<code>GET</code> labels/:id.json](GET_labels_id_json)**
- **[<code>GET</code> labels/:id.xml](GET_labels_id_xml)**
- **[<code>GET</code> labels/:id.rdf](GET_labels_id_rdf)**
- **[<code>GET</code> labels/:id.ttl](GET_labels_id_ttl)**
- **[<code>GET</code> labels/:id.n3](GET_labels_id_n3)**
- **[<code>GET</code> labels/:id.jsonrdf](GET_labels_id_jsonrdf)**
- **[<code>GET</code> labels/:id.jsonld](GET_labels_id_jsonld)**
- **[<code>POST</code> labels](POST_labels)**
- **[<code>PUT</code> labels/:id](PUT_labels_id)**
- **[<code>DELETE</code> labels/:id](DELETE_labels_id)**

### Agent resources

- **[<code>GET</code> agents](GET_agents)**
- **[<code>GET</code> agents/:id](GET_agents_id)**
- **[<code>GET</code> agents/:id.json](GET_agents_id_json)**
- **[<code>GET</code> agents/:id.xml](GET_agents_id_xml)**
- **[<code>GET</code> agents/:id.rdf](GET_agents_id_rdf)**
- **[<code>GET</code> agents/:id.ttl](GET_agents_id_ttl)**
- **[<code>GET</code> agents/:id.n3](GET_agents_id_n3)**
- **[<code>GET</code> agents/:id.jsonrdf](GET_agents_id_jsonrdf)**
- **[<code>GET</code> agents/:id.jsonld](GET_agents_id_jsonld)**
- **[<code>POST</code> agents](POST_agents)**
- **[<code>PUT</code> agents/:id](PUT_agents_id)**
- **[<code>DELETE</code> agents/:id](DELETE_agents_id)**

### Revision resources

- **[<code>GET</code> revisions](GET_revisions)**
- **[<code>GET</code> revisions/:id](GET_revisions_id)**
- **[<code>GET</code> revisions/:id.json](GET_revisions_id_json)**
- **[<code>GET</code> revisions/:id.xml](GET_revisions_id_xml)**
- **[<code>GET</code> revisions/:id.rdf](GET_revisions_id_rdf)**
- **[<code>GET</code> revisions/:id.ttl](GET_revisions_id_ttl)**
- **[<code>GET</code> revisions/:id.n3](GET_revisions_id_n3)**
- **[<code>GET</code> revisions/:id.jsonrdf](GET_revisions_id_jsonrdf)**
- **[<code>GET</code> revisions/:id.jsonld](GET_revisions_id_jsonld)**
- **[<code>DELETE</code> revisions/:id](DELETE_revisions_id)**

### Reference-Thesauri-Catalog (ReTCat) functions

#### General ReTCat functions

- **[<code>GET</code> retcat](GET_retcat)**
- **[<code>GET</code> retcat/:id](GET_retcat_id)**
- **[<code>GET</code> retcat/vocabulary/:id](GET_retcat_vocabulary_id)**
- **[<code>POST</code> retcat/vocabulary/:id](POST_retcat_vocabulary_id)**
- **[<code>PUT</code> retcat/vocabulary/:id](PUT_retcat_vocabulary_id)**
- **[<code>GET</code> retcat/info/qualities](GET_retcat_info_qualities)**
- **[<code>GET</code> retcat/info/groups](GET_retcat_info_groups)**

#### ReTCat detail information functions

- **[<code>GET</code> retcat/resourcequery](GET_retcat_resourcequery)**
- **[<code>GET</code> retcat/resourceinfo](GET_retcat_resourceinfo)**
- **[<code>GET</code> retcat/resourcewayback](GET_retcat_resourcewayback)**

#### Wayback-Machine functions

- **[<code>GET</code> retcat/waybacklink](GET_retcat_waybacklink)**

#### ReTCat items (search + info)

##### Labeling System

- **[<code>GET</code> retcat/query/labelingsystem](GET_retcat_query_labelingsystem)**
- **[<code>GET</code> retcat/query/labelingsystem/:vocabid](GET_retcat_query_labelingsystem_vocabid)**
- **[<code>GET</code> retcat/info/labelingsystem](GET_retcat_info_labelingsystem)**

##### Getty

- **[<code>GET</code> retcat/query/getty/aat](GET_retcat_query_getty_aat)**
- **[<code>GET</code> retcat/query/getty/tgn](GET_retcat_query_getty_tgn)**
- **[<code>GET</code> retcat/query/getty/ulan](GET_retcat_query_getty_ulan)**
- **[<code>GET</code> retcat/info/getty](GET_retcat_info_getty)**

##### Heritage Data

- **[<code>GET</code> retcat/query/heritagedata/historicengland](GET_retcat_query_heritagedata_historicengland)**
- **[<code>GET</code> retcat/query/heritagedata/rcahms](GET_retcat_query_heritagedata_rcahms)**
- **[<code>GET</code> retcat/query/heritagedata/rcahmw](GET_retcat_query_heritagedata_rcahmw)**
- **[<code>GET</code> retcat/info/heritagedata](GET_retcat_info_heritagedata)**

##### ChronOntology

- **[<code>GET</code> retcat/query/chronontology](GET_retcat_query_chronontology)**
- **[<code>GET</code> retcat/info/chronontology](GET_retcat_info_chronontology)**

##### Pleiades Places

- **[<code>GET</code> retcat/query/pelagiospleiadesplaces](GET_retcat_query_pelagiospleiadesplaces)**
- **[<code>GET</code> retcat/info/pelagiospleiadesplaces](GET_retcat_info_pelagiospleiadesplaces)**

##### SKOSMOS

- **[<code>GET</code> retcat/query/skosmos/finto](GET_retcat_query_skosmos_finto)**
- **[<code>GET</code> retcat/info/skosmos/finto](GET_retcat_info_skosmos_finto)**
- **[<code>GET</code> retcat/query/skosmos/finto](GET_retcat_query_skosmos_fao)**
- **[<code>GET</code> retcat/info/skosmos/fao](GET_retcat_info_skosmos_fao)**
- **[<code>GET</code> retcat/query/skosmos/finto](GET_retcat_query_skosmos_unesco)**
- **[<code>GET</code> retcat/info/skosmos/unesco](GET_retcat_info_skosmos_unesco)**

##### DBpedia

- **[<code>GET</code> retcat/query/dbpedia](GET_retcat_query_dbpedia)**
- **[<code>GET</code> retcat/info/dbpedia](GET_retcat_info_dbpedia)**

##### GeoNames

- **[<code>GET</code> retcat/query/geonames](GET_retcat_query_geonames)**
- **[<code>GET</code> retcat/info/geonames](GET_retcat_info_geonames)**

##### HTML resource

- **[<code>GET</code> retcat/query/html](GET_retcat_query_html)**
- **[<code>GET</code> retcat/info/html](GET_retcat_info_html)**


### Search functions

#### SPARQL

- **[<code>GET</code> sparql](GET_sparql)**
- **[<code>POST</code> sparql](POST_sparql)**

#### full-text search

- **[<code>GET</code> search](GET_search)**

#### autocomplete

- **[<code>GET</code> autocomplete/label](GET_autocomplete_label)**
- **[<code>GET</code> autocomplete/label/creator/:id](GET_autocomplete_label_creator_id)**
- **[<code>GET</code> autocomplete/label/vocabulary/:id](GET_autocomplete_label_vocabulary_id)**
- **[<code>GET</code> autocomplete/agent](GET_autocomplete_agent)**
- **[<code>GET</code> autocomplete/vocabulary](GET_autocomplete_vocabulary)**

### Authentication

- **[<code>POST</code> auth/login](POST_auth_login)**
- **[<code>POST</code> auth/status](POST_auth_status)**
- **[<code>POST</code> auth/logout](POST_auth_logout)**
- **[<code>POST</code> auth/newuser](POST_auth_newuser)**
- **[<code>POST</code> auth/deactivate](POST_auth_deactivate)**
- **[<code>POST</code> auth/activate](POST_auth_activate)**
- **[<code>POST</code> auth/hash](POST_auth_hash)**

### System functions

#### information

- **[<code>GET</code> info](GET_info)**

#### dump

- **[<code>GET</code> dump](GET_dump)**
- **[<code>GET</code> dump/repository/id](GET_dump_repository_id)**

#### CSV upload

- **[<code>POST</code> importcsv/mode/start/creator/:creator/validator/:validator](POST_importcsv)**
- **[<code>GET</code> importcsv/mode/:mode](GET_importcsv)**

***

## Items

### Item path

    {host}/item/

- **[<code>GET</code> vocabulary/:id](GET_item_vocabulary_id)**
- **[<code>GET</code> label/:id](GET_item_label_id)**
- **[<code>GET</code> agent/:id](GET_item_agent_id)**
- **[<code>GET</code> revision/:id](GET_item_revision_id)**


***

## Checklist
* ...

***

## Changelog

* ...

***

## FAQ

### What return formats do you support?
The Labeling System API currently returns data in [JSON](http://json.org/ "JSON") format.