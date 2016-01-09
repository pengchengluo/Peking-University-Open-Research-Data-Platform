/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.LoginPage;
import edu.harvard.iq.dataverse.authorization.AuthenticationRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author luopc
 */
@WebServlet(name = "PKUIAAAAuthenticationServlet", urlPatterns = {"/dvn/ssologin"})
public class PKUIAAAAuthenticationServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2954506545442517655L;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        String rand = request.getParameter("rand");
        response.sendRedirect("/iaaaloginpage.xhtml?rand="+rand+"&token="+token);
//        request.getSession(true);
//        DataverseSession session = (DataverseSession)request.getSession().getAttribute("dataverseSession");
//        AuthenticationRequest authReq = new AuthenticationRequest();
//        LoginPage loginPage = new LoginPage();
//        loginPage.init();
//        loginPage.setCredentialsAuthProviderId(PKUIAAAAuthenticationProvider.PROVIDER_ID);
//        loginPage.resetFilledCredentials(null);
//        List<LoginPage.FilledCredential> filledCredList = loginPage.getFilledCredentials();
        
//        for(LoginPage.FilledCredential fc : filledCredList){
//            if(fc.getCredential().getTitle().equals(token)){
//                fc.setValue(token);
//            }else if(fc.getCredential().getTitle().equals(rand)){
//                fc.setValue(rand);
//            }
//        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
