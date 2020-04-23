package com.queomedia.persistence.schema.statmentpostprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ExplicitCreateIfNotExistsFilterTest {

    @Test
    public void testIsEnabledForCreateTable() throws Exception {
        ExplicitCreateIfNotExistsFilter filter = new ExplicitCreateIfNotExistsFilter().addTable("a").addTable("b");
        
        assertEquals(true, filter.isEnabledForCreateTable("create table a ( id number(19,0) not null)"));
        assertEquals(true, filter.isEnabledForCreateTable("create table b ( id number(19,0) not null)"));
        assertEquals(false, filter.isEnabledForCreateTable("create table c ( id number(19,0) not null)"));
    }
    
    @Test
    public void testIsEnabledForCreateTable_Empty() throws Exception {
        ExplicitCreateIfNotExistsFilter empty = new ExplicitCreateIfNotExistsFilter();
        
        assertEquals(false, empty.isEnabledForCreateTable("create table a ( id number(19,0) not null)"));
    }
    
    
    @Test
    public void testIsEnabledForCreateSequence() throws Exception {
        ExplicitCreateIfNotExistsFilter filter = new ExplicitCreateIfNotExistsFilter().addSequence("a").addSequence("b");
        
        assertEquals(true, filter.isEnabledForCreateSequcence("create sequence a start with 1 increment by  1"));
        assertEquals(true, filter.isEnabledForCreateSequcence("create sequence b"));
        assertEquals(false, filter.isEnabledForCreateSequcence("create sequence c start with 1 increment by  1"));
    }
    
    @Test
    public void testIsEnabledForCreateSequence_Empty() throws Exception {
        ExplicitCreateIfNotExistsFilter empty = new ExplicitCreateIfNotExistsFilter();
        
        assertEquals(false, empty.isEnabledForCreateSequcence("create sequence hibernate_sequence start with 1 increment by  1;"));
    }
    
    @Test
    public void testIsEnabledForCreateConstraint() throws Exception {
        ExplicitCreateIfNotExistsFilter filter = new ExplicitCreateIfNotExistsFilter().addTable("a").addTable("b");
        
        assertEquals(true, filter.isEnabledForCreateConstraint("alter table a add constraint FK1 foreign key (FK) references OTHER_TABLE"));
        assertEquals(true, filter.isEnabledForCreateConstraint("alter table b add constraint FK1 foreign key (FK) references OTHER_TABLE"));
        assertEquals(false, filter.isEnabledForCreateConstraint("alter table c add constraint FK1 foreign key (FK) references OTHER_TABLE"));
        assertEquals(false, filter.isEnabledForCreateConstraint("alter table OTHER_TABLE add constraint FK1 foreign key (FK) references a"));
    }
    
    @Test
    public void testIsEnabledForCreateConstraint_Empty() throws Exception {
        ExplicitCreateIfNotExistsFilter empty = new ExplicitCreateIfNotExistsFilter();
        
        assertEquals(false, empty.isEnabledForCreateConstraint("alter table a add constraint FK1 foreign key (FK) references OTHER_TABLE"));
    }

    @Test
    public void testGetTableFromCreateTableStatement() throws Exception {
        ExplicitCreateIfNotExistsFilter filter = new  ExplicitCreateIfNotExistsFilter();
        
        assertEquals("a", filter.getTableFromCreateTableStatement("create table a ( id number(19,0) not null)"));
        assertEquals("a", filter.getTableFromCreateTableStatement("create table   a   ( id number(19,0) not null)"));
        assertEquals("a", filter.getTableFromCreateTableStatement("create table a(id number(19,0) not null)"));
        
        assertEquals("A", filter.getTableFromCreateTableStatement("CREATE TABLE A ( ID number(19,0) not null)"));
        
        assertEquals("table_name", filter.getTableFromCreateTableStatement("create table table_name ( id number(19,0) not null)"));
    }

}
