package com.queomedia.persistence;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Transient;

import org.springframework.core.Conventions;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

/**
 * Business Domain Entity with an transient map for properties.
 *
 * @param <T> the generic type
 */
abstract public class PropertyEntity<T extends Serializable> extends BusinessEntity<T> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5503945145000784842L;

    /** The properties. */
    @Transient
    private transient Map<String, Object> properties = new HashMap<>();

    /**
     * Instantiates a new property entity.
     */
    @Deprecated
    public PropertyEntity() {
        super();
    }

    /**
     * Instantiates a new property entity.
     *
     * @param businessId the business id
     */
    public PropertyEntity(final BusinessId<T> businessId) {
        super(businessId);
    }

    /**
     * Gets the property.
     *
     * @param key the key
     * @return the property
     */
    public Object getProperty(final String key) {
        Check.notNullArgument(key, "key");

        if (!this.properties.containsKey(key)) {
            throw new RuntimeException("Property " + key + " not set");
        }

        return this.properties.get(key);
    }

    /**
     * Sets the property.
     *
     * @param key the key
     * @param value the value
     * @param overwrite allows overwriting a value if the key is set. 
     *          If false and the key is present, an exception is thrown.
     */
    public void setProperty(final String key, final Object value, final boolean overwrite) {
        Check.notNullArgument(key, "key");

        if (this.properties.containsKey(key) && !overwrite) {
            throw new RuntimeException("Key " + key + " already set.");
        }

        this.properties.put(key, value);
    }

    /**
     * Sets the property.
     *
     * @param key the key
     * @param value the value
     */
    public void setProperty(final String key, final Object value) {
        Check.notNullArgument(key, "key");

        this.setProperty(key, value, false);
    }

    /**
     * Sets a variable using class conventions to create a property key.
     * 
     * @see {@link Conventions}
     *
     * @param value the new property
     */
    public void setProperty(final Object value) {
        Check.notNullArgument(value, "value");

        this.setProperty(Conventions.getVariableName(value), value, false);
    }

    /**
     * Sets a property allowing overwriting when present. This is useful
     * if we cannot know if the property has been set but don't want an exception.
     *
     * @param value the new property
     * @see {@link Conventions}
     */
    public void setOrOverwriteProperty(final Object value) {
        Check.notNullArgument(value, "value");

        this.setProperty(Conventions.getVariableName(value), value, true);

    }

    /**
     * Sets a property allowing overwriting when present. This is useful
     * if we cannot know if the property has been set but don't want an exception.
     *
     * @param key the key
     * @param value the value
     */
    public void setOrOverwriteProperty(final String key, final Object value) {
        Check.notNullArgument(key, "key");

        this.setProperty(key, value, true);

    }

    /**
     * Returns all properties as an unmodifiable Map. 
     * This is mainly used for usage with expression language in the frontend 
     * like ${object.properties['someproperty']}
     * 
     *
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

}
