package de.group15.assignment1.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class SinceValidator implements ConstraintValidator<Since, LocalDate> {
    private String constraint;

    @Override
    public void initialize(Since constraintAnnotation) {
        this.constraint = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate ld = LocalDate.parse(constraint, dtf);
        if (value == null) {
            return false;
        }
        return value.isAfter(ld);
    }

}
