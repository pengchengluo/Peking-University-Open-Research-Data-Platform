/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author luopc
 */
public class MapUtil {
    
    public static <T1,T2>  List<Map.Entry<T1,T2>> MapToEntryList(Map<T1,T2> map){
        List<Map.Entry<T1,T2>> entries = new ArrayList<>(map.size());
        Iterator<Map.Entry<T1, T2>> it = map.entrySet().iterator();
        while(it.hasNext()) {
            entries.add(it.next());
        }
        return entries;
    }
    
}
