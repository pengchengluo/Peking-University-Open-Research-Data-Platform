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
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.XMLType;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 北京大学综合数据服务
 *
 * @author luopc
 * @version 1.0 2015-03-24
 */
public class PKUTpub {

    private static final Logger logger = Logger.getLogger(PKUTpubResult.class.getCanonicalName());

    private static String personNameSpace;
    private static String personUrl;
    private static String key;
    private static String appID;
    private static String appIP;
    private static final Service service = new Service();

    static{
        InputStream in = PKUTpub.class.getResourceAsStream("PKUTpub.properties");
        Properties pro = new Properties();
        try {
            pro.load(in);
            personNameSpace = pro.getProperty("personNameSpace");
            personUrl = pro.getProperty("personUrl");
            key = pro.getProperty("key");
            appID = pro.getProperty("appID");
            appIP = pro.getProperty("appIP");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "PKUIAAAValidation read error", ex);
        }
    }
    
    /**
     * 根据学号（教工号），获取人员信息 信息获取不成功时会抛出IAAAException运行时错误
     *
     * @param personID
     * @return
     */
    public static PKUTpubResult getSinglePerson(String personID) {
        Call call = null;
        try {
            call = (Call) service.createCall();
        } catch (ServiceException ex) {
            logger.log(Level.SEVERE,
                    "web service client call creation fail", ex);
            throw new PKUIAAAException("web service client call creation fail", ex);
        }
        call.setTargetEndpointAddress(personUrl);
        QName qn = new QName(personNameSpace, "Result");
        call.setReturnType(qn, Result.class);
        call.registerTypeMapping(Result.class, qn, BeanSerializerFactory.class, BeanDeserializerFactory.class);
        call.setOperationName(new QName(personNameSpace, "getSinglePerson"));
        call.addParameter("personID", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("euid", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("personName", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("nameAbbr", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("deptID", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("userType", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("appID", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("appIP", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("messageAbstract", XMLType.XSD_STRING, ParameterMode.IN);
        String msgBofore = appID + key;
        Result result = null;
        try {
            byte[] msgByte = java.security.MessageDigest.getInstance("MD5").digest(msgBofore.getBytes());
            String msg = BytesUtil.bytes2HexString(msgByte);
            result = (Result) call.invoke(new Object[]{personID, "", "", "", "", "", appID, appIP, msg});
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, "md5 digest error", ex);
            throw new PKUIAAAException("md5 digest error", ex);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, "Result invoke failed", ex);
            throw new PKUIAAAException("Result invoke failed", ex);
        }
        return PKUTpubResultParse(result);
    }

    public static PKUTpubResult PKUTpubResultParse(Result result) {
        if (result == null) {
            logger.log(Level.SEVERE,
                    "Tpub result parser error: result = null");
            throw new PKUIAAAException("Tpub result parser error: result = null");
        } else if (result.Status != 0) {
            String msg = "Tpub result parser error: result status = " + result.Status
                    + ", info = " + result.Info;
            logger.log(Level.SEVERE, msg);
            throw new PKUIAAAException(msg);
        }
        Document personInfoDoc = Jsoup.parseBodyFragment(result.Info.replace("tpub:", ""));
        PKUTpubResult tpub = new PKUTpubResult();
        tpub.setSpeciality(personInfoDoc.select("speciality").get(0).text().trim());
        return tpub;
    }
}
