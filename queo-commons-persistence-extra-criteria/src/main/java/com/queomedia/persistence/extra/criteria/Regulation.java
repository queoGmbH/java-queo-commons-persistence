/**
 * 
 */
package com.queomedia.persistence.extra.criteria;

import com.queomedia.commons.checks.Check;

/**
 * The Class Regulation.
 * 
 * @author engelmann
 * @param <T> the concrete type
 */
class Regulation<T extends Enum<?>> {

    /** The value. */
    private T value;

    /** The rule. */
    private YesNoDontCare rule;

    /**
     * The Constructor.
     * 
     * @param value the value
     * @param rule the rule
     */
    public Regulation(final T value, final YesNoDontCare rule) {
        Check.notNullArgument(value, "value");
        Check.notNullArgument(rule, "rule");

        this.value = value;
        this.rule = rule;
    }

    /**
     * Instantiates a new regulation.
     * 
     * @param value the value
     * @param rule the rule
     */
    public Regulation(final T value, final boolean rule) {
        Check.notNullArgument(value, "value");

        this.value = value;
        if (rule) {
            this.rule = YesNoDontCare.YES;
        } else {
            this.rule = YesNoDontCare.NO;
        }
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Gets the rule.
     * 
     * @return the rule
     */
    public YesNoDontCare getRule() {
        return this.rule;
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

        retValue = "Regulation ( " + super.toString() + tab + "value = " + this.value + tab + "rule = " + this.rule
                + tab + " )";

        return retValue;
    }

}
