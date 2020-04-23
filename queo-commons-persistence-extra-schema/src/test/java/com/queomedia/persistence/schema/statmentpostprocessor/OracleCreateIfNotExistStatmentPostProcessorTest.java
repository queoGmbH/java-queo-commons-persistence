package com.queomedia.persistence.schema.statmentpostprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class OracleCreateIfNotExistStatmentPostProcessorTest {

    @Test
    public void testIsAlterTableAddUniqueConstraintStatement() throws Exception {
        assertTrue(OracleCreateIfNotExistStatmentPostProcessor
                .isAlterTableAddUniqueConstraintStatement("alter table myTable ADD constraint UK_1 unique (bId)"));
        assertTrue(OracleCreateIfNotExistStatmentPostProcessor
                .isAlterTableAddUniqueConstraintStatement("ALTER TABLE myTable ADD CONSTRAINT UK_1 unique (bId)"));
        assertTrue(OracleCreateIfNotExistStatmentPostProcessor
                .isAlterTableAddUniqueConstraintStatement("alter table myTable ADD constraint UK_1 unique (bId, cId)"));
        assertTrue(OracleCreateIfNotExistStatmentPostProcessor
                .isAlterTableAddUniqueConstraintStatement("alter table myTable ADD constraint UK_1 unique(bId)"));

        assertFalse(OracleCreateIfNotExistStatmentPostProcessor.isAlterTableAddUniqueConstraintStatement(
                "alter table myTable add constraint FK foreign key (user_fk) references user"));
    }

    @Test
    public void testPostProcessSingle_createSequence() throws Exception {
        OracleCreateIfNotExistStatmentPostProcessor oracleCreateIfNotExistStatmentPostProcessor = new OracleCreateIfNotExistStatmentPostProcessor();

        String pureStatment = "create sequence hibernate_sequence start with 1 increment by  1";

        String result = oracleCreateIfNotExistStatmentPostProcessor.postProcessSingle(pureStatment);
        assertEquals(
                "begin execute immediate '" + pureStatment
                        + "'; exception when others then if sqlcode != -955 then raise; end if; end;",
                result);
    }

    @Test
    public void testPostProcessSingle_createTable() throws Exception {
        OracleCreateIfNotExistStatmentPostProcessor oracleCreateIfNotExistStatmentPostProcessor = new OracleCreateIfNotExistStatmentPostProcessor();

        String pureStatment = "create table user (id number(19,0) not null, primary key (id))";

        String result = oracleCreateIfNotExistStatmentPostProcessor.postProcessSingle(pureStatment);
        assertEquals(
                "begin execute immediate '" + pureStatment
                        + "'; exception when others then if sqlcode != -955 then raise; end if; end;",
                result);
    }

    @Test
    public void testPostProcessSingle_AddUniqeConstraint() throws Exception {
        OracleCreateIfNotExistStatmentPostProcessor oracleCreateIfNotExistStatmentPostProcessor = new OracleCreateIfNotExistStatmentPostProcessor();

        String pureStatment = "alter table nds_user add constraint UK_acmtkmfxy9b50uysodtdnmmxi unique (businessId)";

        String result = oracleCreateIfNotExistStatmentPostProcessor.postProcessSingle(pureStatment);
        assertEquals(
                "begin execute immediate '" + pureStatment
                        + "'; exception when others then if sqlcode != -2261 then raise; end if; end;",
                result);
    }

    @Test
    public void testPostProcessSingle_AddFkConstraint() throws Exception {
        OracleCreateIfNotExistStatmentPostProcessor oracleCreateIfNotExistStatmentPostProcessor = new OracleCreateIfNotExistStatmentPostProcessor();

        String pureStatment = "alter table nds_user_AUD add constraint FKsr1beuc1upxul2s5u3p2g7gem foreign key (REV) references REVINFO";

        String result = oracleCreateIfNotExistStatmentPostProcessor.postProcessSingle(pureStatment);
        assertEquals(
                "begin execute immediate '" + pureStatment
                        + "'; exception when others then if sqlcode != -2275 then raise; end if; end;",
                result);
    }
}
