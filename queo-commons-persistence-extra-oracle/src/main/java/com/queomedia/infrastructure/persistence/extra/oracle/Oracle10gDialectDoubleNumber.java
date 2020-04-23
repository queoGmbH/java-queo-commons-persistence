package com.queomedia.infrastructure.persistence.extra.oracle;

import org.hibernate.dialect.Oracle10gDialect;

/**
 * Configure the SQL mapping from java Double to oracle number(16,6).
 * 
 * Without this modification, hibernate would create the type "double precision" for double, but oracle make the
 * "double precision" become a float, and then hibernate will complain about the float when validating the
 * database structure on start up.
 * 
 * @author Ralph Engelmann
 *
 */
public class Oracle10gDialectDoubleNumber extends Oracle10gDialect {

    /**
     * Instantiates a new oracle10g dialect double number dialect.
     */
    public Oracle10gDialectDoubleNumber() {
        this.registerColumnType(java.sql.Types.DOUBLE, "number(16,6)");
    }

    
}
