package com.queomedia.persistence.extra.json.switchingbusinessentity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.queomedia.commons.checks.Check;

/**
 * Scan the json-context-path to find a class that is annotated with {@link SwitchingBusinessEntityAnnotation}.
 *
 * <p>
 * This scanner scan for the annotation (in this order) at the field that match the json-name,
 * the getter that match the field name and then at the class. If no annotation is found, then the scanner check
 * the parent json context ({@link JsonStreamContext#getParent()}).
 * </p>
 */
public class SwitchingAnnotationScanner {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchingAnnotationScanner.class);

    /** The serialization mode returned if no annotation is found. */
    private final BusinessEntitySerializationMode defaultMode;

    /**
     * Instantiates a new switching annotation scanner with the given {@code defaultMode}
     *
     * @param defaultMode which mode is retuned if no annotation is found
     */
    public SwitchingAnnotationScanner(final BusinessEntitySerializationMode defaultMode) {
        Check.notNullArgument(defaultMode, "defaultMode");

        this.defaultMode = defaultMode;
    }

    /**
     * Get the {@link BusinessEntitySerializationMode} for the current object of the given Json-Context.
     *
     * @param context the Json-Context
     * @return the switch definition - with fallback to {@link #defaultMode} if no annotation is found
     */
    BusinessEntitySerializationMode getSwitchDefinition(final JsonStreamContext context) {
        Check.notNullArgument(context, "context");

        return findSwitchDefinition(context).orElse(this.defaultMode);
    }

    /**
     * Get the {@link BusinessEntitySerializationMode} for the current object of the given Json-Context without fallback to default.
     *
     * @param context the Json-Context
     * @return the switch definition, or empty if no annotation is found
     */
    Optional<BusinessEntitySerializationMode> findSwitchDefinition(final JsonStreamContext context) {
        Check.notNullArgument(context, "context");

        return findSwitchingBusinessEntityAnnotation(context).map(SwitchingBusinessEntityAnnotation::value);
    }

    /**
     * Try to find the {@link SwitchingBusinessEntityAnnotation} in the given json {@code context}s
     * {@link JsonStreamContext#getCurrentValue() context.getCurrentValue()} or if not found recursive in the
     * {@link JsonStreamContext#getParent() context.getParent()}.
     * 
     * <p>
     * Properties form Superclass are also checked: in this order:
     * </p>
     *
     * @param context the {@link JsonStreamContext} thats currentValue and parentContext is scanned
     * @return the first found {@link SwitchingBusinessEntityAnnotation}
     */
    Optional<SwitchingBusinessEntityAnnotation> findSwitchingBusinessEntityAnnotation(final JsonStreamContext context) {
        Check.notNullArgument(context, "context");

        Object currentValue = context.getCurrentValue();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("currentValue:" + currentValue + " " + (currentValue != null ? currentValue.getClass() : ""));
        }

        /*
         * check getter and fields for annotation before class, because they have higher
         * priority.
         */
        if (currentValue != null) {
            Class<? extends Object> currentClass = currentValue.getClass();

            if (context.getCurrentName() != null) {
                String jsonPropertyName = context.getCurrentName();

                Optional<SwitchingBusinessEntityAnnotation> fieldAnnotation = findAnnotationAtField(jsonPropertyName,
                        currentClass);
                if (fieldAnnotation.isPresent()) {
                    return fieldAnnotation;
                }

                Optional<SwitchingBusinessEntityAnnotation> methodAnnotation = findAnnotationAtGetter(jsonPropertyName,
                        currentClass);
                if (methodAnnotation.isPresent()) {
                    return methodAnnotation;
                }
            }
            Optional<SwitchingBusinessEntityAnnotation> classAnnotation = findAnnotationAtClass(currentClass);
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
     * Try to find the {@link SwitchingBusinessEntityAnnotation} at a field named {@code jsonPropertyName} at the
     * given class. 
     *
     * @param jsonPropertyName the json property name
     * @param clazz the examined class
     * @return the found {@link SwitchingBusinessEntityAnnotation}
     */
    private Optional<SwitchingBusinessEntityAnnotation> findAnnotationAtField(final String jsonPropertyName,
            final Class<? extends Object> clazz) {
        Check.notEmptyArgument(jsonPropertyName, "jsonPropertyName");
        Check.notNullArgument(clazz, "clazz");

        // TODO find field instead of use exception
        try {

            Field field = clazz.getDeclaredField(jsonPropertyName);
            return Optional.ofNullable(field.getAnnotation(SwitchingBusinessEntityAnnotation.class));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    /**
     * Try to find the {@link SwitchingBusinessEntityAnnotation} at a Getter named after {@code jsonPropertyName} at
     * the given class.
     *
     * @param jsonPropertyName the json property name
     * @param clazz the examined class
     * @return the found {@link SwitchingBusinessEntityAnnotation}
     */
    private Optional<SwitchingBusinessEntityAnnotation> findAnnotationAtGetter(final String jsonPropertyName,
            final Class<? extends Object> clazz) {
        Check.notEmptyArgument(jsonPropertyName, "jsonPropertyName");
        Check.notNullArgument(clazz, "clazz");

        // TODO find method instead of use exception
        try {
            /*
             * Getter are always public, so we can use getMethoth instead of
             * getDeclaredMethods and not not need a manual scan in super classes.
             */
            Method method = clazz.getMethod(getterName(jsonPropertyName));
            return Optional.ofNullable(method.getAnnotation(SwitchingBusinessEntityAnnotation.class));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Try to find the {@link SwitchingBusinessEntityAnnotation} at the class level.
     *
     * @param clazz the examined class
     * @return the found {@link SwitchingBusinessEntityAnnotation}
     */
    private Optional<SwitchingBusinessEntityAnnotation> findAnnotationAtClass(
            final Class<? extends Object> clazz) {
        Check.notNullArgument(clazz, "clazz");

        Optional<SwitchingBusinessEntityAnnotation> classAnnotation = Optional
                .ofNullable(clazz.getAnnotation(SwitchingBusinessEntityAnnotation.class));
        return classAnnotation;
    }

    /**
     * Returns a String which capitalizes the first letter of the string.
     *
     * @param fieldName the field name
     * @return the name of the Getter for the field.
     */
    private static String getterName(final String fieldName) {
        Check.notNullArgument(fieldName, "fieldName");

        /* we do not need to check for isser-because there can not be a boolean in hits path */
        return "get" + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
    }
}
