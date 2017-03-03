set foreign_key_checks = 0;

-- not hibernate managed documents
-- alter table document_file_content drop foreign key FKcontentToDoc;
drop table if exists document_file_content;


-- domain
-- alter table component_entity drop foreign key FKdcoa02w8efxks7e9rqwk7ecwc;
drop table if exists component_entity;
drop table if exists composite_entity;

create table component_entity (
    id bigint not null auto_increment,
    businessId bigint not null,
    compositeEntity_fk bigint,
    primary key (id)
) ENGINE=InnoDB;

create table composite_entity (
    id bigint not null auto_increment,
    businessId bigint not null,
    primary key (id)
) ENGINE=InnoDB;

alter table component_entity add constraint UK_2f0v2xxs9iu5nk584p7x085cm unique (businessId);

alter table composite_entity add constraint UK_co3qq81v4wqru1l1l14ypuwtb unique (businessId);

alter table component_entity add constraint FKdcoa02w8efxks7e9rqwk7ecwc foreign key (compositeEntity_fk) references composite_entity (id);



create table document_file_content (document_fkBusinessId bigint not null, chunkNr bigint not null, size bigint not null, conntent mediumblob not null, primary key (document_fkBusinessId, chunkNr)) ENGINE=InnoDB;
alter table document_file_content add constraint FKcontentToDoc foreign key ( document_fkBusinessId  ) references document (businessId);

ALTER TABLE user MODIFY login VARCHAR( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;

set foreign_key_checks = 1;