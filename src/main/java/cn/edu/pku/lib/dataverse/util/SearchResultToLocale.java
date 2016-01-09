/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.util;

import edu.harvard.iq.dataverse.FacetCategory;
import edu.harvard.iq.dataverse.FacetLabel;
import edu.harvard.iq.dataverse.SolrQueryResponse;
import edu.harvard.iq.dataverse.search.SearchFields;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author luopc
 */
@Stateless
public class SearchResultToLocale {
    
    @EJB
    private BundleDataverse bundleDataverse;
    
    public void toLocale(SolrQueryResponse solrQueryResponse, Locale locale){
        if(locale.getLanguage().equals("zh")){
            List<FacetCategory> facetCategories = solrQueryResponse.getFacetCategoryList();
            for(FacetCategory facetCategory : facetCategories){
                if(facetCategory.getName().equals(SearchFields.PUBLICATION_STATUS)){
                    facetCategory.setFriendlyName(bundleDataverse.getValue("dataverse.results.facet.publication.status","zh"));
                    for(FacetLabel facetLabel : facetCategory.getFacetLabel()){
                        if(facetLabel.getName().equals("Draft")){
                            facetLabel.setName(bundleDataverse.
                                    getValue("dataverse.results.facet.publication.status.draft", "zh"));
                        }else if(facetLabel.getName().equals("Published")){
                            facetLabel.setName(bundleDataverse.
                                    getValue("dataverse.results.facet.publication.status.published", "zh"));
                        }else if(facetLabel.getName().equals("Unpublished")){
                            facetLabel.setName(bundleDataverse.
                                    getValue("dataverse.results.facet.publication.status.unpublished", "zh"));
                        }
                    }
                }else if(facetCategory.getName().equals(SearchFields.DATAVERSE_CATEGORY)){
                    facetCategory.setFriendlyName(bundleDataverse.getValue("dataverse.category","zh"));
                    for(FacetLabel facetLabel : facetCategory.getFacetLabel()){
                        if(facetLabel.getName().equals("Researcher")){
                            facetLabel.setName(bundleDataverse.getValue("dataverse.type.selectTab.researchers","zh"));
                        }else if(facetLabel.getName().equals("Research Project")){
                            facetLabel.setName(bundleDataverse.getValue("dataverse.type.selectTab.researchProjects","zh"));
                        }else if(facetLabel.getName().equals("Journal")){
                            facetLabel.setName(bundleDataverse.getValue("dataverse.type.selectTab.journals","zh"));
                        }else if(facetLabel.getName().equals("Organization or Institution")){
                            facetLabel.setName(bundleDataverse.getValue("dataverse.type.selectTab.organizationsAndInsitutions","zh"));
                        }else if(facetLabel.getName().equals("Teaching Course")){
                            facetLabel.setName(bundleDataverse.getValue("dataverse.type.selectTab.teachingCourses","zh"));
                        }else if(facetLabel.getName().equals("Uncategorized")){
                            facetLabel.setName(bundleDataverse.getValue("dataverse.type.selectTab.uncategorized","zh"));
                        }
                    }
                }else if(facetCategory.getName().equals(SearchFields.AFFILIATION) || 
                        facetCategory.getName().equals(SearchFields.AFFILIATION_ZH)){
                    facetCategory.setFriendlyName(bundleDataverse.getValue("affiliation","zh"));
                }else if(facetCategory.getName().equals(SearchFields.PUBLICATION_DATE)){
                    facetCategory.setFriendlyName(bundleDataverse.getValue("dataset.metadata.publicationDate","zh"));
                }
            }
        }
    }
    
}
