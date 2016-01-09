/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse;

import cn.edu.pku.lib.dataverse.cache.CacheServiceBean;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.search.SearchException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import net.sf.ehcache.Element;

/**
 *
 * @author luopc
 */
@ViewScoped
@Named
public class IndexPage implements Serializable{
    
    private static final long serialVersionUID = -8477826130973083209L;
    private static final Logger logger = Logger.getLogger(IndexPage.class.getCanonicalName());
    
    @EJB
    HighQualityDataverseServiceBean highQualityDataverseService;
    @EJB
    CacheServiceBean cacheService;
    @EJB
    SolrSearchServiceBean solrSearchService;
    @EJB
    DataverseServiceBean dataverseService;
    
    private List<HighQualityDataverse> highQualityDataverses;
    private List<List<HighQualityDataverse>> hqDataverse;
    private long datasetCount;
    private long dataverseCount;
    private Dataverse rootDataverse;
    
    public void init(){
        highQualityDataverses =  highQualityDataverseService.findAll();
        hqDataverse = new ArrayList<>();
        for(int i= 0; i < Math.ceil((double)highQualityDataverses.size()/4); i++){
            List<HighQualityDataverse> dataverses = new ArrayList(4);
            for(int j=0;j<4&&(i*4+j)<highQualityDataverses.size();j++){
                dataverses.add(highQualityDataverses.get(i*4+j));
            }
            hqDataverse.add(dataverses);
        }
        Element element = cacheService.getMiddleCache().get("datasetCount");        
        if(element != null){
            datasetCount = (long)element.getObjectValue();
        }else{
            try {
                datasetCount = solrSearchService.getDatasetCount();
                cacheService.getMiddleCache().put(new Element("datasetCount",datasetCount));
            } catch (SearchException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        element = cacheService.getMiddleCache().get("dataverseCount");        
        if(element != null){
            dataverseCount = (long)element.getObjectValue();
        }else{
            try {
                dataverseCount = solrSearchService.getDataverseCount();
                cacheService.getMiddleCache().put(new Element("dataverseCount",dataverseCount));
            } catch (SearchException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        element = cacheService.getMiddleCache().get("rootDataverse");
        if(element != null){
            rootDataverse = (Dataverse)element.getObjectValue();
        }else{
            rootDataverse = dataverseService.findRootDataverse();
            cacheService.getMiddleCache().put(new Element("rootDataverse",rootDataverse));
        }
        
    }

    public List<HighQualityDataverse> getHighQualityDataverses() {
        return highQualityDataverses;
    }
    

    public void setHighQualityDataverses(List<HighQualityDataverse> highQualityDataverses) {
        this.highQualityDataverses = highQualityDataverses;
    }

    public long getDatasetCount() {
        return datasetCount;
    }

    public void setDatasetCount(long datasetCount) {
        this.datasetCount = datasetCount;
    }

    public Dataverse getRootDataverse() {
        return rootDataverse;
    }

    public void setRootDataverse(Dataverse rootDataverse) {
        this.rootDataverse = rootDataverse;
    }

    public List<List<HighQualityDataverse>> getHqDataverse() {
        return hqDataverse;
    }

    public void setHqDataverse(List<List<HighQualityDataverse>> hqDataverse) {
        this.hqDataverse = hqDataverse;
    }

    public long getDataverseCount() {
        return dataverseCount;
    }

    public void setDataverseCount(long dataverseCount) {
        this.dataverseCount = dataverseCount;
    }
}
