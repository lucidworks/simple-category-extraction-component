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
<searchComponent name="colorExtractor" class="com.lucidworks.solr.query.CategoryExtractionComponent" >
      <str name="field">color</str>
      <arr name="values">
        <str>red</str>
        <str>blue</str>
        <str>orange</str>
        <str>yellow</str>
        <str>green</str>
        <str>purple</str>
        <str>violet</str>
        <str>aquamarine</str>
        <str>chartreuse</str>
        <str>pink</str>
        <str>white</str>
        <str>black</str>
        <str>brown</str>
        <str>beige</str>
      </arr>
    </searchComponent>
</pre>

And to create a request handler using this component, insert the component as a 'first-components' so that it executes before the QueryHandler:

<pre>
  <requestHandler name="/infer" class="solr.SearchHandler">
    <lst name="defaults">
      <str name="echoParams">explicit</str>
      <int name="rows">10</int>
      <str name="df">description</str>
    </lst>
     
    <arr name="first-components">
      <str>colorExtractor</str>
    </arr>
  </requestHandler>
</pre>

Loading the test data into a Solr collection:

<pre>
<add>
  <doc>
    <field name="id">1</field>
    <field name="color">red</field>
    <field name="description">This is the red sofa example. Please find with 'red sofa' query.</field>
  </doc>
  <doc>
    <field name="id">2</field>
    <field name="color">blue</field>
    <field name="description">This is a blue sofa, it should only hit on sofas that are blue in color.</field>
  </doc>
  <doc>
    <field name="id">3</field>
    <field name="color">red</field>
    <field name="description">This is a red beach ball. It is red in color but is not something that you should not sit on because you would tend to roll off.</field>
  </doc>
</add>
</pre>

Now searching for 'red sofa' using the /infer handler will only bring back the first record, whereas searching for 'red sofa' with the select handler will bring back all three. (make the df to be 'description' in both the /select and /infer handlers so that we don't have to worry about searching the default 'text' field - OR to a copyField from description to text).
