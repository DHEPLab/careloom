CREATE TABLE project (
                                 id BIGINT UNSIGNED auto_increment NOT NULL,
                                 name varchar(50) NULL,
                                 code varchar(100) NULL,
                                 created_at DATETIME NULL,
                                 created_by varchar(50) NULL,
                                 last_modified_at DATETIME NULL,
                                 last_modified_by varchar(50) NULL,
                                 deleted BIT(1) NULL,
                                 CONSTRAINT project_pk PRIMARY KEY (id)
)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_bin;