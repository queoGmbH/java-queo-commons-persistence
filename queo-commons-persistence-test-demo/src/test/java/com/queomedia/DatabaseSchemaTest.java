package com.queomedia;

import generator.SchemaGeneratorRunner;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import com.queomedia.commons.asserts.AssertUtil;
import com.queomedia.persistence.schema.Dialect;
import com.queomedia.persistence.schema.SchemaGeneratorJpa;

public class DatabaseSchemaTest {

    @Test
    public void testDllSchemaFileMatch() throws Exception {
        File orignalFile = ResourceUtils.getFile("classpath:" + SchemaGeneratorRunner.DDL_FILENAME);
        Assert.assertTrue(orignalFile.exists());

        File temp = File.createTempFile("currentTestDbSchema", ".sql");
        temp.deleteOnExit();

        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(
                "ddl_mysql.sql",
                Dialect.MYSQL);
        gen.generate("persistenceUnit", temp.getAbsolutePath());
        

        // At the moment we can not ensure that the fields in entitys with subclasses are in the same order in hudson and dev system.
        AssertUtil.containsExact(FileUtils.readLines(orignalFile), FileUtils.readLines(temp));
    }
}
