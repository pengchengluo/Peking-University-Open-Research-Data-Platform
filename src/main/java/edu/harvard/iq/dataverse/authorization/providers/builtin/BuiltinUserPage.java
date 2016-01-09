/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.authorization.providers.builtin;

import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUser;
import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUserServiceBean;
import cn.edu.pku.lib.dataverse.statistic.EventLog;
import cn.edu.pku.lib.dataverse.statistic.UsageLogIndexServiceBean;
import cn.edu.pku.lib.util.StringUtil;
import com.ibm.icu.text.MessageFormat;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersionServiceBean;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseHeaderFragment;
import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.MailServiceBean;
import edu.harvard.iq.dataverse.PermissionServiceBean;
import edu.harvard.iq.dataverse.UserNotification;
import static edu.harvard.iq.dataverse.UserNotification.Type.CREATEDV;
import edu.harvard.iq.dataverse.UserNotificationServiceBean;
import edu.harvard.iq.dataverse.authorization.AuthenticationServiceBean;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.UserRecordIdentifier;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroup;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroupServiceBean;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser.UserType;
import edu.harvard.iq.dataverse.passwordreset.PasswordValidator;
import edu.harvard.iq.dataverse.util.JsfHelper;
import static edu.harvard.iq.dataverse.util.JsfHelper.JH;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author xyang
 */
