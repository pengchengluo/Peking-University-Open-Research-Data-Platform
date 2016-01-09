/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.statistic;

import static edu.harvard.iq.dataverse.util.json.NullSafeJsonBuilder.jsonObjectBuilder;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author luopc
 */
public class EventLog {
    public enum EventType{
        VIEW_DATASET,
        VIEW_DATAVERSE,
        
        LOGIN_IN,
        
        DOWNLOAD_FILE,
        
        REQUEST_ACCESS_FILE,
        
        REQUEST_JOIN_GROUP,
        ACCEPT_JOIN_GROUP,
        REJECT_JOIN_GROUP
    }
    
    public static final String DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME);
    
    //basic information
    private EventType eventType;
    private String ip;
    private Date date;
    private String userAgent;
    private String referrer;
    
    //user information
    private String userId;
    private String userName;
    private String affiliation;
    private String position;
    
    //iplocation
    private String continent;
    private String country;
    private String subdivision;
    private String city;
    private Double latitude;
    private Double longitude;
    
    //dataverse、dataset、datafile、group ID
    private Long dataverseId;
    private Long datasetId;
    private Long datafileId;
    private Long groupId;
    
    public EventLog(){}
    
    public EventLog(EventType eventType, String ip, Date date, String userId,
            String userAgent, String referrer){
        this.eventType = eventType;
        this.ip = ip;
        this.date = date;
        this.userId = userId;
        this.userAgent = userAgent;
        this.referrer = referrer;
    }
    
    public EventLog(EventType eventType, String userId, HttpServletRequest request){
        this.eventType = eventType;
        this.ip = request.getRemoteAddr();
        this.date = new Date();
        this.userId = userId;
        this.referrer = request.getHeader("referer");
        this.userAgent = request.getHeader("User-Agent");
    }
    
    public String toJson(){
        JsonObjectBuilder bld = jsonObjectBuilder();
        bld.add("eventType", eventType.toString());
        bld.add("ip", ip);
        bld.add("date", dateTimeFormat.format(date));
        if(userAgent != null) bld.add("userAgent", userAgent);
        if(referrer != null) bld.add("referrer", referrer);
        
        bld.add("userId", userId);
        bld.add("userName", userName);
        if(position != null)bld.add("position", position);
        if(affiliation != null) bld.add("affiliation", affiliation);
        
        if(continent != null) bld.add("continent", continent);
        if(country != null) bld.add("country", country);
        if(subdivision != null) bld.add("subdivision", subdivision);
        if(city != null) bld.add("city", city);
        if(latitude != null) bld.add("latitude", latitude);
        if(longitude != null) bld.add("longitude", longitude);

        if(dataverseId != null) bld.add("dataverseId", dataverseId);
        if(datasetId != null) bld.add("datasetId", datasetId);
        if(datafileId != null) bld.add("datafileId", datafileId);
        if(groupId != null) bld.add("groupId", groupId);
        
        return bld.build().toString();
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append('{');
        str.append("StatisticLog");str.append(", ");
        str.append(date.toString());str.append(", ");
        str.append(ip);str.append(", ");
        str.append(userId);str.append(", ");
        str.append(userAgent);str.append(", ");
        str.append(referrer);
        str.append('}');
        return str.toString();
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
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

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSubdivision() {
        return subdivision;
    }

    public void setSubdivision(String subdivision) {
        this.subdivision = subdivision;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getDataverseId() {
        return dataverseId;
    }

    public void setDataverseId(Long dataverseId) {
        this.dataverseId = dataverseId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getDatafileId() {
        return datafileId;
    }

    public void setDatafileId(Long datafileId) {
        this.datafileId = datafileId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
