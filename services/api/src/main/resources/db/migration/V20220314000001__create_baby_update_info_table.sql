CREATE TABLE baby_update_info (
                                          id BIGINT UNSIGNED auto_increment NOT NULL,
                                          baby_id BIGINT UNSIGNED NULL,
                                          update_normal BIT(1) NULL,
                                          created_at DATETIME NULL,
                                          created_by VARCHAR(50) NULL,
                                          deleted BIT(1) NULL,
                                          last_modified_at DATETIME NULL,
                                          last_modified_by varchar(50) NULL,
                                          CONSTRAINT baby_update_info_pk PRIMARY KEY (id)
)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;