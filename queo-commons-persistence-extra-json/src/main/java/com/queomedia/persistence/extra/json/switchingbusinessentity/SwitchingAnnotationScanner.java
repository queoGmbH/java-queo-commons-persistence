package com.queomedia.persistence.extra.json.switchingbusinessentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.queomedia.commons.checks.Check;

/**
 * Scan the json-context-path to find a class that is annotated with {@link SwitchingBusinessEntityAnnotation}.  
 */
public class SwitchingAnnotationScanner {
    
    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchingAnnotationScanner.class);

    
    /** The serialization mode returned if no annotation is found. */
    private final BusinessEntitySerializationMode defaultMode;

    public SwitchingAnnotationScanner(final BusinessEntitySerializationMode defaultMode) {
        Check.notNullArgument(defaultMode, "defaultMode");

        this.defaultMode = defaultMode;
    }

    BusinessEntitySerializationMode getSwitchDefinition(final JsonStreamContext context) {
        return findSwitchDefinition(context).orElse(this.defaultMode);
    }

    Optional<BusinessEntitySerializationMode> findSwitchDefinition(final JsonStreamContext context) {
        return findSwitchingBusinessEntityAnnotation(context).map(SwitchingBusinessEntityAnnotation::value);
    }

    /**
     * Try to find the {@link SwitchingBusinessEntityAnnotation} in the given json {@code context}s
     * {@link JsonStreamContext#getCurrentValue() context.getCurrentValue()} or if not found recursive in the
     * {@link JsonStreamContext#getParent() context.getParent()}.
     *
     * @param context the {@link JsonStreamContext} thats currentValue and parentContext is scanned
     * @return the first found {@link SwitchingBusinessEntityAnnotation}
     */
    Optional<SwitchingBusinessEntityAnnotation> findSwitchingBusinessEntityAnnotation(
            final JsonStreamContext context) {
        Check.notNullArgument(context, "context");
        
        Object currentValue = context.getCurrentValue();
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("currentValue:" + currentValue + " " + (currentValue != null ? currentValue.getClass() : ""));
        }        

        /*
         * check getter and fields for annotation before class, because they have higher
         * priority.
         */
        if (currentValue != null) {
            Class<? extends Object> currentClass = currentValue.getClass();

            if (context.getCurrentName() != null) {
                                
                Optional<SwitchingBusinessEntityAnnotation> fieldAnnotation = findAnnotationAtField(context, currentClass);
                if (fieldAnnotation.isPresent()) {
                    return fieldAnnotation;
                }
                
                Optional<SwitchingBusinessEntityAnnotation> methodAnnotation = findAnnotationAtGetter(context, currentClass);
                if (methodAnnotation.isPresent()) {
                    return methodAnnotation;
                }
            }
            Optional<SwitchingBusinessEntityAnnotation> classAnnotation = Optional
                    .ofNullable(currentClass.getAnnotation(SwitchingBusinessEntityAnnotation.class));
            if (classAnnotation.isPresent()) {
                return classAnnotation;
            }
        }

        /* recursion to json parent */
        if (context.getParent() != null) {
            return findSwitchingBusinessEntityAnnotation(context.getParent());
        }

        return Optional.empty();
    }
  
    private Optional<SwitchingBusinessEntityAnnotation> findAnnotationAtField(final JsonStreamContext context, Class<? extends Object> currentClass) {
        // TODO find field instead of use exception
        try {

            Field field = currentClass.getDeclaredField(context.getCurrentName());
            return Optional
                    .ofNullable(field.getAnnotation(SwitchingBusinessEntityAnnotation.class));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }
    
    private Optional<SwitchingBusinessEntityAnnotation> findAnnotationAtGetter(final JsonStreamContext context, Class<? extends Object> currentClass) {
        // TODO find methos instead of use exception
        try {
            /*
             * Getter are always public, so we can use getMethoth instead of
             * getDeclaredMethods and not not need a manual scan in super classes.
             */
            Method method = currentClass.getMethod(getterName(context.getCurrentName()));
            return Optional
                    .ofNullable(method.getAnnotation(SwitchingBusinessEntityAnnotation.class));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a String which capitalizes the first letter of the string.
     *
     * @param fieldName the field name
     * @return the name of the Getter for the field.
     */
    public static String getterName(final String fieldName) {
        Check.notNullArgument(fieldName, "fieldName");

        return "get" + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
    }
}