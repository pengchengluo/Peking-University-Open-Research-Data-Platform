/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.XMLType;

/**
 * 北京大学IAAA统一认证
 * @author luopc
 */
public class IaaaValidation {
    
    private static final Logger logger = Logger.getLogger(IaaaValidation.class.getCanonicalName());

    private static final String nameSpace = "http://pku/iaaa/webservice";
    private static final String url = "https://iaaa.pku.edu.cn/iaaaWS/OauthLogon";
    private static final String personNameSpace = "http://pku/datapub/webservice";
    private static final String personUrl = "http://data.pku.edu.cn:7001/dataPubWS/PersonChangeDataWS?WSDL";
    private static final Service service = new Service();
    private static final String appID = "dvn";
    private static final String key = "FED221B65EC66F1FE0430100007F3717";

    public IaaaValidation() {

    }

    public static String bytes2HexString(byte[] b) {    //byte转换为十六进制
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static Result validateToken(String remoteAddr, String token) throws ServiceException, RemoteException {
        Call call = (Call) service.createCall();
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
        String msg = "";
        Result user = null;
        try {
            byte[] msgByte = java.security.MessageDigest.getInstance("MD5").digest(msgBofore.getBytes());
            msg = bytes2HexString(msgByte);
            user = (Result) call.invoke(new Object[]{remoteAddr, appID, token, timestamp, msg});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static Result getSinglePerson(String personID) throws ServiceException, RemoteException {
        Call call = (Call) service.createCall();
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
        String msgBofore = personID + "7696baa1fa4ed9679441764a271e556e";
        String msg = "";
        try {
            byte[] msgByte = java.security.MessageDigest.getInstance("MD5").digest(msgBofore.getBytes());
            msg = bytes2HexString(msgByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Result user = (Result) call.invoke(new Object[]{personID, "", "", "", "", "", "dvn", "162.105.138.117", msg});
        return user;
    }

    public static class Result implements Serializable {
        public String Info;
        public int Status;

        public String getInfo() {
            return Info;
        }

        public void setInfo(String Info) {
            this.Info = Info;
        }

        public int getStatus() {
            return Status;
        }

        public void setStatus(int Status) {
            this.Status = Status;
        }
    }
}
