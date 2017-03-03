/**
 * 
 */
package com.queomedia.persistence.extra.criteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.queomedia.commons.checks.Check;

/**
 * The Class EnumFilter.
 * 
 * @param <T> the generic enum type
 * @author engelmann
 */
public class EnumFilter<T extends Enum<?>> implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5388664863337829843L;

    /** The regulations. */
    private Map<T, Regulation<T>> regulations = new HashMap<T, Regulation<T>>();

    /**
     * Instantiates a new enum filter.
     */
    public EnumFilter() {
    }

    /**
     * Instantiates a new enum filter.
     * 
     * @param value the value
     * @param rule the rule
     */
    public EnumFilter(final T value, final YesNoDontCare rule) {
        Check.notNullArgument(value, "value");
        Check.notNullArgument(rule, "rule");

        this.addRestriction(value, rule);
    }

    /**
     * Instantiates a new enum filter.
     * 
     * @param value the value
     * @param rule the rule
     */
    public EnumFilter(final T value, final boolean rule) {
        Check.notNullArgument(value, "value");
        Check.notNullArgument(rule, "rule");

        this.addRestriction(value, rule);
    }

    /**
     * Instantiates a new enum filter.
     * Set all values to the rule value.
     * 
     * @param values the values
     * @param rule the rule
     */
    public EnumFilter(final Collection<T> values, final boolean rule) {
        Check.notNullArgument(values, "values");
        Check.notNullArgument(rule, "rule");

        for (T value : values) {
            this.addRestriction(value, rule);
        }
    }

    /**
     * Instantiates a new enum filter.
     * All named values are required (yes).
     * 
     * @param values the required values   
     */
    public EnumFilter(final T... values) {
        Check.notNullArgument(values, "values");

        int size = values.length;
        for (int i = 0; i < size; i++) {
            this.addRestriction(values[i], true);
        }
    }

    /**
     * Instantiates a new enum filter.
     * 
     * @param regulation the regulation
     */
    public EnumFilter(final Regulation<T> regulation) {
        Check.notNullArgument(regulation, "regulation");

        this.addRestriction(regulation);
    }

    /**
     * Instantiates a new enum filter.
     * 
     * @param regulations the regulations
     */
    public EnumFilter(final Regulation<T>... regulations) {
        this.addRestriction(regulations);
    }

    /**
     * Adds the restriction.
     * 
     * @param regulation the regulation
     */
    public void addRestriction(final Regulation<T> regulation) {
        Check.notNullArgument(regulation, "regulation");

        this.regulations.put(regulation.getValue(), regulation);
    }

    /**
     * Adds the restrictions.
     * 
     * @param regulations the regulations
     */
    public void addRestriction(final Regulation<T>... regulations) {
        int size = regulations.length;
        for (int i = 0; i < size; i++) {
            this.addRestriction(regulations[i]);
        }
    }

    /**
     * Adds the restriction.
     * 
     * @param value the value
     * @param rule the rule
     */
    public void addRestriction(final T value, final YesNoDontCare rule) {
        Check.notNullArgument(value, "value");
        Check.notNullArgument(rule, "rule");

        this.regulations.put(value, new Regulation<T>(value, rule));
    }

    /**
     * Adds the restriction.
     * 
     * @param value the value
     * @param rule the rule
     */
    public void addRestriction(final T value, final boolean rule) {
        Check.notNullArgument(value, "value");

        if (rule) {
            this.addRestriction(value, YesNoDontCare.YES);
        } else {
            this.addRestriction(value, YesNoDontCare.NO);
        }
    }

    /**
     * Gets the all restrictions.
     * 
     * @return the all restrictions
     */
    public Collection<Regulation<T>> getAllRestrictions() {
        return this.regulations.values();
    }

    /**
     * Checks for restrictions.
     * 
     * @return true, if successful
     */
    public boolean hasRestrictions() {
        return !this.regulations.isEmpty();
    }

    /**
     * Gets the restriction.
     * 
     * @param value the value of Restriction.
     * 
     * @return the restriction
     */
    public YesNoDontCare getRestriction(final T value) {
        Regulation<T> regulation = this.regulations.get(value);

        if (regulation == null) {
            return YesNoDontCare.DONT_CARE;
        } else {
            return regulation.getRule();
        }
    }

    /**
     * Check if this filter would accept an specific value.
     * @param value the enum value to check
     * @return true it is accepted, false if it is not accepted or do not care.
     */
    public boolean doActiveAccept(final T value) {
        Check.notNullArgument(value, "value");

        return this.getRestriction(value) == YesNoDontCare.YES;
    }

    /**
     * Constructs a <code>String</code> with all attributes
     * in name = value format.
     *
     * @return a <code>String</code> representation 
     * of this object.
     */
    @Override
    public String toString() {
        final String tab = "    ";

        String retValue = "";

        retValue = "EnumFilter ( " + super.toString() + tab + "regulations = " + this.regulations + tab + " )";

        return retValue;
    }

    public List<T> getYesRestrictions() {
        List<T> yesList = new ArrayList<T>();
        for (Entry<T, Regulation<T>> entry : this.regulations.entrySet()) {
            if (entry.getValue().getRule() == YesNoDontCare.YES) {
                yesList.add(entry.getValue().getValue());
            }
        }

        return yesList;
    }
    
    /**
     * Creates an deep copy of this Filter.
     * @return the copy
     */
    public EnumFilter<T> copy() {
        EnumFilter<T> copy = new EnumFilter<T>();
        for (Map.Entry<T, Regulation<T>> originalEntity : this.regulations.entrySet()) {
            copy.addRestriction(originalEntity.getKey(), originalEntity.getValue().getRule());
        }
        return copy;
    }
}
