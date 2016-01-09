/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.authorization;

import cn.edu.pku.lib.util.StringUtil;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser.UserType;

/**
 *
 * @author gdurand
 */
public class AuthenticatedUserDisplayInfo extends RoleAssigneeDisplayInfo {
  
    private String lastName;
    private String firstName;
    private String position;
    private UserType userType;
    
    /**
     * @todo Shouldn't we persist the displayName too? It still exists on the
     * authenticateduser table.
     */
    public AuthenticatedUserDisplayInfo(String firstName, String lastName, String emailAddress, String affiliation, String position) {
        super(firstName + " " + lastName,emailAddress,affiliation);
        if(StringUtil.isChinese(lastName)){
            this.setTitle(lastName+firstName);
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.userType = UserType.ORDINARY;
    }
    
    public AuthenticatedUserDisplayInfo(String firstName, String lastName, 
            String emailAddress, String affiliation, String position, UserType userType) {
        super(firstName + " " + lastName,emailAddress,affiliation);
        if(StringUtil.isChinese(lastName)){
            this.setTitle(lastName+firstName);
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.userType = userType;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
}

