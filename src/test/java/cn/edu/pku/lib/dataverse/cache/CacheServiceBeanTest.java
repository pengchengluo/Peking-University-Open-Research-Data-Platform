/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author luopc
 */
public class CacheServiceBeanTest {
    
    @Test
    public void test(){
        CacheServiceBean cacheService = new CacheServiceBean();
        cacheService.init();
        Cache cache = cacheService.getEternalCache();
        cache.put(new Element("abc","edf"));
        Element e = cache.get("abc");
        Assert.assertNotNull(e);
        Assert.assertEquals("abc", e.getObjectKey());
        Assert.assertEquals("edf", e.getObjectValue());
        e = cache.get("eee");
        Assert.assertNull(e);
        cacheService.close();
    }
    
}
