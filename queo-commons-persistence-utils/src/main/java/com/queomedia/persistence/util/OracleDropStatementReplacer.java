package com.queomedia.persistence.util;

import java.util.ArrayList;
import java.util.List;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.schema.Dialect;

/**
 * This class is used to make the created Oracle ddl file fail safe,
 * if a drop would be generated, but the table is missing (pendant to mysql: drop table IF EXIST).
 *
 * This should only be used for {@link Dialect#ORACLE}
 *
 * @author hohlfeld
 */
public class OracleDropStatementReplacer {

    /** oracles error code for table not exists. */
    public static final int ORACLE_EC_TABLE_NOT_EXISTS = -942;

    /** oracles error code for sequence not exists. */
    public static final int ORACLE_EC_SEQUENCE_NOT_EXISTS = -2289;

    /**
     * Parses a String and replaces DROP TABLE and DROP SEQUENCE statements fail safe (if they not exists in target db).
     *
     * The drop statement will be replaced by a procedure, which is catching the failure codes
     * (-942 for table not exist / -2289 for procedure not exists)
     * If this exception was catched the procedure will ends normally.
     * All other Exceptions (exception-numbers) will be thrown.
     *
     * @param original the original Statement as String
     * @return a list of Strings, representing the statement and its changes
     */
    public static List<String> replaceDropStatements(final List<String> original) {
        Check.notEmptyArgument(original, "original");

        List<String> result = new ArrayList<String>();

        for (String line : original) {

            String trimmed = line.toLowerCase().trim();
            int exceptionCode;

            if (trimmed.startsWith("drop table")) {
                exceptionCode = OracleDropStatementReplacer.ORACLE_EC_TABLE_NOT_EXISTS;
            } else if (trimmed.startsWith("drop sequence")) {
                exceptionCode = OracleDropStatementReplacer.ORACLE_EC_SEQUENCE_NOT_EXISTS;
            } else {
                //it's not a drop statement - add original line to result
                result.add(line);
                continue;
            }
            //add procedure
            result.add("begin execute immediate '" + trimmed.substring(0, trimmed.lastIndexOf(";"))
                    + "'; exception when others then if sqlcode != " + exceptionCode + " then raise; end if; end;");
            //execute procedure
            result.add("/");
        }

        return result;
    }

    /**
     * Parses the String into a List of lines and call {@link #replaceDropStatements(List<String>)}
     *
     * @return a list of Strings, representing the statement and its changes
     */
    public static List<String> replaceDropStatements(final String original) {
        Check.notEmptyArgument(original, "original");

        String lines[] = original.split("\\n");
        ArrayList<String> splitted = new ArrayList<String>();

        for (String line : lines) {
            splitted.add(line);
        }

        return OracleDropStatementReplacer.replaceDropStatements(splitted);
    }
}
