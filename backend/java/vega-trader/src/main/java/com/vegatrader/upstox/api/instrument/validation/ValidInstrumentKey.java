package com.vegatrader.upstox.api.instrument.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bean Validation annotation for validating instrument keys.
 * 
 * <p>
 * Usage:
 * 
 * <pre>{@code
 * @ValidInstrumentKey
 * private String instrumentKey;
 * }</pre>
 * 
 * @since 4.0.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidInstrumentKey.InstrumentKeyValidator.class)
public @interface ValidInstrumentKey {

    String message() default "Invalid instrument key format. Expected: SEGMENT|IDENTIFIER";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to allow multiple comma-separated keys.
     */
    boolean allowMultiple() default false;

    /**
     * Validator implementation.
     */
    class InstrumentKeyValidator implements ConstraintValidator<ValidInstrumentKey, String> {

        private boolean allowMultiple;

        @Override
        public void initialize(ValidInstrumentKey annotation) {
            this.allowMultiple = annotation.allowMultiple();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true; // Use @NotNull or @NotBlank for null checks
            }

            if (allowMultiple) {
                return InstrumentKeyPattern.isValidMultiKey(value);
            } else {
                return InstrumentKeyPattern.isValidSingleKey(value);
            }
        }
    }
}
