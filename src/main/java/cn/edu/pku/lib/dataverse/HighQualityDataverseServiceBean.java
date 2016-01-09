/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse;

import cn.edu.pku.lib.dataverse.cache.CacheServiceBean;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.sf.ehcache.Element;

/**
 *
 * @author luopc
 */
@Stateless
@Named
public class HighQualityDataverseServiceBean implements java.io.Serializable{
    
    private static final long serialVersionUID = -5992942858287091944L;
    @EJB
    CacheServiceBean cacheService;
    
    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;
    
    public void save(HighQualityDataverse highQualityDataverse){
        if(highQualityDataverse.getId() == null){
            em.persist(highQualityDataverse);
        }else{
            em.merge(highQualityDataverse);
        }
        resetAllHighQualityDataverseCache();
    }
    
    public void remove(HighQualityDataverse highQualityDataverse){
        em.remove(em.merge(highQualityDataverse));
        resetAllHighQualityDataverseCache();
    }
    
    private List<HighQualityDataverse> resetAllHighQualityDataverseCache(){
        List<HighQualityDataverse> hqDvs = em.createQuery("select object(o) from HighQualityDataverse as o order by o.id").getResultList();
        cacheService.getMiddleCache().put(new Element("allHighQualityDataverse",hqDvs));
        return hqDvs;
    }
    
    public List<HighQualityDataverse> findAll(){
        Element element = cacheService.getMiddleCache().get("allHighQualityDataverse");
        if(element != null){
            return (List<HighQualityDataverse>)element.getObjectValue();
        }else{
            return resetAllHighQualityDataverseCache();
        }
    }
}
