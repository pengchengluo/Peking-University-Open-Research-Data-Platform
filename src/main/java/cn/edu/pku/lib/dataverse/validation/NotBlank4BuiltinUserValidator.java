/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.validation;

import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUser;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser.UserType;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author luopc
 */
public class NotBlank4BuiltinUserValidator implements 
        ConstraintValidator<NotBlank4BuiltinUser, BuiltinUser>{

    @Override
    public void initialize(NotBlank4BuiltinUser constraintAnnotation) {
    }

    @Override
    public boolean isValid(BuiltinUser value, ConstraintValidatorContext context) {
        if(value.getUserName()==null || value.getUserName().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("用户名不能为空").addConstraintViolation();
            return false;
        }
        if(value.getEmail()==null || value.getEmail().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("电子邮箱不能为空").addConstraintViolation();
            return false;
        }
        if(value.getFirstName()==null || value.getFirstName().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("名称不能为空").addConstraintViolation();
            return false;
        }
        if(value.getLastName()==null || value.getLastName().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("姓氏不能为空").addConstraintViolation();
            return false;
        }
        if(value.getAffiliation()==null || value.getAffiliation().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("单位不能为空").addConstraintViolation();
            return false;
        }
        if(value.getDepartment()==null || value.getDepartment().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("部门不能为空").addConstraintViolation();
            return false;
        }
        if(value.getSpeciality()==null || value.getSpeciality().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("专业不能为空").addConstraintViolation();
            return false;
        }
        if(value.getResearchInterest()==null || value.getResearchInterest().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("研究领域不能为空").addConstraintViolation();
            return false;
        }
        if(value.getPosition()==null || value.getPosition().trim().length()==0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("身份不能为空").addConstraintViolation();
            return false;
        }
        
        if(value.getUserType() == UserType.ADVANCE){
            if(value.getGender() == null || value.getGender().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("性别不能为空").addConstraintViolation();
                return false;
            }
            if(value.getEducation() == null || value.getEducation().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("学历不能为空").addConstraintViolation();
                return false;
            }
            if(value.getProfessionalTitle() == null || value.getProfessionalTitle().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("职称不能为空").addConstraintViolation();
                return false;
            }
            if(value.getPosition().equals("student") && 
                    (value.getSupervisor()==null || value.getSupervisor().trim().length()==0)){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("导师不能为空").addConstraintViolation();
                return false;
            }
            if(value.getOfficePhone()==null || value.getOfficePhone().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("办公电话不能为空").addConstraintViolation();
            }
            if(value.getCellphone()==null || value.getCellphone().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("手机号不能为空").addConstraintViolation();
            }
            if(value.getOtherEmail()==null || value.getOtherEmail().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("其它邮箱不能为空").addConstraintViolation();
            }
            if(value.getCountry()==null || value.getCountry().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("国家或地区不能为空").addConstraintViolation();
            }
            if(value.getProvince()==null || value.getProvince().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("所在省不能为空").addConstraintViolation();
            }
            if(value.getCity()==null || value.getCity().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("所在城市不能为空").addConstraintViolation();
            }
            if(value.getAddress()==null || value.getAddress().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("地址不能为空").addConstraintViolation();
            }
            if(value.getZipCode()==null || value.getZipCode().trim().length()==0){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("邮编不能为空").addConstraintViolation();
            }
        }
        return true;
    }
    
}
