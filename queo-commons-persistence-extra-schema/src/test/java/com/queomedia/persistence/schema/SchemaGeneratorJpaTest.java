package com.queomedia.persistence.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class SchemaGeneratorJpaTest {

    /**
     *  Test that JSR-303 Annotations are taken in account, even on embeddeds.
     *
     * @throws Exception - no exception expected
     */
    @Test
    public void testGenerateDdlScript() throws Exception {

        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.MYSQL);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit", ";", false);

        /* The test are the varchar(100) columns, because the 100 is set via JSR303: @Size(max=100) */
        String expectedScript = ""//
                + "id integer not null,        " //
                + "embeddedString varchar(100)," //
                + "normalString varchar(100),  " //
                + "primary key (id)            "; //

        Pattern pattern = Pattern.compile(normalize("create table DemoEntity \\((.+?)\\) ENGINE=InnoDB"), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(normalize(generateDdlScript));
        boolean found = matcher.find();        
        assertTrue("not statement to create table DemoEntity found", found);        
        String demoEntityPart = matcher.group(1);

        assertEquals(normalize(expectedScript), demoEntityPart);
    }

    /**
     * scenario: alter table drop key statements should be commented out, even if they are splitted into several lines.
     * but drop table statements should remain
     *
     * @throws Exception - no exception expected
     */
    @Test
    public void testPostProcessStatements() throws Exception {

        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.MYSQL);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit", ";", false);       

        Assert.assertThat(generateDdlScript,
                Matchers.containsString(
                        "-- alter table DemoEntiyWithRelation drop foreign key FKkx2ht41rvx878qivxu39d3hnd;"));
        
        Assert.assertThat(generateDdlScript, Matchers.containsString("drop table if exists DemoEntity;"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("drop table if exists DemoEntityWithMinConstraint;"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("drop table if exists DemoEntiyWithRelation;"));
    }
    
    /**
     * scenario: drop table and alter table drop key statements should be commented out.
     *
     * @throws Exception - no exception expected
     */
    @Test
    public void testPostProcessStatementsSkipDrop() throws Exception {

        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.MYSQL);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit", ";", true);
        
        Assert.assertThat(generateDdlScript,
                Matchers.containsString(
                        "-- alter table DemoEntiyWithRelation drop foreign key FKkx2ht41rvx878qivxu39d3hnd;"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("-- drop table if exists DemoEntity;"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("-- drop table if exists DemoEntityWithMinConstraint;"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("-- drop table if exists DemoEntiyWithRelation;"));
    }
    
    
    /**
     * scenario: drop table and alter table drop key statements should be commented out.
     *
     * @throws Exception - no exception expected
     */
    @Test
    public void testPostProcessStatementsSkipDropOracle() throws Exception {

        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.ORACLE);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit", ";", true);

        System.out.println(generateDdlScript);
        
        Assert.assertThat(generateDdlScript,
                Matchers.containsString(
                        "-- alter table DemoEntiyWithRelation drop foreign key FKkx2ht41rvx878qivxu39d3hnd"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("-- begin execute immediate 'drop table if exists DemoEntity';"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("-- begin execute immediate 'drop table if exists DemoEntityWithMinConstraint';"));
        Assert.assertThat(generateDdlScript, Matchers.containsString("-- begin execute immediate 'drop table if exists DemoEntiyWithRelation';"));
    }

    /**
     * Test check constraint naming.
     *
     * @throws NoSuchAlgorithmException - no exception expected
     */
    @Test
    public void testCheckConstraintNaming() throws NoSuchAlgorithmException {
        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.SQL_SERVER_2012);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnitSQLServer", ";", false);

        String expectedScript = "" + "id int not null," + "maxValue int not null,"
                + "CONSTRAINT chk_94c741d885c721b1cdeb1f9a9340a32a CHECK (maxValue<=3)," + "minValue int not null,"
                + "CONSTRAINT chk_86491c8d06439ee66755326e35ee779b CHECK (minValue>=1)," + "primary key (id)";

        String demoEntityWithMinConstraintPart = StringUtils.substringBefore(StringUtils
                .substringAfter(normalize(generateDdlScript), normalize("create table DemoEntityWithMinConstraint (")),
                normalize(");"));
        assertEquals("found : " + demoEntityWithMinConstraintPart,
                normalize(expectedScript),
                demoEntityWithMinConstraintPart);
    }

    private String normalize(String s) {
        return s.replaceAll("\\s", "").replaceAll("\\r", "").replaceAll("\\n", "");
    }

    /** The statements must been not doubled. */
    @Test
    public void testGenerateCoreStatementsJpa21way() {
        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.SQL_SERVER_2012);
        List<String> statements = generator.generateCoreStatementsJpa21way("examplePersistenceUnit");

        int size = statements.size();
        //check that no statment occures twice
        for (int i1 = 0; i1 < size; i1++) {
            for (int i2 = 0; i2 < size; i2++) {
                if (i1 != i2) {
                    if (statements.get(i1).equals(statements.get(i2))) {
                        Assert.fail("the stamtent `" + statements.get(i1) + "` occures twice, at index " + i1 + " and "
                                + i2);
                    }
                }
            }
        }

    }

}
