/**
 * 
 */
package com.queomedia.persistence.extra.criteria;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplmentedCaseExecption;

/**
 * The Class FilterRestrictions.
 * 
 * @author engelmann
 */
public final class FilterRestrictions {

    /** Tool classes need no restrictions. */
    private FilterRestrictions() {
    }

    /**
     * Adds the criterion it it is not null.
     * 
     * @param crit the crit
     * @param nullAllowed the null allowed criterion
     */
    public static void addNullAllowed(final Criteria crit, final Criterion nullAllowed) {
        Check.notNullArgument(crit, "crit");

        if (nullAllowed != null) {
            crit.add(nullAllowed);
        }
    }

    /**
     * Yes no.
     * 
     * @param propertyName the property name
     * @param restriction the restriction
     * 
     * @return the criterion
     */
    public static Criterion yesNo(final String propertyName, final YesNoDontCare restriction) {
        Check.notNullArgument(propertyName, "propertyName");
        Check.notNullArgument(restriction, "restriction");

        switch (restriction) {
        case DONT_CARE:
            return null;
        case YES:
            return Restrictions.eq(propertyName, true);
        case NO:
            return Restrictions.eq(propertyName, false);

        default:
            throw new NotImplmentedCaseExecption("The case " + restriction + " is not implemented.");
        }
    }

    /**
     * Enum filter.
     * 
     * @param propertyName the property name
     * @param restrictions the restrictions
     * 
     * @return the criterion
     */
    protected static <T extends Enum<?>> Criterion enumFilter(final String propertyName,
            final EnumFilter<T> restrictions) {
        Check.notNullArgument(propertyName, "propertyName");
        Check.notNullArgument(restrictions, "restrictions");

        if (!restrictions.hasRestrictions()) {
            return null;
        }

        List<T> yesList = new ArrayList<T>();
        List<T> noList = new ArrayList<T>();

        for (Regulation<T> regulation : restrictions.getAllRestrictions()) {
            switch (regulation.getRule()) {
            case DONT_CARE:
                break;
            case YES:
                yesList.add(regulation.getValue());
                break;
            case NO:
                noList.add(regulation.getValue());
                break;

            default:
                throw new NotImplmentedCaseExecption("The case " + regulation.getRule() + " is not implemented.");
            }
        }

        if ((yesList.size() > 0) && (noList.size() == 0)) {
            return Restrictions.in(propertyName, yesList.toArray());
        }
        if ((yesList.size() == 0) && (noList.size() > 0)) {
            return Restrictions.not(Restrictions.in(propertyName, noList.toArray()));
        }
        if ((yesList.size() > 0) && (noList.size() > 0)) {
            return Restrictions.and(Restrictions.in(propertyName, yesList.toArray()), Restrictions.not(Restrictions
                    .in(propertyName, noList.toArray())));
        }
        return null;
    }

    /**
     * Adds the yes no.
     * 
     * @param crit the crit
     * @param propertyName the property name
     * @param restriction the restriction
     */
    public static void addYesNo(final Criteria crit, final String propertyName, final YesNoDontCare restriction) {
        Check.notNullArgument(crit, "crit");
        Check.notNullArgument(propertyName, "propertyName");
        Check.notNullArgument(restriction, "restriction");

        FilterRestrictions.addNullAllowed(crit, FilterRestrictions.yesNo(propertyName, restriction));
    }

    /**
     * Adds the enum filter.
     * 
     * @param crit the crit
     * @param propertyName the property name
     * @param restrictions the restrictions
     */
    public static <T extends Enum<?>> void addEnumFilter(final Criteria crit, final String propertyName,
            final EnumFilter<T> restrictions) {
        Check.notNullArgument(crit, "crit");
        Check.notNullArgument(propertyName, "propertyName");
        Check.notNullArgument(restrictions, "restrictions");

        FilterRestrictions.addNullAllowed(crit, FilterRestrictions.enumFilter(propertyName, restrictions));
    }
    
    /**
     * Apply the limit to the critiera query.
     * @param crit the criteria query
     * @param limit the limit
     */
    public static void setLimit(final Criteria crit, final Limit limit) {
        Check.notNullArgument(crit, "crit");
        Check.notNullArgument(limit, "limit");

        if (limit.getFirstResult() != null) {
            crit.setFirstResult(limit.getFirstResult());
        }
        if (limit.getMaxResult() != null) {
            crit.setMaxResults(limit.getMaxResult());
        }
    }
}
