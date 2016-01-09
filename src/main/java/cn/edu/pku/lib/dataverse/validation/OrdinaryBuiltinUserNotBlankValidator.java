/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.validation;

import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author luopc
 */
@FacesValidator("cn.edu.pku.lib.dataverse.validation.OrdinaryBuiltinUserNotBlankValidator")
public class OrdinaryBuiltinUserNotBlankValidator implements Validator{

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String inputValue = (String)value;
        if(inputValue == null || inputValue.trim().length()==0){
            FacesMessage message = new FacesMessage(
                    ResourceBundle.getBundle("ValidationMessages",context.getViewRoot().getLocale())
                            .getString("org.hibernate.validator.constraints.NotBlank.message"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
    
}
