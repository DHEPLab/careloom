SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `carer_modify_record`;
CREATE TABLE `carer_modify_record`  (
                                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                        `created_at` datetime NULL DEFAULT NULL,
                                        `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
                                        `deleted` bit(1) NOT NULL,
                                        `last_modified_at` datetime NULL DEFAULT NULL,
                                        `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
                                        `carer_id` bigint(20) NULL DEFAULT NULL,
                                        `carer_json` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                        `user_id` bigint(20) NULL DEFAULT NULL,
                                        `change_column` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;


