/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse;

import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUser;
import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUserServiceBean;
import cn.edu.pku.lib.dataverse.statistic.EventLog;
import cn.edu.pku.lib.dataverse.statistic.UsageLogIndexServiceBean;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseHeaderFragment;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.DvObjectServiceBean;
import edu.harvard.iq.dataverse.EjbDataverseEngine;
import edu.harvard.iq.dataverse.PermissionServiceBean;
import edu.harvard.iq.dataverse.UserNotification;
import edu.harvard.iq.dataverse.UserNotificationServiceBean;
import static edu.harvard.iq.dataverse.actionlogging.ActionLogRecord.ActionType.BuiltinUser;
import edu.harvard.iq.dataverse.authorization.AuthenticationServiceBean;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.groups.Group;
import edu.harvard.iq.dataverse.authorization.groups.GroupException;
import edu.harvard.iq.dataverse.authorization.groups.GroupServiceBean;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroup;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroupServiceBean;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUser;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUserServiceBean;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser.UserType;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.impl.CreateExplicitGroupCommand;
import edu.harvard.iq.dataverse.engine.command.impl.UpdateExplicitGroupCommand;
import edu.harvard.iq.dataverse.util.JsfHelper;
import static edu.harvard.iq.dataverse.util.JsfHelper.JH;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author luopc
 */
@ViewScoped
@Named
public class ManageUserGroupPage implements Serializable {

    private static final Logger logger = Logger.getLogger(ManageUserGroupPage.class.getCanonicalName());

    @EJB
    DvObjectServiceBean dvObjectService;
    @EJB
    PermissionServiceBean permissionService;
    @EJB
    GroupServiceBean groupService;
    @EJB
    ExplicitGroupServiceBean explicitGroupService;
    @EJB
    AuthenticationServiceBean authenticationServiceBean;
    @EJB
    EjbDataverseEngine commandEngine;
    @EJB
    GroupServiceBean groupServiceBean;
    @EJB
    UserNotificationServiceBean userNotificationService;
    @Inject
    DataverseSession session;
    @EJB
    BuiltinUserServiceBean builtinUserService;
    @EJB
    PKUIAAAUserServiceBean pkuIAAAUserService;
    @EJB
    UsageLogIndexServiceBean usageLogIndexService;

    private ExplicitGroup explicitGroup;
    private Long groupId;

    private DvObject dvObject = new Dataverse();

    private String searchIdentifier = "";
    private List<AuthenticatedUser> searchUsers = Collections.EMPTY_LIST;
    private BuiltinUser builtinUser;
    private PKUIAAAUser pkuIAAAUser;
    
    private String rejectReason;
    private String otherRejectReason;

    public String init() {
        if (groupId != null) {
            explicitGroup = explicitGroupService.findById(groupId);
            if (explicitGroup != null) {
                dvObject = (Dataverse) explicitGroup.getOwner();
                if (permissionService.on(dvObject).has(Permission.ManageDataversePermissions)) {
                    rejectReason = "other";
                    return "";
                } else {
                    return "/loginpage.xhtml" + DataverseHeaderFragment.getRedirectPage();
                }
            }
        }
        return "/404.xhtml";
    }

    public DvObject getDvObject() {
        return dvObject;
    }

    public void setDvObject(DvObject dvObject) {
        this.dvObject = dvObject;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<AuthenticatedUser> getAuthenticatedUsers() {
        Set<AuthenticatedUser> authUsersSet = explicitGroup.getContainedAuthenticatedUsers();
        List<AuthenticatedUser> authUsers = new ArrayList<AuthenticatedUser>();
        for (AuthenticatedUser user : authUsersSet) {
            authUsers.add(user);
        }
        return authUsers;
    }
    
    public void downloadAuthenticatedUsers(){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
        File file = generateExcelRequestJoinGroupLogFile();
        if(file == null){
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            return ;
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=\"user_member.xlsx\"");
        response.setContentLength((int)file.length());
        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                ){
            byte[] buffer = new byte[1024*4];
            int length;
            while((length = in.read(buffer))>0){
                out.write(buffer,0,length);
            }
        }catch(IOException ioe){
            logger.log(Level.SEVERE, null, ioe);
        }
        context.responseComplete();
        if(file.exists())file.delete();
    }
    
