/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.cache;

import java.net.URL;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import net.sf.ehcache.Cache;

import net.sf.ehcache.CacheManager;
/**
 *
 * @author luopc
 */
@Singleton
public class CacheServiceBean {
    private CacheManager manager;
    
    @PostConstruct
    public void init(){
        URL url = CacheServiceBean.class.getResource("ehcache.xml");
        manager = CacheManager.newInstance(url);
    }
    
    public Cache getEternalCache(){
        return manager.getCache("eternal");
    }
    
    public Cache getMiddleCache(){
        return manager.getCache("middle");
    }
    
    @PreDestroy
    public void close(){
        if(manager != null){
            manager.shutdown();
        }
    }
    
}
