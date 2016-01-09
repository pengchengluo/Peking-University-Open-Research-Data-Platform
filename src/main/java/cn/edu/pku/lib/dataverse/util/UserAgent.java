/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.util;

/**
 *
 * @author luopc
 */
public class UserAgent {
    
    private String userAgent;
    
    public UserAgent(String userAgent){
        this.userAgent = userAgent;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public boolean isChrome(){
        return userAgent.toLowerCase().contains("chrome");
    }
    
    public boolean isFireFox(){
        return userAgent.toLowerCase().contains("firefox");
    }
    
    public boolean isIE(){
        String lowerCase = userAgent.toLowerCase();
        return lowerCase.toLowerCase().contains("msie") 
                || lowerCase.contains("trident");
    }
    
}
