package com.queomedia.persistence.schema;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class SchemaGeneratorJpaTest {

    /** Test that JSR-303 Annotations are taken in account, even on embeddeds.*/
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

    /** scenario: alter table drop key statements should be commented out, even if they are splitted into several lines. */
    @Test
    public void testPostProcessStatements() throws Exception {

        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.MYSQL);
        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit");

        Assert.assertThat(generateDdlScript,
                Matchers.containsString("-- alter table DemoEntiyWithRelation drop foreign key FK_1yi2wnn3sol2mjdlng1yr4utf;"));
    }

    private String normalize(String s) {
        return s.replaceAll("\\s", "");
    }

}
