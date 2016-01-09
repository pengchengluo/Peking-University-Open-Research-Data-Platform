/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;
/**
 *
 * @author skraffmi
 */
//@Target({FIELD})
//@Retention(RUNTIME)
//@Constraint(validatedBy = {EMailValidator.class})
//@Documented
@Documented
@Constraint(validatedBy = {EMailValidator.class})
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
@NotNull
public @interface  ValidateEmail {
  public String message() default "{edu.harvard.iq.dataverse.ValidateEmail.message}";

  public Class<?>[] groups() default {};

  public Class<? extends Payload>[] payload() default {};
    
}
