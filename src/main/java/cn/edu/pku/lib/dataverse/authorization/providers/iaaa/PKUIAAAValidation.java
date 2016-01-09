/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

import cn.edu.pku.lib.util.BytesUtil;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.XMLType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 北京大学IAAA统一认证
 *
 * @author luopc
 * @version 1.0 2015-03-24
 */
public class PKUIAAAValidation {

    private static final Logger logger = Logger.getLogger(PKUIAAAValidation.class.getCanonicalName());

    private static String nameSpace;
    private static String url;
    private static final Service service = new Service();
    private static String appID;
//    private static final String key = "FED221B65EC66F1FE0430100007F3717";
    private static String key;
    
    static{
        InputStream in = PKUIAAAValidation.class.getResourceAsStream("PKUIAAAValidation.properties");
        Properties pro = new Properties();
        try {
            pro.load(in);
            nameSpace = pro.getProperty("nameSpace");
            url = pro.getProperty("url");
            appID = pro.getProperty("appID");
            key = pro.getProperty("key");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "PKUIAAAValidation read error", ex);
        }
    }

    public static PKUIAAAResult validateToken(String remoteAddr, String token){
        Call call = null;
        try{
            call = (Call) service.createCall();
        }catch(ServiceException ex){
            logger.log(Level.SEVERE," web service client call creation fail", ex);
            throw new PKUIAAAException("web service client call creation fail", ex);
        }
        call.setTargetEndpointAddress(url);
        call.setEncodingStyle(nameSpace);//传非字符串类型参数需要明确
        QName qn = new QName(nameSpace, "Result");
        call.setReturnType(qn, Result.class);
        call.registerTypeMapping(Result.class, qn, BeanSerializerFactory.class, BeanDeserializerFactory.class);

        call.setOperationName(new QName(nameSpace, "userLogon"));
        call.addParameter("remoteAddr", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("appID", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("token", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("timestamp", XMLType.XSD_LONG, ParameterMode.IN);
        call.addParameter("msgAbstract", XMLType.XSD_STRING, ParameterMode.IN);

        long timestamp = new java.util.Date().getTime();
        String msgBofore = remoteAddr + appID + token + timestamp + key;
        Result result = null;
        try {
            byte[] msgByte = java.security.MessageDigest.getInstance("MD5").digest(msgBofore.getBytes());
            String msg = BytesUtil.bytes2HexString(msgByte);
            result = (Result) call.invoke(new Object[]{remoteAddr, appID, token, timestamp, msg});
        }catch(NoSuchAlgorithmException ex){
            logger.log(Level.SEVERE, "md5 digest error", ex);
            throw new PKUIAAAException("md5 digest error", ex);
        }catch(RemoteException ex){
            logger.log(Level.SEVERE, "Result invoke failed", ex);
            throw new PKUIAAAException("Result invoke failed", ex);
        }
        return PKUIAAAResultParse(result);
    }

    private static PKUIAAAResult PKUIAAAResultParse(Result result) {
        if (result == null) {
            logger.log(Level.SEVERE, "IAAA result parser error: result = null");
            throw new PKUIAAAException("IAAA result parser error: result = null");
        } else if (result.Status != 0) {
            String msg = "IAAA result parser error: result status = " + result.Status
                    + ", info = " + result.Info;
            logger.log(Level.SEVERE, msg);
            throw new PKUIAAAException(msg);
        }
        Document doc = Jsoup.parseBodyFragment(result.Info.replace("iaaa:", ""));
        PKUIAAAResult iaaa = new PKUIAAAResult();
        iaaa.setLogonID(doc.select("logonid").get(0).text().trim());
        iaaa.setName(doc.select("name").get(0).text().trim());
        iaaa.setDept(doc.select("dept").get(0).text().trim());
        iaaa.setUserType(doc.select("userType").get(0).text().trim());
        return iaaa;
    }
}
