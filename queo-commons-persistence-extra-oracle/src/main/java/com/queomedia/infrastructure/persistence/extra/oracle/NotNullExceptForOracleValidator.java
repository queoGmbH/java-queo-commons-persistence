package com.queomedia.infrastructure.persistence.extra.oracle;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that checks if an field is not null.
 * 
 * @author Ralph Engelmann
 * @see NotNullExceptForOracle
 */
public class NotNullExceptForOracleValidator implements ConstraintValidator<NotNullExceptForOracle, Object> {

    /*
     * (non-Javadoc)
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize(final NotNullExceptForOracle constraintAnnotation) {
        //nothing to do
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        return value != null;
    }

}
