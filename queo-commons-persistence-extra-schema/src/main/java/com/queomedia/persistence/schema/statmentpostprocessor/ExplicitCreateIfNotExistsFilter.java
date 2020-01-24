package com.queomedia.persistence.schema.statmentpostprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.queomedia.persistence.schema.statmentpostprocessor.OracleCreateIfNotExistStatmentPostProcessor.CreateIfNotExistsFilter;

/**
 * Filter that matches if the table name or sequence name matches.
 */
public class ExplicitCreateIfNotExistsFilter implements CreateIfNotExistsFilter {

    private final List<String> matchingTables;

    private final List<String> matchinSequences;

    public ExplicitCreateIfNotExistsFilter(final List<String> matchingTables, final List<String> matchinSequences) {
        this.matchingTables = new ArrayList<String>(matchingTables);
        this.matchinSequences = new ArrayList<String>(matchinSequences);
    }

    public ExplicitCreateIfNotExistsFilter() {
        this(Collections.emptyList(), Collections.emptyList());
    }

    public ExplicitCreateIfNotExistsFilter addTable(final String table) {
        this.matchingTables.add(table);

        return this;
    }

    public ExplicitCreateIfNotExistsFilter addSequence(final String sequence) {
        this.matchinSequences.add(sequence);

        return this;
    }

    @Override
    public boolean isEnabledForCreateTable(final String sql) {
        if (this.matchingTables.isEmpty()) {
            return false;
        }

        return containsCaseInsentitive(this.matchingTables, getTableFromCreateTableStatement(sql));
    }

    @Override
    public boolean isEnabledForCreateSequcence(final String sql) {
        if (this.matchinSequences.isEmpty()) {
            return false;
        }

        return containsCaseInsentitive(this.matchinSequences, getTableFromCreateSequcence(sql));
    }

    @Override
    public boolean isEnabledForCreateConstraint(final String sql) {
        if (this.matchingTables.isEmpty()) {
            return false;
        }

        return containsCaseInsentitive(this.matchingTables, getTableFromAlterTableStatement(sql));
    }

    /**
     * Regular expression pattern that match an "create table (NAME) (..." expression, and
     * "return" the table name in the first matcher group.
     */
    private final Pattern CREATE_TABLE_PATTERN = Pattern.compile("^create\\s+table\\s+([^\\s\\(]*?)\\s*\\(.*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    String getTableFromCreateTableStatement(final String sql) {

        Matcher matcher = CREATE_TABLE_PATTERN.matcher(sql.trim());
        if (!matcher.matches()) {
            throw new RuntimeException("error while parsing sql ddl command `" + sql
                    + "`, expected a statement like `create table <tablename> (...)...");
        }
        return matcher.group(1);
    }

    private String getTableFromCreateSequcence(final String sql) {
        String[] parts = StringUtils.split(sql.trim());
        if (parts.length < 2) {
            throw new RuntimeException("error while parsing sql ddl command `" + sql
                    + "`, expected a statement like `create sequence <tableName> [start...]`");
        }
        if (!parts[0].equalsIgnoreCase("create") || !parts[1].equalsIgnoreCase("sequence")) {
            throw new RuntimeException("error while parsing sql ddl command `" + sql
                    + "`, expected a statement like `create sequence <tableName> [start...]` 1th word must be create, 2th word must be sequence");
        }
        if ((parts.length > 3) && !parts[3].equalsIgnoreCase("start")) {
            throw new RuntimeException("error while parsing sql ddl command `" + sql
                    + "`, expected a statement like `create sequence <tableName> start...` 4th word must be start");
        }
        return parts[2];
    }

    private String getTableFromAlterTableStatement(final String sql) {
        String[] parts = StringUtils.split(sql.trim());
        if (parts.length < 4) {
            throw new RuntimeException("error while parsing sql ddl command `" + sql
                    + "`, expected a statement like `alter table <tableName> add ...`");
        }
        if (!parts[0].equalsIgnoreCase("alter") || !parts[1].equalsIgnoreCase("table")
                || !parts[3].equalsIgnoreCase("add")) {
            throw new RuntimeException("error while parsing sql ddl command `" + sql
                    + "`, expected a statement like `alter table <tableName> add ...` 1th word must be create, 2th word must be sequence, 4th word must be add");
        }
        return parts[2];
    }

    private boolean containsCaseInsentitive(final Collection<String> a, final String b) {
        return a.stream().anyMatch(b::equalsIgnoreCase);
    }

}
