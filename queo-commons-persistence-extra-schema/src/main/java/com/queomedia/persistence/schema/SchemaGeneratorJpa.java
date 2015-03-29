package com.queomedia.persistence.schema;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.Persistence;

import org.hibernate.cfg.AvailableSettings;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.schema.prettyprint.SqlPrettyPrinter;

/**
 * JPA 2.1 based schema generator.
 */
public class SchemaGeneratorJpa {

    /**
     * The file name of the generated ddl.
     * 
     * @deprecated Deprecated, because a less stateful way is to initialize the Schema-Gegerator just with the dialect an other settings,
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
     * @param packageNames the package names
     * @param namingStrategyOrNull the naming strategy, or null if none used
     * @param ddlFileName The file name of the generated dll.
     * @param dialect the dialect
     * @throws Exception the exception
     */
    public SchemaGeneratorJpa(final Dialect dialect) throws Exception {
        Check.notNullArgument(dialect, "dialect");

        this.dialect = dialect;
        this.ddlFileName = null;
    }

    /**
     * Instantiates a new schema generator.
     *
     * @param packageNames the package names
     * @param namingStrategyOrNull the naming strategy, or null if none used
     * @param ddlFileName The file name of the generated dll.
     * @param dialect the dialect
     * @throws Exception the exception
     */
    @Deprecated
    public SchemaGeneratorJpa(final String ddlFileName, final Dialect dialect) throws Exception {
        Check.notNullArgument(dialect, "dialect");

        this.dialect = dialect;
        this.ddlFileName = ddlFileName;
    }

    /**
     * @deprecated use {@link #generate(String, String)}  instead:
     * {@code generateDdl(persistenceUntitName, "src/main/resources/" + ddlFileName);}  
     */
    @Deprecated
    public void generate(String persistenceUntitName) {
        if (ddlFileName == null) {
            throw new IllegalStateException("use method SchemaGeneratorJpa.generate(final String persistenceUntitName, final String fileName) instead ");
        }
        generateDdlFile(persistenceUntitName, "src/main/resources/" + this.ddlFileName);
    }

    /**
     * @Deprecated use {@link #generateDdlFile(String, String)} - it is just renamed
     */
    @Deprecated
    public void generate(final String persistenceUntitName, final String fileName) {
        generateDdlFile(persistenceUntitName, fileName);
    }

    /**
     * Generate the DDL Script File.
     *
     * @param persistenceUntitName the persistence untit name
     * @param fileName the file name where to store the generated ddl script
    */
    public void generateDdlFile(final String persistenceUntitName, final String fileName) {

        //TODO: use try-with-resource after update to java7
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
            writer.write(generateDdlScript(persistenceUntitName));
        } catch (IOException ex) {
            throw new RuntimeException("error while writing ddl script - filename=`" + fileName + "`");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
                throw new RuntimeException("error in generate ddl script while closing file - filename=`" + fileName
                        + "`");
            }
        }
    }

    /**
     * Generate the ddl Script.
     *
     * @param persistenceUntitName the persistence untit name
     * @param fileName the file name where to store the generated ddl script
    */
    public String generateDdlScript(final String persistenceUntitName) {

        List<String> statements = generateSpringJpa21way(persistenceUntitName);
        String primaryScript = postProcessStatements(statements);

        AdditionalScript additionalScript = AdditionalScript.load(dialect);
        String extendedScript = additionalScript.getPrePart() + primaryScript + additionalScript.getPostPart();

        return extendedScript;
    }

    String postProcessStatements(List<String> statements) {
        if (this.dialect == Dialect.MYSQL) {
            statements = addCommentToDropConstraintStatement(statements);
        }
        if (this.dialect == Dialect.ORACLE) {
            statements = addCatchExceptionAroundDropTableStatement(statements);
            statements = addCatchExceptionAroundDropSequenceStatement(statements);
        }

        if (this.dialect == Dialect.MYSQL) {
            statements = addSeperator(statements, ";");
        }
        if (this.dialect == Dialect.ORACLE) {
            statements = addSeperator(statements, "\n/\n");
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

    /** Generate a Script in the JPA 2.1 way. */
    private List<String> generateSpringJpa21way(final String persistenceUntitName) {
        StringWriter stringWriter = new StringWriter();
        try {
            Map<String, Object> props = new HashMap<String, Object>();

            /** need to disable validation, else the database connection would be used to validate (or update) the existing DB schema */
            props.put(AvailableSettings.HBM2DDL_AUTO, "none");

            props.put("javax.persistence.schema-generation.database.action", "none");
            props.put("javax.persistence.schema-generation.scripts.action", "drop-and-create");

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

            Persistence.generateSchema(persistenceUntitName, props);
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

    /**
     * Split the Script in statments,
     * This is relative easy, because one line is one statement.
     */
    private List<String> splitJpaScriptInStatments(String rawScript) {
        return Arrays.asList(rawScript.split("\\r?\\n"));
    }

    private List<String> addSeperator(List<String> statements, String seperator) {
        ArrayList<String> withSeparator = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            withSeparator.add(statement + seperator);
        }
        return withSeparator;
    }

    private Pattern dropKeyStatementPattern = Pattern.compile("alter table \\S* drop foreign key \\S*");

    List<String> addCommentToDropConstraintStatement(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (dropKeyStatementPattern.matcher(statement).matches()) {
                result.add("-- " + statement);
            } else {
                result.add(statement);
            }
        }
        return result;
    }

    private Pattern dropTableStatementPattern = Pattern.compile("drop table \\S* cascade constraints");

    List<String> addCatchExceptionAroundDropTableStatement(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (dropTableStatementPattern.matcher(statement).matches()) {
                result.add("begin execute immediate '" + statement
                        + "'; exception when others then if sqlcode != -942 then raise; end if; end;");
            } else {
                result.add(statement);
            }
        }
        return result;
    }

    private Pattern dropSequenceStatementPattern = Pattern.compile("drop sequence \\S*");

    List<String> addCatchExceptionAroundDropSequenceStatement(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            if (dropSequenceStatementPattern.matcher(statement).matches()) {
                result.add("begin execute immediate '" + statement
                        + "'; exception when others then if sqlcode != -2289 then raise; end if; end;");
            } else {
                result.add(statement);
            }
        }
        return result;
    }
}
