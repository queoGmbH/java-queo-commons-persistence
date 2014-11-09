CREATE TABLE IF NOT EXISTS document_file_content (
  document_fkBusinessId bigint not null,
  chunkNr int(11) NOT NULL,
  size int(11) NOT NULL,
  conntent blob(16777216) NOT NULL,
  primary key (document_fkBusinessId, chunkNr)
);
--Foreign key constaint not possible because the othere table does not exists yet.
