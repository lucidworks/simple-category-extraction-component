# simple-category-extraction-component
Simple white-list based query introspection Solr Search Component - solves the 'red sofa' problem
described in http://lucidworks.com/blog/well-tempered-search-application-fugue/

==============================
Solr Search Component that filters incoming query looking for category terms identified by a white list. Changes the query from q=foo bar to q=bar fq=category:foo if
the keyword 'foo' is a instance of 'category'.

=============================
Configuration:

For example, to create a category extractor for 'color' this would configure the search component in solrconfig.xml:

<pre>
&lt;searchComponent name="colorExtractor" class="com.lucidworks.solr.query.CategoryExtractionComponent" >
      &lt;str name="field">color&lt;/str>
      &lt;arr name="values">
        &lt;str>red&lt;/str>
        &lt;str>orange&lt;/str>
        &lt;str>yellow&lt;/str>
        &lt;str>blue&lt;/str>
        &lt;str>green&lt;/str>
        &lt;str>purple&lt;/str>
        &lt;str>violet&lt;/str>
        &lt;str>aquamarine&lt;/str>
        &lt;str>chartreuse&lt;/str>
        &lt;str>pink&lt;/str>
        &lt;str>white&lt;/str>
        &lt;str>black&lt;/str>
        &lt;str>brown&lt;/str>
        &lt;str>beige&lt;/str>
      &lt;/arr>
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

Now searching for 'red sofa' using the /infer handler will only bring back the first record, whereas searching for 'red sofa' with the select handler will bring back all three. (make the df to be 'description' in both the /select and /infer handlers so that we don't have to worry about searching the default 'text' field - OR to a copyField from description to text).

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
