/**
 * 
 */
package com.queomedia.persistence.impl;

import java.util.Random;
/**
 * A generator to generate unique IDs.
 * @author Engelmann
 *
 */
public class UniqueIdGenerator {

    /**
     * Half size of a long.
     */
    private static final int HALF_LONG_BIT_SIZE = 32;

    /** Used random generator. */
    private Random random;

    /**
     * Instantiates a new unique id generator.
     */
    public UniqueIdGenerator() {
        super();
        this.random = new Random();
    }

    /**
    * Format time and random long together to one long value.
    * 
    * @param time the time
    * @param rand the rand
    * 
    * @return the long
    */
    protected long format(final long time, final long rand) {
        long value = (int) (rand ^ rand >>> UniqueIdGenerator.HALF_LONG_BIT_SIZE);
        return value + ((time ^ time >>> UniqueIdGenerator.HALF_LONG_BIT_SIZE) << UniqueIdGenerator.HALF_LONG_BIT_SIZE);
    }

    /**
     * Generate a unique id.
     * @return unique id
     */
    public long getUID() {
        return format(System.currentTimeMillis(), this.random.nextLong());
    }

}