@ViewScoped
@Named("DataverseUserPage")
public class BuiltinUserPage implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(BuiltinUserPage.class.getCanonicalName());

    public enum EditMode {

        CREATE, EDIT, CHANGE_PASSWORD, FORGOT
    };

    @Inject
    DataverseSession session;
    @EJB
    DataverseServiceBean dataverseService;
    @EJB
    UserNotificationServiceBean userNotificationService;
    @EJB
    DatasetServiceBean datasetService;
    @EJB
    DatasetVersionServiceBean datasetVersionService;
    @EJB
    PermissionServiceBean permissionService;
    @EJB
    BuiltinUserServiceBean builtinUserService;
    @EJB
    PKUIAAAUserServiceBean pkuIAAAUserService;
    @EJB
    AuthenticationServiceBean authenticationService;
    @EJB
    ExplicitGroupServiceBean explicitGroupService;
    @EJB
    MailServiceBean mailService;

    @EJB
    AuthenticationServiceBean authSvc;
    @EJB
    UsageLogIndexServiceBean usageLogIndexService;

    private AuthenticatedUser currentUser;
    private BuiltinUser builtinUser;
    private PKUIAAAUser pkuIAAAUser;
    private EditMode editMode;
    private AuthenticatedUser.UserType registerUserType;
    private Long joinGroupId;
    private String redirectDataverseId;
    private String redirectDatasetId;

    @NotBlank(message = "Please enter a password for your account.")
    private String inputPassword;

    @NotBlank(message = "Please enter a password for your account.")
    private String currentPassword;
    private Long dataverseId;
    private List<UserNotification> notificationsList;
    private int activeIndex;
    private String selectTab = "somedata";
    UIInput usernameField;

    public EditMode getChangePasswordMode() {
        return EditMode.CHANGE_PASSWORD;
    }

    public AuthenticatedUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
    }

    public BuiltinUser getBuiltinUser() {
        return builtinUser;
    }

    public void setBuiltinUser(BuiltinUser builtinUser) {
        this.builtinUser = builtinUser;
    }

    public PKUIAAAUser getPkuIAAAUser() {
        return pkuIAAAUser;
    }

    public void setPkuIAAAUser(PKUIAAAUser pkuIAAAUser) {
        this.pkuIAAAUser = pkuIAAAUser;
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public String getInputPassword() {
        return inputPassword;
    }

    public void setInputPassword(String inputPassword) {
        this.inputPassword = inputPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public Long getDataverseId() {

        if (dataverseId == null) {
            dataverseId = dataverseService.findRootDataverse().getId();
        }
        return dataverseId;
    }

    public void setDataverseId(Long dataverseId) {
        this.dataverseId = dataverseId;
    }

    public List getNotificationsList() {
        return notificationsList;
    }

    public void setNotificationsList(List notificationsList) {
        this.notificationsList = notificationsList;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public String getSelectTab() {
        return selectTab;
    }

    public void setSelectTab(String selectTab) {
        this.selectTab = selectTab;
    }

    public UIInput getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(UIInput usernameField) {
        this.usernameField = usernameField;
    }

    public UserType getRegisterUserType() {
        return registerUserType;
    }

    public void setRegisterUserType(UserType registerUserType) {
        this.registerUserType = registerUserType;
    }

    public Long getJoinGroupId() {
        return joinGroupId;
    }

    public void setJoinGroupId(Long joinGroupId) {
        this.joinGroupId = joinGroupId;
    }

    public String getRedirectDataverseId() {
        return redirectDataverseId;
    }

    public void setRedirectDataverseId(String redirectDataverseId) {
        this.redirectDataverseId = redirectDataverseId;
    }

    public String getRedirectDatasetId() {
        return redirectDatasetId;
    }

    public void setRedirectDatasetId(String redirectDatasetId) {
        this.redirectDatasetId = redirectDatasetId;
    }

    public String init() {
        if (editMode == EditMode.CREATE) {
            if (!session.getUser().isAuthenticated()) { // in create mode for new user
                builtinUser = new BuiltinUser();
                if (registerUserType != null && registerUserType == AuthenticatedUser.UserType.ADVANCE) {
                    builtinUser.setUserType(AuthenticatedUser.UserType.ADVANCE);
                } else {
                    builtinUser.setUserType(AuthenticatedUser.UserType.ORDINARY);
                }
                HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                if (redirectDataverseId != null) {
                    Cookie cookie = new Cookie("redirectDataverseId", redirectDataverseId);
                    cookie.setMaxAge(-1);
                    response.addCookie(cookie);
                }
                if (redirectDatasetId != null) {
                    Cookie cookie = new Cookie("redirectDatasetId", redirectDatasetId);
                    cookie.setMaxAge(-1);
                    response.addCookie(cookie);
                }
                builtinUser.setPosition("student");
                return "";
            } else {
                editMode = null; // we can't be in create mode for an existing user
            }
        }

        if (session.getUser().isAuthenticated()) {
            currentUser = (AuthenticatedUser) session.getUser();
            notificationsList = userNotificationService.findByUser(((AuthenticatedUser) currentUser).getId());
            if (currentUser.isBuiltInUser()) {
                builtinUser = builtinUserService.findByUserName(currentUser.getUserIdentifier());
            } else if (currentUser.isPKUIAAAUser()) {
                pkuIAAAUser = pkuIAAAUserService.findByUserName(currentUser.getUserIdentifier());
            }
            switch (selectTab) {
                case "notifications":
                    activeIndex = 1;
                    displayNotification();
                    break;
                // case "groupsRoles":
                // activeIndex = 2;
                // break;
                case "accountInfo":
                    activeIndex = 2;
                    // activeIndex = 3;
                    break;
                default:
                    activeIndex = 0;
                    break;
            }

        } else {
            return "/loginpage.xhtml" + DataverseHeaderFragment.getRedirectPage();
        }

        return "";
    }

    public void edit(ActionEvent e) {
        editMode = EditMode.EDIT;
    }

    public void changePassword(ActionEvent e) {
        editMode = EditMode.CHANGE_PASSWORD;
    }

    public void forgotPassword(ActionEvent e) {
        editMode = EditMode.FORGOT;
    }

    public void validateUserName(FacesContext context, UIComponent toValidate, Object value) {
        String userName = (String) value;
        boolean userNameFound = false;
        BuiltinUser user = builtinUserService.findByUserName(userName);
        if (editMode == EditMode.CREATE) {
            if (user != null) {
                userNameFound = true;
            }
        } else {
            if (user != null && !user.getId().equals(builtinUser.getId())) {
                userNameFound = true;
            }
        }
        if (userNameFound) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, JH.localize("user.username.taken"), null);
            context.addMessage(toValidate.getClientId(context), message);
        }
    }

    public void validateUserEmail(FacesContext context, UIComponent toValidate, Object value) {
        String userEmail = (String) value;
        boolean userEmailFound = false;
        AuthenticatedUser aUser = authenticationService.getAuthenticatedUserByEmail(userEmail);
        if (builtinUser != null) {
            BuiltinUser user = builtinUserService.findByEmail(userEmail);
            if (editMode == EditMode.CREATE) {
                if (user != null || aUser != null) {
                    userEmailFound = true;
                }
            } else {
                //In edit mode...
                if (user != null || aUser != null) {
                    userEmailFound = true;
                }
                //if there's a match on edit make sure that the email belongs to the 
                // user doing the editing by checking ids
                if ((user != null && user.getId().equals(builtinUser.getId())) || (aUser != null && aUser.getId().equals(builtinUser.getId()))) {
                    userEmailFound = false;
                }
            }
        }
        if (pkuIAAAUser != null) {
            PKUIAAAUser user = pkuIAAAUserService.findByEmail(userEmail);
            if (user != null || aUser != null) {
                userEmailFound = true;
            }
            //if there's a match on edit make sure that the email belongs to the 
            // user doing the editing by checking ids
            if ((user != null && user.getId().equals(pkuIAAAUser.getId())) || (aUser != null && aUser.getId().equals(pkuIAAAUser.getId()))) {
                userEmailFound = false;
            }
        }
        if (userEmailFound) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, JH.localize("user.email.taken"), null);
            context.addMessage(toValidate.getClientId(context), message);
        }
    }

    public void validateUserNameEmail(FacesContext context, UIComponent toValidate, Object value) {
        String userName = (String) value;
        boolean userNameFound = false;
        BuiltinUser user = builtinUserService.findByUserName(userName);
        if (user != null) {
            userNameFound = true;
        } else {
            BuiltinUser user2 = builtinUserService.findByEmail(userName);
            if (user2 != null) {
                userNameFound = true;
            }
        }
        if (!userNameFound) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage("Username or Email is incorrect.");
            context.addMessage(toValidate.getClientId(context), message);
        }
    }

    public void validateCurrentPassword(FacesContext context, UIComponent toValidate, Object value) {

        String password = (String) value;

        if (StringUtils.isBlank(password)) {
            logger.log(Level.WARNING, "current password is blank");

            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Password Error", "Password is blank: re-type it again.");
            context.addMessage(toValidate.getClientId(context), message);
            return;

        } else {
            logger.log(Level.INFO, "current paswword is not blank");
        }

        if (!PasswordEncryption.getVersion(builtinUser.getPasswordEncryptionVersion()).check(password, builtinUser.getEncryptedPassword())) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password Error", "Password is incorrect.");
            context.addMessage(toValidate.getClientId(context), message);
        }
    }

    public void validateNewPassword(FacesContext context, UIComponent toValidate, Object value) {
        String password = (String) value;
        if (StringUtils.isBlank(password)) {
            logger.log(Level.WARNING, "new password is blank");

            ((UIInput) toValidate).setValid(false);

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Password Error", "The new password is blank: re-type it again");
            context.addMessage(toValidate.getClientId(context), message);
            return;

        } else {
            logger.log(Level.INFO, "new paswword is not blank");
        }

        int minPasswordLength = 6;
        boolean forceNumber = true;
        boolean forceSpecialChar = false;
        boolean forceCapitalLetter = false;
        int maxPasswordLength = 255;

        PasswordValidator validator = PasswordValidator.buildValidator(forceSpecialChar, forceCapitalLetter, forceNumber, minPasswordLength, maxPasswordLength);
        boolean passwordIsComplexEnough = password != null && validator.validatePassword(password);
        if (!passwordIsComplexEnough) {
            ((UIInput) toValidate).setValid(false);
            String messageDetail = "Password is not complex enough. The password must have at least one letter, one number and be at least " + minPasswordLength + " characters in length.";
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password Error", messageDetail);
            context.addMessage(toValidate.getClientId(context), message);
        }
    }

    public void updatePassword(String userName) {
        String plainTextPassword = PasswordEncryption.generateRandomPassword();
        BuiltinUser user = builtinUserService.findByUserName(userName);
        if (user == null) {
            user = builtinUserService.findByEmail(userName);
        }
        user.updateEncryptedPassword(PasswordEncryption.get().encrypt(plainTextPassword), PasswordEncryption.getLatestVersionNumber());
        builtinUserService.save(user);
    }

    private boolean checkUserTypeMatchGroup(Locale locale) {
        if (joinGroupId != null && builtinUser != null) {
            ExplicitGroup group = explicitGroupService.findById(joinGroupId);
            if (group != null) {
                if (group.getRequestJoinUserType() == AuthenticatedUser.UserType.ADVANCE
                        && builtinUser.getUserType() == AuthenticatedUser.UserType.ORDINARY) {
                    JsfHelper.addWarningMessage(ResourceBundle.getBundle("Bundle", locale)
                            .getString("dataverse.permissions.groups.applyfor.requireAdvanceUser.whenRegister"));
                    return false;
                }
            } else {
                JsfHelper.addWarningMessage("Group doesn't exist.");
                return false;
            }
        }
        return true;
    }

    private boolean userJoinGroup(AuthenticatedUser auUser, Locale locale) {
        if (joinGroupId != null && builtinUser != null) {
            ExplicitGroup group = explicitGroupService.findById(joinGroupId);
            if (group != null) {
                group.getJoinGroupRequesters().add(auUser);
                explicitGroupService.persist(group);
                EventLog eventLog = usageLogIndexService.buildEventLog(EventLog.EventType.REQUEST_JOIN_GROUP,
                        session.getUser(),
                        (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
                eventLog.setGroupId(group.getId());
                usageLogIndexService.index(eventLog);
                for (AuthenticatedUser au : permissionService.getUsersWithPermissionOn(Permission.ManageDataversePermissions, group.getOwner())) {
                    userNotificationService.sendNotification(
                            au, new Timestamp(new Date().getTime()), UserNotification.Type.REQUESTJOINGROUP, group.getId(), locale);
                }
                return true;
            }
        }
        return false;
    }

    public String save() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        if (!checkUserTypeMatchGroup(locale)) {
            return null;
        }

        if (editMode == EditMode.CREATE) {
            inputPassword = StringUtil.generateRandomString(10);
        }
        boolean passwordChanged = false;
        if (editMode == EditMode.CREATE || editMode == EditMode.CHANGE_PASSWORD) {
            if (inputPassword != null) {
                builtinUser.updateEncryptedPassword(PasswordEncryption.get().encrypt(inputPassword), PasswordEncryption.getLatestVersionNumber());
                passwordChanged = true;
            } else {
                // just defensive coding: for in case when the validator is not
                // working
                logger.log(Level.WARNING, "inputPassword is still null");
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, JH.localize("user.noPasswd"), null);
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, message);
                return null;
            }
        }
        if (builtinUser != null) {
            builtinUser = builtinUserService.save(builtinUser);
        }
        if (pkuIAAAUser != null) {
            pkuIAAAUser = pkuIAAAUserService.save(pkuIAAAUser);
        }

        if (editMode == EditMode.CREATE) {
            if (builtinUser != null) {
                String[] paramArray = {builtinUser.getUserName(), inputPassword};
                mailService.sendSystemEmail(builtinUser.getEmail(),
                        ResourceBundle.getBundle("Bundle", locale).getString("notification.email.register.subject"),
                        MessageFormat.format(ResourceBundle.getBundle("Bundle", locale).getString("notification.email.register.content"), paramArray),
                        locale);
            }
            AuthenticatedUser au = authSvc.createAuthenticatedUser(
                    new UserRecordIdentifier(BuiltinAuthenticationProvider.PROVIDER_ID, builtinUser.getUserName()),
                    builtinUser.getUserName(), builtinUser.getDisplayInfo(), false);
            if (au == null) {
                // username exists
                getUsernameField().setValid(false);
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, JH.localize("user.username.taken"), null);
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(getUsernameField().getClientId(context), message);
                return null;
            }
