/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.util.MarkupChecker;
import edu.harvard.iq.dataverse.util.StringUtil;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author skraffmiller
 */
@ViewScoped
public class DatasetVersionUI implements Serializable {

    @EJB
    DataverseServiceBean dataverseService;
    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;   
    
    public DatasetVersionUI() {
    }

    private Map<MetadataBlock, List<DatasetField>> metadataBlocksForView = new HashMap();
    private Map<MetadataBlock, List<DatasetField>> metadataBlocksForEdit = new HashMap();

    public Map<MetadataBlock, List<DatasetField>> getMetadataBlocksForView() {
        return metadataBlocksForView;
    }

    public void setMetadataBlocksForView(Map<MetadataBlock, List<DatasetField>> metadataBlocksForView) {
        this.metadataBlocksForView = metadataBlocksForView;
    }

    public Map<MetadataBlock, List<DatasetField>> getMetadataBlocksForEdit() {
        return metadataBlocksForEdit;
    }

    public void setMetadataBlocksForEdit(Map<MetadataBlock, List<DatasetField>> metadataBlocksForEdit) {
        this.metadataBlocksForEdit = metadataBlocksForEdit;
    }
    
    public DatasetVersionUI  initDatasetVersionUI(DatasetVersion datasetVersion, boolean createBlanks) {
        /*takes in the values of a dataset version 
         and apportions them into lists for 
         viewing and editng in the dataset page.
         */
        
        setDatasetVersion(datasetVersion);
        //this.setDatasetAuthors(new ArrayList());
        this.setDatasetRelPublications(new ArrayList());
        this.setDatasetRelPublicationsZh(new ArrayList());

        // loop through vaues to get fields for view mode
        for (DatasetField dsf : datasetVersion.getDatasetFields()) {
            //Special Handling for various fields displayed above tabs in dataset page view.
            if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.title)) {
                setTitle(dsf);
            }if(dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.titleZh)){
                setTitleZh(dsf);
            }else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.description)) {
                setDescription(dsf);
                String descriptionString = "";
                if(dsf.getDatasetFieldCompoundValues() != null && dsf.getDatasetFieldCompoundValues().get(0) != null){
                    DatasetFieldCompoundValue descriptionValue = dsf.getDatasetFieldCompoundValues().get(0);               
                    for (DatasetField subField : descriptionValue.getChildDatasetFields()) {
                        if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.descriptionText) && !subField.isEmptyForDisplay()) {                          
                                descriptionString = subField.getValue();                             
                        }
                    }
                }                 
                setDescriptionDisplay(MarkupChecker.sanitizeBasicHTML(descriptionString) );
            }else if(dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.descriptionZh)){
                setDescriptionZh(dsf);
                String descriptionStringZh = "";
                if(dsf.getDatasetFieldCompoundValues() != null && dsf.getDatasetFieldCompoundValues().get(0) != null){
                    DatasetFieldCompoundValue descriptionValue = dsf.getDatasetFieldCompoundValues().get(0);
                    for(DatasetField subField : descriptionValue.getChildDatasetFields()){
                        if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.descriptionTextZh) && !subField.isEmptyForDisplay()) {                          
                                descriptionStringZh = subField.getValue();
                        }
                    }
                }
                setDescriptionDisplayZh(MarkupChecker.sanitizeBasicHTML(descriptionStringZh));
            }
            else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.keyword)) {
                setKeyword(dsf);
                String keywordString = "";
                for (DatasetFieldCompoundValue keywordValue : dsf.getDatasetFieldCompoundValues()) {
                    for (DatasetField subField : keywordValue.getChildDatasetFields()) {
                        if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.keywordValue) && !subField.isEmptyForDisplay()) {
                            if (keywordString.isEmpty()){
                                keywordString = subField.getValue();
                            } else {
                                keywordString += ", " +  subField.getValue();
                            }                               
                        }
                    }
                } 
                setKeywordDisplay(keywordString);
            }else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.keywordZh)) {
                setKeywordZh(dsf);
                String keywordStringZh = "";
                for (DatasetFieldCompoundValue keywordValue : dsf.getDatasetFieldCompoundValues()) {
                    for (DatasetField subField : keywordValue.getChildDatasetFields()) {
                        if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.keywordValueZh) && !subField.isEmptyForDisplay()) {
                            if (keywordStringZh.isEmpty()){
                                keywordStringZh = subField.getValue();
                            } else {
                                keywordStringZh += ", " +  subField.getValue();
                            }                               
                        }
                    }
                } 
                setKeywordDisplayZh(keywordStringZh);
            }
            else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.subject) && !dsf.isEmptyForDisplay()) {
                setSubject(dsf);
            }else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.subjectZh) && !dsf.isEmptyForDisplay()) {
                setSubjectZh(dsf);
            }else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.notesText) && !dsf.isEmptyForDisplay()) {
                this.setNotes(dsf);
            }else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.notesTextZh) && !dsf.isEmptyForDisplay()) {
                this.setNotesZh(dsf);
            }else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.publication)) {
                //Special handling for Related Publications
                // Treated as below the tabs for editing, but must get first value for display above tabs    
                if (this.datasetRelPublications.isEmpty()) {
                    for (DatasetFieldCompoundValue relPubVal : dsf.getDatasetFieldCompoundValues()) {
                        DatasetRelPublication datasetRelPublication = new DatasetRelPublication();
                        datasetRelPublication.setTitle(dsf.getDatasetFieldType().getTitle());
                        datasetRelPublication.setDescription(dsf.getDatasetFieldType().getDescription());
                        for (DatasetField subField : relPubVal.getChildDatasetFields()) {
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationCitation)) {
                                datasetRelPublication.setText(subField.getValue());
                            }
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationIDNumber)) {
                                datasetRelPublication.setIdNumber(subField.getValue());
                            }
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationIDType)) {
                                datasetRelPublication.setIdType(subField.getValue());
                            }
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationURL)) {
                                datasetRelPublication.setUrl(subField.getValue());
                            }
                        }
                        this.getDatasetRelPublications().add(datasetRelPublication);
                    }
                }
            }else if (dsf.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationZh)) {
                //Special handling for Related Publications
                // Treated as below the tabs for editing, but must get first value for display above tabs    
                if (this.datasetRelPublicationsZh.isEmpty()) {
                    for (DatasetFieldCompoundValue relPubVal : dsf.getDatasetFieldCompoundValues()) {
                        DatasetRelPublication datasetRelPublication = new DatasetRelPublication();
                        datasetRelPublication.setTitle(dsf.getDatasetFieldType().getTitle());
                        datasetRelPublication.setDescription(dsf.getDatasetFieldType().getDescription());
                        for (DatasetField subField : relPubVal.getChildDatasetFields()) {
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationCitationZh)) {
                                datasetRelPublication.setText(subField.getValue());
                            }
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationIDNumberZh)) {
                                datasetRelPublication.setIdNumber(subField.getValue());
                            }
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationIDTypeZh)) {
                                datasetRelPublication.setIdType(subField.getValue());
                            }
                            if (subField.getDatasetFieldType().getName().equals(DatasetFieldConstant.publicationURLZh)) {
                                datasetRelPublication.setUrl(subField.getValue());
                            }
                        }
                        this.getDatasetRelPublicationsZh().add(datasetRelPublication);
                    }
                }
            }
        }
        
        datasetVersion.setDatasetFields(initDatasetFields(createBlanks));
        
        setMetadataValueBlocks(datasetVersion);
        
        return this;
    }

    private Dataset getDataset() {
        return this.datasetVersion.getDataset();
    }

    private DatasetVersion datasetVersion;

    public DatasetVersion getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(DatasetVersion datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    private DatasetField title;
    private DatasetField description;
    private DatasetField keyword;
    private DatasetField subject;
    private DatasetField notes; 
    private String keywordDisplay;
    
    private DatasetField titleZh;
    private DatasetField descriptionZh;
    private DatasetField keywordZh;
    private DatasetField subjectZh;
    private DatasetField notesZh; 
    private String keywordDisplayZh;

    public String getKeywordDisplay() {
        return keywordDisplay;
    }

    public void setKeywordDisplay(String keywordDisplay) {
        this.keywordDisplay = keywordDisplay;
    }
    
    private String descriptionDisplay;
    private String descriptionDisplayZh;

    public String getDescriptionDisplay() {
        return descriptionDisplay;
    }

    public void setDescriptionDisplay(String descriptionDisplay) {
        this.descriptionDisplay = descriptionDisplay;
    }

    public String getDescriptionDisplayZh() {
        return descriptionDisplayZh;
    }

    public void setDescriptionDisplayZh(String descriptionDisplayZh) {
        this.descriptionDisplayZh = descriptionDisplayZh;
    }
            
  
    private List<DatasetRelPublication> datasetRelPublications;
    private List<DatasetRelPublication> datasetRelPublicationsZh;

    public DatasetField getTitle() {
        return title;
    }

    public void setTitle(DatasetField title) {
        this.title = title;
    }
    
    public DatasetField getDescription() {
        return description;
    }

    public void setDescription(DatasetField description) {
        this.description = description;
    }    

    public DatasetField getKeyword() {
        return keyword;
    }

    public void setKeyword(DatasetField keyword) {
        this.keyword = keyword;
    }

    public DatasetField getSubject() {
        return subject;
    }

    public void setSubject(DatasetField subject) {
        this.subject = subject;
    }

    public DatasetField getNotes() {
        return notes;
    }

    public void setNotes(DatasetField notes) {
        this.notes = notes;
    }

    public DatasetField getTitleZh() {
        return titleZh;
    }

    public void setTitleZh(DatasetField titleZh) {
        this.titleZh = titleZh;
    }

    public DatasetField getDescriptionZh() {
        return descriptionZh;
    }

    public void setDescriptionZh(DatasetField descriptionZh) {
        this.descriptionZh = descriptionZh;
    }

    public DatasetField getKeywordZh() {
        return keywordZh;
    }

    public void setKeywordZh(DatasetField keywordZh) {
        this.keywordZh = keywordZh;
    }

    public DatasetField getSubjectZh() {
        return subjectZh;
    }

    public void setSubjectZh(DatasetField subjectZh) {
        this.subjectZh = subjectZh;
    }

    public DatasetField getNotesZh() {
        return notesZh;
    }

    public void setNotesZh(DatasetField notesZh) {
        this.notesZh = notesZh;
    }

    public String getKeywordDisplayZh() {
        return keywordDisplayZh;
    }

    public void setKeywordDisplayZh(String keywordDisplayZh) {
        this.keywordDisplayZh = keywordDisplayZh;
    }


    public List<DatasetRelPublication> getDatasetRelPublications() {
        return datasetRelPublications;
    }

    public void setDatasetRelPublications(List<DatasetRelPublication> datasetRelPublications) {
        this.datasetRelPublications = datasetRelPublications;
    }

    public List<DatasetRelPublication> getDatasetRelPublicationsZh() {
        return datasetRelPublicationsZh;
    }

    public void setDatasetRelPublicationsZh(List<DatasetRelPublication> datasetRelPublicationsZh) {
        this.datasetRelPublicationsZh = datasetRelPublicationsZh;
    }



    public String getRelPublicationCitation() {
        if (!this.datasetRelPublications.isEmpty()) {
            return this.getDatasetRelPublications().get(0).getText();
        } else {
            return "";
        }
    }
    
    public String getRelPublicationCitationZh() {
        if (!this.datasetRelPublicationsZh.isEmpty()) {
            return this.getDatasetRelPublicationsZh().get(0).getText();
        } else {
            return "";
        }
    }

    public String getRelPublicationId() {
        if (!this.datasetRelPublications.isEmpty()) {
            if (!(this.getDatasetRelPublications().get(0).getIdNumber() == null)  && !this.getDatasetRelPublications().get(0).getIdNumber().isEmpty()){
                            return this.getDatasetRelPublications().get(0).getIdType() + ": " + this.getDatasetRelPublications().get(0).getIdNumber();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
    
    public String getRelPublicationIdZh() {
        if (!this.datasetRelPublicationsZh.isEmpty()) {
            if (!(this.getDatasetRelPublicationsZh().get(0).getIdNumber() == null)  && !this.getDatasetRelPublicationsZh().get(0).getIdNumber().isEmpty()){
                            return this.getDatasetRelPublicationsZh().get(0).getIdType() + ": " + this.getDatasetRelPublicationsZh().get(0).getIdNumber();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
    
    public String getRelPublicationUrl() {
        if (!this.datasetRelPublications.isEmpty()) {
            return this.getDatasetRelPublications().get(0).getUrl();
        } else {
            return "";
        }
    }
    
    public String getRelPublicationUrlZh() {
        if (!this.datasetRelPublicationsZh.isEmpty()) {
            return this.getDatasetRelPublicationsZh().get(0).getUrl();
        } else {
            return "";
        }
    }

    public String getUNF() {
        //todo get UNF to calculate and display here.
        return "";
    }

    //TODO - make sure getCitation works
    private String getYearForCitation(String dateString) {
        //get date to first dash only
        if (dateString.indexOf("-") > -1) {
            return dateString.substring(0, dateString.indexOf("-"));
        }
        return dateString;
    }

    public String getReleaseDate() {
        if (datasetVersion.getReleaseTime() != null) {
            Date relDate = datasetVersion.getReleaseTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(relDate);
            return Integer.toString(calendar.get(Calendar.YEAR));
        }
        return "";
    }
    
    public String getCreateDate() {
        if (datasetVersion.getCreateTime() != null) {
            Date relDate = datasetVersion.getCreateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(relDate);
            return Integer.toString(calendar.get(Calendar.YEAR));
        }
        return "";
    }

    public String getProductionDate() {
        for (DatasetField dsfv : datasetVersion.getDatasetFields()) {
            if (dsfv.getDatasetFieldType().getName().equals(DatasetFieldConstant.productionDate)) {
                return dsfv.getValue();
            }
        }
        return "";
    }

    public String getDistributionDate() {
        for (DatasetField dsfv : datasetVersion.getDatasetFields()) {
            if (dsfv.getDatasetFieldType().getName().equals(DatasetFieldConstant.distributionDate)) {
                return dsfv.getValue();
            }
        }
        return "";
    }
    
 // TODO: clean up init methods and get them to work, cascading all the way down.
    // right now, only work for one level of compound objects
    private DatasetField initDatasetField(DatasetField dsf, boolean createBlanks) {
        if (dsf.getDatasetFieldType().isCompound()) {
            for (DatasetFieldCompoundValue cv : dsf.getDatasetFieldCompoundValues()) {
                // for each compound value; check the datasetfieldTypes associated with its type
                for (DatasetFieldType dsfType : dsf.getDatasetFieldType().getChildDatasetFieldTypes()) {
                    boolean add = createBlanks;
                    for (DatasetField subfield : cv.getChildDatasetFields()) {
                        if (dsfType.equals(subfield.getDatasetFieldType())) {
                            add = false;
                            break;
                        }
                    }

                    if (add) {
                        cv.getChildDatasetFields().add(DatasetField.createNewEmptyChildDatasetField(dsfType, cv));
                    }
                }
                
                sortDatasetFields(cv.getChildDatasetFields());
            }
        }

        return dsf;
    }

    private List<DatasetField> initDatasetFields(boolean createBlanks) {
        //retList - Return List of values
        List<DatasetField> retList = new ArrayList();
        for (DatasetField dsf : this.datasetVersion.getDatasetFields()) {
            retList.add(initDatasetField(dsf, createBlanks));
        }
     

        //Test to see that there are values for 
        // all fields in this dataset via metadata blocks
        //only add if not added above
        for (MetadataBlock mdb : this.getDataset().getOwner().getMetadataBlocks()) {
            for (DatasetFieldType dsfType : mdb.getDatasetFieldTypes()) {
                if (!dsfType.isSubField()) {
                    boolean add = createBlanks;
                    //don't add if already added as a val
                    for (DatasetField dsf : retList) {
                        if (dsfType.equals(dsf.getDatasetFieldType())) {
                            add = false;
                            break;
                        }
                    }

                    if (add) {
                        retList.add(DatasetField.createNewEmptyDatasetField(dsfType, this.datasetVersion));
                    }
                }
            }
        }

        //sort via display order on dataset field
        Collections.sort(retList, new Comparator<DatasetField>() {
            public int compare(DatasetField d1, DatasetField d2) {
                int a = d1.getDatasetFieldType().getDisplayOrder();
                int b = d2.getDatasetFieldType().getDisplayOrder();
                return Integer.valueOf(a).compareTo(Integer.valueOf(b));
            }
        });

        return sortDatasetFields(retList);
    }  
    
    private List<DatasetField> sortDatasetFields (List<DatasetField> dsfList) {
        Collections.sort(dsfList, new Comparator<DatasetField>() {
            public int compare(DatasetField d1, DatasetField d2) {
                int a = d1.getDatasetFieldType().getDisplayOrder();
                int b = d2.getDatasetFieldType().getDisplayOrder();
                return Integer.valueOf(a).compareTo(Integer.valueOf(b));
            }
        });
        return dsfList;
    }    

    public void setMetadataValueBlocks(DatasetVersion datasetVersion) {
        //TODO: A lot of clean up on the logic of this method
        metadataBlocksForView.clear();
        metadataBlocksForEdit.clear();
        Long dvIdForInputLevel = datasetVersion.getDataset().getOwner().getId();
        
        if (!dataverseService.find(dvIdForInputLevel).isMetadataBlockRoot()){
            dvIdForInputLevel = dataverseService.find(dvIdForInputLevel).getMetadataRootId();
        }
        
        List<DatasetField> filledInFields = this.datasetVersion.getDatasetFields(); 
        
        List <MetadataBlock> actualMDB = new ArrayList();
            
        actualMDB.addAll(this.datasetVersion.getDataset().getOwner().getMetadataBlocks());
        
        for (DatasetField dsfv : filledInFields) {
            if (!dsfv.isEmptyForDisplay()) {
                MetadataBlock mdbTest = dsfv.getDatasetFieldType().getMetadataBlock();
                if (!actualMDB.contains(mdbTest)) {
                    actualMDB.add(mdbTest);
                }
            }
        }       
        
        for (MetadataBlock mdb : actualMDB) {
            mdb.setEmpty(true);
            mdb.setHasRequired(false);
            List<DatasetField> datasetFieldsForView = new ArrayList();
            List<DatasetField> datasetFieldsForEdit = new ArrayList();
            for (DatasetField dsf : datasetVersion.getDatasetFields()) {
                if (dsf.getDatasetFieldType().getMetadataBlock().equals(mdb)) {
                    datasetFieldsForEdit.add(dsf);
                    if (dsf.isRequired()) {
                        mdb.setHasRequired(true);
                    }
                    if (!dsf.isEmptyForDisplay()) {
                        mdb.setEmpty(false);
                        datasetFieldsForView.add(dsf);
                    }
                }
            }

            if (!datasetFieldsForView.isEmpty()) {
                metadataBlocksForView.put(mdb, datasetFieldsForView);
            }
            if (!datasetFieldsForEdit.isEmpty()) {
                metadataBlocksForEdit.put(mdb, datasetFieldsForEdit);
            }
        }
    }

}
