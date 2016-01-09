/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.search.SearchException;
import edu.harvard.iq.dataverse.search.SearchFields;
import edu.harvard.iq.dataverse.util.SystemConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author luopc
 */
@Stateless
@Named
public class SolrSearchServiceBean {
    
    private static final Logger logger = Logger.getLogger(SolrSearchServiceBean.class.getCanonicalName());
    
    private SolrServer solrServer;
    
    @EJB
    SystemConfig systemConfig;
    
    @PostConstruct
    public void init(){
        solrServer = new HttpSolrServer("http://" + systemConfig.getSolrHostColonPort() + "/solr");
    }
    
    @PreDestroy
    public void close(){
        if(solrServer != null){
            solrServer.shutdown();
        }
    }
    
    public long getDatasetCount() throws SearchException{
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        solrQuery.setFilterQueries(SearchFields.TYPE+":datasets",
                SearchFields.PUBLICATION_STATUS+":Published");
        solrQuery.setStart(0);
        solrQuery.setRows(0);
        QueryResponse queryResponse = null;
        try {
            queryResponse = solrServer.query(solrQuery);
            return queryResponse.getResults().getNumFound();
        } catch (HttpSolrServer.RemoteSolrException | SolrServerException ex) {  
            logger.log(Level.INFO, null, ex);
            throw new SearchException("Internal Dataverse Search Engine Error", ex);
        }
    }
    
    public long getDataverseCount() throws SearchException{
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        solrQuery.setFilterQueries(SearchFields.TYPE+":dataverses",
                SearchFields.PUBLICATION_STATUS+":Published");
        solrQuery.setStart(0);
        solrQuery.setRows(0);
        QueryResponse queryResponse = null;
        try {
            queryResponse = solrServer.query(solrQuery);
            return queryResponse.getResults().getNumFound();
        } catch (HttpSolrServer.RemoteSolrException | SolrServerException ex) {  
            logger.log(Level.INFO, null, ex);
            throw new SearchException("Internal Dataverse Search Engine Error", ex);
        }
    }
    
    public SolrSearchResult searchDataverseByName(String query,Locale locale) throws SearchException{
        boolean isZh = locale.getLanguage().equals("zh");
        SolrQuery solrQuery = new SolrQuery();
        StringBuilder queryStr = new StringBuilder();
        query = ClientUtils.escapeQueryChars(query);
        queryStr.append("(");
        queryStr.append(SearchFields.NAME);
        queryStr.append(":");
        queryStr.append(query);
        queryStr.append(" OR ");
        queryStr.append(SearchFields.NAME_ZH);
        queryStr.append(":");
        queryStr.append(query);
        queryStr.append(")");
        solrQuery.setQuery(queryStr.toString());
        solrQuery.setFilterQueries(SearchFields.TYPE+":dataverses",
                SearchFields.PUBLICATION_STATUS+":Published");
        if(isZh){
            solrQuery.setFields(SearchFields.ENTITY_ID, SearchFields.NAME_ZH,
                    SearchFields.DATAVERSE_AFFILIATION_ZH, SearchFields.DESCRIPTION_ZH);
        }else{
            solrQuery.setFields(SearchFields.ENTITY_ID, SearchFields.NAME,
                    SearchFields.DATAVERSE_AFFILIATION, SearchFields.DESCRIPTION);
        }
        logger.log(Level.INFO, "Solr query:{0}", solrQuery);
        solrQuery.setRows(100);
        QueryResponse queryResponse = null;
        SolrSearchResult result = new SolrSearchResult();
        try {
            queryResponse = solrServer.query(solrQuery);
        } catch (HttpSolrServer.RemoteSolrException ex) {
            logger.log(Level.INFO, null, ex);
            result.setTotal(0);
            result.setResults(Collections.EMPTY_LIST);
            return result;
        } catch (SolrServerException ex) {            
            throw new SearchException("Internal Dataverse Search Engine Error", ex);
        }
        SolrDocumentList docs = queryResponse.getResults();
        docs.getNumFound();
        Iterator<SolrDocument> iter = docs.iterator();
        List<Dataverse> dataverses = new ArrayList<>();
        while(iter.hasNext()){
            SolrDocument solrDocument = iter.next();
            Dataverse dataverse = new Dataverse();
            if(isZh){
                dataverse.setId((Long)solrDocument.getFieldValue(SearchFields.ENTITY_ID));
                dataverse.setNameZh((String)solrDocument.getFieldValue(SearchFields.NAME_ZH));
                dataverse.setAffiliationZh((String)solrDocument.getFieldValue(SearchFields.DATAVERSE_AFFILIATION_ZH));
                dataverse.setDescriptionZh((String)solrDocument.getFieldValue(SearchFields.DESCRIPTION_ZH));
            }else{
                dataverse.setId((Long)solrDocument.getFieldValue(SearchFields.ENTITY_ID));
                dataverse.setName((String)solrDocument.getFieldValue(SearchFields.NAME));
                dataverse.setAffiliation((String)solrDocument.getFieldValue(SearchFields.DATAVERSE_AFFILIATION));
                dataverse.setDescription((String)solrDocument.getFieldValue(SearchFields.DESCRIPTION));
            }
            dataverses.add(dataverse);
        }
        result.setResults(dataverses);
        result.setTotal((int)docs.getNumFound());
        return result;
    }
    
}
