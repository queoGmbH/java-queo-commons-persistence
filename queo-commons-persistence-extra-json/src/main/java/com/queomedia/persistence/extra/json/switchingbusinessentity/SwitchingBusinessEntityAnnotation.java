package com.queomedia.persistence.extra.json.switchingbusinessentity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.queomedia.persistence.BusinessEntity;

/**
 * Annotation to control how the Switchting-Business-Entity-Serializer (and -Deserializer) serialize {@link BusinessEntity}s:
 * either as full entity with all properties or just its business id.
 *
 * <p>
 * This annotation can be placed at the Business Entity itself or at any parent class or field in the jackson-serialization-path.
 * <br>
 * If it is placed at a class, then this mode is used for all {@link BusinessEntity}s in the jackson-serialization-path that are in a
 * children relation ship to this class.
 * <br>
 * If it is placed at a field, then this mode is used for all {@link BusinessEntity}s in the jackson-serialization-path that are in a
 * children relation ship to this field.
 * </p>
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD })
public @interface SwitchingBusinessEntityAnnotation {

    /** Controll how*/
    public BusinessEntitySerializationMode value();

}
