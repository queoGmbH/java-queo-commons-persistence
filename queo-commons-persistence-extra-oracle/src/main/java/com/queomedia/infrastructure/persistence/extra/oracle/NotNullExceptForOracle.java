package com.queomedia.infrastructure.persistence.extra.oracle;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The annotated element must not be <code>null</code>.
 * Accepts any type.
 * 
 * Behaves like {@link javax.validation.constraints.NotNull} constraint, except that this annotation is unknown to
 * hibernate schema generator and therefore it does not make the database column Not Null.
 * 
 * <p>
 * It is intended that {@link NotNullExceptForOracle} and {@link NotNullString} are used together if one want to have
 * a String that <b>can be empty but not null</b> in the Java world. In the oracle DB it become null.
 * usage: 
 * </p>
 * <pre><code>
 *  {@literal @}NotNullExceptForOracle
 *  {@literal @}Type(type = "com.queomedia.infrastructure.persistence.extra.oracle.NotNullString")
 *  private String comment;
 * </code></pre>
 *
 * @author Ralph Engelmann
 */
@Documented
@Constraint(validatedBy = { NotNullExceptForOracleValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface NotNullExceptForOracle {

    /**
     * The message or message key for constraint violation.
     *
     * @return the string
     */
    String message() default "{javax.validation.constraints.NotNull.message}";

    /**
     * The validation group.
     *
     * @return the class[]
     */
    Class<?>[] groups() default { };

    /**
     * Payload.
     *
     * @return the class&lt;? extends payload&gt;[]
     */
    Class<? extends Payload>[] payload() default { };

    /**
     * Defines several <code>@NotNullExceptForOracle</code> annotations on the same element
     * @see NotNullExceptForOracle
     *
     * @author Ralph Englemann
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    @interface List {

        /**
         * The several <code>@NotNullExceptForOracle</code> annotations.
         *
         * @return the not null except for oracle[]
         */
        NotNullExceptForOracle[] value();
    }
}
