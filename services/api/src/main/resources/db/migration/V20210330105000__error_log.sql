CREATE TABLE `error_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL,
  `type` VARCHAR(25) NOT NULL,
  `type_id` BIGINT(20) NOT NULL,
  `msg` VARCHAR(500) NOT NULL,
  `create_at` DATETIME NULL DEFAULT NULL,
  `created_by` VARCHAR(50) NULL DEFAULT NULL,
  `deleted` BIT(1) NOT NULL,
  `last_modified_at` DATETIME NULL DEFAULT NULL,
  `last_modified_by` VARCHAR(50) NULL DEFAULT NULL,
PRIMARY KEY (`id`));

ALTER TABLE error_log ADD CONSTRAINT log_user_id FOREIGN KEY (user_id) REFERENCES `user`(`id`);