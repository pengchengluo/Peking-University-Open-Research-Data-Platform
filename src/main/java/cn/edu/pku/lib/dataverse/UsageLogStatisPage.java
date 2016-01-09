/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse;

import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUser;
import cn.edu.pku.lib.dataverse.authorization.providers.iaaa.PKUIAAAUserServiceBean;
import cn.edu.pku.lib.dataverse.statistic.EventLog;
import cn.edu.pku.lib.dataverse.statistic.EventLog.EventType;
import cn.edu.pku.lib.dataverse.statistic.StatisticConstant;
import cn.edu.pku.lib.dataverse.statistic.UsageLogSearchQuery;
import cn.edu.pku.lib.dataverse.statistic.UsageLogSearchResult;
import cn.edu.pku.lib.dataverse.statistic.UsageLogSearchServiceBean;
import cn.edu.pku.lib.dataverse.util.Pair;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseHeaderFragment;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.DvObjectServiceBean;
import edu.harvard.iq.dataverse.FileMetadata;
import edu.harvard.iq.dataverse.PermissionServiceBean;
import edu.harvard.iq.dataverse.authorization.AuthenticationServiceBean;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroup;
import edu.harvard.iq.dataverse.authorization.groups.impl.explicit.ExplicitGroupServiceBean;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUser;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUserServiceBean;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author luopc
 */
@ViewScoped
@Named
public class UsageLogStatisPage implements Serializable {

    private static final long serialVersionUID = -1718646164721462359L;
    
    private static final Logger logger = Logger.getLogger(UsageLogStatisPage.class.getCanonicalName());

    private Long id;
    private DvObject dvObject;

    @EJB
    DvObjectServiceBean dvObjectService;
    @EJB
    PermissionServiceBean permissionService;
    @EJB
    UsageLogSearchServiceBean usageLogSearchService;
    @EJB
    ExplicitGroupServiceBean explicitGroupService;
    @EJB
    DatasetServiceBean datasetService;
    @EJB
    AuthenticationServiceBean authenticationServiceBean;
    @EJB
    BuiltinUserServiceBean builtinUserService;
    @EJB
    PKUIAAAUserServiceBean pkuIAAAUserService;

    //logs for view dataverse or dataset
    private UsageLogSearchQuery queryForDvObj;
    private CartesianChartModel viewStatisForDvObj;
    private UsageLogSearchResult viewLogForDvObj;

    //query information for file download logs
    private UsageLogSearchQuery queryForFile;
    private CartesianChartModel statisForFile;
    private UsageLogSearchResult logForFile;
    //download information for dataset and dataverse
    private boolean hasDataFile;
    private List<FileMetadata> fileMetadatas;
    private List<Long> allDataFileIds;
    private Map<Long,FileMetadata> fileId2DataFile;
    private long selectedDataFileId;
    //download information for dataverse
    private List<Dataset> datasets;
    private Map<Long,List<FileMetadata>> datasetId2DataFile;
    private Map<Long,Dataset> fileId2Dataset;
    private Map<Long,List<Long>> allDataFileIds4Dataset;
    private long selectedDatasetId;
    
    private UsageLogSearchQuery queryForGroup;
    private CartesianChartModel statisForGroup;
    private UsageLogSearchResult logForGroup;
    private List<ExplicitGroup> groups;
    private List<Long> allGroupIds;
    private Map<Long,ExplicitGroup> groupId2Group;
    private long selectedGroupId;
    private long selectedEventType;
    private Map<EventType,String> eventType2String;
    