    private File generateExcelRequestJoinGroupLogFile(){
        //excel workbook
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Groups' user member"));
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        
        //generate header
        String heads = ResourceBundle.getBundle("Bundle", locale).getString("dataverse.permissions.groups.member.header");
        String[] array = heads.split(",");
        Row row = sheet.createRow(0);
        for(int k=0; k<array.length; k++){
            Cell cell = row.createCell(k);
            cell.setCellValue(array[k]);
        }
        
        //generate logs
        Set<AuthenticatedUser> authUsersSet = explicitGroup.getContainedAuthenticatedUsers();
        int j=1;
        Cell cell;
        for(AuthenticatedUser user : authUsersSet){
            row = sheet.createRow(j);
            if(user.isBuiltInUser()){
                BuiltinUser b = builtinUserService.findByUserName(user.getUserIdentifier());  
                cell = row.createCell(0);cell.setCellValue(b.getUserName());
                cell = row.createCell(1);cell.setCellValue(b.getLastName());
                cell = row.createCell(2);cell.setCellValue(b.getFirstName());
                cell = row.createCell(3);
                if(b.getUserType()==UserType.ORDINARY)cell.setCellValue("ORDINARY");
                else if(b.getUserType()==UserType.ADVANCE)cell.setCellValue("ADVANCE");
                else cell.setCellValue("");
                cell = row.createCell(4);cell.setCellValue(b.getAffiliation());
                cell = row.createCell(5);cell.setCellValue(b.getPosition());               
                cell = row.createCell(6);cell.setCellValue(b.getDepartment());
                cell = row.createCell(7);cell.setCellValue(b.getEmail());
                cell = row.createCell(8);cell.setCellValue(b.getSpeciality());
                cell = row.createCell(9);cell.setCellValue(b.getResearchInterest());
                cell = row.createCell(10);cell.setCellValue(b.getGender());
                cell = row.createCell(11);cell.setCellValue(b.getEducation());
                cell = row.createCell(12);cell.setCellValue(b.getProfessionalTitle());
                cell = row.createCell(13);cell.setCellValue(b.getSupervisor());
                cell = row.createCell(14);cell.setCellValue(b.getCertificateType());
                cell = row.createCell(15);cell.setCellValue(b.getCertificateNumber());
                cell = row.createCell(16);cell.setCellValue(b.getOfficePhone());
                cell = row.createCell(17);cell.setCellValue(b.getCellphone());
                cell = row.createCell(18);cell.setCellValue(b.getOtherEmail());
                cell = row.createCell(19);cell.setCellValue(b.getCountry());
                cell = row.createCell(20);cell.setCellValue(b.getProvince());
                cell = row.createCell(21);cell.setCellValue(b.getCity());
                cell = row.createCell(22);cell.setCellValue(b.getAddress());
                cell = row.createCell(23);cell.setCellValue(b.getZipCode());
                cell = row.createCell(24);cell.setCellValue("Built In");
            }else if(user.isPKUIAAAUser()){
                PKUIAAAUser p = pkuIAAAUserService.findByUserName(user.getUserIdentifier());
                cell = row.createCell(0);cell.setCellValue(p.getUserName());
                cell = row.createCell(1);cell.setCellValue(p.getLastName());
                cell = row.createCell(2);cell.setCellValue(p.getFirstName());
                cell = row.createCell(3);
                if(p.getUserType()==UserType.ORDINARY)cell.setCellValue("ORDINARY");
                else if(p.getUserType()==UserType.ADVANCE)cell.setCellValue("ADVANCE");
                else cell.setCellValue("");
                cell = row.createCell(4);cell.setCellValue(p.getAffiliation());
                cell = row.createCell(5);cell.setCellValue(p.getPosition());               
                cell = row.createCell(6);cell.setCellValue(p.getDepartment());
                cell = row.createCell(7);cell.setCellValue(p.getEmail());
                cell = row.createCell(8);cell.setCellValue(p.getSpeciality());
                cell = row.createCell(9);cell.setCellValue(p.getResearchInterest());
                cell = row.createCell(10);cell.setCellValue(p.getGender());
                cell = row.createCell(11);cell.setCellValue(p.getEducation());
                cell = row.createCell(12);cell.setCellValue(p.getProfessionalTitle());
                cell = row.createCell(13);cell.setCellValue(p.getSupervisor());
                cell = row.createCell(14);cell.setCellValue(p.getCertificateType());
                cell = row.createCell(15);cell.setCellValue(p.getCertificateNumber());
                cell = row.createCell(16);cell.setCellValue(p.getOfficePhone());
                cell = row.createCell(17);cell.setCellValue(p.getCellphone());
                cell = row.createCell(18);cell.setCellValue(p.getOtherEmail());
                cell = row.createCell(19);cell.setCellValue(p.getCountry());
                cell = row.createCell(20);cell.setCellValue(p.getProvince());
                cell = row.createCell(21);cell.setCellValue(p.getCity());
                cell = row.createCell(22);cell.setCellValue(p.getAddress());
                cell = row.createCell(23);cell.setCellValue(p.getZipCode());
                cell = row.createCell(24);cell.setCellValue("PKU IAAA");
            }
            j++;
        }
        
        String filesRootDirectory = System.getProperty("dataverse.files.directory");
        if (filesRootDirectory == null || filesRootDirectory.equals("")) {
            filesRootDirectory = "/tmp/files";
        }
        File file = new File(filesRootDirectory + "/temp/" + UUID.randomUUID());
        try(FileOutputStream out = new FileOutputStream(file)){
            wb.write(out);
            return file;
        }catch(IOException ioe){
            logger.log(Level.SEVERE, null ,ioe);
        }
        if(file.exists()){
            file.delete();
        }
        return null;
    }

