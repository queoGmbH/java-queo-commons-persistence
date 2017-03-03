package com.queomedia.persistence.schema;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;

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
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit");

        /* The test are the varchar(100) columns, because the 100 is set via JSR303: @Size(max=100) */
        String expectedScript = ""//
                + "id integer not null,        " //
                + "embeddedString varchar(100)," //
                + "normalString varchar(100),  " //
                + "primary key (id)            "; //

        //cut out the mapping for the "DemoEntity"
        String demoEntityPart = StringUtils.substringBefore(StringUtils.substringAfter(normalize(generateDdlScript),
                normalize("create table DemoEntity (")), normalize(") ENGINE=InnoDB"));
        assertEquals("found : " + demoEntityPart, normalize(expectedScript), demoEntityPart);
    }

    /**
     *  scenario: alter table drop key statements should be commented out, even if they are splitted into several lines.
     *
     * @throws Exception - no exception expected
     */
    @Test
    public void testPostProcessStatements() throws Exception {

        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.MYSQL);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit");
        
        Assert.assertThat(generateDdlScript,
                Matchers.containsString("-- alter table DemoEntiyWithRelation drop foreign key FKkx2ht41rvx878qivxu39d3hnd;"));
    }

    /**
     * Test check constraint naming.
     *
     * @throws NoSuchAlgorithmException - no exception expected
     */
    @Test
    public void testCheckConstraintNaming() throws NoSuchAlgorithmException {
        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.SQL_SERVER_2012);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnitSQLServer");

        String expectedScript = ""
                + "id int not null," 
                + "maxValue int not null,"
                + "CONSTRAINT chk_94c741d885c721b1cdeb1f9a9340a32a CHECK (maxValue<=3),"
                + "minValue int not null," 
                + "CONSTRAINT chk_86491c8d06439ee66755326e35ee779b CHECK (minValue>=1)," 
                + "primary key (id)";

        String demoEntityWithMinConstraintPart = StringUtils.substringBefore(StringUtils.substringAfter(normalize(generateDdlScript),
                normalize("create table DemoEntityWithMinConstraint (")),
                normalize(");"));
        assertEquals("found : " + demoEntityWithMinConstraintPart,
                normalize(expectedScript),
                demoEntityWithMinConstraintPart);
    }

    private String normalize(String s) {
        return s.replaceAll("\\s", "");
    }

}
