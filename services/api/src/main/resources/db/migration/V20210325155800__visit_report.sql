CREATE TABLE `visit_report` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `visit_report_obj_data` JSON NOT NULL,
  `created_at` DATETIME NULL DEFAULT NULL,
  `created_by` VARCHAR(50) NULL DEFAULT NULL,
  `deleted` BIT(1) NOT NULL,
  `last_modified_at` DATETIME NULL DEFAULT NULL,
  `last_modified_by` VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);