package com.queomedia;

/**
 * Util class which provides constants for different spring configuration files.
 * @author engelmann
 *
 */
public final class SpringTestContext {

    /**
     * The file name of an test application context that is nearly similar to the application (but without web stuff).
     */
    public static final String APPLICATION = "classpath:META-INF/spring/testContext.xml";
    
    /**
     * Util classes need no constructor.
     */
    private SpringTestContext() {
        super();
    }
}
