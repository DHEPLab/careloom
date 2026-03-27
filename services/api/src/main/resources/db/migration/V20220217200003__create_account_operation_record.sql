CREATE TABLE account_operation_record (
                                                  id BIGINT UNSIGNED auto_increment NOT NULL,
                                                  account_id BIGINT UNSIGNED NULL,
                                                  account_type varchar(20) NOT NULL,
                                                  close_time DATETIME NULL,
                                                  revert_time DATETIME NULL,
                                                  revert BIT NULL,
                                                  created_at DATETIME NULL,
                                                  created_by varchar(64) NULL,
                                                  last_modified_by varchar(64) NULL,
                                                  last_modified_at DATETIME NULL,
                                                  deleted BIT NULL,
                                                  CONSTRAINT account_operation_record_pk PRIMARY KEY (id)
)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_bin;