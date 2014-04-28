package com.queomedia.persistence.schema;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.Entity;

import org.apache.commons.io.FileUtils;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.reflections.Reflections;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.schema.bugfix.ConfigurationWithBeanValidation;

/**
 * The Class SchemaGenerator.
 * @deprecated use {@link SchemaGeneratorJpa}.
 */
@Deprecated
public class SchemaGenerator {

    /**
     * The file name of the generated dll.
     */
    public final String ddlFileName;

    /**
     * The configuration.
     */
    private final Configuration cfg;

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
    public SchemaGenerator(final List<String> packageNames, final NamingStrategy namingStrategyOrNull,
            final String ddlFileName, final Dialect dialect) throws Exception {
        Check.notNullArgument(packageNames, "packageNames");
        Check.notNullArgument(dialect, "dialect");

        this.dialect = dialect;

        org.hibernate.dialect.Dialect hiberneteDialect = (org.hibernate.dialect.Dialect) Class
                .forName(dialect.getDialectClass()).newInstance();

        if (beanValidationInClasspath()) {
            this.cfg = new ConfigurationWithBeanValidation(hiberneteDialect);
        } else {
            this.cfg = new Configuration();
        }

        this.cfg.setProperty(AvailableSettings.HBM2DDL_AUTO, "create");
        this.cfg.setProperty("javax.persistence.validation.mode", "ddl");

        if (namingStrategyOrNull != null) {
            this.cfg.setNamingStrategy(namingStrategyOrNull);
        }

        this.ddlFileName = ddlFileName;

        List<Class<?>> classes = getClasses(packageNames);
        Collections.sort(classes, ClassComparator.INSTANCE);
        for (Class<?> clazz : classes) {
            this.cfg.addAnnotatedClass(clazz);
        }

    }

    /**
     * Method that actually creates the file.
     * @param fileName the file name
     */
    public void generate(final String fileName) {

        this.cfg.setProperty("hibernate.dialect", this.dialect.getDialectClass());

        System.out.println("--------------------");

        SchemaExport export = new SchemaExport(this.cfg);
        export.setDelimiter(";");
        export.setOutputFile(fileName);
        export.execute(true, false, false, false);

        try {
            addAdditinalStatements(fileName, this.dialect);
        } catch (IOException e) {
            throw new RuntimeException("error will extending dll with addtional statement", e);
        }

        try {
            addCommentDropConstraintStatements(fileName, this.dialect);
        } catch (IOException e) {
            throw new RuntimeException("error will extending dll by escaping drop foraign key relation ships", e);
        }
    }

    public void generate() {
        generate("src/main/resources/" + this.ddlFileName);
    }

    /**
     * Add the statements from the addtional file.
     *
     * @param fileName the file name
     * @param dialect the dialect
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void addAdditinalStatements(final String fileName, final Dialect dialect) throws IOException {

        AdditionalScript additionalScript = AdditionalScript.load(dialect);

        File outputFile = new File(fileName);
        String original = FileUtils.readFileToString(outputFile, "utf-8");
        String extended = additionalScript.getPrePart() + original + additionalScript.getPostPart();
        FileUtils.writeStringToFile(outputFile, extended);

        System.out.println("----Extended--------");
        System.out.println(extended);
    }


    /**
     * Add the statements from the addtional file.
     *
     * @param fileName the file name
     * @param dialect the dialect
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void addCommentDropConstraintStatements(final String fileName, final Dialect dialect) throws IOException {
        Check.notNullArgument(fileName, "fileName");
        Check.notNullArgument(dialect, "dialect");

        File outputFile = new File(fileName);
        List<String> original = FileUtils.readLines(outputFile, "utf-8");

        final List<String> withCorrectedDropStatements;
        if (dialect == Dialect.ORACLE) {
            withCorrectedDropStatements = OracleReplacer.replace(original);
        } else {
            withCorrectedDropStatements = original;
        }

        String withComment = addCommentDropConstraintStatements(withCorrectedDropStatements, dialect);
        FileUtils.writeStringToFile(outputFile, withComment);

        System.out.println("----With Comment--------");
        System.out.println(withComment);
    }
    
    private Pattern dropKeyStatementPattern = Pattern.compile("alter table .* drop foreign key [^;]*;");
    
    String addCommentDropConstraintStatements(final List<String> original, final Dialect dialect) {
        StringBuilder shirnked = new StringBuilder();

        for (int i = 0; i < original.size(); i++) {
            if ((i + 2) < original.size()) {
                String combined3Lines = original.get(i).trim() + " " + original.get(i + 1).trim() + " "
                        + original.get(i + 2).trim();

                if (dropKeyStatementPattern.matcher(combined3Lines).matches()) {
                    shirnked.append("-- " + combined3Lines + "\n");
                    i += 2; //skip the next two lines
                } else {
                    shirnked.append(original.get(i) + "\n");
                }
            } else {
                shirnked.append(original.get(i) + "\n");
            }

        }
        return shirnked.toString();
    }

    /**
     * Utility method used to fetch Class list based on a package name.
     *
     * @param packageNames (should be the package containing your annotated beans.
     *
     * @return the classes
     *
     * @throws Exception the exception
     */
    private List<Class<?>> getClasses(final List<String> packageNames) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (String packageName : packageNames) {
            System.out.println("scan:" + packageName);

            Reflections reflections = new Reflections(packageName);
            classes.addAll(reflections.getTypesAnnotatedWith(Entity.class));

        }
        return classes;
    }

    /**
     * This filter accepts only java class files.
     */
    public static class ClassFileFilter implements FileFilter {

        /**
         * The holy instance of class file filter.
         */
        public static final ClassFileFilter INSTANCE = new ClassFileFilter();

        @Override
        public boolean accept(final File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".class");
        }
    };

    /**
     * This filter accepts only java class files.
     */
    public static class PackageDirectoryFilter implements FileFilter {

        /**
         * The holy instance of normal directory filter.
         */
        public static final PackageDirectoryFilter INSTANCE = new PackageDirectoryFilter();

        @Override
        public boolean accept(final File pathname) {
            return pathname.isDirectory() && !pathname.isHidden() && !pathname.getName().equalsIgnoreCase(".svn")
                    && !pathname.getName().contains(".");
        }
    };

    /**
     * Check if Bean validation is in classpath.
     *
     * @return true, if NotNull class is found.
     */
    private boolean beanValidationInClasspath() {
        try {
            Class.forName("javax.validation.constraints.NotNull");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
