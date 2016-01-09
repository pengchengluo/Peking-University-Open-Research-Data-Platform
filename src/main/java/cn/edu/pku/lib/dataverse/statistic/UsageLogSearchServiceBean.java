/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.statistic;

import cn.edu.pku.lib.dataverse.statistic.EventLog.EventType;
import cn.edu.pku.lib.dataverse.util.Pair;
import cn.edu.pku.lib.util.StringUtil;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.core.search.aggregation.DateHistogramAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author luopc
 */
@Stateless
public class UsageLogSearchServiceBean {

    private static final Logger logger = Logger.getLogger(UsageLogSearchServiceBean.class.getCanonicalName());

    private JestClient client;

    @EJB
    SettingsServiceBean settingsServiceBean;

    @PostConstruct
    public void init() {
        JestClientFactory factory = new JestClientFactory();
        String elasticSearchUrl = settingsServiceBean.getValueForKey(SettingsServiceBean.Key.ElasticSearchUrl,
                "http://localhost:9200");
        factory.setHttpClientConfig(new HttpClientConfig.Builder(elasticSearchUrl)
                .multiThreaded(true)
                .build());
        client = factory.getObject();
    }

    private String getQuery(List<EventType> events, String ip, String userId,
            String userName, String affiliation, String position,
            Date start, Date end,
            List<Long> dataverseIds,
            List<Long> datasetIds, List<Long> datafileIds, List<Long> groupIds) {
        if ((events == null || events.isEmpty()) && ip == null && userId == null &&
                userName == null && affiliation == null && position == null &&
                start == null && end == null &&
                (dataverseIds == null || dataverseIds.isEmpty()) &&
                (datasetIds == null || datasetIds.isEmpty()) &&
                (datafileIds == null || datafileIds.isEmpty()) &&
                (groupIds == null || groupIds.isEmpty())) {
            return null;
        }
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("\"query\":{\"filtered\":{\"filter\": {\"bool\":{");
//        queryBuilder.append("\"query\":{\"bool\":{");
        List<String> query = new ArrayList<>();
        if (events != null) {
            StringBuilder eventQuery = new StringBuilder();
            List<String> eventQueryList = new ArrayList<>(events.size());
            for(EventType event : events){
                eventQueryList.add("\"should\":{\"term\":{\"eventType\":\"" + event + "\"}}");
            }
            eventQuery.append("\"must\":{\"bool\":{");
            eventQuery.append(StringUtil.listToString(eventQueryList,","));
            eventQuery.append("}}");
            query.add(eventQuery.toString());
            
        }
        if (ip != null) {
            query.add("\"must\":{\"term\":{\"ip\":\"" + ip + "\"}}");
        }
        if (userId != null) {
            query.add("\"must\":{\"term\":{\"userId\":\"" + StringUtil.jsonEscape(userId) + "\"}}");
        }
        if(userName != null){
            query.add("\"must\":{\"term\":{\"userName\":\"" + StringUtil.jsonEscape(userName) + "\"}}");
        }
        if(affiliation != null){
            query.add("\"must\":{\"term\":{\"affiliation\":\"" + StringUtil.jsonEscape(affiliation) + "\"}}");
        }
        if(position != null){
            query.add("\"must\":{\"term\":{\"position\":\"" + StringUtil.jsonEscape(position) + "\"}}");
        }
        String range = getRange(start, end);
        if(range != null){
            query.add("\"must\":{"+range+"}");
        }
        
        if(dataverseIds != null){
            for(Long dataverseId : dataverseIds){
                query.add("\"should\":{\"term\":{\"dataverseId\":" + dataverseIds + "}}");
            }
        }
        if(datasetIds != null){
            for(Long datasetId : datasetIds){
                query.add("\"should\":{\"term\":{\"datasetId\":" + datasetId + "}}");
            }
        }
        if(datafileIds != null){
            for(Long datafileId : datafileIds){
                query.add("\"should\":{\"term\":{\"datafileId\":" + datafileId + "}}");
            }
        }
        if(groupIds != null){
            for(Long groupId : groupIds){
                query.add("\"should\":{\"term\":{\"groupId\":" + groupId + "}}");
            }
        }
        queryBuilder.append(StringUtil.listToString(query, ","));
        queryBuilder.append("}}}}");
        return queryBuilder.toString();
    }

    private String getDateHistogramAggregation(String interval) {
        if (interval == null) {
            return null;
        }
        switch (interval) {
            case StatisticConstant.AGG_INTERVAL_YEAR:
                return "\"aggs\":{\"event_over_time\":{\"date_histogram\":{\"field\":\"date\",\"interval\":\"year\",\"format\" : \"yyyy\"}}}";
            case StatisticConstant.AGG_INTERVAL_MONTH:
                return "\"aggs\":{\"event_over_time\":{\"date_histogram\":{\"field\":\"date\",\"interval\":\"month\",\"format\" : \"yyyy-MM\"}}}";
            case StatisticConstant.AGG_INTERVAL_DAY:
                return "\"aggs\":{\"event_over_time\":{\"date_histogram\":{\"field\":\"date\",\"interval\":\"day\",\"format\" : \"yyyy-MM-dd\"}}}";
            case StatisticConstant.AGG_INTERVAL_HOUR:
                return "\"aggs\":{\"event_over_time\":{\"date_histogram\":{\"field\":\"date\",\"interval\":\"day\",\"format\" : \"yyyy-MM-dd hh\"}}}";
            default:
                return "\"aggs\":{\"event_over_time\":{\"date_histogram\":{\"field\":\"date\",\"interval\":\"day\",\"format\" : \"yyyy-MM-dd\"}}}";
        }
    }
    
    private String getSortByDate(){
        return "\"sort\" : [{\"date\" : {\"order\" : \"desc\"}}]";
    }
    
