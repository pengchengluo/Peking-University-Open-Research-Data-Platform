/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.util;

import com.ibm.icu.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author luopc
 */
@Stateless
@Named("BundleDataverse")
public class BundleDataverse {

    private ResourceBundle bundleDefault;
    private ResourceBundle bundleZh;

    public String getValue(String key) {
        return getValue(key, null);
    }

    public String getValue(String key, String language) {
        if (bundleDefault == null) {
            bundleDefault = ResourceBundle.getBundle("Bundle", Locale.ENGLISH);
            bundleZh = ResourceBundle.getBundle("Bundle", Locale.SIMPLIFIED_CHINESE);
        }
        if (language != null) {
            if (language.startsWith("zh")) {
                return bundleZh.getString(key);
            }
        }
        return bundleDefault.getString(key);
    }
    
    public String getValueByParam(String key, String language, String... params) {
        if (bundleDefault == null) {
            bundleDefault = ResourceBundle.getBundle("Bundle", Locale.ENGLISH);
            bundleZh = ResourceBundle.getBundle("Bundle", Locale.SIMPLIFIED_CHINESE);
        }
        if (language != null) {
            if (language.startsWith("zh")) {
                return MessageFormat.format(bundleZh.getString(key), params);
            }
        }
        return MessageFormat.format(bundleDefault.getString(key), params);
    }
}
