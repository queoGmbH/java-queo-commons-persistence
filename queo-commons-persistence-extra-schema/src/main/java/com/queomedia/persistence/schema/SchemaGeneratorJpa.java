package com.queomedia.persistence.schema;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.beanvalidation.BeanValidation20IntegratorProvider;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.PackageInternalInvoker;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.schema.prettyprint.SqlPrettyPrinter;

/**
 * JPA 2.1 based schema generator.
 */
public class SchemaGeneratorJpa {

    /**
     * The file name of the generated ddl.
     *
     * @deprecated Deprecated, because a less stateful way is to initialize the Schema-Generator just with the dialect an other settings,
     * and then given them the Filename or Steam with the generate method.
     *
     * Can be null
     */
    @Deprecated
    public final String ddlFileName;

    /** The dialect. */
    private final Dialect dialect;

    /**
     * Instantiates a new schema generator.
     *
     * @param dialect the dialect
     */
    public SchemaGeneratorJpa(final Dialect dialect) {
        Check.notNullArgument(dialect, "dialect");

        this.dialect = dialect;
        this.ddlFileName = null;
    }

    /**
     * Instantiates a new schema generator.
     *
     * @param ddlFileName The file name of the generated dll.
     * @param dialect the dialect
     */
    @Deprecated
    public SchemaGeneratorJpa(final String ddlFileName, final Dialect dialect) {
        Check.notNullArgument(dialect, "dialect");

        this.dialect = dialect;
        this.ddlFileName = ddlFileName;
    }

    /**
     * Deprecated!
     *
     * @param persistenceUnitName the persistence unit name
     * @deprecated use {@link #generate(String, String)}  instead:
     * {@code generateDdl(persistenceUnitName, "src/main/resources/" + ddlFileName);}
     */
    @Deprecated
    public void generate(final String persistenceUnitName) {
        if (this.ddlFileName == null) {
            throw new IllegalStateException(
                    "use method SchemaGeneratorJpa.generate(final String persistenceUnitName, final String fileName) instead ");
        }
        generateDdlFile(persistenceUnitName, "src/main/resources/" + this.ddlFileName);
    }

    /**
     * Deprecated!
     *
     * @param persistenceUnitName the persistence unit name
     * @param fileName the file name
     * @deprecated use {@link #generateDdlFile(String, String)} - it is just renamed
     */
    @Deprecated
    public void generate(final String persistenceUnitName, final String fileName) {
        generateDdlFile(persistenceUnitName, fileName);
    }

