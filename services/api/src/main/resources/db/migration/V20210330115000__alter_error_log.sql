ALTER TABLE `error_log` DROP FOREIGN KEY `log_user_id`;
ALTER TABLE `error_log` CHANGE COLUMN `user_id` `user_id` BIGINT(20) NULL ;
ALTER TABLE `error_log` ADD CONSTRAINT `log_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);