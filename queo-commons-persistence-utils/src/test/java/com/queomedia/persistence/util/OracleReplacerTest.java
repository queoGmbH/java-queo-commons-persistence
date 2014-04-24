package com.queomedia.persistence.util;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the OracleReplacerTest class.
 *
 * @author hohlfeld
 */
public class OracleReplacerTest {

    /**
     * test an illegal drop statement - IllegalArgumentException expected.
     */
    @Test (expected = IllegalArgumentException.class)
    public void testWrongDropStatement() {

        OracleReplacer.replace("drop something;");
    }

    /**
     * a simple drop table statement.
     */
    @Test
    public void testSimpleDropTable() {
        String dropTable = "drop table test_table cascade constraints;";
        String result = OracleReplacer.replace(dropTable).get(0);
        Assert.assertThat("statement must be changed", result.length(), Matchers.greaterThan(dropTable.length()));
        Assert.assertThat("only trailing semicolon must be cut off from original statement",
                result,
                Matchers.containsString(dropTable.substring(0, dropTable.length() - 1)));
    }

    /**
     * a simple drop table statement.
     */
    @Test
    public void testSimpleDropSequence() {
        String dropSequence = "drop sequence test_sequence cascade constraints;";
        String result = OracleReplacer.replace(dropSequence).get(0);
        Assert.assertThat("statement must be changed", result.length(), Matchers.greaterThan(dropSequence.length()));
        Assert.assertThat("only trailing semicolon must be cut off from original statement",
                result,
                Matchers.containsString(dropSequence.substring(0, dropSequence.length() - 1)));
    }

    /**
     * Respect comment with trailing semicolon
     */
    @Test
    public void testCommentLine() {
        String comment = "-- this is only an comment with trailing semicolon;";
        List<String> result = OracleReplacer.replace(comment);
        Assert.assertThat(result.size(), Matchers.equalTo(2));
        Assert.assertThat(result.get(0), Matchers.equalTo("-- this is only an comment with trailing semicolon"));
        Assert.assertThat(result.get(1), Matchers.equalTo("-- /"));
    }

    public static final List<String> sampleComplexStatement = Arrays
            .asList("SET CONSTRAINTS ALL DEFERRED;                                     ",
                    "                                                                  ",
                    "    -- this is a comment with trailing semicolon;                 ",
                    "                                                                  ",
                    "    drop table department cascade constraints;                    ",
                    "                                                                  ",
                    "    drop table test_user cascade constraints;                     ",
                    "                                                                  ",
                    "    drop table test_user2security_roles cascade constraints;      ",
                    "                                                                  ",
                    "    drop sequence hibernate_sequence;                             ",
                    "                                                                  ",
                    "   create table department (                                      ",
                    "        id number(19,0) not null,                                 ",
                    "        businessId number(19,0) not null unique,                  ",
                    "        title varchar2(255 char) not null unique,                 ",
                    "        primary key (id)                                          ",
                    "    );                                                            ",
                    "                                                                  ",
                    "    create table test_user (                                      ",
                    "        id number(19,0) not null,                                 ",
                    "        businessId number(19,0) not null unique,                  ",
                    "        creationDate timestamp not null,                          ",
                    "        emailAddress varchar2(255 char) not null,                 ",
                    "        encryptedPassword varchar2(128 char) not null,            ",
                    "        firstName varchar2(20 char) not null,                     ",
                    "        lastName varchar2(40 char) not null,                      ",
                    "        locale varchar2(255 char) not null,                       ",
                    "        login varchar2(255 char) not null unique,                 ",
                    "        loginEnabled char(1 char) not null,                       ",
                    "        passwordRenewDate timestamp not null,                     ",
                    "        department_fk number(19,0),                               ",
                    "        primary key (id)                                          ",
                    "    );                                                            ",
                    "                                                                  ",
                    "    create table test_user2security_roles (                       ",
                    "        User_fk number(19,0) not null,                            ",
                    "        securityRoles varchar2(255 char)                          ",
                    "   );                                                             ",
                    "                                                                  ",
                    "    alter table test_user                                         ",
                    "        add constraint FKE3DC7F9074F5AD8B                         ",
                    "        foreign key (department_fk)                               ",
                    "        references department;                                    ",
                    "                                                                  ",
                    "    alter table test_user2security_roles                          ",
                    "        add constraint FK9FE5E852AB7AAE3                          ",
                    "        foreign key (User_fk)                                     ",
                    "        references shims_user;                                    ",
                    "                                                                  ",
                    "    create sequence hibernate_sequence;                           ");

