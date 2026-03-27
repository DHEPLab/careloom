-- baby_history definition

CREATE TABLE `baby_history` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `created_at` datetime DEFAULT NULL,
                                `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `deleted` bit(1) NOT NULL,
                                `last_modified_at` datetime DEFAULT NULL,
                                `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `birthday` date DEFAULT NULL,
                                `edc` date DEFAULT NULL,
                                `feeding_pattern` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `identity` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `name` varchar(10) CHARACTER SET gbk COLLATE gbk_chinese_ci DEFAULT NULL,
                                `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `stage` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `chw_id` bigint DEFAULT NULL,
                                `area` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `curriculum_id` bigint DEFAULT NULL,
                                `assisted_food` bit(1) NOT NULL,
                                `approved` bit(1) NOT NULL,
                                `action_from_app` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `close_account_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `history_id` bigint unsigned DEFAULT NULL,
                                `longitude` decimal(10,6) DEFAULT NULL,
                                `latitude` decimal(10,6) DEFAULT NULL,
                                `show_location` bit(1) DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FK52xwhd26ynrhd0mi6qdck6ux7` (`chw_id`) USING BTREE,
                                KEY `FKclftxyb8c1dwqgfiv41s2dprv` (`curriculum_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- carer_history definition

CREATE TABLE `carer_history` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `created_at` datetime DEFAULT NULL,
                                 `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `deleted` bit(1) NOT NULL,
                                 `last_modified_at` datetime DEFAULT NULL,
                                 `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `family_ties` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `master_carer` bit(1) NOT NULL,
                                 `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                 `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                 `wechat` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `baby_id` bigint DEFAULT NULL,
                                 `history_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FKesxg0gdragoyd8atftlvisw4o` (`baby_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- community_house_worker_history definition

CREATE TABLE `community_house_worker_history` (
                                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                                  `created_at` datetime DEFAULT NULL,
                                                  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                                  `deleted` bit(1) NOT NULL,
                                                  `last_modified_at` datetime DEFAULT NULL,
                                                  `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                                  `identity` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                                  `supervisor_id` bigint DEFAULT NULL,
                                                  `tags` json DEFAULT NULL,
                                                  `history_id` bigint unsigned DEFAULT NULL,
                                                  PRIMARY KEY (`id`),
                                                  KEY `FKehn3fk7ktkpxj40h9242dptof` (`supervisor_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- curriculum_history definition

CREATE TABLE `curriculum_history` (
                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                      `created_at` datetime DEFAULT NULL,
                                      `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                      `deleted` bit(1) NOT NULL,
                                      `last_modified_at` datetime DEFAULT NULL,
                                      `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                      `branch` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                      `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                      `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                      `published` bit(1) NOT NULL,
                                      `source_id` bigint DEFAULT NULL,
                                      `history_id` bigint unsigned DEFAULT NULL,
                                      PRIMARY KEY (`id`),
                                      KEY `FK3ew3mxf1e969ll21wwjf5e639` (`source_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- lesson_history definition

CREATE TABLE `lesson_history` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `created_at` datetime DEFAULT NULL,
                                  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `deleted` bit(1) NOT NULL,
                                  `last_modified_at` datetime DEFAULT NULL,
                                  `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `end_of_applicable_days` int NOT NULL,
                                  `modules` json NOT NULL,
                                  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `questionnaire_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `sms_questionnaire_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `stage` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `start_of_applicable_days` int NOT NULL,
                                  `curriculum_id` bigint DEFAULT NULL,
                                  `source_id` bigint DEFAULT NULL,
                                  `questionnaire_id` bigint DEFAULT NULL,
                                  `history_id` bigint unsigned DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKb2m3bre39wl0j8bf0aiapuqgi` (`curriculum_id`) USING BTREE,
                                  KEY `FKjkbkfiipill91ovdg4w61qwy0` (`source_id`) USING BTREE,
                                  KEY `m_lesson_o_questionnaire` (`questionnaire_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- module_history definition

CREATE TABLE `module_history` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `created_at` datetime DEFAULT NULL,
                                  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `deleted` bit(1) NOT NULL,
                                  `last_modified_at` datetime DEFAULT NULL,
                                  `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `branch` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `components` json DEFAULT NULL,
                                  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `published` bit(1) NOT NULL,
                                  `topic` int NOT NULL,
                                  `version_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                  `history_id` bigint unsigned DEFAULT NULL,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- questionnaire_history definition

CREATE TABLE `questionnaire_history` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                         `questions` json NOT NULL,
                                         `branch` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                         `published` bit(1) NOT NULL,
                                         `source_id` bigint DEFAULT NULL,
                                         `created_at` datetime DEFAULT NULL,
                                         `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                         `deleted` bit(1) NOT NULL,
                                         `last_modified_at` datetime DEFAULT NULL,
                                         `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                         `history_id` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
                                         PRIMARY KEY (`id`),
                                         KEY `source_id` (`source_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- tag_history definition

CREATE TABLE `tag_history` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                               `history_id` bigint unsigned DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- user_history definition

CREATE TABLE `user_history` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `created_at` datetime DEFAULT NULL,
                                `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `deleted` bit(1) NOT NULL,
                                `last_modified_at` datetime DEFAULT NULL,
                                `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `password_hash` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                `chw_id` bigint DEFAULT NULL,
                                `last_modified_password_at` datetime DEFAULT NULL,
                                `history_id` bigint unsigned DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FK2s48epbxv6n5pbvgc9dwb6ue0` (`chw_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- visit_history definition

CREATE TABLE `visit_history` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `created_at` datetime DEFAULT NULL,
                                 `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `deleted` bit(1) NOT NULL,
                                 `last_modified_at` datetime DEFAULT NULL,
                                 `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `complete_time` datetime DEFAULT NULL,
                                 `month` int NOT NULL,
                                 `next_module_index` int NOT NULL,
                                 `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `status` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                 `visit_time` datetime NOT NULL,
                                 `year` int NOT NULL,
                                 `baby_id` bigint NOT NULL,
                                 `lesson_id` bigint NOT NULL,
                                 `day` int NOT NULL,
                                 `chw_id` bigint NOT NULL,
                                 `start_time` datetime DEFAULT NULL,
                                 `complete_chw_id` bigint DEFAULT NULL,
                                 `delete_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                 `history_id` bigint unsigned DEFAULT NULL,
                                 `distance` decimal(20,6) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FK1vrf57uxfypegehkhk0m7l8j1` (`complete_chw_id`) USING BTREE,
                                 KEY `FK2j2kupipmgku1fbm89jdh0epw` (`chw_id`) USING BTREE,
                                 KEY `FKmumitrl8l8mvbpbgne6s0eyvi` (`baby_id`) USING BTREE,
                                 KEY `FKpm8tem62xkvub1ulcbqil6qqj` (`lesson_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- visit_report_history definition

CREATE TABLE `visit_report_history` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `visit_report_obj_data` json NOT NULL,
                                        `created_at` datetime DEFAULT NULL,
                                        `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                        `deleted` bit(1) NOT NULL,
                                        `last_modified_at` datetime DEFAULT NULL,
                                        `last_modified_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                        `visit_id` bigint NOT NULL,
                                        `history_id` bigint unsigned DEFAULT NULL,
                                        PRIMARY KEY (`id`),
                                        KEY `v_r_visit` (`visit_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;