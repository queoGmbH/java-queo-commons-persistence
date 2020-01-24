package com.queomedia.persistence.schema.statmentpostprocessor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.queomedia.commons.checks.Check;

public class OracleCreateIfNotExistStatmentPostProcessor implements StatementPostProcessor {

    /**
     * Oracle error code for a create sequence like {@code create sequence mysequence} if it already exists.
     */
    private static final int CREATE_SEQUENCE_EXIST_ERROR_CODE = -955;

    /**
     * Oracle error code for a table create statement like {@code create table mytable(...)} if it already exists.
     */
    private static final int CREATE_TABLE_EXIST_ERROR_CODE = -955;

    /**
     * Oracle error code for alter table add foreign key constraint like
     * {@code alter table myTable add constraint FKmy foreign key (REV) references REVINFO}
     * if it already exists.
     */
    private static final int ALTER_TABLE_FK_CONSTRAINT_EXIST_ERROR_CODE = -2275;

    /**
     * Oracle error code for alter table add unique constraint like:
     * {@code alter table myTable add constraint UK_acmtkmfxy9b50uysodtdnmmxi unique (businessId)}
     * if it already exists.
     */
    private static final int ALTER_TABLE_UNIQUE_CONSTRAINT_EXIST_ERROR_CODE = -2261;

    private final CreateIfNotExistTableFilter createIfNotExistTableFilter;

    private final CreateIfNotExistSequenceFilter createIfNotExistSequenceFilter;

    private final CreateIfNotExistConstraintFilter createIfNotExistConstraintFilter;

    public OracleCreateIfNotExistStatmentPostProcessor(final CreateIfNotExistTableFilter createIfNotExistTableFilter,
            final CreateIfNotExistSequenceFilter createIfNotExistSequenceFilter,
            final CreateIfNotExistConstraintFilter createIfNotExistConstraintFilter) {
        this.createIfNotExistTableFilter = createIfNotExistTableFilter;
        this.createIfNotExistSequenceFilter = createIfNotExistSequenceFilter;
        this.createIfNotExistConstraintFilter = createIfNotExistConstraintFilter;
    }

    public OracleCreateIfNotExistStatmentPostProcessor(final CreateIfNotExistsFilter createIfNotExistsFilter) {
        this.createIfNotExistTableFilter = createIfNotExistsFilter;
        this.createIfNotExistSequenceFilter = createIfNotExistsFilter;
        this.createIfNotExistConstraintFilter = createIfNotExistsFilter;
    }

    public OracleCreateIfNotExistStatmentPostProcessor() {
        this(CreateIfNotExistForAll.INSTANCE);
    }

    @Override
    public List<String> postProcess(final String sqlStatment) {
        return Arrays.asList(postProcessSingle(sqlStatment));
    }

    public String postProcessSingle(final String sqlStatment) {
        String normalizedStatment = sqlStatment.trim().toLowerCase(Locale.ENGLISH);

        if (normalizedStatment.isEmpty()) {
            return sqlStatment;
        }
        if (normalizedStatment.startsWith("--")) {
            return sqlStatment;
        }
        if (normalizedStatment.startsWith("begin execute immediate")) {
            return sqlStatment;
        }

        if (normalizedStatment.startsWith("create table")
                && this.createIfNotExistTableFilter.isEnabledForCreateTable(sqlStatment)) {
            return executeImmediateAndIgnoreException(sqlStatment, CREATE_TABLE_EXIST_ERROR_CODE);
        }

        if (normalizedStatment.startsWith("create sequence")
                && this.createIfNotExistSequenceFilter.isEnabledForCreateSequcence(sqlStatment)) {
            return executeImmediateAndIgnoreException(sqlStatment, CREATE_SEQUENCE_EXIST_ERROR_CODE);
        }

        if (normalizedStatment.startsWith("alter table") && normalizedStatment.contains(" add constraint ")
                && this.createIfNotExistConstraintFilter.isEnabledForCreateConstraint(sqlStatment)) {
            if (normalizedStatment.contains("foreign key")) {
                return executeImmediateAndIgnoreException(sqlStatment, ALTER_TABLE_FK_CONSTRAINT_EXIST_ERROR_CODE);
            }
            if (isAlterTableAddUniqueConstraintStatement(normalizedStatment)) {
                return executeImmediateAndIgnoreException(sqlStatment, ALTER_TABLE_UNIQUE_CONSTRAINT_EXIST_ERROR_CODE);
            }
            return sqlStatment;
        }

        return sqlStatment;
    }

    @FunctionalInterface
    public interface CreateIfNotExistTableFilter {
        boolean isEnabledForCreateTable(String sql);
    }

    @FunctionalInterface
    public interface CreateIfNotExistSequenceFilter {
        boolean isEnabledForCreateSequcence(String sql);
    }

    @FunctionalInterface
    public interface CreateIfNotExistConstraintFilter {
        boolean isEnabledForCreateConstraint(String sql);
    }

    public interface CreateIfNotExistsFilter
            extends CreateIfNotExistTableFilter, CreateIfNotExistSequenceFilter, CreateIfNotExistConstraintFilter {
    }

    public static class CreateIfNotExistForAll implements CreateIfNotExistsFilter {

        public static CreateIfNotExistForAll INSTANCE = new CreateIfNotExistForAll();

        @Override
        public boolean isEnabledForCreateTable(final String sql) {
            return true;
        }

        @Override
        public boolean isEnabledForCreateConstraint(final String sql) {
            return true;
        }

        @Override
        public boolean isEnabledForCreateSequcence(final String sql) {
            return true;
        }
    }

    /**
     * Build an execute immediate statement that catch and ignore the given exception.
     *
     * @param sqlStatement the sql statement that should been executed
     * @param errorCode the error code
     * @return the complete statment
     */
    private String executeImmediateAndIgnoreException(String sqlStatement, int errorCode) {
        Check.notNullArgument(sqlStatement, "sqlStatement");

        return "begin execute immediate '" + sqlStatement + "'; exception when others then if sqlcode != " + errorCode
                + " then raise; end if; end;";
    }

    static boolean isAlterTableAddUniqueConstraintStatement(final String sqlStatement) {
        Check.notNullArgument(sqlStatement, "sqlStatement");

        Pattern pattern = Pattern.compile("alter\\s+table\\s+.*\\s+add\\s+constraint\\s+.*\\s+unique\\s*\\(.*",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sqlStatement.trim());
        return matcher.matches();
    }

}
