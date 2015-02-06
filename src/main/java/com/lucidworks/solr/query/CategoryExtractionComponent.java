package com.lucidworks.solr.query;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class CategoryExtractionComponent extends QueryComponent {
	
  private static final Logger Log = LoggerFactory.getLogger( CategoryExtractionComponent.class );
    
    
  private HashSet<String> categorySet = new HashSet<String>( );
  private String categoryField;
	
  @Override
  public void init( NamedList initArgs ) {
    Log.info( "init ..." );

    // get the flat list? or the values of category
    List<String> values = (List<String>)initArgs.get( "values" );
    if ( values != null ) {
      for (String value : values ) {
        Log.info( "adding category value:" + value );
        categorySet.add( value.toLowerCase( ).trim( ) );
      }
    }
      
    // the Solr-Lucene field that will be used to create the filter query
    String catField = (String)initArgs.get( "field" );
    if ( catField != null ) {
      Log.info( "setting category field: " + catField );
      this.categoryField = catField;
    }
  }

  @Override
  public void prepare( ResponseBuilder rb ) throws IOException
  {
    SolrQueryRequest req = rb.req;
      
    SolrParams params = req.getParams( );
   
    ModifiableSolrParams modParams = new ModifiableSolrParams( params );
      
    String qStr = params.get( CommonParams.Q );
    // tokenize the query string, if any part of it matches, remove the token from the list and
    // add a filter query with <categoryField>:value
    StringTokenizer strtok = new StringTokenizer(qStr, " .,:;\"'" );
    StringBuilder strbldr= new StringBuilder( );
      
    while (strtok.hasMoreTokens( ) ) {
      String tok = strtok.nextToken( ).toLowerCase( );
      Log.info( "got token: " + tok );
      if (categorySet.contains( tok )) {
        String fq = new String( categoryField + ":" + tok );
        Log.info( "adding fq " + fq );
        modParams.add( "fq", fq );
      }
      else
      {
        strbldr.append( tok );
        if (strbldr.length() > 0) {
          strbldr.append( " " );
        }
      }
    }
    
    String modQ = strbldr.toString( );
    // should we add the category fields here: need to test this first...
      
    // if the query is now empty, make sure it hits on everything
    if (modQ.trim().length() == 0) {
      modQ = "*:*";
    }
      
    Log.info( "final q string is: '" + modQ + "'" );
    modParams.set( "q", modQ );
      
    req.setParams( modParams );
  }
    
  @Override
  public void process(ResponseBuilder rb) throws IOException
  {
      // do nothing - needed so we don't execute the query here.
  }

}
