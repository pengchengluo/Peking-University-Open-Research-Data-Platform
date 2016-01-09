/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.statistic;

import cn.edu.pku.lib.dataverse.statistic.EventLog.EventType;
import java.util.Date;
import java.util.List;

/**
 *
 * @author luopc
 */
public class UsageLogSearchQuery implements Cloneable {

    private String dateHistogramInterval;

    private List<EventType> events;
    private String ip;
    private String userId;
    private String userName;
    private String affiliation;
    private String position;

    private Date startTime;
    private Date endTime;

    private List<String> fields;

    private List<Long> dataverseIds;
    private List<Long> datasetIds;
    private List<Long> datafileIds;
    private List<Long> groupIds;

    private Long from = 0L;
    private Long size = 10L;

    public String getDateHistogramInterval() {
        return dateHistogramInterval;
    }

    public void setDateHistogramInterval(String dateHistogramInterval) {
        this.dateHistogramInterval = dateHistogramInterval;
    }

    public List<EventType> getEvents() {
        return events;
    }

    public void setEvents(List<EventType> events) {
        this.events = events;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<Long> getDataverseIds() {
        return dataverseIds;
    }

    public void setDataverseIds(List<Long> dataverseIds) {
        this.dataverseIds = dataverseIds;
    }

    public List<Long> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<Long> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public List<Long> getDatafileIds() {
        return datafileIds;
    }

    public void setDatafileIds(List<Long> datafileIds) {
        this.datafileIds = datafileIds;
    }

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public UsageLogSearchQuery clone() {      
        UsageLogSearchQuery o = null;    
        try {         
            o = (UsageLogSearchQuery) super.clone();
        } catch (CloneNotSupportedException e) {  
            e.printStackTrace();
        } 
        return o;  
    }
}
