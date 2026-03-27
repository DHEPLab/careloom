CREATE TABLE visit_position_record (
                                               id BIGINT UNSIGNED auto_increment NOT NULL,
                                               visit_id BIGINT UNSIGNED NULL,
                                               baby_id BIGINT UNSIGNED NULL,
                                               longitude DECIMAL(20,6) NULL,
                                               latitude DECIMAL(20,6) NULL,
                                               distance DECIMAL(20,6) NULL,
                                               created_at DATETIME NULL,
                                               last_modified_at DATETIME NULL,
                                               created_by VARCHAR(50) NULL,
                                               last_modified_by varchar(50) NULL,
                                               deleted BIT NULL,
                                               CONSTRAINT visit_position_record_pk PRIMARY KEY (id)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;