    public List<Group> getGroups() {
        List<Group> groups = new ArrayList<>();
        Set<String> groupIDs = explicitGroup.getContatinedGlobalRoleAssignee();
        for (String groupID : groupIDs) {
            Group group = groupServiceBean.getNoneExplicitGroupByIdentifier(groupID);
            if (group != null) {
                groups.add(group);
            }
        }
        for (ExplicitGroup expGroup : explicitGroup.getContainedExplicitGroups()) {
            expGroup.setProvider(explicitGroupService.getProvider());
            groups.add(expGroup);
        }
        return groups;
    }

    public List<Group> getAvaliableGroups() {
        List<Group> groups = new ArrayList<>();
        groups.addAll(groupService.findGlobalGroups());
        for (ExplicitGroup group : explicitGroupService.findAvailableFor(dvObject)) {
            if (group.getId() != this.groupId) {
                groups.add(group);
            }
        }
        return groups;
    }

    public ExplicitGroup getExplicitGroup() {
        return explicitGroup;
    }

    public void setExplicitGroup(ExplicitGroup explicitGroup) {
        this.explicitGroup = explicitGroup;
    }

    public String getSearchIdentifier() {
        return searchIdentifier;
    }

    public void setSearchIdentifier(String searchIdentifier) {
        this.searchIdentifier = searchIdentifier;
    }

