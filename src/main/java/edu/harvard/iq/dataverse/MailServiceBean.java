/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import cn.edu.pku.lib.dataverse.util.BundleDataverse;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPSenderFailedException;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroup;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroupServiceBean;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean.Key;
import edu.harvard.iq.dataverse.util.SystemConfig;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * original author: roberttreacy
 */
@Stateless
public class MailServiceBean implements java.io.Serializable {

    @EJB
    UserNotificationServiceBean userNotificationService;
    @EJB
    DataverseServiceBean dataverseService;
    @EJB
    DatasetServiceBean datasetService;
    @EJB
    DatasetVersionServiceBean versionService; 
    @EJB
    SystemConfig systemConfig;
    @EJB
    SettingsServiceBean settingsService;
    @EJB
    BundleDataverse bundleDataverse;
    @EJB
    ExplicitGroupServiceBean explicitGroupService;
    
    private static final Logger logger = Logger.getLogger(MailServiceBean.class.getCanonicalName());
    
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    /**
     * Creates a new instance of MailServiceBean
     */
    public MailServiceBean() {
    }

    public void sendMail(String host, String from, String to, String subject, String messageText) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));
            msg.setSubject(subject);
            msg.setText(messageText);
            Transport.send(msg);
        } catch (AddressException ae) {
            ae.printStackTrace(System.out);
        } catch (MessagingException me) {
            me.printStackTrace(System.out);
        }
    }

    @Resource(name = "mail/notifyMailSession")
    private Session session;

    public boolean sendSystemEmail(String to, String subject, String messageText) {
        return sendSystemEmail(to, subject, messageText, Locale.ENGLISH);
    }
    
    public boolean sendSystemEmail(String to, String subject, String messageText,
            Locale locale) {
        boolean sent = false;
        try {
             Message msg = new MimeMessage(session);

            InternetAddress systemAddress = getSystemAddress();
            if (systemAddress != null) {
                msg.setFrom(systemAddress);
                msg.setSentDate(new Date());
                msg.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(to, false));
                msg.setSubject(subject);
                msg.setText(messageText + ResourceBundle.getBundle("Bundle", locale).getString("notification.email.closing"));
                try {
                    Transport.send(msg);
                    sent = true;
                } catch (SMTPSendFailedException ssfe) {
                    logger.warning("Failed to send mail to " + to + " (SMTPSendFailedException)");
                }
            } else {
              // commenting out the warning so as not to clutter the log of installations that haven't set up mail  
              //  logger.warning("Skipping sending mail to " + to + ", because the \"no-reply\" address not set.");
            }
        } catch (AddressException ae) {
            logger.warning("Failed to send mail to " + to);
            ae.printStackTrace(System.out);
        } catch (MessagingException me) {
            logger.warning("Failed to send mail to " + to);
            me.printStackTrace(System.out);
        }
        return sent;
    }
    
    private InternetAddress getSystemAddress() {
       String systemEmail =  settingsService.getValueForKey(Key.SystemEmail);
      
       if (systemEmail!=null) {
           try { 
            return new InternetAddress(systemEmail);
           } catch(AddressException e) {
               return null;
           }
       }
       return null;
     
    }

    //@Resource(name="mail/notifyMailSession")
    public void sendMail(String from, String to, String subject, String messageText) {
        sendMail(from, to, subject, messageText, new HashMap(), Locale.ENGLISH);
    }
    
    public void sendMail(String from, String to, String subject, String messageText, Locale locale) {
        sendMail(from, to, subject, messageText, new HashMap(), locale);
    }

    public void sendMail(String from, String to, String subject, String messageText, Map extraHeaders, Locale locale) {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(session.getProperties().getProperty("mail.from")));
            if (from.matches(EMAIL_PATTERN)) {
//                msg.setFrom(new InternetAddress(from));
                msg.setReplyTo(InternetAddress.parse(from));
            } else {
                // set fake from address; instead, add it as part of the message
                //msg.setFrom(new InternetAddress("invalid.email.address@mailinator.com"));
//                msg.setFrom(getSystemAddress());
                InternetAddress[] address = new InternetAddress[1];
                address[0] = getSystemAddress();
                msg.setReplyTo(address);
                messageText = "From: " + from + "\n\n" + messageText;
            }
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));
            msg.setSubject(subject);
            msg.setText(messageText+"\n\n"+bundleDataverse.getValueByParam("contact.email.footer", locale.getLanguage(), from, from));

            if (extraHeaders != null) {
                for (Object key : extraHeaders.keySet()) {
                    String headerName = key.toString();
                    String headerValue = extraHeaders.get(key).toString();

                    msg.addHeader(headerName, headerValue);
                }
            }

            Transport.send(msg);
        } catch (AddressException ae) {
            ae.printStackTrace(System.out);
        } catch (MessagingException me) {
            me.printStackTrace(System.out);
        }
    }
    
    public Boolean sendNotificationEmail(UserNotification notification){
        return sendNotificationEmail(notification, Locale.ENGLISH);
    }
    
    public Boolean sendNotificationEmail(UserNotification notification, Locale locale){        
        boolean retval = false;
        String emailAddress = getUserEmailAddress(notification);
        if (emailAddress != null){
           Object objectOfNotification =  getObjectOfNotification(notification);
           if (objectOfNotification != null){
               String messageText = getMessageTextBasedOnNotification(notification, objectOfNotification, locale);
               String subjectText = getSubjectTextBasedOnNotification(notification, locale);
               if (!(messageText.isEmpty() || subjectText.isEmpty())){
                    retval = sendSystemEmail(emailAddress, subjectText, messageText, locale); 
               } else {
                   logger.warning("Skipping " + notification.getType() +  " notification, because couldn't get valid message");
               }
           } else { 
               logger.warning("Skipping " + notification.getType() +  " notification, because no valid Object was found");
           }           
        } else {
            logger.warning("Skipping " + notification.getType() +  " notification, because email address is null");
        }
        return retval;
    }
    
    private String getSubjectTextBasedOnNotification(UserNotification userNotification) {
        return getSubjectTextBasedOnNotification(userNotification, Locale.ENGLISH);
    }
        
    private String getSubjectTextBasedOnNotification(UserNotification userNotification, Locale locale) {
        switch (userNotification.getType()) {
            case CREATEDV:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.create.dataverse.subject");
            case REQUESTFILEACCESS:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.request.file.access.subject");
            case GRANTFILEACCESS:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.grant.file.access.subject");
            case REJECTFILEACCESS:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.rejected.file.access.subject");
            case REQUESTJOINGROUP:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.request.join.group.subject");
            case GRANTJOINGROUP:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.grant.join.group.subject");
            case REJECTJOINGROUP:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.rejected.join.group.subject");
            case MAPLAYERUPDATED:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.update.maplayer");
            case CREATEDS:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.create.dataset.subject");
            case SUBMITTEDDS:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.submit.dataset.subject");
            case PUBLISHEDDS:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.publish.dataset.subject");
            case RETURNEDDS:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.returned.dataset.subject");
            case CREATEACC:
                return ResourceBundle.getBundle("Bundle", locale).getString("notification.email.create.account.subject");
        }
        return "";
    }
    
    private String getDatasetManagePermissionsLink(Dataset dataset){        
        return  systemConfig.getDataverseSiteUrl() + "/permissions-manage.xhtml?id=" + dataset.getId();
    }
    
    private String getDataverseManagePermissionsLink(Dataverse dataverse){        
        return  systemConfig.getDataverseSiteUrl() + "/permissions-manage.xhtml?id=" + dataverse.getId();
    }
    
    private String getDatasetLink(Dataset dataset){        
        return  systemConfig.getDataverseSiteUrl() + "/dataset.xhtml?persistentId=" + dataset.getGlobalId();
    }  
    
    private String getDataverseLink(Dataverse dataverse){       
        return  systemConfig.getDataverseSiteUrl() + "/dataverse/" + dataverse.getAlias();
    }
    
    private String getUserGroupManagementLink(ExplicitGroup group){        
        return  systemConfig.getDataverseSiteUrl() + "/user-group-manage.xhtml?groupId=" + group.getId();
    }
   
    private String getMessageTextBasedOnNotification(UserNotification userNotification,
            Object targetObject){
        return getMessageTextBasedOnNotification(userNotification, targetObject, Locale.ENGLISH);
    }
            
    private String getMessageTextBasedOnNotification(UserNotification userNotification,
            Object targetObject, Locale locale){
        
        String messageText = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.greeting");
        DatasetVersion version = null;
        Dataset dataset = null;
        ExplicitGroup group = null;
        String pattern ="";

        switch (userNotification.getType()) {
            case CREATEDV:
                Dataverse dataverse = (Dataverse) targetObject;
                String ownerDataverseName = getOwnerDataverseName(dataverse);
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.createDataverse");
                if (ownerDataverseName != null) {
                    String[] paramArrayCreateDV = {dataverse.getDisplayName(), getDataverseLink(dataverse),dataverse.getOwner().getDisplayName(), getDataverseLink(dataverse.getOwner())};
                    messageText += MessageFormat.format(pattern, paramArrayCreateDV);
                } else {
                    if(locale.getLanguage().equals("zh")){
                        messageText += MessageFormat.format(pattern, "根数据空间", "根数据空间");
                    }else
                        messageText += MessageFormat.format(pattern, "Root Dataverse", "Root Dataverse");
                }
                return messageText;
            case REQUESTFILEACCESS:
                dataset = (Dataset) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.requestFileAccess");
                String[] paramArrayRequestFileAccess = {dataset.getDisplayName(), getDatasetManagePermissionsLink(dataset)};
                messageText += MessageFormat.format(pattern, paramArrayRequestFileAccess);
                return messageText;
            case GRANTFILEACCESS:
                dataset = (Dataset) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.grantFileAccess");
                String[] paramArrayGrantFileAccess = {dataset.getDisplayName(), getDatasetLink(dataset)};
                messageText += MessageFormat.format(pattern, paramArrayGrantFileAccess);
                return messageText;
            case REJECTFILEACCESS:
                dataset = (Dataset) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.rejectFileAccess");
                String[] paramArrayRejectFileAccess = {dataset.getDisplayName(), getDatasetLink(dataset)};
                messageText += MessageFormat.format(pattern, paramArrayRejectFileAccess);
                return messageText;
            case REQUESTJOINGROUP:
                group = (ExplicitGroup) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.requestJoinGroup");
                String[] paramArrayRequestJoinGroup = {group.getDisplayName(),getUserGroupManagementLink(group)};
                messageText += MessageFormat.format(pattern, paramArrayRequestJoinGroup);
                return messageText;
            case GRANTJOINGROUP:
                group = (ExplicitGroup) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.grantJoinGroup");
                String[] paramArrayGrantJoinGroup = {group.getDisplayName()};
                messageText += MessageFormat.format(pattern, paramArrayGrantJoinGroup);
                return messageText;
            case REJECTJOINGROUP:
                group = (ExplicitGroup) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.rejectJoinGroup");
                String[] paramArrayRejectJoinGroup = {group.getDisplayName(),userNotification.getMessage()};
                messageText += MessageFormat.format(pattern, paramArrayRejectJoinGroup);
                return messageText;
            case CREATEDS:
                version =  (DatasetVersion) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.createDataset");                
                String[] paramArrayCreateDataset = {version.getDataset().getDisplayName(), getDatasetLink(version.getDataset()), 
                    version.getDataset().getOwner().getDisplayName(), getDataverseLink(version.getDataset().getOwner())};               
                messageText += MessageFormat.format(pattern, paramArrayCreateDataset);
                return messageText;
            case MAPLAYERUPDATED:
                version =  (DatasetVersion) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.worldMap.added");
                String[] paramArrayMapLayer = {version.getDataset().getDisplayName(), getDatasetLink(version.getDataset())};
                messageText += MessageFormat.format(pattern, paramArrayMapLayer);
                return messageText;                   
            case SUBMITTEDDS:
                version =  (DatasetVersion) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.wasSubmittedForReview");
                String[] paramArraySubmittedDataset = {version.getDataset().getDisplayName(), getDatasetLink(version.getDataset()), 
                    version.getDataset().getOwner().getDisplayName(),  getDataverseLink(version.getDataset().getOwner())};
                messageText += MessageFormat.format(pattern, paramArraySubmittedDataset);
                return messageText;
            case PUBLISHEDDS:
                version =  (DatasetVersion) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.wasPublished");
                String[] paramArrayPublishedDataset = {version.getDataset().getDisplayName(), getDatasetLink(version.getDataset()), 
                    version.getDataset().getOwner().getDisplayName(),  getDataverseLink(version.getDataset().getOwner())};
                messageText += MessageFormat.format(pattern, paramArrayPublishedDataset);
                return messageText;
            case RETURNEDDS:
                version =  (DatasetVersion) targetObject;
                pattern = ResourceBundle.getBundle("Bundle", locale).getString("notification.email.wasReturnedByReviewer");
                String[] paramArrayReturnedDataset = {version.getDataset().getDisplayName(), getDatasetLink(version.getDataset()), 
                    version.getDataset().getOwner().getDisplayName(),  getDataverseLink(version.getDataset().getOwner())};
                messageText += MessageFormat.format(pattern, paramArrayReturnedDataset);
                return messageText;
            case CREATEACC:
                messageText += ResourceBundle.getBundle("Bundle", locale).getString("notification.email.welcome");
                return messageText;
        }
        
        return "";
    }
    
    private Object getObjectOfNotification (UserNotification userNotification){
        switch (userNotification.getType()) {
            case CREATEDV:
                return dataverseService.find(userNotification.getObjectId());
            case REQUESTFILEACCESS:
            case GRANTFILEACCESS:
            case REJECTFILEACCESS:
                return datasetService.find(userNotification.getObjectId());
            case REQUESTJOINGROUP:
                return explicitGroupService.findById(userNotification.getObjectId());
            case GRANTJOINGROUP:
            case REJECTJOINGROUP:
                return explicitGroupService.findById(userNotification.getObjectId());
            case MAPLAYERUPDATED:
            case CREATEDS:
            case SUBMITTEDDS:
            case PUBLISHEDDS:
            case RETURNEDDS:
                return versionService.find(userNotification.getObjectId());
            case CREATEACC:
                return userNotification.getUser();
        }
        return null;
    }
    

    
    
    private String getUserEmailAddress(UserNotification notification) {
        if (notification != null) {
            if (notification.getUser() != null) {
                if (notification.getUser().getDisplayInfo() != null) {
                    if (notification.getUser().getDisplayInfo().getEmailAddress() != null) {
                        logger.fine("Email address: "+notification.getUser().getDisplayInfo().getEmailAddress());
                        return notification.getUser().getDisplayInfo().getEmailAddress();
                    }
                }
            }
        }
        
        logger.fine("no email address");
        return null; 
    }
     
    private String getOwnerDataverseName(Dataverse dataverse) {
        if (dataverse.getOwner() != null) {
            return dataverse.getOwner().getDisplayName();
        } 
        return null;
    }
}
