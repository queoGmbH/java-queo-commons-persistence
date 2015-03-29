package com.queomedia.persistence.schema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SchemaGeneratorJpaTest {

    @Test
    public void testGenerateDdlScript() throws Exception {

        SchemaGeneratorJpa generator = new SchemaGeneratorJpa(Dialect.MYSQL);

        String generateDdlScript = generator.generateDdlScript("examplePersistenceUnit");

        String expectedScript = "" + "drop table if exists DemoEntity;" //
                + "                                " //
                + "create table DemoEntity (       " //
                + "    id integer not null,        " //
                + "    embeddedString varchar(100)," //
                + "    normalString varchar(100),  " //
                + "    primary key (id)            " //
                + ") ENGINE=InnoDB;                ";//

        assertEquals(normalize(expectedScript), normalize(generateDdlScript));
    }
    
    private String normalize(String s) {
        return s.replaceAll("\\s", "");
    }
    
    

}
