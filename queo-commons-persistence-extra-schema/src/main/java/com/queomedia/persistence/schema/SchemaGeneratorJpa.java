package com.queomedia.persistence.schema;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.Persistence;

import org.apache.commons.io.FileUtils;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.schema.prettyprint.MySqlPrettyPrinter;

/**
 * JPA 2.1 based shema generator.
 */
public class SchemaGeneratorJpa {

    /**
     * The file name of the generated dll.
     */
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
    public SchemaGeneratorJpa(
            final String ddlFileName, final Dialect dialect) throws Exception {
        Check.notNullArgument(dialect, "dialect");

        this.dialect = dialect;
        this.ddlFileName = ddlFileName;
    }

    public void generate(String persistenceUntitName) {
        generate(persistenceUntitName, "src/main/resources/" + this.ddlFileName);
    }
    
    /**
     * Method that actually creates the file.
     * @param fileName the file name
     */
    public void generate(final String persistenceUntitName, final String fileName) {

        List<String> statements = generateSpringJpa21way(persistenceUntitName);        
        statements = addSeperator(statements);
        statements = addCommentToDropConstraintStatement(statements);
        
        MySqlPrettyPrinter mySqlPrettyPrinter = new MySqlPrettyPrinter();        
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

        AdditionalScript additionalScript = AdditionalScript.load(dialect);
        String extendedScript = additionalScript.getPrePart() + formattedStatements.toString()
                + additionalScript.getPostPart();
        saveScript(fileName, extendedScript);
    }

    /** Generate a Script in the JPA 2.1 way. */
    private List<String> generateSpringJpa21way(final String persistenceUntitName) {
        StringWriter stringWriter = new StringWriter();
        try {
            Map<String, Object> props = new HashMap<String, Object>();

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

    private List<String> addSeperator(List<String> statements) {
        ArrayList<String> withSeparator = new ArrayList<String>(statements.size());
        for (String statement : statements) {
            withSeparator.add(statement + ";");
        }
        return withSeparator;
    }

    private void saveScript(final String fileName, String content) {
        try {
            FileUtils.writeStringToFile(new File(fileName), content, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException("could not create file `" + fileName + "`", e);
        }
    }


      
    private Pattern dropKeyStatementPattern = Pattern.compile("alter table .* drop foreign key [^;]*;");

    String addCommentToDropConstraintStatement(final String statement) {
        if (dropKeyStatementPattern.matcher(statement).matches()) {
            return "-- " + statement;
        } else {
            return statement;
        }
    }
    
    List<String> addCommentToDropConstraintStatement(final List<String> statements) {
        List<String> result = new ArrayList<String>(statements.size());        
        for(String statement : statements) {
            result.add(addCommentToDropConstraintStatement(statement));
        }
        return result;
    }
}
