/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.validation;

import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser.UserType;
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
@FacesValidator("cn.edu.pku.lib.dataverse.validation.AdvanceBuiltinUserSupervisorValidation")
public class AdvanceBuiltinUserSupervisorValidation implements Validator{

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        UIInput positionUI = (UIInput)component.findComponent("position");
        UIInput userTypeUI = (UIInput)component.findComponent("userType");
        String position = (String)positionUI.getLocalValue();
        UserType userType = (UserType)userTypeUI.getLocalValue();
        String inputValue = (String)value;
        if(position.equals("student") && userType == UserType.ADVANCE){
            if(inputValue == null || inputValue.trim().length()==0){
                FacesMessage message = new FacesMessage(
                        ResourceBundle.getBundle("ValidationMessages",context.getViewRoot().getLocale())
                                .getString("cn.edu.pku.lib.dataverse.validation.AdvanceBuiltinUserSupervisorValidation.message"));
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(message);
            }
        }
    }
    
}