    public String getDisplayString(EventType eventType){
        switch(eventType){
            case REQUEST_JOIN_GROUP:
                return ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale())
                        .getString("log.search.filter.userGroup.status.request");
            case REJECT_JOIN_GROUP:
                return ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale())
                        .getString("log.search.filter.userGroup.status.reject");
            case ACCEPT_JOIN_GROUP:
                return ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale())
                        .getString("log.search.filter.userGroup.status.accept");
            default:
                return "";
        }
    }

    public String init() {
        dvObject = dvObjectService.findDvObject(id);
        if (!permissionService.on(dvObject).has(dvObject instanceof Dataverse ? Permission.ManageDataversePermissions : Permission.ManageDatasetPermissions)) {
            return "/loginpage.xhtml" + DataverseHeaderFragment.getRedirectPage();
        }
        
        //init property
        statisForFile = new CartesianChartModel();
        ChartSeries downloadAmount = new ChartSeries();
        downloadAmount.setLabel(ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale()).getString("log.filedownload.distribute.label"));
        downloadAmount.set(new Date(), 0);
        statisForFile.addSeries(downloadAmount);
        
        // for dvObject
        queryForDvObj = new UsageLogSearchQuery();
        queryForDvObj.setDateHistogramInterval(StatisticConstant.AGG_INTERVAL_DAY);
        if (dvObject.isInstanceofDataverse()) {
            queryForDvObj.setEvents(Arrays.asList(EventType.VIEW_DATAVERSE));
            queryForDvObj.setDataverseIds(Arrays.asList(id));
        }
        if (dvObject.isInstanceofDataset()) {
            queryForDvObj.setEvents(Arrays.asList(EventType.VIEW_DATASET));
            queryForDvObj.setDatasetIds(Arrays.asList(id));
        }
        dvObjectViewStatis();

        // for file
        queryForFile = new UsageLogSearchQuery();
        if (dvObject.isInstanceofDataset()) {
            hasDataFile = false;
            fileMetadatas = ((Dataset) dvObject).getLatestVersion().getFileMetadatas();
            allDataFileIds = new ArrayList<>();
            fileId2DataFile = new HashMap<>();
            fileId2Dataset = new HashMap<>();
            for (FileMetadata fileMetadata : fileMetadatas) {
                Long fileId = fileMetadata.getDataFile().getId();
                allDataFileIds.add(fileId);
                fileId2DataFile.put(fileId, fileMetadata);
                fileId2Dataset.put(fileId,(Dataset) dvObject);
                hasDataFile = true;
            }
            if(hasDataFile){
                queryForFile.setDatafileIds(allDataFileIds);
                queryForFile.setDateHistogramInterval(StatisticConstant.AGG_INTERVAL_DAY);
                queryForFile.setEvents(Arrays.asList(EventType.DOWNLOAD_FILE));
                fileDownloadStatis();
            }
        }
        if(dvObject.isInstanceofDataverse()){
            hasDataFile = false;
            datasets = datasetService.findPublishedByOwnerId(dvObject.getId());
            datasetId2DataFile =  new HashMap<>();
            fileId2Dataset = new HashMap<>();
            fileId2DataFile = new HashMap<>();
            allDataFileIds4Dataset = new HashMap<>();
            allDataFileIds = new ArrayList<>();
            List<FileMetadata> allFileMetadatas = new ArrayList<>();
            for(Dataset dataset : datasets){
                List<Long> ids = new ArrayList<>();
                List<FileMetadata> fileMetadatas = dataset.getLatestVersion().getFileMetadatas();
                for(FileMetadata fileMetadata : fileMetadatas){
                    Long fileId = fileMetadata.getDataFile().getId();
                    allFileMetadatas.add(fileMetadata);
                    fileId2Dataset.put(fileId, dataset);
                    ids.add(fileId);
                    allDataFileIds.add(fileId);
                    fileId2DataFile.put(fileId, fileMetadata);
                    hasDataFile = true;
                }
                allDataFileIds4Dataset.put(dataset.getId(), ids);
                datasetId2DataFile.put(dataset.getId(), fileMetadatas);
            }
            fileMetadatas = allFileMetadatas;
            allDataFileIds4Dataset.put(0L, allDataFileIds);
            datasetId2DataFile.put(0L, allFileMetadatas);
            
            if(hasDataFile){
                queryForFile.setDatafileIds(allDataFileIds);
                queryForFile.setDateHistogramInterval(StatisticConstant.AGG_INTERVAL_DAY);
                queryForFile.setEvents(Arrays.asList(EventType.DOWNLOAD_FILE));
                fileDownloadStatis();
            }
        }
        
        //for group
        queryForGroup = new UsageLogSearchQuery();
        if(dvObject.isInstanceofDataverse()){
            allGroupIds = new ArrayList<>();
            groupId2Group = new HashMap<>();
            groups = explicitGroupService.findByOwner(dvObject.getId());
            for(ExplicitGroup group : groups){
                allGroupIds.add(group.getId());
                groupId2Group.put(group.getId(), group);
            }
            if(!groups.isEmpty()){
                queryForGroup.setGroupIds(allGroupIds);
                queryForGroup.setDateHistogramInterval(StatisticConstant.AGG_INTERVAL_DAY);
                requestJoinGroupStatis();
            }
        }
        return "";
    }

    public void dvObjectViewStatis(int page) {
        queryForDvObj.setFrom((page - 1) * queryForDvObj.getSize());
        dvObjectViewStatis();
    }

    public void dvObjectViewStatis() {
        viewLogForDvObj = usageLogSearchService.search(queryForDvObj);
        viewStatisForDvObj = new CartesianChartModel();
        ChartSeries viewAmount = new ChartSeries();
        viewAmount.setLabel(ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale()).getString("log.view.distribute.label"));
        for (Pair<String, Long> pair : viewLogForDvObj.getDateHistogram()) {
            viewAmount.set(pair.getFirst(), pair.getSecond());
        }
        viewStatisForDvObj.addSeries(viewAmount);
    }

    public void fileDownloadStatis(int page){
        queryForFile.setFrom((page - 1)*queryForFile.getSize());
        fileDownloadStatis();
    }
    
    public void fileDownloadStatis() {       
        if(selectedDataFileId == 0){
            queryForFile.setDatafileIds(allDataFileIds);
        }else{
            queryForFile.setDatafileIds(Arrays.asList(selectedDataFileId));
        }
        
        logForFile = usageLogSearchService.search(queryForFile);
        statisForFile = new CartesianChartModel();
        ChartSeries downloadAmount = new ChartSeries();
        downloadAmount.setLabel(ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale()).getString("log.filedownload.distribute.label"));
        for(Pair<String,Long> pair : logForFile.getDateHistogram()){
            downloadAmount.set(pair.getFirst(), pair.getSecond());
        }
        statisForFile.addSeries(downloadAmount);
    }
    
    public void requestJoinGroupStatis(int page){
        queryForGroup.setFrom((page-1)*queryForGroup.getSize());
        requestJoinGroupStatis();
    }
    
    public void requestJoinGroupStatis(){
        if(!dvObject.isInstanceofDataverse())return ;
        
        if(selectedGroupId == 0){
            queryForGroup.setGroupIds(allGroupIds);
        }else{
            queryForGroup.setGroupIds(Arrays.asList(selectedGroupId));
        }
        
        if(selectedEventType == 0){
            queryForGroup.setEvents(Arrays.asList(EventType.REQUEST_JOIN_GROUP,
                    EventType.REJECT_JOIN_GROUP,
                    EventType.ACCEPT_JOIN_GROUP));
        }else if(selectedEventType == 1){
            queryForGroup.setEvents(Arrays.asList(EventType.REQUEST_JOIN_GROUP));
        }else if(selectedEventType == 2){
            queryForGroup.setEvents(Arrays.asList(EventType.REJECT_JOIN_GROUP));
        }else if(selectedEventType == 3){
            queryForGroup.setEvents(Arrays.asList(EventType.ACCEPT_JOIN_GROUP));
        }
        
        logForGroup = usageLogSearchService.search(queryForGroup);
        statisForGroup = new CartesianChartModel();
        ChartSeries requestAmount = new ChartSeries();
        requestAmount.setLabel(ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale()).getString("log.requestjoingroup.distribute.label"));
        for(Pair<String,Long> pair : logForGroup.getDateHistogram()){
            requestAmount.set(pair.getFirst(), pair.getSecond());
        }
        statisForGroup.addSeries(requestAmount);
    }
    
    public void changeSelectedDataset(ValueChangeEvent event){
        this.fileMetadatas = this.datasetId2DataFile.get((Long)event.getNewValue());
        this.allDataFileIds = this.allDataFileIds4Dataset.get((Long)event.getNewValue());
    }
    
    public void downloadFileDownloadLog(String type){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
        File file = null;
        switch (type){
            case "csv": 
                file = generateCSVDownloadLogFile();break;
            case "xlsx":
                file = generateExcelDownloadLogFile();break;
            default:
                file = generateCSVDownloadLogFile();
        }
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
        switch (type){
            case "csv":
                response.setHeader("Content-Disposition", "attachment;filename=\"download_information.csv\"");break;
            case "xlsx":
                response.setHeader("Content-Disposition", "attachment;filename=\"download_information.xlsx\"");break;
            default:
                response.setHeader("Content-Disposition", "attachment;filename=\"download_information.csv\"");
        }
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
    
    public void downloadRequestJoinGroupLog(String type){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
        File file = null;
        switch (type){
            case "csv": 
                file = generateCSVRequestJoinGroupLogFile();break;
            case "xlsx":
                file = generateExcelRequestJoinGroupLogFile();break;
            default:
                file = generateCSVRequestJoinGroupLogFile();
        }
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
        switch (type){
            case "csv":
                response.setHeader("Content-Disposition", "attachment;filename=\"join_user_group_information.csv\"");break;
            case "xlsx":
                response.setHeader("Content-Disposition", "attachment;filename=\"join_user_group_information.xlsx\"");break;
            default:
                response.setHeader("Content-Disposition", "attachment;filename=\"join_user_group_information.csv\"");
        }
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
    
    private File generateExcelDownloadLogFile(){
        //excel workbook
        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("File Download Statistic"));
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        Locale local = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        
        //generate header
        String heads = ResourceBundle.getBundle("Bundle", FacesContext.getCurrentInstance().getViewRoot().getLocale()).getString("log.filedownload.header");
        String[] array = heads.split(",");
        Row row = sheet.createRow(0);
        for(int k=0; k<array.length; k++){
            Cell cell = row.createCell(k);
            cell.setCellValue(array[k]);
        }
        
        //generate logs
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final long size = 100L;
        UsageLogSearchQuery query = queryForFile.clone();
        query.setSize(size);
        query.setDateHistogramInterval(null);
        UsageLogSearchResult searchResult = null;
        int i=0;
        int j=1;
        Cell cell;
        do{
            query.setFrom(i*size);
            searchResult = usageLogSearchService.search(query);
            List<EventLog> logs = searchResult.getEventLogs();
            for(EventLog log : logs){
                row = sheet.createRow(j);
                AuthenticatedUser user;
                if(log.getUserId().equals(":guest") ||
                    (user = authenticationServiceBean.getAuthenticatedUser(log.getUserId()))==null){
                    cell = row.createCell(0);cell.setCellValue(log.getDate());cell.setCellStyle(cellStyle);
                    cell = row.createCell(1);cell.setCellValue(log.getIp());
                    cell = row.createCell(2);cell.setCellValue(log.getContinent());
                    cell = row.createCell(3);cell.setCellValue(log.getCountry());
                    cell = row.createCell(4);cell.setCellValue(log.getSubdivision());
                    cell = row.createCell(5);cell.setCellValue(log.getCity());
                    cell = row.createCell(6);cell.setCellValue(log.getUserId());
                    cell = row.createCell(7);cell.setCellValue(log.getUserName());
                    cell = row.createCell(8);cell.setCellValue(log.getAffiliation());
                    cell = row.createCell(9);cell.setCellValue(log.getPosition());
                    cell = row.createCell(10);cell.setCellValue(fileId2Dataset.get(log.getDatafileId()).getDisplayName(local));
                    cell = row.createCell(11);cell.setCellValue(fileId2DataFile.get(log.getDatafileId()).getLabel());
                }else{
                    if(user.isBuiltInUser()){
                        BuiltinUser b = builtinUserService.findByUserName(user.getUserIdentifier());
                        cell = row.createCell(0);cell.setCellValue(log.getDate());cell.setCellStyle(cellStyle);
                        cell = row.createCell(1);cell.setCellValue(log.getIp());
                        cell = row.createCell(2);cell.setCellValue(log.getContinent());
                        cell = row.createCell(3);cell.setCellValue(log.getCountry());
                        cell = row.createCell(4);cell.setCellValue(log.getSubdivision());
                        cell = row.createCell(5);cell.setCellValue(log.getCity());
                        
                        cell = row.createCell(6);cell.setCellValue(log.getUserId());
                        cell = row.createCell(7);cell.setCellValue(log.getUserName());
                        cell = row.createCell(8);cell.setCellValue(b.getAffiliation());
                        cell = row.createCell(9);cell.setCellValue(b.getPosition());
                        cell = row.createCell(10);cell.setCellValue(fileId2Dataset.get(log.getDatafileId()).getDisplayName(local));
                        cell = row.createCell(11);cell.setCellValue(fileId2DataFile.get(log.getDatafileId()).getLabel());
    
                        cell = row.createCell(12);cell.setCellValue(b.getDepartment());
                        cell = row.createCell(13);cell.setCellValue(b.getEmail());
                        cell = row.createCell(14);cell.setCellValue(b.getSpeciality());
                        cell = row.createCell(15);cell.setCellValue(b.getResearchInterest());
                        cell = row.createCell(16);cell.setCellValue(b.getGender());
                        cell = row.createCell(17);cell.setCellValue(b.getEducation());
                        
                        cell = row.createCell(18);cell.setCellValue(b.getProfessionalTitle());
                        cell = row.createCell(19);cell.setCellValue(b.getSupervisor());
                        cell = row.createCell(20);cell.setCellValue(b.getCertificateType());
                        cell = row.createCell(21);cell.setCellValue(b.getCertificateNumber());
                        cell = row.createCell(22);cell.setCellValue(b.getOfficePhone());
                        cell = row.createCell(23);cell.setCellValue(b.getCellphone());
                        
                        cell = row.createCell(24);cell.setCellValue(b.getOtherEmail());
                        cell = row.createCell(25);cell.setCellValue(b.getCountry());
                        cell = row.createCell(26);cell.setCellValue(b.getProvince());
                        cell = row.createCell(27);cell.setCellValue(b.getCity());
                        cell = row.createCell(28);cell.setCellValue(b.getAddress());
                        cell = row.createCell(29);cell.setCellValue(b.getZipCode());
                        
                        cell = row.createCell(30);cell.setCellValue("Built In");
                    }else if(user.isPKUIAAAUser()){
                        PKUIAAAUser p = pkuIAAAUserService.findByUserName(user.getUserIdentifier());
                        cell = row.createCell(0);cell.setCellValue(log.getDate());cell.setCellStyle(cellStyle);
                        cell = row.createCell(1);cell.setCellValue(log.getIp());
                        cell = row.createCell(2);cell.setCellValue(log.getContinent());
                        cell = row.createCell(3);cell.setCellValue(log.getCountry());
                        cell = row.createCell(4);cell.setCellValue(log.getSubdivision());
                        cell = row.createCell(5);cell.setCellValue(log.getCity());
                        
                        cell = row.createCell(6);cell.setCellValue(log.getUserId());
                        cell = row.createCell(7);cell.setCellValue(log.getUserName());
                        cell = row.createCell(8);cell.setCellValue(p.getAffiliation());
                        cell = row.createCell(9);cell.setCellValue(p.getPosition());
                        cell = row.createCell(10);cell.setCellValue(fileId2Dataset.get(log.getDatafileId()).getDisplayName(local));
                        cell = row.createCell(11);cell.setCellValue(fileId2DataFile.get(log.getDatafileId()).getLabel());
                        
                        cell = row.createCell(12);cell.setCellValue(p.getDepartment());
                        cell = row.createCell(13);cell.setCellValue(p.getEmail());
                        cell = row.createCell(14);cell.setCellValue(p.getSpeciality());
                        cell = row.createCell(15);cell.setCellValue(p.getResearchInterest());
                        cell = row.createCell(16);cell.setCellValue(p.getGender());
                        cell = row.createCell(17);cell.setCellValue(p.getEducation());
                        
                        cell = row.createCell(18);cell.setCellValue(p.getProfessionalTitle());
                        cell = row.createCell(19);cell.setCellValue(p.getSupervisor());
                        cell = row.createCell(20);cell.setCellValue(p.getCertificateType());
                        cell = row.createCell(21);cell.setCellValue(p.getCertificateNumber());
                        cell = row.createCell(22);cell.setCellValue(p.getOfficePhone());
                        cell = row.createCell(23);cell.setCellValue(p.getCellphone());
                        
                        cell = row.createCell(24);cell.setCellValue(p.getOtherEmail());
                        cell = row.createCell(25);cell.setCellValue(p.getCountry());
                        cell = row.createCell(26);cell.setCellValue(p.getProvince());
                        cell = row.createCell(27);cell.setCellValue(p.getCity());
                        cell = row.createCell(28);cell.setCellValue(p.getAddress());
                        cell = row.createCell(29);cell.setCellValue(p.getZipCode());
                        
                        cell = row.createCell(30);cell.setCellValue("PKU IAAA");
                    }
                }
                j++;
            }
            i++;
        }while(i<searchResult.getPages());
        
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
    
    private File generateCSVDownloadLogFile(){
        String filesRootDirectory = System.getProperty("dataverse.files.directory");
        if (filesRootDirectory == null || filesRootDirectory.equals("")) {
            filesRootDirectory = "/tmp/files";
        }
        
        Locale local = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        File file = new File(filesRootDirectory + "/temp/" + UUID.randomUUID());
        
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"utf-8"));
                CSVPrinter csvPrinter = new CSVPrinter(out,CSVFormat.DEFAULT);){
            UsageLogSearchQuery query = queryForFile.clone();
            final long size = 100L;
            query.setSize(size);
            query.setDateHistogramInterval(null);
            UsageLogSearchResult searchResult = null;
            int i=0;
            String heads = ResourceBundle.getBundle("Bundle", local).getString("log.filedownload.header");
            csvPrinter.printRecord(Arrays.asList(heads.split(",")));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            do{
                query.setFrom(i*size);
                searchResult = usageLogSearchService.search(query);
                List<EventLog> logs = searchResult.getEventLogs();
                for(EventLog log : logs){
                    AuthenticatedUser user;
                    if(log.getUserId().equals(":guest") ||
                            (user = authenticationServiceBean.getAuthenticatedUser(log.getUserId()))==null){
                        csvPrinter.printRecord(format.format(log.getDate()),log.getIp(),log.getContinent(),log.getCountry(),log.getSubdivision(),log.getCity(),
                                log.getUserId(),log.getUserName(),log.getAffiliation(),log.getPosition(),
                                fileId2Dataset.get(log.getDatafileId()).getDisplayName(local),
                                fileId2DataFile.get(log.getDatafileId()).getLabel());
                    }else{
                        if(user.isBuiltInUser()){
                            BuiltinUser b = builtinUserService.findByUserName(user.getUserIdentifier());
                            csvPrinter.printRecord(format.format(log.getDate()),log.getIp(),log.getContinent(),log.getCountry(),log.getSubdivision(),log.getCity(),
                                log.getUserId(),log.getUserName(),b.getAffiliation(),b.getPosition(),
                                fileId2Dataset.get(log.getDatafileId()).getDisplayName(local),
                                fileId2DataFile.get(log.getDatafileId()).getLabel(),b.getDepartment(),b.getEmail(),
                                b.getSpeciality(),b.getResearchInterest(),b.getGender(),b.getEducation(),b.getProfessionalTitle(),b.getSupervisor(),
                                b.getCertificateType(),b.getCertificateNumber(),b.getOfficePhone(),b.getCellphone(),b.getOtherEmail(),
                                b.getCountry(),b.getProvince(),b.getCity(),b.getAddress(),b.getZipCode(),"Built In");
                        }else if(user.isPKUIAAAUser()){
                            PKUIAAAUser p = pkuIAAAUserService.findByUserName(user.getUserIdentifier());
                            csvPrinter.printRecord(format.format(log.getDate()),log.getIp(),log.getContinent(),log.getCountry(),log.getSubdivision(),log.getCity(),
                                log.getUserId(),log.getUserName(),p.getAffiliation(),p.getPosition(),
                                fileId2Dataset.get(log.getDatafileId()).getDisplayName(local),
                                fileId2DataFile.get(log.getDatafileId()).getLabel(),p.getDepartment(),p.getEmail(),
                                p.getSpeciality(),p.getResearchInterest(),p.getGender(),p.getEducation(),p.getProfessionalTitle(),p.getSupervisor(),
                                p.getCertificateType(),p.getCertificateNumber(),p.getOfficePhone(),p.getCellphone(),p.getOtherEmail(),
                                p.getCountry(),p.getProvince(),p.getCity(),p.getAddress(),p.getZipCode(),"PKU IAAA");
                        }
                    }
                }
                i++;
            }while(i<searchResult.getPages());
            return file;
        }catch(IOException ioe){
            logger.log(Level.SEVERE, null ,ioe);
        }
        if(file.exists()){
            file.delete();
        }
        return null;
    }
    
    private File generateExcelRequestJoinGroupLogFile(){
        //excel workbook
        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("User Join Group Statistic"));
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        
        //generate header
        String heads = ResourceBundle.getBundle("Bundle", locale).getString("log.requestjoingroup.header");
        String[] array = heads.split(",");
        Row row = sheet.createRow(0);
        for(int k=0; k<array.length; k++){
            Cell cell = row.createCell(k);
            cell.setCellValue(array[k]);
        }
        
        //generate logs
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final long size = 100L;
        UsageLogSearchQuery query = queryForGroup.clone();
        query.setSize(size);
        query.setDateHistogramInterval(null);
        UsageLogSearchResult searchResult = null;
        int i=0;
        int j=1;
        Cell cell;
        do{
            query.setFrom(i*size);
            searchResult = usageLogSearchService.search(query);
            List<EventLog> logs = searchResult.getEventLogs();
            for(EventLog log : logs){
                row = sheet.createRow(j);
                AuthenticatedUser user;
                if(log.getUserId().equals(":guest") ||
                    (user = authenticationServiceBean.getAuthenticatedUser(log.getUserId()))==null){
                    cell = row.createCell(0);cell.setCellValue(log.getDate());cell.setCellStyle(cellStyle);
                    cell = row.createCell(1);cell.setCellValue(log.getIp());
                    cell = row.createCell(2);cell.setCellValue(log.getContinent());
                    cell = row.createCell(3);cell.setCellValue(log.getCountry());
                    cell = row.createCell(4);cell.setCellValue(log.getSubdivision());
                    cell = row.createCell(5);cell.setCellValue(log.getCity());
                    cell = row.createCell(6);cell.setCellValue(log.getUserId());
                    cell = row.createCell(7);cell.setCellValue(log.getUserName());
                    cell = row.createCell(8);cell.setCellValue(log.getAffiliation());
                    cell = row.createCell(9);cell.setCellValue(log.getPosition());
                    cell = row.createCell(10);cell.setCellValue(getDisplayString(log.getEventType()));
                    cell = row.createCell(11);cell.setCellValue(groupId2Group.get(log.getGroupId()).getDisplayName());
                }else{
                    if(user.isBuiltInUser()){
                        BuiltinUser b = builtinUserService.findByUserName(user.getUserIdentifier());
                        cell = row.createCell(0);cell.setCellValue(log.getDate());cell.setCellStyle(cellStyle);
                        cell = row.createCell(1);cell.setCellValue(log.getIp());
                        cell = row.createCell(2);cell.setCellValue(log.getContinent());
                        cell = row.createCell(3);cell.setCellValue(log.getCountry());
                        cell = row.createCell(4);cell.setCellValue(log.getSubdivision());
                        cell = row.createCell(5);cell.setCellValue(log.getCity());
                        
                        cell = row.createCell(6);cell.setCellValue(log.getUserId());
                        cell = row.createCell(7);cell.setCellValue(log.getUserName());
                        cell = row.createCell(8);cell.setCellValue(b.getAffiliation());
                        cell = row.createCell(9);cell.setCellValue(b.getPosition());
                        cell = row.createCell(10);cell.setCellValue(getDisplayString(log.getEventType()));
                        cell = row.createCell(11);cell.setCellValue(groupId2Group.get(log.getGroupId()).getDisplayName());
    
                        cell = row.createCell(12);cell.setCellValue(b.getDepartment());
                        cell = row.createCell(13);cell.setCellValue(b.getEmail());
                        cell = row.createCell(14);cell.setCellValue(b.getSpeciality());
                        cell = row.createCell(15);cell.setCellValue(b.getResearchInterest());
                        cell = row.createCell(16);cell.setCellValue(b.getGender());
                        cell = row.createCell(17);cell.setCellValue(b.getEducation());
                        
                        cell = row.createCell(18);cell.setCellValue(b.getProfessionalTitle());
                        cell = row.createCell(19);cell.setCellValue(b.getSupervisor());
                        cell = row.createCell(20);cell.setCellValue(b.getCertificateType());
                        cell = row.createCell(21);cell.setCellValue(b.getCertificateNumber());
                        cell = row.createCell(22);cell.setCellValue(b.getOfficePhone());
                        cell = row.createCell(23);cell.setCellValue(b.getCellphone());
                        
                        cell = row.createCell(24);cell.setCellValue(b.getOtherEmail());
                        cell = row.createCell(25);cell.setCellValue(b.getCountry());
                        cell = row.createCell(26);cell.setCellValue(b.getProvince());
                        cell = row.createCell(27);cell.setCellValue(b.getCity());
                        cell = row.createCell(28);cell.setCellValue(b.getAddress());
                        cell = row.createCell(29);cell.setCellValue(b.getZipCode());
                        
                        cell = row.createCell(30);cell.setCellValue("Built In");
                    }else if(user.isPKUIAAAUser()){
                        PKUIAAAUser p = pkuIAAAUserService.findByUserName(user.getUserIdentifier());
                        cell = row.createCell(0);cell.setCellValue(log.getDate());cell.setCellStyle(cellStyle);
                        cell = row.createCell(1);cell.setCellValue(log.getIp());
                        cell = row.createCell(2);cell.setCellValue(log.getContinent());
                        cell = row.createCell(3);cell.setCellValue(log.getCountry());
                        cell = row.createCell(4);cell.setCellValue(log.getSubdivision());
                        cell = row.createCell(5);cell.setCellValue(log.getCity());
                        
                        cell = row.createCell(6);cell.setCellValue(log.getUserId());
                        cell = row.createCell(7);cell.setCellValue(log.getUserName());
                        cell = row.createCell(8);cell.setCellValue(p.getAffiliation());
                        cell = row.createCell(9);cell.setCellValue(p.getPosition());
                        cell = row.createCell(10);cell.setCellValue(getDisplayString(log.getEventType()));
                        cell = row.createCell(11);cell.setCellValue(groupId2Group.get(log.getGroupId()).getDisplayName());
                        
                        cell = row.createCell(12);cell.setCellValue(p.getDepartment());
                        cell = row.createCell(13);cell.setCellValue(p.getEmail());
                        cell = row.createCell(14);cell.setCellValue(p.getSpeciality());
                        cell = row.createCell(15);cell.setCellValue(p.getResearchInterest());
                        cell = row.createCell(16);cell.setCellValue(p.getGender());
                        cell = row.createCell(17);cell.setCellValue(p.getEducation());
                        
                        cell = row.createCell(18);cell.setCellValue(p.getProfessionalTitle());
                        cell = row.createCell(19);cell.setCellValue(p.getSupervisor());
                        cell = row.createCell(20);cell.setCellValue(p.getCertificateType());
                        cell = row.createCell(21);cell.setCellValue(p.getCertificateNumber());
                        cell = row.createCell(22);cell.setCellValue(p.getOfficePhone());
                        cell = row.createCell(23);cell.setCellValue(p.getCellphone());
                        
                        cell = row.createCell(24);cell.setCellValue(p.getOtherEmail());
                        cell = row.createCell(25);cell.setCellValue(p.getCountry());
                        cell = row.createCell(26);cell.setCellValue(p.getProvince());
                        cell = row.createCell(27);cell.setCellValue(p.getCity());
                        cell = row.createCell(28);cell.setCellValue(p.getAddress());
                        cell = row.createCell(29);cell.setCellValue(p.getZipCode());
                        
                        cell = row.createCell(30);cell.setCellValue("PKU IAAA");
                    }
                }
                j++;
            }
            i++;
        }while(i<searchResult.getPages());
        
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

    private File generateCSVRequestJoinGroupLogFile(){
        String filesRootDirectory = System.getProperty("dataverse.files.directory");
        if (filesRootDirectory == null || filesRootDirectory.equals("")) {
            filesRootDirectory = "/tmp/files";
        }
        
        Locale local = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        File file = new File(filesRootDirectory + "/temp/" + UUID.randomUUID());
        
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"utf-8"));
                CSVPrinter csvPrinter = new CSVPrinter(out,CSVFormat.DEFAULT);){
            UsageLogSearchQuery query = queryForGroup.clone();
            final long size = 100L;
            query.setSize(size);
            query.setDateHistogramInterval(null);
            UsageLogSearchResult searchResult = null;
            int i=0;
            String heads = ResourceBundle.getBundle("Bundle", local).getString("log.requestjoingroup.header");
            csvPrinter.printRecord(Arrays.asList(heads.split(",")));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            do{
                query.setFrom(i*size);
                searchResult = usageLogSearchService.search(query);
                List<EventLog> logs = searchResult.getEventLogs();
                for(EventLog log : logs){
                    AuthenticatedUser user;
                    if(log.getUserId().equals(":guest") ||
                            (user = authenticationServiceBean.getAuthenticatedUser(log.getUserId()))==null){
                        csvPrinter.printRecord(format.format(log.getDate()),log.getIp(),log.getContinent(),log.getCountry(),log.getSubdivision(),log.getCity(),
                                log.getUserId(),log.getUserName(),log.getAffiliation(),log.getPosition(),
                                getDisplayString(log.getEventType()),groupId2Group.get(log.getGroupId()).getDisplayName());
                    }else{
                        if(user.isBuiltInUser()){
                            BuiltinUser b = builtinUserService.findByUserName(user.getUserIdentifier());
                            csvPrinter.printRecord(format.format(log.getDate()),log.getIp(),log.getContinent(),log.getCountry(),log.getSubdivision(),log.getCity(),
                                log.getUserId(),log.getUserName(),b.getAffiliation(),b.getPosition(),
                                groupId2Group.get(log.getGroupId()).getDisplayName(),b.getDepartment(),b.getEmail(),
                                b.getSpeciality(),b.getResearchInterest(),b.getGender(),b.getEducation(),b.getProfessionalTitle(),b.getSupervisor(),
                                b.getCertificateType(),b.getCertificateNumber(),b.getOfficePhone(),b.getCellphone(),b.getOtherEmail(),
                                b.getCountry(),b.getProvince(),b.getCity(),b.getAddress(),b.getZipCode(),"Built In");
                        }else if(user.isPKUIAAAUser()){
                            PKUIAAAUser p = pkuIAAAUserService.findByUserName(user.getUserIdentifier());
                            csvPrinter.printRecord(format.format(log.getDate()),log.getIp(),log.getContinent(),log.getCountry(),log.getSubdivision(),log.getCity(),
                                log.getUserId(),log.getUserName(),p.getAffiliation(),p.getPosition(),
                                groupId2Group.get(log.getGroupId()).getDisplayName(),p.getDepartment(),p.getEmail(),
                                p.getSpeciality(),p.getResearchInterest(),p.getGender(),p.getEducation(),p.getProfessionalTitle(),p.getSupervisor(),
                                p.getCertificateType(),p.getCertificateNumber(),p.getOfficePhone(),p.getCellphone(),p.getOtherEmail(),
                                p.getCountry(),p.getProvince(),p.getCity(),p.getAddress(),p.getZipCode(),"PKU IAAA");
                        }
                    }
                }
                i++;
            }while(i<searchResult.getPages());
            return file;
        }catch(IOException ioe){
            logger.log(Level.SEVERE, null ,ioe);
        }
        if(file.exists()){
            file.delete();
        }
        return null;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DvObject getDvObject() {
        return dvObject;
    }

    public void setDvObject(DvObject dvObject) {
        this.dvObject = dvObject;
    }

    public UsageLogSearchQuery getQueryForDvObj() {
        return queryForDvObj;
    }

    public void setQueryForDvObj(UsageLogSearchQuery queryForDvObj) {
        this.queryForDvObj = queryForDvObj;
    }

    public CartesianChartModel getViewStatisForDvObj() {
        return viewStatisForDvObj;
    }

    public void setViewStatisForDvObj(CartesianChartModel viewStatisForDvObj) {
        this.viewStatisForDvObj = viewStatisForDvObj;
    }

    public UsageLogSearchResult getViewLogForDvObj() {
        return viewLogForDvObj;
    }

    public void setViewLogForDvObj(UsageLogSearchResult viewLogForDvObj) {
        this.viewLogForDvObj = viewLogForDvObj;
    }

    public UsageLogSearchQuery getQueryForFile() {
        return queryForFile;
    }

    public void setQueryForFile(UsageLogSearchQuery queryForFile) {
        this.queryForFile = queryForFile;
    }

    public CartesianChartModel getStatisForFile() {
        return statisForFile;
    }

    public void setStatisForFile(CartesianChartModel statisForFile) {
        this.statisForFile = statisForFile;
    }

    public UsageLogSearchResult getLogForFile() {
        return logForFile;
    }

    public void setLogForFile(UsageLogSearchResult logForFile) {
        this.logForFile = logForFile;
    }

    public List<FileMetadata> getFileMetadatas() {
        return fileMetadatas;
    }

    public void setFileMetadatas(List<FileMetadata> fileMetadatas) {
        this.fileMetadatas = fileMetadatas;
    }

    public UsageLogSearchQuery getQueryForGroup() {
        return queryForGroup;
    }

    public void setQueryForGroup(UsageLogSearchQuery queryForGroup) {
        this.queryForGroup = queryForGroup;
    }

    public CartesianChartModel getStatisForGroup() {
        return statisForGroup;
    }

    public void setStatisForGroup(CartesianChartModel statisForGroup) {
        this.statisForGroup = statisForGroup;
    }

    public UsageLogSearchResult getLogForGroup() {
        return logForGroup;
    }

    public void setLogForGroup(UsageLogSearchResult logForGroup) {
        this.logForGroup = logForGroup;
    }

    public List<ExplicitGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ExplicitGroup> groups) {
        this.groups = groups;
    }

    public Map<Long, FileMetadata> getFileId2DataFile() {
        return fileId2DataFile;
    }

    public void setFileId2DataFile(Map<Long, FileMetadata> fileId2DataFile) {
        this.fileId2DataFile = fileId2DataFile;
    }

    public long getSelectedDataFileId() {
        return selectedDataFileId;
    }

    public void setSelectedDataFileId(long selectedDataFileId) {
        this.selectedDataFileId = selectedDataFileId;
    }

    public long getSelectedDatasetId() {
        return selectedDatasetId;
    }

    public void setSelectedDatasetId(long selectedDatasetId) {
        this.selectedDatasetId = selectedDatasetId;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }

    public Map<Long, Dataset> getFileId2Dataset() {
        return fileId2Dataset;
    }

    public void setFileId2Dataset(Map<Long, Dataset> fileId2Dataset) {
        this.fileId2Dataset = fileId2Dataset;
    }

    public boolean isHasDataFile() {
        return hasDataFile;
    }

    public void setHasDataFile(boolean hasDataFile) {
        this.hasDataFile = hasDataFile;
    }

    public long getSelectedGroupId() {
        return selectedGroupId;
    }

    public void setSelectedGroupId(long selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    public List<Long> getAllGroupIds() {
        return allGroupIds;
    }

    public void setAllGroupIds(List<Long> allGroupIds) {
        this.allGroupIds = allGroupIds;
    }

    public Map<Long, ExplicitGroup> getGroupId2Group() {
        return groupId2Group;
    }

    public void setGroupId2Group(Map<Long, ExplicitGroup> groupId2Group) {
        this.groupId2Group = groupId2Group;
    }

    public long getSelectedEventType() {
        return selectedEventType;
    }

    public void setSelectedEventType(long selectedEventType) {
        this.selectedEventType = selectedEventType;
    }
}