    private String getRange(Date start, Date end){
        if(start == null && end == null)return null;
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.format(start);
        List<String> arrays = new ArrayList<>();
        if(start != null)
            arrays.add("\"gte\":\""+format.format(start)+"\"");
        if(end != null)
            arrays.add("\"lte\":\""+format.format(end)+"\"");
        arrays.add("\"format\":\"yyyy-MM-dd HH:mm:ss\"");
        arrays.add("\"time_zone\": \"+08:00\"");
        String range = "\"range\":{\"date\":{"+StringUtil.listToString(arrays, ",")+"}}";
        return range;
    }
    
    private String getFields(List<String> fields){
        if(fields == null || fields.isEmpty())return null;
        StringBuilder str = new StringBuilder();
        str.append("\"fields\" : [");
        if(fields.size()>0){
            str.append("\"");
            str.append(fields.get(0));
            str.append("\"");
        }
        for(int i=1 ; i<fields.size(); i++){
            str.append(",\"");
            str.append(fields.get(i));
            str.append("\"");
        }
        str.append("]");
        return str.toString();
    }

    public String getQuery(UsageLogSearchQuery query) {
        List<String> subQuery = new ArrayList<>();
        if (query.getFrom() != null && query.getFrom() >= 0) {
            subQuery.add("\"from\":" + query.getFrom());
        }
        if (query.getSize() != null && query.getSize() >= 0) {
            subQuery.add("\"size\":" + query.getSize());
        }
        String queryHistogram = getDateHistogramAggregation(query.getDateHistogramInterval());
        if(queryHistogram != null)subQuery.add(queryHistogram);
        subQuery.add(getQuery(query.getEvents(), query.getIp(), query.getUserId(),
                query.getUserName(), query.getAffiliation(), query.getPosition(),
                query.getStartTime(), query.getEndTime(),
                query.getDataverseIds(), query.getDatasetIds(), query.getDatafileIds(),
                query.getGroupIds()));
        subQuery.add(getSortByDate());
        String queryFields = getFields(query.getFields());
        if(queryFields != null)subQuery.add(queryFields);

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{");
        queryBuilder.append(StringUtil.listToString(subQuery, ","));
        queryBuilder.append("}");
        return queryBuilder.toString();
    }

    public List<Pair<String, Long>> getDateHistogram(UsageLogSearchQuery searchQuery) {
        String query = getQuery(searchQuery);
        Search search = new Search.Builder(query)
                .addIndex(StatisticConstant.INDEX_NAME)
                .addType(StatisticConstant.INDEX_TYPE)
                .build();
        List<Pair<String, Long>> data = new ArrayList<>();
        try {
            SearchResult result = client.execute(search);
            MetricAggregation aggregation = result.getAggregations();
            DateHistogramAggregation dateHistogram = aggregation.getDateHistogramAggregation("event_over_time");
            for (DateHistogramAggregation.DateHistogram unit : dateHistogram.getBuckets()) {
                data.add(new Pair(unit.getTimeAsString(), unit.getCount()));
            }
        } catch (IOException ioe) {
            logger.log(Level.INFO, null, ioe);
        }
        return data;
    }

    public UsageLogSearchResult search(UsageLogSearchQuery searchQuery) {
        String query = getQuery(searchQuery);
        Search search = new Search.Builder(query)
                .addIndex(StatisticConstant.INDEX_NAME)
                .addType(StatisticConstant.INDEX_TYPE)
                .build();

        SearchResult result;
        UsageLogSearchResult logResult = new UsageLogSearchResult();
        try {
            result = client.execute(search);
            if (result != null) {
                List<Hit<EventLog, Void>> hits = result.getHits(EventLog.class);
                logResult.setTotal(result.getTotal());
                List<EventLog> logs = new ArrayList<>(hits.size());
                for (Hit<EventLog, Void> hit : hits) {
                    logs.add(hit.source);
                }
                logResult.setEventLogs(logs);
                logResult.setPages((int) ((result.getTotal() - 1) / searchQuery.getSize() + 1));
                logResult.setCurrentPage((int) (searchQuery.getFrom() / searchQuery.getSize() + 1));
            }

            List<Pair<String, Long>> data = new ArrayList<>();
            MetricAggregation aggregation = result.getAggregations();
            DateHistogramAggregation dateHistogram = aggregation.getDateHistogramAggregation("event_over_time");
            if (dateHistogram != null && dateHistogram.getBuckets().size() > 0) {
                for (DateHistogramAggregation.DateHistogram unit : dateHistogram.getBuckets()) {
                    data.add(new Pair(unit.getTimeAsString(), unit.getCount()));
                }
            } else if(searchQuery.getDateHistogramInterval()!=null){
                SimpleDateFormat format = null;
                switch (searchQuery.getDateHistogramInterval()) {
                    case StatisticConstant.AGG_INTERVAL_YEAR:
                        format = new SimpleDateFormat("yyyy");break;
                    case StatisticConstant.AGG_INTERVAL_MONTH:
                        format = new SimpleDateFormat("yyyy-MM");break;
                    case StatisticConstant.AGG_INTERVAL_DAY:
                        format = new SimpleDateFormat("yyyy-MM-dd");break;
                    case StatisticConstant.AGG_INTERVAL_HOUR:
                        format = new SimpleDateFormat("yyyy-MM-dd hh");break;
                    default:
                        format = new SimpleDateFormat("yyyy-MM-dd");
                }
                data.add(new Pair(format.format(new Date()),0));
            }
            logResult.setDateHistogram(data);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return logResult;
    }

    @PreDestroy
    public void close() {
        if(client != null){
            client.shutdownClient();
            client = null;
        }
    }
}
