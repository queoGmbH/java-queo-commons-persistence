package com.queomedia;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.queomedia.persistence.schema.Dialect;
import com.queomedia.persistence.schema.SchemaGeneratorJpa;
import com.queomedia.persistence.schema.statmentpostprocessor.ExplicitCreateIfNotExistsFilter;
import com.queomedia.persistence.schema.statmentpostprocessor.OracleCreateIfNotExistStatmentPostProcessor;

public class OraclePostProcessorTest {

    @Test
    public void testDllSchemaFileMatchWithOracleCreateIfNotExistStatmentPostProcessor() throws Exception {
        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(Dialect.ORACLE);
        gen.addStatementPostProcessor(new OracleCreateIfNotExistStatmentPostProcessor());
        String result = gen.generateDdlScript("persistenceUnit", ";", false);

        // @formatter:off
        String expected = ""
                + "-- alter table component_entity drop foreign key FKdcoa02w8efxks7e9rqwk7ecwc;\n"
                + "begin execute immediate 'drop table if exists component_entity'; exception when others then if sqlcode != -942 then raise; end if; end;;\n"
                + "begin execute immediate 'drop table if exists composite_entity'; exception when others then if sqlcode != -942 then raise; end if; end;;\n"
                + "begin execute immediate 'drop table if exists constraint_entity'; exception when others then if sqlcode != -942 then raise; end if; end;;\n"
                + "\n" + "begin execute immediate\n" 
                + "  'create table component_entity (\n"
                + "    id bigint not null auto_increment,\n" 
                + "    businessId bigint not null,\n"
                + "    compositeEntity_fk bigint,\n" 
                + "    primary key (id)\n" 
                + "  ) engine=InnoDB';\n"
                + "exception when others then if sqlcode != -955 then raise; end if; end;;\n" 
                + "\n"
                + "begin execute immediate\n" 
                + "  'create table composite_entity (\n"
                + "    id bigint not null auto_increment,\n" + "    businessId bigint not null,\n"
                + "    primary key (id)\n" 
                + "  ) engine=InnoDB';\n"
                + "exception when others then if sqlcode != -955 then raise; end if; end;;\n" + "\n"
                + "begin execute immediate\n" 
                + "  'create table constraint_entity (\n"
                + "    id bigint not null auto_increment,\n" 
                + "    businessId bigint not null,\n"
                + "    notEmptyString_hibernateValidator varchar(255) not null,\n"
                + "    notEmptyString_javaxValidation varchar(255) not null,\n"
                + "    notNullObject varchar(255) not null,\n" 
                + "    object varchar(255),\n"
                + "    primitive integer not null,\n" 
                + "    primary key (id)\n" + "  ) engine=InnoDB';\n"
                + "exception when others then if sqlcode != -955 then raise; end if; end;;\n" 
                + "\n"
                + "begin execute immediate 'alter table component_entity add constraint UK_2f0v2xxs9iu5nk584p7x085cm unique (businessId)'; exception when others then if sqlcode != -955 then raise; end if; end;;\n"
                + "\n"
                + "begin execute immediate 'alter table composite_entity add constraint UK_co3qq81v4wqru1l1l14ypuwtb unique (businessId)'; exception when others then if sqlcode != -955 then raise; end if; end;;\n"
                + "\n"
                + "begin execute immediate 'alter table constraint_entity add constraint UK_4bar7hjr6vc0yomhga21l1nmn unique (businessId)'; exception when others then if sqlcode != -955 then raise; end if; end;;\n"
                + "\n"
                + "begin execute immediate 'alter table component_entity add constraint FKdcoa02w8efxks7e9rqwk7ecwc foreign key (compositeEntity_fk) references composite_entity (id)'; exception when others then if sqlcode != -955 then raise; end if; end;;\n"
                + "\n";
        // @formatter:on
        assertEquals(expected, result);
    }

