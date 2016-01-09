/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.statistic;

import cn.edu.pku.lib.dataverse.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author luopc
 */
public class UsageLogSearchResult {
    
    private int total;
    private int pages;
    private int currentPage;
    private List<EventLog> eventLogs;
    private List<Pair<String,Long>> dateHistogram;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public List<EventLog> getEventLogs() {
        return eventLogs;
    }

    public void setEventLogs(List<EventLog> eventLogs) {
        this.eventLogs = eventLogs;
    }

    public List<Pair<String, Long>> getDateHistogram() {
        return dateHistogram;
    }

    public void setDateHistogram(List<Pair<String, Long>> dateHistogram) {
        this.dateHistogram = dateHistogram;
    }
    
    public List<Integer> getPageInterval(int pageCount){
        int begin = (currentPage-1)/pageCount*pageCount+1;
        int end = (currentPage-1)/pageCount*pageCount+pageCount;
        end =  end < pages ? end : pages;
        List<Integer> list = new ArrayList(end-begin+1);
        for(int i=begin; i<=end ;i++)list.add(i);
        return list;
    }
}