    public static final List<String> expectedResult = Arrays
            .asList("SET CONSTRAINTS ALL DEFERRED                                      ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    -- this is a comment with trailing semicolon                  ",
                    "    -- /                                                          ",
                    "                                                                  ",
                    "    begin execute immediate 'drop table department cascade constraints'; exception when others then if sqlcode != -942 then raise; end if; end;                    ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    begin execute immediate 'drop table test_user cascade constraints'; exception when others then if sqlcode != -942 then raise; end if; end;                     ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    begin execute immediate 'drop table test_user2security_roles cascade constraints'; exception when others then if sqlcode != -942 then raise; end if; end;      ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    begin execute immediate 'drop sequence hibernate_sequence'; exception when others then if sqlcode != -2289 then raise; end if; end;                             ",
                    "    /                                                             ",
                    "                                                                  ",
                    "   create table department (                                      ",
                    "        id number(19,0) not null,                                 ",
                    "        businessId number(19,0) not null unique,                  ",
                    "        title varchar2(255 char) not null unique,                 ",
                    "        primary key (id)                                          ",
                    "    )                                                             ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    create table test_user (                                      ",
                    "        id number(19,0) not null,                                 ",
                    "        businessId number(19,0) not null unique,                  ",
                    "        creationDate timestamp not null,                          ",
                    "        emailAddress varchar2(255 char) not null,                 ",
                    "        encryptedPassword varchar2(128 char) not null,            ",
                    "        firstName varchar2(20 char) not null,                     ",
                    "        lastName varchar2(40 char) not null,                      ",
                    "        locale varchar2(255 char) not null,                       ",
                    "        login varchar2(255 char) not null unique,                 ",
                    "        loginEnabled char(1 char) not null,                       ",
                    "        passwordRenewDate timestamp not null,                     ",
                    "        department_fk number(19,0),                               ",
                    "        primary key (id)                                          ",
                    "    )                                                             ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    create table test_user2security_roles (                       ",
                    "        User_fk number(19,0) not null,                            ",
                    "        securityRoles varchar2(255 char)                          ",
                    "    )                                                             ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    alter table test_user                                         ",
                    "        add constraint FKE3DC7F9074F5AD8B                         ",
                    "        foreign key (department_fk)                               ",
                    "        references department                                     ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    alter table test_user2security_roles                          ",
                    "        add constraint FK9FE5E852AB7AAE3                          ",
                    "        foreign key (User_fk)                                     ",
                    "        references shims_user                                     ",
                    "    /                                                             ",
                    "                                                                  ",
                    "    create sequence hibernate_sequence                            ",
                    "    /                                                             ");

    @Test
    public void testComplexSatement() {

        List<String> result = OracleReplacer.replace(OracleReplacerTest.sampleComplexStatement);
        Assert.assertThat("result statement must have exectly that number of lines",
                OracleReplacerTest.expectedResult.size(),
                Matchers.equalTo(result.size()));
        for (int i = 0; i < OracleReplacerTest.expectedResult.size(); i++) {
            Assert.assertThat(result.get(i),
                    Matchers.equalToIgnoringWhiteSpace(OracleReplacerTest.expectedResult.get(i)));
        }
    }

}
