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
@FacesValidator("cn.edu.pku.lib.dataverse.validation.AdvancePkuIAAAUserNotBlankValidator")
public class AdvancePkuIAAAUserNotBlankValidator implements Validator{

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        UIInput userTypeUI = (UIInput)component.findComponent("userTypeIAAA");
        UserType userType = (UserType)userTypeUI.getLocalValue();
        String inputValue = (String)value;
        if(userType == null || userType == UserType.ADVANCE){ //userType == null为编辑模式下，用户类型为高级
            if(inputValue == null || inputValue.trim().length()==0){
                FacesMessage message = new FacesMessage(
                        ResourceBundle.getBundle("ValidationMessages",context.getViewRoot().getLocale())
                                .getString("org.hibernate.validator.constraints.NotBlank.message"));
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(message);
            }
        }
    }
}
