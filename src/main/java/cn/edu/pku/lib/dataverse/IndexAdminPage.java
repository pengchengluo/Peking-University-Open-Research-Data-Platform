/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseHeaderFragment;
import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.search.SearchException;
import edu.harvard.iq.dataverse.util.JsfHelper;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author luopc
 */
@ViewScoped
@Named
public class IndexAdminPage implements Serializable{
    
    private static final long serialVersionUID = -69780080052012743L;
    
    @EJB
    HighQualityDataverseServiceBean highQualityDataverseService;
    @EJB
    DataverseServiceBean dataverseService;
    
    @EJB
    SolrSearchServiceBean solrSearchService;
    
    @Inject
    DataverseSession session;
    
    private String query;

    private SolrSearchResult searchResults;
    private List<HighQualityDataverse> highQualityDataverses;
    
    
    public String init(){
        if(!session.getUser().isSuperuser()){
            return "/loginpage.xhtml" + DataverseHeaderFragment.getRedirectPage();
        }
        findAllHighQualityDataverse();
        return null;
    }
    
    public IndexAdminPage(){
        searchResults = new SolrSearchResult();
        searchResults.setTotal(0);
        searchResults.setResults(Collections.EMPTY_LIST);
    }
    
    public List<HighQualityDataverse> findAllHighQualityDataverse(){
        highQualityDataverses =  highQualityDataverseService.findAll();
        return highQualityDataverses;
    }

    public void searchDataverseByName(){
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        try{
            searchResults = solrSearchService.searchDataverseByName(query,locale);
        }catch(SearchException ex){
            JsfHelper.addErrorMessage("Solr Search Exception");
            searchResults = new SolrSearchResult();
            searchResults.setTotal(0);
            searchResults.setResults(Collections.EMPTY_LIST);
        }
    }
    
    public void addHighQualityDataverse(Long id){
        HighQualityDataverse hqDv = new HighQualityDataverse();
        Dataverse dataverse = dataverseService.findById(id);
        if(dataverse == null){
            JsfHelper.addErrorMessage("Dataverse not found in database");
        }else{
            hqDv.setDataverse(dataverse);
            highQualityDataverseService.save(hqDv);
            JsfHelper.addSuccessMessage("Set "+dataverse.getName()+" dataverse as high quality successfully.");
        }
        findAllHighQualityDataverse();
    }
    
    public void removeHighQualityDataverse(HighQualityDataverse hqDv){
        try{
            highQualityDataverseService.remove(hqDv);
            JsfHelper.addSuccessMessage("Remove high quality dataverse "+ hqDv.getDataverse().getName()+" successfully");
        }catch(Exception e){
            JsfHelper.addErrorMessage("Remove high quality dataverse "+ hqDv.getDataverse().getName()+" unsuccessfully");
        }
        findAllHighQualityDataverse();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SolrSearchResult getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SolrSearchResult searchResults) {
        this.searchResults = searchResults;
    }

    public List<HighQualityDataverse> getHighQualityDataverses() {
        return highQualityDataverses;
    }

    public void setHighQualityDataverses(List<HighQualityDataverse> highQualityDataverses) {
        this.highQualityDataverses = highQualityDataverses;
    }
}