//            session.setUser(au);
            userNotificationService.sendNotification(au,
                    new Timestamp(new Date().getTime()),
                    UserNotification.Type.CREATEACC, null, locale);
            //return "/dataverse.xhtml?alias=" + dataverseService.findRootDataverse().getAlias() + "&faces-redirect=true";
            String[] param = {builtinUser.getEmail()};
            JsfHelper.addInfoMessage(
                    MessageFormat.format(ResourceBundle.getBundle("Bundle", locale).getString("login.newRegisteredUser"), param));
            if(userJoinGroup(au, locale)){
                editMode = null;
                return null;
            }
            return "/loginpage.xhtml?redirectPage=dataverse.xhtml&faces-redirect=true";
        } else {
            if (builtinUser != null) {
                authSvc.updateAuthenticatedUser(currentUser, builtinUser.getDisplayInfo());
            }
            if (pkuIAAAUser != null) {
                authSvc.updateAuthenticatedUser(currentUser, pkuIAAAUser.getDisplayInfo());
            }
            editMode = null;
            String msg = "Your account information has been successfully updated.";
            if (passwordChanged) {
                msg = "Your account password has been successfully changed.";
            }
            JsfHelper.addFlashMessage(msg);
            return null;
        }
    }

    public String cancel() {
        if (editMode == EditMode.CREATE) {
            return "/dataverse.xhtml?alias=" + dataverseService.findRootDataverse().getAlias() + "&faces-redirect=true";
        }

        if (editMode == EditMode.EDIT) {
            if (currentUser.isBuiltInUser()) {
                builtinUser = builtinUserService.findByUserName(currentUser.getUserIdentifier());
            } else if (currentUser.isPKUIAAAUser()) {
                pkuIAAAUser = pkuIAAAUserService.findByUserName(currentUser.getUserIdentifier());
            }
        }

        editMode = null;
        return null;
    }

    public void submit(ActionEvent e) {
        updatePassword(builtinUser.getUserName());
        editMode = null;
    }

    public String remove(Long notificationId) {
        UserNotification userNotification = userNotificationService.find(notificationId);
        userNotificationService.delete(userNotification);
        for (UserNotification uNotification : notificationsList) {
            if (uNotification.getId() == userNotification.getId()) {
                notificationsList.remove(uNotification);
                break;
            }
        }
        return null;
    }

    public String removeAllNotifications() {
        userNotificationService.deleteByUserId(currentUser.getId());
        notificationsList = userNotificationService.findByUser(((AuthenticatedUser) currentUser).getId());
        return null;
    }

    public void onTabChange(TabChangeEvent event) {
        if (event.getTab().getId().equals("notifications")) {
            displayNotification();
        }
    }

    public void displayNotification() {
        for (UserNotification userNotification : notificationsList) {
            switch (userNotification.getType()) {
                case CREATEDV:
                    userNotification.setTheObject(dataverseService.find(userNotification.getObjectId()));
                    break;

                case REQUESTFILEACCESS:
                case GRANTFILEACCESS:
                case REJECTFILEACCESS:
                    userNotification.setTheObject(datasetService.find(userNotification.getObjectId()));
                    break;

                case REQUESTJOINGROUP:
                    userNotification.setTheObject(explicitGroupService.findById(userNotification.getObjectId()));
                    break;
                case GRANTJOINGROUP:
                    userNotification.setTheObject(explicitGroupService.findById(userNotification.getObjectId()));
                    break;
                case REJECTJOINGROUP:
                    userNotification.setTheObject(explicitGroupService.findById(userNotification.getObjectId()));
                    break;

                case MAPLAYERUPDATED:
                case CREATEDS:
                case SUBMITTEDDS:
                case PUBLISHEDDS:
                case RETURNEDDS:
                    userNotification.setTheObject(datasetVersionService.find(userNotification.getObjectId()));
                    break;

                case CREATEACC:
                    userNotification.setTheObject(userNotification.getUser());
            }

            userNotification.setDisplayAsRead(userNotification.isReadNotification());
            if (userNotification.isReadNotification() == false) {
                userNotification.setReadNotification(true);
                userNotificationService.save(userNotification);
            }
        }
    }

    public void userTypeChanged(ValueChangeEvent event) {
        if ((UserType) event.getNewValue() == UserType.ORDINARY) {
            if (this.builtinUser != null) {
                this.builtinUser.setUserType(UserType.ORDINARY);
            }
            if (this.pkuIAAAUser != null) {
                this.pkuIAAAUser.setUserType(UserType.ORDINARY);
            }
        } else {
            if (this.builtinUser != null) {
                this.builtinUser.setUserType(UserType.ADVANCE);
            }
            if (this.pkuIAAAUser != null) {
                this.pkuIAAAUser.setUserType(UserType.ADVANCE);
            }
        }
        FacesContext.getCurrentInstance().renderResponse();
    }
}
