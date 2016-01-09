/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.statistic;



import java.util.Arrays;
import java.util.Date;
import org.junit.Test;

/**
 *
 * @author luopc
 */
public class StatisticLogTest {
    
    @Test
    public void testJson() throws InterruptedException{
        UsageLogSearchServiceBean usageLog = new UsageLogSearchServiceBean();
        UsageLogSearchQuery query = new UsageLogSearchQuery();
        query.setEvents(Arrays.asList(EventLog.EventType.VIEW_DATAVERSE,EventLog.EventType.DOWNLOAD_FILE));
        query.setFields(Arrays.asList("date","ip"));
        query.setFrom(0L);
        query.setSize(10L);
        query.setStartTime(new Date());
        Thread.sleep(1000);
        query.setEndTime(new Date());
        System.out.println(usageLog.getQuery(query));
        
//        UsageLogSearchQuery query = new UsageLogSearchQuery();
////        query.setDateHistogramInterval(StatisticConstant.AGG_INTERVAL_DAY);
//        query.setEvent(EventLog.EventType.VIEW_DATAVERSE);
//        UsageLogSearchServiceBean search = new UsageLogSearchServiceBean();
//        UsageLogSearchResult result = search.search(query);
//        System.out.println("???");
//        System.out.println(result.getTotal());
//        for(EventLog log : result.getEventLogs()){
//            System.out.println(log.toJson());
//        }
//        for(Pair<String,Long> pair : result.getDateHistogram()){
//            System.out.println(pair.getFirst()+"\t"+pair.getSecond());
//        }
//        Gson gson = new Gson();
//        String json =  gson.toJson(log);
//        System.out.println(json);
//        StatisticLog log2 = gson.fromJson(json, StatisticLog.class);
//        System.out.print(gson.toJson(log2));
        
//        ElasticSearchLoggerServiceBean logger = new ElasticSearchLoggerServiceBean();
////        EventLog log = new EventLog(EventLog.EventType.VIEW_DATASET, "162.105.134.230",
////                new Date(), "1406189042",
////                "Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13",
////                "http://www.pku.edu.cn");
////        try{
////            logger.log(log);
////        }catch(Exception ioe){
////            ioe.printStackTrace();
////        }
//
//        Map<String,Long> map = logger.getLogCountByDay(EventLog.EventType.VIEW_DATASET, 7L);
//        for(String key : map.keySet()){
//            System.out.println(key+"\t"+map.get(key));
//        }
//        System.out.println("-----------------------");
//        map = logger.getLogCountByDay();
//        for(String key : map.keySet()){
//            System.out.println(key+"\t"+map.get(key));
//        }
//        System.out.println("-----------------------");
//        map = logger.getLogCountByDay(EventLog.EventType.VIEW_DATASET);
//        for(String key : map.keySet()){
//            System.out.println(key+"\t"+map.get(key));
//        }
//        
//        System.out.println("-----------------------");
//        map = logger.getLogCountByMonth(EventLog.EventType.VIEW_DATASET);
//        for(String key : map.keySet()){
//            System.out.println(key+"\t"+map.get(key));
//        }
//        
//        logger.close();
    }

}
