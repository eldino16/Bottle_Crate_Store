package de.group15.assignment1.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = SinceValidator.class)
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
public @interface Since {
    String message() default "the date must be newer than";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value();
}
