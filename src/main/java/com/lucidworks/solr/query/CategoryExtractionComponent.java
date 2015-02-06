package com.lucidworks.solr.query;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.FieldCache;
import org.apache.solr.search.SolrIndexSearcher;

import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class CategoryExtractionComponent extends QueryComponent {
	
  private static final Logger Log = LoggerFactory.getLogger( CategoryExtractionComponent.class );
    
  private String categoryField;
	
  @Override
  public void init( NamedList initArgs ) {
    Log.info( "init ..." );
      
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
      
    SolrIndexSearcher searcher = req.getSearcher();
    SortedDocValues fieldValues = FieldCache.DEFAULT.getTermsIndex( searcher.getAtomicReader( ), categoryField );
      
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
      BytesRef key = new BytesRef( tok.getBytes() );
      if (fieldValues.lookupTerm( key ) >= 0) {
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
