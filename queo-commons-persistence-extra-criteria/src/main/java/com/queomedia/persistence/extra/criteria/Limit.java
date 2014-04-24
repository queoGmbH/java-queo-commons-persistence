package com.queomedia.persistence.extra.criteria;

/**
 * The Class Limit.
 * 
 * @author Engelmann
 */
public class Limit {
	
	/**
	 * No Limit.
	 */
	public static final Limit NO = new Limit();

    /**
     * The first result.
     * This value can be null in order to indicate that no first result is set.
     */
    private final Integer firstResult;

    /**
     * The max result.
     * This value can be null in order to indicate that no max result is set.
     */
    private final Integer maxResult;

    /**
     * Instantiates a class that defines no Limit.
     */
    public Limit() {
        this.firstResult = null;
        this.maxResult = null;
    }

    /**
     * The Constructor.
     * 
     * @param maxResult the max result
     */
    public Limit(final int maxResult) {
        this.firstResult = null;
        this.maxResult = maxResult;
    }

    /**
     * The Constructor.
     * 
     * @param firstResult the first result
     * @param maxResult the max result
     */
    public Limit(final int firstResult, final int maxResult) {
        this.firstResult = firstResult;
        this.maxResult = maxResult;
    }

    /**
     * Gets the first result.
     * 
     * @return the first result
     */
    public Integer getFirstResult() {
        return this.firstResult;
    }

    /**
     * Gets the max result.
     * 
     * @return the max result
     */
    public Integer getMaxResult() {
        return this.maxResult;
    }

    public boolean hasLimit() {
        return (this.firstResult != null) || (this.maxResult != null);
    }
}
