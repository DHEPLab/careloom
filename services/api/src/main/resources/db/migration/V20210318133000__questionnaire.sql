CREATE TABLE `questionnaire` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NOT NULL,
  `questions` JSON NOT NULL,
  `branch` VARCHAR(10) NOT NULL,
  `published` BIT(1) NOT NULL,
  `source_id` BIGINT(20) NULL DEFAULT NULL,
  `created_at` DATETIME NULL DEFAULT NULL,
  `created_by` VARCHAR(50) NULL DEFAULT NULL,
  `deleted` BIT(1) NOT NULL,
  `last_modified_at` DATETIME NULL DEFAULT NULL,
  `last_modified_by` VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

ALTER TABLE `questionnaire` ADD FOREIGN KEY (`source_id`) REFERENCES `questionnaire` (`id`);