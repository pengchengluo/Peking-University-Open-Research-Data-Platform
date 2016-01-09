package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.authorization.AuthenticationProviderDisplayInfo;
import edu.harvard.iq.dataverse.authorization.AuthenticationRequest;
import edu.harvard.iq.dataverse.authorization.AuthenticationResponse;
import edu.harvard.iq.dataverse.authorization.CredentialsAuthenticationProvider;
import edu.harvard.iq.dataverse.authorization.UserLister;
import edu.harvard.iq.dataverse.authorization.groups.GroupProvider;
import edu.harvard.iq.dataverse.authorization.users.User;
import java.util.Arrays;
import java.util.List;
import static edu.harvard.iq.dataverse.authorization.CredentialsAuthenticationProvider.Credential;
import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.groups.Group;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author luopc
 * @version 1.0
 */
public class PKUIAAAAuthenticationProvider implements CredentialsAuthenticationProvider, UserLister, GroupProvider {
    
    private static final Logger logger = Logger.getLogger(PKUIAAAAuthenticationProvider.class.getCanonicalName());
    
    public static final String PROVIDER_ID = "pkuiaaa";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_RAND = "rand";
    private static final List<Credential> CREDENTIALS_LIST = 
            Arrays.asList(new Credential(KEY_TOKEN),
                    new Credential(KEY_RAND));
      
    final PKUIAAAUserServiceBean bean;

    public PKUIAAAAuthenticationProvider( PKUIAAAUserServiceBean bean ) {
        this.bean = bean;
    }

    @Override
    public List<User> listUsers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public AuthenticationProviderDisplayInfo getInfo() {
        return new AuthenticationProviderDisplayInfo(getId(), "PKU IAAA Login Provider", "PKU University internal user repository");
    }

    @Override
    public AuthenticationResponse authenticate( AuthenticationRequest authReq ) {
        String remoteAddr = authReq.getIpAddress().toString();
        String token = authReq.getCredential(KEY_TOKEN);
        PKUIAAAUser user = new PKUIAAAUser();
        try{
            PKUIAAAResult iaaaResult = PKUIAAAValidation.validateToken(remoteAddr, token);
            user.setUserName(iaaaResult.getLogonID());
            if(iaaaResult.getUserType().equals(PKUIAAAResult.USER_TYPE_STUDENT)){
                PKUTpubResult tpubResult = PKUTpub.getSinglePerson(user.getUserName());
                user.setSpeciality(tpubResult.getSpeciality());
            }
            String name = iaaaResult.getName();
            user.setLastName(name.substring(0,1));
            user.setFirstName(name.substring(1));
            user.setEmail(user.getUserName()+"@pku.edu.cn");
            user.setAffiliation("北京大学");
            user.setDepartment(iaaaResult.getDept());
            if(iaaaResult.getUserType().equals("职工"))
                user.setPosition("faculty");
            else if(iaaaResult.getUserType().equals("学生"))
                user.setPosition("student");
            else 
                user.setPosition("other");
//            user.setPosition(iaaaResult.getUserType());
            user.setUserType(AuthenticatedUser.UserType.ORDINARY);
            if(bean.findByUserName(user.getUserName()) == null){
                bean.save(user);
            }
        }catch(PKUIAAAException ex){
            return AuthenticationResponse.makeFail("PKU IAAA authentication error");
        }
//        user.setUserName("1406189042");
//        user.setLastName("罗");
//        user.setFirstName("鹏程");
//        user.setEmail("luopc@lib.pku.edu.cn");
//        user.setAffiliation("北京大学");
//        user.setDepartment("图书馆");
//        user.setSpeciality("情报学");
//        user.setResearchInterest("信息组织");
//        user.setPosition("教职工");
        if(bean.findByUserName(user.getUserName()) == null){
            bean.save(user);
        }
        return AuthenticationResponse.makeSuccess(user.getUserName(), user.getDisplayInfo());
   }

    @Override
    public List<Credential> getRequiredCredentials() {
        return CREDENTIALS_LIST;
    }

    @Override
    public String getGroupProviderAlias() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getGroupProviderInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Group get(String groupAlias) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set groupsFor(RoleAssignee ra, DvObject dvo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set findGlobalGroups() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