    @Test
    public void testDllSchema_ExplicitCreateIfNotExistsFilter() throws Exception {
        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(Dialect.ORACLE);

        gen.addStatementPostProcessor(new OracleCreateIfNotExistStatmentPostProcessor(
                new ExplicitCreateIfNotExistsFilter().addTable("component_entity")));
        String result = gen.generateDdlScript("persistenceUnit", ";", false);
        
        // @formatter:off
        String expected = ""
                + "-- alter table component_entity drop foreign key FKdcoa02w8efxks7e9rqwk7ecwc;\n"
                + "begin execute immediate 'drop table if exists component_entity'; exception when others then if sqlcode != -942 then raise; end if; end;;\n"
                + "begin execute immediate 'drop table if exists composite_entity'; exception when others then if sqlcode != -942 then raise; end if; end;;\n"
                + "begin execute immediate 'drop table if exists constraint_entity'; exception when others then if sqlcode != -942 then raise; end if; end;;\n"
                + "\n"
                + "begin execute immediate\n"
                + "  'create table component_entity (\n"
                + "    id bigint not null auto_increment,\n"
                + "    businessId bigint not null,\n"
                + "    compositeEntity_fk bigint,\n"
                + "    primary key (id)\n"
                + "  ) engine=InnoDB';\n"
                + "exception when others then if sqlcode != -955 then raise; end if; end;;\n"
                + "\n"
                + "create table composite_entity (\n"
                + "    id bigint not null auto_increment,\n"
                + "    businessId bigint not null,\n"
                + "    primary key (id)\n"
                + ") engine=InnoDB;\n"
                + "\n"
                + "create table constraint_entity (\n"
                + "    id bigint not null auto_increment,\n"
                + "    businessId bigint not null,\n"
                + "    notEmptyString_hibernateValidator varchar(255) not null,\n"
                + "    notEmptyString_javaxValidation varchar(255) not null,\n"
                + "    notNullObject varchar(255) not null,\n"
                + "    object varchar(255),\n"
                + "    primitive integer not null,\n"
                + "    primary key (id)\n"
                + ") engine=InnoDB;\n"
                + "\n"
                + "begin execute immediate 'alter table component_entity add constraint UK_2f0v2xxs9iu5nk584p7x085cm unique (businessId)'; exception when others then if sqlcode != -955 then raise; end if; end;;\n"
                + "\n"
                + "alter table composite_entity add constraint UK_co3qq81v4wqru1l1l14ypuwtb unique (businessId);\n"
                + "\n"
                + "alter table constraint_entity add constraint UK_4bar7hjr6vc0yomhga21l1nmn unique (businessId);\n"
                + "\n"
                + "begin execute immediate 'alter table component_entity add constraint FKdcoa02w8efxks7e9rqwk7ecwc foreign key (compositeEntity_fk) references composite_entity (id)'; exception when others then if sqlcode != -955 then raise; end if; end;;\n"
                + "\n";
        // @formatter:on
        assertEquals(expected, result);
    }
    
    
    @Test
    public void testDllSchema_ExplicitCreateIfNotExistsFilter_slashSeperator() throws Exception {
        SchemaGeneratorJpa gen = new SchemaGeneratorJpa(Dialect.ORACLE);

        gen.addStatementPostProcessor(new OracleCreateIfNotExistStatmentPostProcessor(
                new ExplicitCreateIfNotExistsFilter().addTable("component_entity")));
        String result = gen.generateDdlScript("persistenceUnit", "\n/", false);
        
        System.out.println(result);
        
        // @formatter:off
        String expected = ""
                + "-- alter table component_entity drop foreign key FKdcoa02w8efxks7e9rqwk7ecwc\n"
                + "-- /\n"
                + "begin execute immediate 'drop table if exists component_entity'; exception when others then if sqlcode != -942 then raise; end if; end;\n"
                + "/\n"
                + "begin execute immediate 'drop table if exists composite_entity'; exception when others then if sqlcode != -942 then raise; end if; end;\n"
                + "/\n"
                + "begin execute immediate 'drop table if exists constraint_entity'; exception when others then if sqlcode != -942 then raise; end if; end;\n"
                + "/\n"
                + "\n"
                + "begin execute immediate\n"
                + "  'create table component_entity (\n"
                + "    id bigint not null auto_increment,\n"
                + "    businessId bigint not null,\n"
                + "    compositeEntity_fk bigint,\n"
                + "    primary key (id)\n"
                + "  ) engine=InnoDB';\n"
                + "exception when others then if sqlcode != -955 then raise; end if; end;\n"
                + "/\n"
                + "\n"
                + "create table composite_entity (\n"
                + "    id bigint not null auto_increment,\n"
                + "    businessId bigint not null,\n"
                + "    primary key (id)\n"
                + ") engine=InnoDB\n"
                + "/\n"
                + "\n"
                + "create table constraint_entity (\n"
                + "    id bigint not null auto_increment,\n"
                + "    businessId bigint not null,\n"
                + "    notEmptyString_hibernateValidator varchar(255) not null,\n"
                + "    notEmptyString_javaxValidation varchar(255) not null,\n"
                + "    notNullObject varchar(255) not null,\n"
                + "    object varchar(255),\n"
                + "    primitive integer not null,\n"
                + "    primary key (id)\n"
                + ") engine=InnoDB\n"
                + "/\n"
                + "\n"
                + "begin execute immediate 'alter table component_entity add constraint UK_2f0v2xxs9iu5nk584p7x085cm unique (businessId)'; exception when others then if sqlcode != -955 then raise; end if; end;\n"
                + "/\n"
                + "\n"
                + "alter table composite_entity add constraint UK_co3qq81v4wqru1l1l14ypuwtb unique (businessId)\n"
                + "/\n"
                + "\n"
                + "alter table constraint_entity add constraint UK_4bar7hjr6vc0yomhga21l1nmn unique (businessId)\n"
                + "/\n"
                + "\n"
                + "begin execute immediate 'alter table component_entity add constraint FKdcoa02w8efxks7e9rqwk7ecwc foreign key (compositeEntity_fk) references composite_entity (id)'; exception when others then if sqlcode != -955 then raise; end if; end;\n"
                + "/\n"
                + "\n";
        // @formatter:on
        assertEquals(expected, result);
    }
}
