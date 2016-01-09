/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.validation;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.apache.commons.validator.routines.EmailValidator;

/**
 *
 * @author luopc
 */
@FacesValidator("cn.edu.pku.lib.dataverse.validation.EMailValidator")
public class EMailValidator implements Validator{

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null) {
            if(!EmailValidator.getInstance().isValid(((String)value).trim())){
                FacesMessage message = new FacesMessage(
                        ResourceBundle.getBundle("ValidationMessages",context.getViewRoot().getLocale())
                                .getString("cn.edu.pku.lib.dataverse.validation.EMailValidator.message"));
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(message);
            }
        }
    }
    
}
