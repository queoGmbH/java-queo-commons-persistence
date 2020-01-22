package com.queomedia.persistence.schema.statmentpostprocessor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OracleCreateIfNotExistStatmentPostProcessor implements StatementPostProcessor {

    private static final String WRAP_START = "begin execute immediate '";

    private static final String WRAP_END = "'; exception when others then if sqlcode != -955 then raise; end if; end;";

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
            return WRAP_START + sqlStatment + WRAP_END;
        }

        if (normalizedStatment.startsWith("create sequence")
                && this.createIfNotExistSequenceFilter.isEnabledForCreateSequcence(sqlStatment)) {
            return WRAP_START + sqlStatment + WRAP_END;
        }

        if (normalizedStatment.startsWith("alter table") && normalizedStatment.contains(" add constraint ")
                && this.createIfNotExistConstraintFilter.isEnabledForCreateConstraint(sqlStatment)) {
            return WRAP_START + sqlStatment + WRAP_END;
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

}