    /**
     * Generate the DDL Script File.
     *
     * @param persistenceUnitName the persistence untit name
     * @param fileName the file name where to store the generated ddl script
    */
    public void generateDdlFile(final String persistenceUnitName, final String fileName) {

        //TODO: use try-with-resource after update to java7
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
            writer.write(generateDdlScript(persistenceUnitName));
        } catch (IOException ex) {
            throw new RuntimeException("error while writing ddl script - filename=`" + fileName + "`");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("error while writing ddl script - filename=`" + fileName + "`");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
                throw new RuntimeException(
                        "error in generate ddl script while closing file - filename=`" + fileName + "`");
            }
        }
    }

    /**
     * Generate the ddl Script.
     *
     * @param persistenceUnitName the persistence untit name
     * @return the ddl script
     * @throws NoSuchAlgorithmException thrown if the hash algorithm couldn't resolve the md5 instance
     */
    public String generateDdlScript(final String persistenceUnitName) throws NoSuchAlgorithmException {

        final List<String> statements = generateCoreStatementsJpa21way(persistenceUnitName);
        String primaryScript = postProcessStatements(statements);

        AdditionalScript additionalScript = AdditionalScript.load(this.dialect);
        String extendedScript = additionalScript.getPrePart() + primaryScript + additionalScript.getPostPart();

        return extendedScript;
    }

    /** Generate a Script in the JPA 2.1 way. */
    List<String> generateCoreStatementsJpa21way(final String persistenceUntitName) {
        StringWriter stringWriter = new StringWriter();
        try {
            Map<String, Object> props = buildPersistenceProperties(stringWriter);

            if (Version.hibernateVersion().isGreatherOrEqualsThan(new Version(5, 1, 0))) {
                hiberante51GenerateSchemaWorkarround(persistenceUntitName, props);
            } else {
                Persistence.generateSchema(persistenceUntitName, props);
            }

        } finally {
            if (stringWriter != null) {
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return splitJpaScriptInStatments(stringWriter.toString());
    }

    private Map<String, Object> buildPersistenceProperties(final StringWriter stringWriter) {
        Map<String, Object> props = new HashMap<String, Object>();

        /** need to disable validation, else the database connection would be used to validate (or update) the existing DB schema */
        props.put(AvailableSettings.HBM2DDL_AUTO, "none");

        props.put("javax.persistence.schema-generation.database.action", "none");
        props.put("javax.persistence.schema-generation.scripts.action", "drop-and-create");
        //            props.put("javax.persistence.schema-generation.scripts.action", "create-drop");

        //            koennte verwendet werden um das Script einzupflecten
        //            props.put("javax.persistence.schema-generation.drop-source", "script-then-metadata");
        //            props.put("javax.persistence.schema-generation.create-source", "metadata-then-script");
        //            javax.persistence.schema-generation.scripts.create-script-source
        //            javax.persistence.schema-generation.scripts.drop-script-source

        /*
         * Needed if scripts are to be generated and no connection to target database. Values are those obtained
         * from JDBC DatabaseMetaData.
         */
        //            props.put("javax.persistence.database-product-name", "MySql");
        //            props.put("javax.persistence.database-major-version", "5");
        //            props.put("javax.persistence.database-minor-version", "1");

        props.put("javax.persistence.schema-generation-connection", new DummyConnection());

        props.put("javax.persistence.schema-generation.scripts.drop-target", stringWriter);
        props.put("javax.persistence.schema-generation.scripts.create-target", stringWriter);

        //            HashSet<ValidationMode> validationModes = new HashSet<>();
        //            validationModes.add(ValidationMode.DDL);
        //            props.put("javax.persistence.validation.mode", validationModes);

        //        props.put("hibernate.hbm2ddl.auto", "create-drop");
        //        props.put("javax.persistence.validation.mode", "ddl");
        //        props.put("hibernate.validator.apply_to_ddl", "true");
        
        /** Activate pachted TypeSaveActivator that respect BeanValidation 2.0 NotEmpty and NotBlank annotations. */
        props.put("javax.persistence.validation.mode", "NONE");
        props.put("javax.persistence.validation20.mode", "AUTO");
        BeanValidation20IntegratorProvider.addToConfiguration(props);
        
        return props;
    }

    /**
     * Create database schemas and/or tables and/or create DDL scripts as determined by the supplied properties
     * <b>
     * This is the workaround to make the Statement: {@code Persistence.generateSchema(persistenceUntitName, props)}
     * work in Hibernate 5.1.0 - 5.2.12 and maybe onwards.
     * </b>
     *
     * <p>
     * The problem is that in {@link EntityManagerFactoryBuilderImpl} since version 5.1 within the method "generate".
     * misses the statement "sfBuilder.build()"  (it was there in 5.0.12 but it was removed in 5.1).
     * Therefore the Integrators (BeanValidatorIntegrator) is not
     * started anymore, and therefore the Validation-Annotations are not considered for the schema generation.
     * </p>
     * 
     * <p>
     * The strange thing is, that {@code builder.build()} already start the sql generation process, so
     *  SchemaManagementToolCoordinator.process does not need to be invoked.
     * </p>
     *
     * @param persistenceUnitName the name of the persistence unit
     * @param properties properties for schema generation; these may also contain provider-specific properties. The
     *        values of these properties override any values that may have been configured elsewhere.
     */
    private void hiberante51GenerateSchemaWorkarround(final String persistenceUntitName,
            final Map<String, Object> props) {
        Check.notNullArgument(persistenceUntitName, "persistenceUntitName");
        Check.notNullArgument(props, "props");

        EntityManagerFactoryBuilderImpl builder = getEntityManagerFactoryBuilder(persistenceUntitName, props);
       
        builder.build();
        /*
         * builder.build just triggers the schema generation, so the next lines are not needed.
         */
        //        ServiceRegistry serviceRegistry = reflectStandardServiceRegistryField(builder);
        //
        //                    SchemaManagementToolCoordinator.process(builder.getMetadata(),
        //                            serviceRegistry,
        //                            builder.getConfigurationValues(),
        //                            DelayedDropRegistryNotAvailableImpl.INSTANCE);
    }

    private EntityManagerFactoryBuilderImpl getEntityManagerFactoryBuilder(final String persistenceUntitName,
            final Map<String, Object> props) {
        for (PersistenceProvider provider : PersistenceProviderResolverHolder.getPersistenceProviderResolver()
                .getPersistenceProviders()) {
            HibernatePersistenceProvider hbpProvider = (HibernatePersistenceProvider) provider;            

            return (EntityManagerFactoryBuilderImpl) PackageInternalInvoker
                    .getEntityManagerFactoryBuilderOrNull(hbpProvider, persistenceUntitName, props);
        }

        throw new RuntimeException("Could not obtain matching EntityManagerFactoryBuilder with persistance unit name: `"
                + persistenceUntitName + "`");
    }

    //    /**
    //     * Read the private field "standardServiceRegistry" of class {@link EntityManagerFactoryBuilderImpl}.
    //     *
    //     * @param builder the builder
    //     * @return the field value
    //     */
    //    private ServiceRegistry reflectStandardServiceRegistryField(final EntityManagerFactoryBuilderImpl builder) {
    //        try {
    //            Field ssRegistryField = EntityManagerFactoryBuilderImpl.class.getDeclaredField("standardServiceRegistry");
    //            ssRegistryField.setAccessible(true);
    //            ServiceRegistry serviceRegistry = (ServiceRegistry) ssRegistryField.get(builder);
    //            return serviceRegistry;
    //        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
    //            throw new RuntimeException("could not read field standardServiceRegistry from " + builder, e);
    //        }
    //    }

    /**
     * Post process the generated statements in a way that is database depentend.
     * @param statements
     * @return a script that contains that statements
     * @throws NoSuchAlgorithmException
     */
    String postProcessStatements(List<String> statements) throws NoSuchAlgorithmException {
        if (this.dialect == Dialect.MYSQL) {
            statements = addCommentToDropConstraintStatementMySql(statements);
        }
        if (this.dialect == Dialect.ORACLE) {
            statements = addCatchExceptionAroundDropTableStatementOracle(statements);
            statements = addCatchExceptionAroundDropSequenceStatementOracle(statements);
        }

        if (this.dialect == Dialect.MYSQL) {
            statements = addSeperator(statements, ";");
        }
        if (this.dialect == Dialect.ORACLE) {
            statements = addSeperator(statements, "\n/\n");
        }
        if (this.dialect == Dialect.SQL_SERVER_2012) {
            statements = addConditionToDropConstraintStatementSqlServer2012(statements);
            statements = addConditionToDropTableStatementSqlServer2012(statements);
            statements = dropCheckConstraintStatementsAndAddWithConstraintNameSqlServer2012(statements);
            statements = addSeperator(statements, ";");
        }

        SqlPrettyPrinter mySqlPrettyPrinter = new SqlPrettyPrinter();
        List<List<String>> groupedStatments = mySqlPrettyPrinter.groupStatments(statements);

        StringBuilder formattedStatements = new StringBuilder();
        for (List<String> group : groupedStatments) {
            for (String statement : group) {
                formattedStatements.append(mySqlPrettyPrinter.formatLineStatement(statement));
                formattedStatements.append("\n");
            }
            //empty line between the groupes
            formattedStatements.append("\n");
        }
        return formattedStatements.toString();
    }

    /**
     * Split the Script in statments,
     * This is relative easy, because one line is one statement.
     */
    private List<String> splitJpaScriptInStatments(final String rawScript) {
        return Arrays.asList(rawScript.split("\\r?\\n"));
    }

    private List<String> addSeperator(final List<String> statements, final String seperator) {
        ArrayList<String> withSeparator = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            withSeparator.add(statement + seperator);
        }
        return withSeparator;
    }

    private Pattern dropKeyStatementPattern = Pattern.compile("alter table \\S* drop foreign key \\S*");

    List<String> addCommentToDropConstraintStatementMySql(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (this.dropKeyStatementPattern.matcher(statement).matches()) {
                result.add("-- " + statement);
            } else {
                result.add(statement);
            }
        }
        return result;
    }

    private Pattern dropTableStatementPattern = Pattern.compile("drop table \\S* cascade constraints");

    List<String> addCatchExceptionAroundDropTableStatementOracle(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (this.dropTableStatementPattern.matcher(statement).matches()) {
                result.add("begin execute immediate '" + statement
                        + "'; exception when others then if sqlcode != -942 then raise; end if; end;");
            } else {
                result.add(statement);
            }
        }
        return result;
    }

    private Pattern dropSequenceStatementPattern = Pattern.compile("drop sequence \\S*");

    List<String> addCatchExceptionAroundDropSequenceStatementOracle(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (this.dropSequenceStatementPattern.matcher(statement).matches()) {
                result.add("begin execute immediate '" + statement
                        + "'; exception when others then if sqlcode != -2289 then raise; end if; end;");
            } else {
                result.add(statement);
            }
        }
        return result;
    }

    private static Pattern SQL_SERVER_DROP_CONSTRAINT_STATEMENT_PATTERN = Pattern
            .compile("alter table \\S* drop constraint \\S*");

    /**
     * Build the drop constraint statements.
     * Surround with existence check for the SQL Server.
     *
     * @param statements statements to manipulate
     * @return statements to execute
     */
    private List<String> addConditionToDropConstraintStatementSqlServer2012(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (SQL_SERVER_DROP_CONSTRAINT_STATEMENT_PATTERN.matcher(statement).matches()) {
                String[] statementParts = statement.split(" ");
                String constraintName = statementParts[5];
                result.add(String.format("IF (OBJECT_ID('%s', 'F') IS NOT NULL)\n" + "  BEGIN\n" + "    %s\n" + "  END",
                        constraintName,
                        statement));
            } else {
                result.add(statement);
            }
        }
        return result;
    }

    private static Pattern SQL_SERVER_DROP_TABLE_STATEMENT_PATTERN = Pattern.compile("drop table \\S*");

    /**
     * Build the drop table statements.
     * Surround with existence check for the SQL Server.
     *
     * @param statements statements to manipulate
     * @return statements to execute
     */
    private List<String> addConditionToDropTableStatementSqlServer2012(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (SQL_SERVER_DROP_TABLE_STATEMENT_PATTERN.matcher(statement).matches()) {
                result.add(
                        String.format("IF OBJECT_ID('%s', 'U') IS NOT NULL\n\t", statement.split(" ")[2]) + statement);
            } else {
                result.add(statement);
            }
        }
        return result;
    }

    /**
     * Pattern to find a check constraint in a statement.
     */
    private static Pattern SQL_SERVER_CHECK_CONSTRAINT_STATEMENT_PATTERN = Pattern.compile("check");

    /**
     * Drop the old check constraints and add new constraint with unique constraint name.
     * f.e. minValue int not null check (minValue>=1) gets to:
     *      minValue int not null,
     *      constraint chk_NAME check (minValue>=1)
     *
     * Should be done in MSSQL_SERVER because with no name the check constraint has a different
     * name in each database which is a real pain if you have to drop a constrainted column.
     *
     * @param statements all ddl statements
     * @return ddl script with refined check constraint naming
     * @throws NoSuchAlgorithmException thrown if the hash algorithm couldn't resolve the md5 instance
     */
    private List<String> dropCheckConstraintStatementsAndAddWithConstraintNameSqlServer2012(final List<String> statements)
            throws NoSuchAlgorithmException {
        List<String> result = new ArrayList<String>(statements.size());

        for (String statement : statements) {
            if (SQL_SERVER_CHECK_CONSTRAINT_STATEMENT_PATTERN.matcher(statement).find()) {
                result.add(replaceCheckConstraintColumns(statement));
            } else {
                result.add(statement);
            }
        }

        return result;
    }

    /**
     * Replaces all check constraint columns out of the given statement.
     *
     * @param statement the statement with the check constraint columns
     * @return complete statement with replaced check constraint columns
     * @throws NoSuchAlgorithmException thrown if the hash algorithm couldn't resolve the md5 instance
     */
    private String replaceCheckConstraintColumns(final String statement) throws NoSuchAlgorithmException {
        StringBuilder builder = new StringBuilder();
        String tableCreationStatement = parseTableCreationStatement(statement);
        builder.append(tableCreationStatement);

        String[] columns = parseTableColumns(statement);

        List<String> refinedColumns = new ArrayList<String>();

        for (String column : columns) {
            if (SQL_SERVER_CHECK_CONSTRAINT_STATEMENT_PATTERN.matcher(column).find()) {
                String[] columnParts = column.split("check");

                // add the statement before the check constraint
                refinedColumns.add(StringUtils.normalizeSpace(columnParts[0]));

                // add the constraint with a unique name
                String checkConstraintValue = StringUtils.normalizeSpace(columnParts[1]);
                refinedColumns.add("CONSTRAINT chk_" + generateCheckConstraintName(tableCreationStatement, column)
                        + " CHECK" + checkConstraintValue);
            } else {
                refinedColumns.add(column);
            }
        }

        builder.append(StringUtils.join(refinedColumns.toArray(), ","));
        builder.append(parseTableEndStatement(statement));
        return builder.toString();
    }

    /**
     * Parses the tables end statement starting with the closing bracket.
     *
     * @param statement the complete creation table statement
     * @return return the substring after the last closing bracket
     */
    private String parseTableEndStatement(final String statement) {
        return statement.substring(statement.lastIndexOf(")"));
    }

    /**
     * Parses the table columns between the creation table brackets by splitting at each comma.
     *
     * @param statement the complete creation table statement
     * @return the collection of all table columns
     */
    private String[] parseTableColumns(final String statement) {
        return statement.substring(statement.indexOf("(") + 1, statement.lastIndexOf(")")).split(",");
    }

    /**
     * Parses the "create table NAME (" out of the given statement.
     *
     * @param statement the statement
     * @return String of the creation statement
     */
    private String parseTableCreationStatement(final String statement) {
        return statement.substring(0, statement.indexOf("(") + 1);
    }

    /**
     * Generate the used constraint name for the given row out of the table creation statement + the check constraint row.
     *
     * @param tableCreationStatement the creation statement
     * @param rowStatement the check constraint row statement
     * @return hashed constraint name
     * @throws NoSuchAlgorithmException thrown if the hash algorithm couldn't resolve the md5 instance
     */
    private String generateCheckConstraintName(final String tableCreationStatement, final String rowStatement)
            throws NoSuchAlgorithmException {
        String normalizedRowStatement = StringUtils.normalizeSpace(rowStatement);
        String normalizedTableCreationStatement = StringUtils.normalizeSpace(tableCreationStatement);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(normalizedRowStatement.getBytes());
        md.update(normalizedTableCreationStatement.getBytes());

        byte[] bytes = md.digest();

        return convertBytesArrayToHex(bytes);
    }

    /**
     * Convert the bytes[] into hex format.
     *
     * @param bytes the bytes array to format
     * @return hex string of the bytes array
     */
    private String convertBytesArrayToHex(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
