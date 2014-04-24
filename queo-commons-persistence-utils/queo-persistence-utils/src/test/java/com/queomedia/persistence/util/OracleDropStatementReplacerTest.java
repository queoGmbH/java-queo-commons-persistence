package com.queomedia.persistence.util;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the OracleDropStatementReplacerTest class.
 *
 * @author hohlfeld
 */
public class OracleDropStatementReplacerTest {

    /**
     * test a 2 line statement.
     */
    @Test
    public void testCorrectLineSplitting() {

        List<String> result = OracleDropStatementReplacer.replaceDropStatements("drop me not;\nanother statement;");

        Assert.assertThat(result.get(0), Matchers.equalTo("drop me not;"));
        Assert.assertThat(result.get(1), Matchers.equalTo("another statement;"));
    }

    /**
     * a simple drop table statement.
     */
    @Test
    public void testSimpleDropTable() {
        String dropTable = "drop table test_table cascade constraints;";
        String result = OracleDropStatementReplacer.replaceDropStatements(dropTable).get(0);
        Assert.assertThat("statement must be changed", result.length(), Matchers.greaterThan(dropTable.length()));
        Assert.assertThat("only trailing semicolon must be cut off from original statement",
                result,
                Matchers.containsString(dropTable.substring(0, dropTable.lastIndexOf(";"))));
    }

    /**
     * a simple drop table statement.
     */
    @Test
    public void testSimpleDropSequence() {
        String dropSequence = "drop sequence test_sequence cascade constraints;";
        String result = OracleDropStatementReplacer.replaceDropStatements(dropSequence).get(0);
        Assert.assertThat("statement must be changed", result.length(), Matchers.greaterThan(dropSequence.length()));
        Assert.assertThat("only trailing semicolon must be cut off from original statement",
                result,
                Matchers.containsString(dropSequence.substring(0, dropSequence.lastIndexOf(";"))));
    }

    public static final List<String> sampleComplexStatement = Arrays
            .asList("SET CONSTRAINTS ALL DEFERRED;                                     ",
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
            .asList("SET CONSTRAINTS ALL DEFERRED;                                     ",
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

    @Test
    public void testComplexSatement() {

        List<String> result = OracleDropStatementReplacer
                .replaceDropStatements(OracleDropStatementReplacerTest.sampleComplexStatement);
        Assert.assertThat("result statement must have exectly that number of lines",
                OracleDropStatementReplacerTest.expectedResult.size(),
                Matchers.equalTo(result.size()));
        for (int i = 0; i < OracleDropStatementReplacerTest.expectedResult.size(); i++) {
            Assert.assertThat(result.get(i),
                    Matchers.equalToIgnoringWhiteSpace(OracleDropStatementReplacerTest.expectedResult.get(i)));
        }
    }

}
