/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author luopc
 * @version 1.0
 */
public class PKUIAAAResult {
    public static final String USER_TYPE_STUDENT = "学生";

    private String logonID;
    private String name;
    private String dept;
    private String userType;

    public String getLogonID() {
        return logonID;
    }

    public void setLogonID(String logonID) {
        this.logonID = logonID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
