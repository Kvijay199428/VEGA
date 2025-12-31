package com.vegatrader.upstox.api.instrument.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bean Validation annotation for validating exchange codes.
 * 
 * @since 4.0.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidExchange.ExchangeValidator.class)
public @interface ValidExchange {

    String message() default "Invalid exchange code. Expected: NSE, NFO, CDS, BSE, BFO, BCD, MCX, or NSCOM";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Validator implementation.
     */
    class ExchangeValidator implements ConstraintValidator<ValidExchange, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true;
            }
            return InstrumentKeyPattern.isValidExchange(value);
        }
    }
}
