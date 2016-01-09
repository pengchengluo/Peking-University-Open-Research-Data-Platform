/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.util;

/**
 *
 * @author luopc
 * @version 1.0
 * 2015-03-24
 */
public class BytesUtil {

    /**
     * 字节数组转为16进制字符串
     * @param b
     * @return 
     */
    public static String bytes2HexString(byte[] b) {
        StringBuilder ret =  new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                ret.append('0');
            }
            ret.append(hex.toUpperCase());
        }
        return ret.toString();
    }
}
