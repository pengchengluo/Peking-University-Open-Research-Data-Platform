/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.statistic;

import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUser;
import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUserServiceBean;
import cn.edu.pku.lib.util.StringUtil;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUser;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUserServiceBean;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.authorization.users.User;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author luopc
 */
@Stateless
public class UsageLogIndexServiceBean {
    
    private static final Logger logger = Logger.getLogger(UsageLogIndexServiceBean.class.getCanonicalName());
    private static final org.apache.log4j.Logger usageLogger = org.apache.log4j.Logger.getLogger(UsageLogIndexServiceBean.class.getCanonicalName());

    private JestClient client;
    
    @EJB
    SettingsServiceBean settingsServiceBean;
    @EJB
    BuiltinUserServiceBean builtinUserService;
    @EJB
    PKUIAAAUserServiceBean pkuIAAAUserService;
    @EJB
    GeoipServiceBean geoipService;
    
    @PostConstruct
    public void init(){
        JestClientFactory factory = new JestClientFactory();
        String elasticSearchUrl = settingsServiceBean.getValueForKey(SettingsServiceBean.Key.ElasticSearchUrl,
                "http://localhost:9200");
        factory.setHttpClientConfig(new HttpClientConfig.Builder(elasticSearchUrl)
                .multiThreaded(true)
                .build());
        client = factory.getObject();
    }
    
    public void index(EventLog eventLog){
        usageLogger.info("[usage_msg]:"+eventLog.toJson());
        Index index = new Index.Builder(eventLog)
                .index(StatisticConstant.INDEX_NAME)
                .type(StatisticConstant.INDEX_TYPE).build();
        try {
            client.execute(index);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    @PreDestroy
    public void close(){
        if(client != null){
            client.shutdownClient();
            client = null;
        }
    }

    private void addUserInfo(EventLog eventLog, User user){
        if(!user.isAuthenticated()){
            eventLog.setUserId(user.getIdentifier());
            return ;
        }
        AuthenticatedUser aUser = (AuthenticatedUser)user;
        if(aUser.isBuiltInUser()){
            BuiltinUser builtinUser = builtinUserService.findByUserName(aUser.getUserIdentifier());
            eventLog.setUserId(builtinUser.getUserName());
            if(StringUtil.isChinese(builtinUser.getLastName())){
                eventLog.setUserName(builtinUser.getLastName()+builtinUser.getFirstName());
            }else{
                eventLog.setUserName(builtinUser.getFirstName()+" "+builtinUser.getLastName());
            }
            if(builtinUser.getAffiliation() != null)
                eventLog.setAffiliation(builtinUser.getAffiliation());
            if(builtinUser.getPosition() != null)
                eventLog.setPosition(builtinUser.getPosition());
        }else if(aUser.isPKUIAAAUser()){
            PKUIAAAUser pkuIAAAUser = pkuIAAAUserService.findByUserName(aUser.getUserIdentifier());
            eventLog.setUserId(pkuIAAAUser.getUserName());
            if(StringUtil.isChinese(pkuIAAAUser.getLastName())){
                eventLog.setUserName(pkuIAAAUser.getLastName()+pkuIAAAUser.getFirstName());
            }else{
                eventLog.setUserName(pkuIAAAUser.getFirstName()+" "+pkuIAAAUser.getLastName());
            }
            if(pkuIAAAUser.getAffiliation() != null){
                eventLog.setAffiliation(pkuIAAAUser.getAffiliation());
            }
            if(pkuIAAAUser.getPosition() != null)
                eventLog.setPosition(pkuIAAAUser.getPosition());
        }
    }
    
    public EventLog buildEventLog(EventLog.EventType eventType, User user, HttpServletRequest request){
        EventLog eventLog = new EventLog();
        eventLog.setEventType(eventType);
        eventLog.setIp(request.getRemoteAddr());
        eventLog.setDate(new Date());
        eventLog.setUserAgent(request.getHeader("User-Agent"));
        eventLog.setReferrer(request.getHeader("referer"));

        addUserInfo(eventLog, user);
        geoipService.addGeoInfo(eventLog);

        return eventLog;
    }
}
