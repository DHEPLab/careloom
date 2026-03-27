CREATE TABLE `questionnaire_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(500) NOT NULL,
  `answer` VARCHAR(500) NOT NULL,
  `created_at` DATETIME NULL DEFAULT NULL,
  `created_by` VARCHAR(50) NULL DEFAULT NULL,
  `deleted` BIT(1) NOT NULL,
  `last_modified_at` DATETIME NULL DEFAULT NULL,
  `last_modified_by` VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);