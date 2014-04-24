package com.queomedia.persistence.schema;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

public class SchemaGeneratorTest {

    private List<String> sampleContent = Arrays
            .asList("        alter table password_lost_request                                           ",
                    "            drop                                                                    ",
                    "            foreign key FK8E76FF983A03B20A;                                         ",
                    "                                                                                    ",
                    "        alter table user2funding                                                    ",
                    "            drop                                                                    ",
                    "            foreign key FKF9FA77562C2A1EB3;                                         ",
                    "                                                                                    ",
                    "        alter table user2funding                                                    ",
                    "            drop                                                                    ",
                    "            foreign key FKF9FA77563A03B20A;                                         ",
                    "                                                                                    ",
                    "        drop table if exists funding;                                               ",
                    "                                                                                    ",
                    "        drop table if exists media_event;                                           ",
                    "                                                                                    ",
                    "        drop table if exists password_lost_request;                                 ",
                    "                                                                                    ",
                    "                                                                                    ",
                    "        create table funding (                                                      ",
                    "            id bigint not null auto_increment,                                      ",
                    "            businessId bigint not null unique,                                      ",
                    "            cancelEmail varchar(255),                                               ",
                    "            city varchar(255),                                                      ",
                    "            website varchar(255),                                                   ",
                    "            primary key (id)                                                        ",
                    "        ) ENGINE=InnoDB;                                                            ",
                    "                                                                                    ",
                    "        create table media_event (                                                  ",
                    "            id bigint not null auto_increment,                                      ",
                    "            businessId bigint not null unique,                                      ",
                    "            content varchar(255),                                                   ",
                    "            url varchar(255),                                                       ",
                    "            primary key (id)                                                        ",
                    "        ) ENGINE=InnoDB;                                                            ");

    /** scenario: alter table drop key statments should be commented out, even if they are splitted into several lines. */
    @Test
    public void testAddCommentDropConstraintStatements() throws Exception {
        SchemaGenerator schemaGenerator = new SchemaGenerator(Arrays.asList("com.queomedia"), null, "ddl.sql", Dialect.MYSQL);

        String result = schemaGenerator.addCommentDropConstraintStatements(sampleContent, Dialect.MYSQL);

        assertThat(result,
                Matchers.containsString("-- alter table password_lost_request drop foreign key FK8E76FF983A03B20A;"));
    }
}
