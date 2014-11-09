set foreign_key_checks = 0;

-- not hibernate managed documents
-- alter table document_file_content drop foreign key FKcontentToDoc;
drop table if exists document_file_content;


-- domain
/* <<add at the beginning hibernate, add at the end hibernate>> */

create table document_file_content (document_fkBusinessId bigint not null, chunkNr bigint not null, size bigint not null, conntent mediumblob not null, primary key (document_fkBusinessId, chunkNr)) ENGINE=InnoDB;
alter table document_file_content add constraint FKcontentToDoc foreign key ( document_fkBusinessId  ) references document (businessId);

ALTER TABLE user MODIFY login VARCHAR( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;

set foreign_key_checks = 1;