/**
 * Provides common extensions for Hibernate/JPA in context of Oracle DB.
 * Hibernate/JPA extensions that are not common, like project specific CustomTypes are not part of this package.
 * 
 * One problem solved by this package is the oracle db handling for strings. Because oracle does not distinguish
 * between an empty String and null (in varchar or varchar2 columns) and therefore make an empty string a null string.
 * This is a problem with our application, because we even decide that there is often no meaningfull difference between
 * an empty and null String, but in contrast to oracle we decide to use an empty string.  
 * For this empty null String mapping problem, one can attach two annotations to entity string fields that can be empty
 * <pre><code>
 *  {@literal @}NotNullExceptForOracle
 *  {@literal @}Type(type = "com.queomedia.infrastructure.persistence.NotNullString")
 *  private String comment;
 * </code></pre>
 * 
 * <ul>
 *  <li>
 *      {@link com.queomedia.infrastructure.persistence.extra.oracle.NotNullExceptForOracle} - works like a {@code NotNull}
 *      constraint for the java world, but Hibernate schema generate will NOT make the column "not Null"
 *  </li>
 *  <li>
 *      Type(type = "com.queomedia.infrastructure.persistence.NotNullString") kicks in when the field is loaded from
 *      the database, it will convert an null String to an empty String
 *  <li>
 * </ul>
 */
package com.queomedia.infrastructure.persistence.extra.oracle;
