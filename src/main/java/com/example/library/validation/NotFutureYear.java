package com.example.library.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Năm không được lớn hơn năm hiện tại. */
@Documented
@Constraint(validatedBy = NotFutureYearValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotFutureYear {

    String message() default "Năm xuất bản không được lớn hơn năm hiện tại";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
