package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.LoginPage.FilledCredential;
import edu.harvard.iq.dataverse.UserServiceBean;
import edu.harvard.iq.dataverse.authorization.AuthenticationProvider;
import edu.harvard.iq.dataverse.authorization.AuthenticationProviderDisplayInfo;
import edu.harvard.iq.dataverse.authorization.AuthenticationRequest;
import edu.harvard.iq.dataverse.authorization.AuthenticationServiceBean;
import edu.harvard.iq.dataverse.authorization.CredentialsAuthenticationProvider;
import edu.harvard.iq.dataverse.authorization.exceptions.AuthenticationFailedException;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUserServiceBean;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import static edu.harvard.iq.dataverse.util.JsfHelper.JH;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author luopc
 * @author Michael Bar-Sinai
 */
@ViewScoped
@Named("PKUIAAALoginPage")
public class PKUIAAALoginPage implements java.io.Serializable {
    private static final Logger logger = Logger.getLogger(PKUIAAALoginPage.class.getName());
    
    private String token;
    private String rand;
  
    @Inject DataverseSession session;    
    
    @EJB
    AuthenticationServiceBean authSvc;
    
    private final String credentialsAuthProviderId = "pkuiaaa";
    
    private String redirectPage = "dataverse.xhtml";

    public String login() {
        AuthenticationRequest authReq = new AuthenticationRequest();
        authReq.putCredential("token", token);
        authReq.putCredential("rand", rand);
        authReq.setIpAddress(session.getUser().getRequestMetadata().getIpAddress());
        try {
            AuthenticatedUser r = authSvc.authenticate(credentialsAuthProviderId, authReq);
            logger.log(Level.INFO, "User authenticated: {0}", r.getEmail());
            session.setUser(r);
            try {            
                redirectPage = URLDecoder.decode(redirectPage, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                logger.log(Level.SEVERE, null, ex);
                redirectPage = "dataverse.xhtml";
            }
            logger.log(Level.INFO, "Sending user to = " + redirectPage);
            return redirectPage + (redirectPage.indexOf("?") == -1 ? "?" : "&") + "faces-redirect=true";
        } catch (AuthenticationFailedException ex) {
            JH.addMessage(FacesMessage.SEVERITY_ERROR, "The username and/or password you entered is invalid. Contact support@dataverse.org if you need assistance accessing your account.", ex.getResponse().getMessage());
            return null;
        }
        
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRand() {
        return rand;
    }

    public void setRand(String rand) {
        this.rand = rand;
    }    
}