    public List<AuthenticatedUser> getSearchUsers() {
        return searchUsers;
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

    public void setSearchUsers(List<AuthenticatedUser> searchUsers) {
        this.searchUsers = searchUsers;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getOtherRejectReason() {
        return otherRejectReason;
    }

    public void setOtherRejectReason(String otherRejectReason) {
        this.otherRejectReason = otherRejectReason;
    }
    
    public void search() {
        AuthenticatedUser user = authenticationServiceBean.getAuthenticatedUser(this.searchIdentifier);
        if (user == null) {
            searchUsers = Collections.EMPTY_LIST;
        } else {
            searchUsers = new ArrayList<AuthenticatedUser>();
            searchUsers.add(user);
        }
    }

    public boolean addMember(RoleAssignee roleAssignee) {
        boolean isAdded = false;
        if (!explicitGroup.contains(roleAssignee)) {
            try {
                explicitGroup.add(roleAssignee);
                logger.info("Attempting to add user " + roleAssignee.getIdentifier() + " for group" + explicitGroup.getGroupAliasInOwner()); // TODO MBS remove
                explicitGroup = commandEngine.submit(new UpdateExplicitGroupCommand(session.getUser(), explicitGroup));
                explicitGroup.setProvider(explicitGroupService.getProvider());
                JsfHelper.addSuccessMessage("Succesfully add user " + roleAssignee.getIdentifier());
                isAdded = true;
            } catch (GroupException ge) {
                JH.addMessage(FacesMessage.SEVERITY_FATAL, ge.getMessage());
                logger.log(Level.SEVERE, ge.getMessage());
            } catch (CommandException ex) {
                logger.log(Level.WARNING, "User add failed", ex);
                JsfHelper.JH.addMessage(FacesMessage.SEVERITY_ERROR,
                        "User add failed.",
                        ex.getMessage());
            } catch (Exception ex) {
                JH.addMessage(FacesMessage.SEVERITY_FATAL, "The group was not able to be updated.");
                logger.log(Level.SEVERE, "Error add user: " + ex.getMessage(), ex);
            }
        } else {
            JsfHelper.addInfoMessage("Group " + explicitGroup.getDisplayName() + " has already contained user " + roleAssignee.getIdentifier());
            isAdded = true;
        }
        return isAdded;
    }

    public void grantJoinGroupToRequests(AuthenticatedUser user) {
        if (addMember(user)) {
            try {
                explicitGroup.getJoinGroupRequesters().remove(user);
                explicitGroup = commandEngine.submit(new UpdateExplicitGroupCommand(session.getUser(), explicitGroup));
                explicitGroup.setProvider(explicitGroupService.getProvider());
                JsfHelper.addSuccessMessage("Joining group requestd by " + user.getName() + " was granted.");
                Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                userNotificationService.sendNotification(user, new Timestamp(new Date().getTime()), UserNotification.Type.GRANTJOINGROUP, explicitGroup.getId(),locale);
                EventLog eventLog = usageLogIndexService.buildEventLog(EventLog.EventType.ACCEPT_JOIN_GROUP,
                    session.getUser(),
                    (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
                eventLog.setGroupId(explicitGroup.getId());
                usageLogIndexService.index(eventLog);
            } catch (CommandException ex) {
                logger.log(Level.WARNING, "Request user remove failed", ex);
                JsfHelper.JH.addMessage(FacesMessage.SEVERITY_ERROR,
                        "Request user remove failed.",
                        ex.getMessage());
            } catch (Exception ex) {
                JH.addMessage(FacesMessage.SEVERITY_FATAL, "The group was not able to be updated.");
                logger.log(Level.SEVERE, "Error request user remove: " + ex.getMessage(), ex);
            }
        }
    }

    public void rejectJoinGroupToRequests(AuthenticatedUser user) {
        try {
            explicitGroup.getJoinGroupRequesters().remove(user);
            explicitGroup = commandEngine.submit(new UpdateExplicitGroupCommand(session.getUser(), explicitGroup));
            explicitGroup.setProvider(explicitGroupService.getProvider());
            JsfHelper.addSuccessMessage("Joining group requested by " + user.getName() + " was rejected.");
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            if(rejectReason.equals("other")){
                rejectReason = otherRejectReason;
            }
            userNotificationService.sendNotification(user, new Timestamp(new Date().getTime()), UserNotification.Type.REJECTJOINGROUP,
                    explicitGroup.getId(),rejectReason,locale);
            EventLog eventLog = usageLogIndexService.buildEventLog(EventLog.EventType.REJECT_JOIN_GROUP,
                    session.getUser(),
                    (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
            eventLog.setGroupId(explicitGroup.getId());
            usageLogIndexService.index(eventLog);
        } catch (CommandException ex) {
            logger.log(Level.WARNING, "Request user remove failed", ex);
            JsfHelper.JH.addMessage(FacesMessage.SEVERITY_ERROR,
                    "Request user remove failed.",
                    ex.getMessage());
        } catch (Exception ex) {
            JH.addMessage(FacesMessage.SEVERITY_FATAL, "The group was not able to be updated.");
            logger.log(Level.SEVERE, "Error request user remove: " + ex.getMessage(), ex);
        }finally{
            rejectReason = "other";
        }
    }

//    public void addGroup(Group group){
//        explicitGroup.add(group);
//    }
    public void deleteMember(RoleAssignee roleAssignee) {
        if (explicitGroup.contains(roleAssignee)) {
            explicitGroup.remove(roleAssignee);
            try {
                logger.info("Attempting to delete user " + roleAssignee.getIdentifier() + " for group" + explicitGroup.getGroupAliasInOwner()); // TODO MBS remove
                explicitGroup = commandEngine.submit(new UpdateExplicitGroupCommand(session.getUser(), explicitGroup));
                explicitGroup.setProvider(explicitGroupService.getProvider());
                JsfHelper.addSuccessMessage("Succesfully delete user " + roleAssignee.getIdentifier());
            } catch (CommandException ex) {
                logger.log(Level.WARNING, "User delete failed", ex);
                JsfHelper.JH.addMessage(FacesMessage.SEVERITY_ERROR,
                        "User delete failed.",
                        ex.getMessage());
            } catch (Exception ex) {
                JH.addMessage(FacesMessage.SEVERITY_FATAL, "The group was not able to be updated.");
                logger.log(Level.SEVERE, "Error delete user: " + ex.getMessage(), ex);
            }
        } else {
            JsfHelper.addInfoMessage("Group " + explicitGroup.getDisplayName() + " doesn't contain user " + roleAssignee.getIdentifier());
        }
    }
    
    public void viewUserDetail(AuthenticatedUser user){
        builtinUser = null;
        pkuIAAAUser = null;
        if(user.isBuiltInUser()){
            builtinUser = builtinUserService.findByUserName(user.getUserIdentifier());
        }else if(user.isPKUIAAAUser()){
            pkuIAAAUser = pkuIAAAUserService.findByUserName(user.getUserIdentifier());
        }
    }
    
    public void save(){
        explicitGroupService.persist(explicitGroup);
        JsfHelper.addSuccessMessage("Succesfully update group information.");
    }
}
