package com.queomedia.persistence.extra.json.switchingbusinessentity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.queomedia.commons.checks.Check;

/**
 * Scan the json-context-path to find a class that is annotated with {@link SwitchingBusinessEntityAnnotation}.  
 */
class SwitchingAnnotationScanner {
    
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

    Optional<SwitchingBusinessEntityAnnotation> findSwitchingBusinessEntityAnnotation(
            final JsonStreamContext context) {
        Object currentValue = context.getCurrentValue();
        System.out.println(
                "currentValue:" + currentValue + " " + (currentValue != null ? currentValue.getClass() : ""));

        /*
         * check getter and fields for annotation before class, because they have higher
         * priority.
         */
        if (currentValue != null) {
            Class<? extends Object> currentClass = currentValue.getClass();

            if (context.getCurrentName() != null) {
                // TODO find field instead of use exception
                try {

                    Field field = currentClass.getDeclaredField(context.getCurrentName());
                    Optional<SwitchingBusinessEntityAnnotation> fieldAnnotation = Optional
                            .ofNullable(field.getAnnotation(SwitchingBusinessEntityAnnotation.class));
                    if (fieldAnnotation.isPresent()) {
                        return fieldAnnotation;
                    }
                } catch (NoSuchFieldException e) {
                    // TODO scan super classes
                }

                // TODO find methos instead of use exception
                try {
                    /*
                     * Getter are always public, so we can use getMethoth instead of
                     * getDeclaredMethods and not not need a manual scan in super classes.
                     */
                    Method method = currentClass.getMethod(getterName(context.getCurrentName()));
                    Optional<SwitchingBusinessEntityAnnotation> methodAnnotation = Optional
                            .ofNullable(method.getAnnotation(SwitchingBusinessEntityAnnotation.class));
                    if (methodAnnotation.isPresent()) {
                        return methodAnnotation;
                    }
                } catch (NoSuchMethodException e) {
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

    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    public static String getterName(final String fieldName) {
        Check.notNullArgument(fieldName, "fieldName");

        return "get" + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
    }
}