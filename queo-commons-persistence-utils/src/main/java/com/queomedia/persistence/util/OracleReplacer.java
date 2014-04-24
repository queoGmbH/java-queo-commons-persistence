package com.queomedia.persistence.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
public class OracleReplacer {

    /** oracles error code for table not exists. */
    public static final int ORACLE_EC_TABLE_NOT_EXISTS = -942;

    /** oracles error code for sequence not exists. */
    public static final int ORACLE_EC_SEQUENCE_NOT_EXISTS = -2289;

    /** the delimiter we use for executing statements. */
    public static final String ORACLE_DELIMITER = "/";

    /** the oracle comment identifier. */
    public static final String ORACLE_COMMENT = "--";

    /**
     * Replaces the incompatible parts of the generated Oracle Script to work with our sql-maven-plugin configuration.
     *
     * @param original the original statement
     * @return the replaced statement
     */
    public static List<String> replace(final List<String> original) {

        List<String> result = new ArrayList<String>();

        for (String line : original) {
            String trimmedLower = line.toLowerCase().trim();

            if (trimmedLower.startsWith("drop")) {
                result.addAll(OracleReplacer.getDropProcedure(trimmedLower));
            } else if (trimmedLower.endsWith(";")) {
                result.addAll(OracleReplacer.getReplacedDelimiter(trimmedLower));
            } else {

                result.add(line);
            }
        }

        return result;
    }

    /**
     * Parses the String into a List of lines and call {@link #replace(List<String>)}
     *
     * @return a list of Strings, representing the statement and its changes
     */
    public static List<String> replace(final String original) {
        Check.notEmptyArgument(original, "original");

        String lines[] = original.split("\n");
        ArrayList<String> splitted = new ArrayList<String>();

        for (String line : lines) {
            splitted.add(line);
        }

        return OracleReplacer.replace(splitted);
    }

    /**
     * Parses a String and replaces DROP TABLE and DROP SEQUENCE statements fail safe (if they not exists in target db).
     *
     * The drop statement will be replaced by a procedure, which is catching the failure codes
     * (-942 for table not exist / -2289 for procedure not exists)
     * If this exception was catched the procedure will ends normally.
     * All other Exceptions (exception-numbers) will be thrown.
     *
     * @param dropLine the dropLine Statement as String
     * @return a list of Strings, representing the procedure as first entry and the execution delimiter line as second entry
     */
    private static List<String> getDropProcedure(final String dropLine) {

        int exceptionCode;
        if (dropLine.startsWith("drop table")) {
            exceptionCode = OracleReplacer.ORACLE_EC_TABLE_NOT_EXISTS;
        } else if (dropLine.startsWith("drop sequence")) {
            exceptionCode = OracleReplacer.ORACLE_EC_SEQUENCE_NOT_EXISTS;
        } else {
            String[] parts = dropLine.split(" ");
            if(parts.length < 3) {
                throw new IllegalArgumentException("Drop statement hast to have at least 3 parts ('drop' object-type object-name)!");
            }
            throw new IllegalArgumentException("Drop Statement for: " + parts[0] + " " + parts[1] + " not supported!");
        }

        String procedure = "begin execute immediate '" + StringUtils.substringBeforeLast(dropLine, ";")
                + "'; exception when others then if sqlcode != " + exceptionCode + " then raise; end if; end;";

        return Arrays.asList(new String[] { procedure, "/" });
    }

    /**
     * Splits the line into instruction line and execution line (our delimiter)
     * (also for comment lines that ends with semicolon ->
     * but the delimiter on second line will be also commented)
     *
     * @param lineWithDelimiter the line to split into instruction and execution line
     * @return the instruction- and execution (delimiter) line
     */
    private static List<String> getReplacedDelimiter(final String lineWithDelimiter) {

        String instruction = StringUtils.substringBeforeLast(lineWithDelimiter, ";");

        String delimiter;
        if(lineWithDelimiter.startsWith(OracleReplacer.ORACLE_COMMENT)) {
            delimiter = OracleReplacer.ORACLE_COMMENT + " " + OracleReplacer.ORACLE_DELIMITER;
        } else {
            delimiter = OracleReplacer.ORACLE_DELIMITER;
        }

        return Arrays.asList(new String[] { instruction, delimiter });
    }
}
