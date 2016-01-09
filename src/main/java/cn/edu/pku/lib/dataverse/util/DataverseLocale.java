/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author luopc
 */
@Named
@SessionScoped
public class DataverseLocale implements Serializable {
    
    private static final long serialVersionUID = -4294031399458044273L;
    
    private static final Logger logger = 
            Logger.getLogger(DataverseLocale.class.getCanonicalName());
    
    private Locale locale;
    
    public DataverseLocale(){
        try{
            locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
        }catch(Exception e){
            logger.log(Level.WARNING, "locale set warning", e);
            locale = Locale.CHINA;
        }
    }
    
    public Locale getLocale(){
        return locale;
    }
    
    public boolean isLocaleZh(){
        return locale.getLanguage().equals("zh");
    }
    
    public void setLocaleZh(){
        locale = Locale.CHINA;
    }
    
    public void setLocaleEn(){
        locale = Locale.ENGLISH;
    }
    
    public void changePageToZh(){
        locale = Locale.CHINA;
        try{
            String url = ((HttpServletRequest)FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest()).getHeader("referer");
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        }catch(IOException ioe){
            logger.log(Level.SEVERE, "rediect error", ioe);
        }
    }
    
    public void changePageToEn(){
        locale = Locale.ENGLISH;
        try{
            String url = ((HttpServletRequest)FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest()).getHeader("referer");
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        }catch(IOException ioe){
            logger.log(Level.SEVERE, "rediect error", ioe);
        }
    }
}
