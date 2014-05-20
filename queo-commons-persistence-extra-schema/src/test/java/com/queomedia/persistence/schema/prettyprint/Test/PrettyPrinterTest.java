package com.queomedia.persistence.schema.prettyprint.Test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.queomedia.persistence.schema.prettyprint.SqlPrettyPrinter;

public class PrettyPrinterTest {

    private SqlPrettyPrinter prettyPrinter = new SqlPrettyPrinter();

    @Test
    public void testFormatLineStatement() throws Exception {
        String result = this.prettyPrinter
                .formatLineStatement("create table test (id bigint not null auto_increment, businessId bigint not null, title varchar(255) not null, testType_fk bigint not null, primary key (id)) ENGINE=InnoDB;");
        Assert.assertEquals("create table test (\n" //$NON-NLS-1$
                + "    id bigint not null auto_increment,\n" //$NON-NLS-1$
                + "    businessId bigint not null,\n" //$NON-NLS-1$
                + "    title varchar(255) not null,\n" //$NON-NLS-1$
                + "    testType_fk bigint not null,\n" //$NON-NLS-1$
                + "    primary key (id)\n" //$NON-NLS-1$
                + ") ENGINE=InnoDB;", //$NON-NLS-1$
                result);
    }

    /** No line break for comma within brackets (for example "counter decimal(0,10)" ) */
    @Test
    public void testFormatLineStatement_withNestedBrackets() throws Exception {
        String result = this.prettyPrinter
                .formatLineStatement("create table test (id bigint not null auto_increment, businessId bigint not null, counter decimal(0,10) not null, testType_fk bigint not null, primary key (id)) ENGINE=InnoDB;");
        Assert.assertEquals("create table test (\n" //$NON-NLS-1$
                + "    id bigint not null auto_increment,\n" //$NON-NLS-1$
                + "    businessId bigint not null,\n" //$NON-NLS-1$
                + "    counter decimal(0,10) not null,\n" //$NON-NLS-1$
                + "    testType_fk bigint not null,\n" //$NON-NLS-1$
                + "    primary key (id)\n" //$NON-NLS-1$
                + ") ENGINE=InnoDB;", //$NON-NLS-1$
                result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGroupStatments() throws Exception {

        List<String> statements = Arrays.asList("create table body_style (id bigint not...",
                "create table brand (id bigint not null...");

        List<List<String>> result = this.prettyPrinter.groupStatments(statements);
        /** Expected two groups, because the table is different */
        Assert.assertEquals(Arrays.asList(Arrays.asList(statements.get(0)), Arrays.asList(statements.get(1))), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGroupStatments_WithGrouped() throws Exception {

        List<String> statements = Arrays
                .asList("alter table brand add constraint UK_p4n95fvucbqir68t581d8awwb  unique (businessId)",
                        "alter table brand add constraint UK_25q2uijrs0cd169bxmbaixswu  unique (title)");

        List<List<String>> result = this.prettyPrinter.groupStatments(statements);
        /** Expected two groups, because the table is different */
        Assert.assertEquals(Arrays.asList(Arrays.asList(statements.get(0), statements.get(1))), result);
    }

    @Test
    public void testExtractFromStatment() throws Exception {
        Assert.assertEquals(new SqlPrettyPrinter.Group("create", "body_style"),
                SqlPrettyPrinter.Group.extractFromStatment("create table body_style (id bigint not..."));
    }
}
