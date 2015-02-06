# simple-category-extraction-component
Simple white-list based query introspection Solr Search Component - solves the 'red sofa' problem
described in http://lucidworks.com/blog/well-tempered-search-application-fugue/

==============================
Solr Search Component that filters incoming query looking for category terms identified by a white list. Changes the query from q=foo bar to q=bar fq=category:foo if
the keyword 'foo' is a instance of 'category'. The implementation uses a way cool suggestion by Erik Hatcher to use the FieldCache field values to determine what are valid values for a field.

To use this code, you will need to have Solr downloaded and installed. The latest released version is 4.10.3 (Solr 5.0 should be out soon, this code has not been tested with 5.0 yet).

Compile the code by running Maven (if you don't have this, you will need to download it from Apache) from the directory that you downloaded this plugin code to:

>mvn package

This will generate a jar file in the ./target directory called category-extraction-component-1.0-SNAPSHOT.jar

Copy this to the solr/lib folder in your solr directory (actually <where you downloaded solr>/example/solr/lib. If this 'lib' folder doesn't exist (and it won't for a new install), create it first then copy the jar file into it.


=============================
Configuration:

For example, to create a category extractor for 'color' this would configure the search component in solrconfig.xml. You will need to modify the solrconfig.xml to do this, open this file in a text editor - or XML editor if you have one. The file is located in <solr install dir>/example/solr/collection1/conf).

Add these elements:

<pre>
&lt;searchComponent name="colorExtractor" class="com.lucidworks.solr.query.CategoryExtractionComponent" >
      &lt;str name="field">color&lt;/str>
    &lt;/searchComponent>
</pre>

And to create a request handler using this component, insert the component as a 'first-components' so that it executes before the QueryHandler:

<pre>
  &lt;requestHandler name="/infer" class="solr.SearchHandler">
    &lt;lst name="defaults">
      &lt;str name="echoParams">explicit&lt;/str>
      &lt;int name="rows">10&lt;/int>
      &lt;str name="df">description&lt;/str>
    &lt;/lst>
     
    &lt;arr name="first-components">
      &lt;str>colorExtractor&lt;/str>
    &lt;/arr>
  &lt;/requestHandler>
</pre>

restart solr (java -jar start.jar will do fine for this demo) If you don't know how to do this, consult the Solr documentation.

Loading the test data into a Solr collection:

<pre>
&lt;add>
  &lt;doc>
    &lt;field name="id">1&lt;/field>
    &lt;field name="color">red&lt;/field>
    &lt;field name="description">This is the red sofa example. Please find with 'red sofa' query.&lt;/field>
  &lt/doc>
  &lt;doc>
    &lt;field name="id">2&lt;/field>
    &lt;field name="color">blue&lt;/field>
    &lt;field name="description">This is a blue sofa, it should only hit on sofas that are blue in color.&lt;/field>
  &lt;/doc>
  &lt;doc>
    &lt;field name="id">3&lt;/field>
    &lt;field name="color">red&lt;/field>
    &lt;field name="description">This is a red beach ball. It is red in color but is not something that you should not sit on because you would tend to roll off.&lt;/field>
  &lt;/doc>
&lt;/add>
</pre>

You can do this using curl (on Linux) If you are on Windows ...

cd to src/main/test/resources

curl -X POST -H 'Content-Type: text/xml; charset=utf-8' -d @inputdata.xml http://localhost:8983/solr/collection1/update

and force a commit using:

curl http://localhost:8983/solr/collection1/update --data '<commit/>' -H 'Content-type:text/xml; charset=utf-8'


Now bring up the Solr Admin at http://localhost:8983/solr to make sure that you have a collection. Select collection1 from the Core Selector Dropdown button. Click on the Query tab and click on the "Execute Query" button. You should see the three documents that you just indexed.

The color field will now have the values: 'red' and 'blue'. The component now knows what the valid values for 'color' are - the ones that have been indexed into the collection!

Now searching for 'red sofa' using the /infer handler will only bring back the first record, whereas searching for 'red sofa' with the select handler will bring back all three. (In my solrconfig.xml, I made the df to be 'description' in both the /select and /infer handlers so that we don't have to worry about searching the default 'text' field - OR I could have added  a copyField from description to text - OR I could have used 'text' for the 'description' field in the input SolrXML file  - always more than way to do cat skinnin'). Note that searching for 'green sofa' in either handler would bring back two records (the ones with sofa) because the index doesn't recognize 'green' as a sofa color because we haven't given it one to index yet. This means that all colors that are associated with a record by having a color facet value will work, no other colors will. And we can do this with ANY field that we can facet on!

Out-Of-The-Box Solr  /request handler

http://localhost:8983/solr/collection1/select?q=red+sofa&wt=json&indent=true

<pre>
{
  "responseHeader":{
    "status":0,
    "QTime":1,
    "params":{
      "indent":"true",
      "q":"red sofa",
      "wt":"json"}},
  "response":{"numFound":3,"start":0,"docs":[
      {
        "id":"1",
        "color":"red",
        "description":"This is the red sofa example. Please find with 'red sofa' query.",
        "_version_":1492376486934478848},
      {
        "id":"3",
        "color":"red",
        "description":"This is a red beach ball. It is red in color but is not something that you should not sit on because you would tend to roll off.",
        "_version_":1492376486956498944},
      {
        "id":"2",
        "color":"blue",
        "description":"This is a blue sofa, it should only hit on sofas that are blue in color.",
        "_version_":1492376486955450368}]
  }}
</pre>

And with our handy-dandy new /infer request handler:

http://localhost:8983/solr/collection1/infer?q=red+sofa&wt=json&indent=true

<pre>

{
  "responseHeader":{
    "status":0,
    "QTime":7,
    "params":{
      "indent":"true",
      "q":"red sofa",
      "wt":"json"}},
  "response":{"numFound":1,"start":0,"docs":[
      {
        "id":"1",
        "color":"red",
        "description":"This is the red sofa example. Please find with 'red sofa' query.",
        "_version_":1492376486934478848}]
  }}
</pre>

And now for my next magic trick ...
