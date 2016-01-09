/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.authorization.groups.impl.iaaa;

import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.RoleAssigneeDisplayInfo;
import edu.harvard.iq.dataverse.authorization.groups.Group;
import edu.harvard.iq.dataverse.authorization.groups.GroupProvider;
import edu.harvard.iq.dataverse.authorization.groups.impl.builtin.AllUsers;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;

/**
 *
 * @author luopc
 */
public class IAAAUsers implements Group{
    
    public static final IAAAUsers instance = new IAAAUsers();
       
    private final String identifier = ":IAAAUsers";
    
    public static final IAAAUsers get() { return instance; }
    
    /**
     * Prevent instance creation
     */
    private IAAAUsers() {}

    @Override
    public String getAlias() {
        return getGroupProvider().getGroupProviderAlias() + Group.PATH_SEPARATOR
                + "iaaa-users";
    }

    @Override
    public String getDisplayName() {
        return "IAAA Users";
    }

    @Override
    public String getDescription() {
        return "Users come from Peking Univisity IAAA unified authentication";
    }

    @Override
    public boolean contains(RoleAssignee ra) {
        return (ra instanceof AuthenticatedUser) &&
                ((AuthenticatedUser)ra).isPKUIAAAUser();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public GroupProvider getGroupProvider() {
        return IAAAGroupsProvider.get();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public RoleAssigneeDisplayInfo getDisplayInfo() {
        return new RoleAssigneeDisplayInfo("IAAA Users", null);
    }
    
    @Override
    public String toString() {
        return "[IAAAUsers " + getIdentifier() + "]";
    }
}
