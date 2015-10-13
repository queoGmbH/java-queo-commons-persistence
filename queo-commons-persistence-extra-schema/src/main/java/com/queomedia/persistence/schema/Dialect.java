package com.queomedia.persistence.schema;

/**
 * Holds the classnames of hibernate dialects for easy reference.
 */
public enum Dialect {

    /**
     * The Oracle dialect.
     */
    ORACLE("org.hibernate.dialect.Oracle10gDialect"),

    /**
     * The MySql dialect.
     */
    MYSQL("org.hibernate.dialect.MySQL5InnoDBDialect"),

    /**
     * The HSQL dialect.
     */
    HSQL("org.hibernate.dialect.HSQLDialect"),

    /**
     * The DB2 dialect.
     */
    DB2("org.hibernate.dialect.DB2Dialect"),

    /**
     * The MS SQL dialect.
     */
    SQL_SERVER_2012("org.hibernate.dialect.SQLServer2012Dialect");

    /**
     * The dialect class.
     */
    private String dialectClass;

    /**
     * Instantiates a new dialect.
     * 
     * @param dialectClass the dialect class
     */
    private Dialect(final String dialectClass) {
        this.dialectClass = dialectClass;
    }

    /**
     * Gets the dialect class.
     * 
     * @return the dialect class
     */
    public String getDialectClass() {
        return this.dialectClass;
    }
}
