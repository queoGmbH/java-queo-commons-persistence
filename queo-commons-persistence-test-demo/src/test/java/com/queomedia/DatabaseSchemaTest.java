package com.queomedia;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import com.queomedia.commons.asserts.AssertUtil;
import com.queomedia.persistence.schema.Dialect;
import com.queomedia.persistence.schema.SchemaGeneratorJpa;

import generator.SchemaGeneratorRunner;

public class DatabaseSchemaTest {

    @Test
    public void testDllSchemaFileMatch() throws Exception {
        File orignalFile = ResourceUtils.getFile("classpath:" + SchemaGeneratorRunner.DDL_FILENAME);
        Assert.assertTrue(orignalFile.exists());

        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(Dialect.MYSQL);
        String currentScript = gen.generateDdlScript("persistenceUnit", ";", false);

        List<String> originalLines = FileUtils.readLines(orignalFile);
        List<String> currentScriptLines = Arrays.asList(currentScript.split("\\r?\\n"));

        AssertUtil.containsExact(originalLines, currentScriptLines);
    }

    @Test
    public void testDllSchemaFileMatch_deprecatedStyle() throws Exception {
        File orignalFile = ResourceUtils.getFile("classpath:" + SchemaGeneratorRunner.DDL_FILENAME);
        Assert.assertTrue(orignalFile.exists());

        File temp = File.createTempFile("currentTestDbSchema", ".sql");
        temp.deleteOnExit();

        SchemaGeneratorJpa gen = new SchemaGeneratorJpa("ddl_mysql.sql", Dialect.MYSQL);
        gen.generate("persistenceUnit", temp.getAbsolutePath());

        // At the moment we can not ensure that the fields in entitys with subclasses are in the same order in hudson and dev system.
        AssertUtil.containsExact(FileUtils.readLines(orignalFile), FileUtils.readLines(temp));
    }

    /**
     * A property with NotNull annotation as well as primitives must least to an not null column in the database.
     * 
     * @throws Exception no exception expected
     */
    @Test
    public void testDllSchemaNotEmptyConstraint() throws Exception {
        File orignalFile = ResourceUtils.getFile("classpath:" + SchemaGeneratorRunner.DDL_FILENAME);
        Assert.assertTrue(orignalFile.exists());

        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(Dialect.MYSQL);
        String currentScript = gen.generateDdlScript("persistenceUnit", ";", false);
        List<String> currentScriptLines = Arrays.asList(currentScript.split("\\r?\\n"));

        assertThat(selectBySubstring("notNullObject", currentScriptLines), Matchers.endsWith("not null,"));
        assertThat(selectBySubstring("primitive", currentScriptLines), Matchers.endsWith("integer not null,"));
    }

    /**
     * A property of type String with org.hibernate.validator.constraints.NotEmpty
     * annotation must lead to an not null constraint in the database.
     *
     * @throws Exception no exception expected
     */
    @Test
    public void testDllSchemaNotEmptyConstraint_hibernateValidatorNotEmpty() throws Exception {
        File orignalFile = ResourceUtils.getFile("classpath:" + SchemaGeneratorRunner.DDL_FILENAME);
        Assert.assertTrue(orignalFile.exists());

        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(Dialect.MYSQL);
        String currentScript = gen.generateDdlScript("persistenceUnit", ";", false);
        List<String> currentScriptLines = Arrays.asList(currentScript.split("\\r?\\n"));

        assertThat(selectBySubstring("notEmptyString_hibernateValidator", currentScriptLines),
                Matchers.endsWith("varchar(255) not null,"));
    }

    /**
     * A property of type String with javax.validation.constraints.NotEmpty
     * annotation must lead to an not null constraint in the database.
     * 
     * @throws Exception no exception expected
     */
    @Test
    public void testDllSchemaNotEmptyConstraint_javaxValidationNotEmpty() throws Exception {
        File orignalFile = ResourceUtils.getFile("classpath:" + SchemaGeneratorRunner.DDL_FILENAME);
        Assert.assertTrue(orignalFile.exists());

        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(Dialect.MYSQL);
        String currentScript = gen.generateDdlScript("persistenceUnit", ";", false);
        List<String> currentScriptLines = Arrays.asList(currentScript.split("\\r?\\n"));

        assertThat(selectBySubstring("notEmptyString_javaxValidation", currentScriptLines),
                Matchers.endsWith("varchar(255) not null,"));
    }

    private String selectBySubstring(String substring, Collection<String> from) {
        for (String line : from) {
            if (line.contains(substring)) {
                return line;
            }
        }
        throw new RuntimeException("there was no string that contains the substring `" + substring
                + "` in the collection of strings: " + from);
    }
}
