SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for baby
-- ----------------------------
CREATE TABLE `baby` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `action_from_app` varchar(10) DEFAULT NULL,
  `approved` bit(1) NOT NULL,
  `area` varchar(100) NOT NULL,
  `assisted_food` bit(1) NOT NULL,
  `birthday` date DEFAULT NULL,
  `close_account_reason` varchar(100) DEFAULT NULL,
  `edc` date DEFAULT NULL,
  `feeding_pattern` varchar(20) DEFAULT NULL,
  `gender` varchar(10) NOT NULL,
  `identity` varchar(50) DEFAULT NULL,
  `location` varchar(200) NOT NULL,
  `name` varchar(10) NOT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `stage` varchar(10) NOT NULL,
  `chw_id` bigint(20) DEFAULT NULL,
  `curriculum_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK52xwhd26ynrhd0mi6qdck6ux7` (`chw_id`),
  KEY `FKclftxyb8c1dwqgfiv41s2dprv` (`curriculum_id`),
  CONSTRAINT `FK52xwhd26ynrhd0mi6qdck6ux7` FOREIGN KEY (`chw_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKclftxyb8c1dwqgfiv41s2dprv` FOREIGN KEY (`curriculum_id`) REFERENCES `curriculum` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for carer
-- ----------------------------
CREATE TABLE `carer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `family_ties` varchar(20) DEFAULT NULL,
  `master_carer` bit(1) NOT NULL,
  `name` varchar(10) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `wechat` varchar(20) DEFAULT NULL,
  `baby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKesxg0gdragoyd8atftlvisw4o` (`baby_id`),
  CONSTRAINT `FKesxg0gdragoyd8atftlvisw4o` FOREIGN KEY (`baby_id`) REFERENCES `baby` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for community_house_worker
-- ----------------------------
CREATE TABLE `community_house_worker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `identity` varchar(50) NOT NULL,
  `tags` json DEFAULT NULL,
  `supervisor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKehn3fk7ktkpxj40h9242dptof` (`supervisor_id`),
  CONSTRAINT `FKehn3fk7ktkpxj40h9242dptof` FOREIGN KEY (`supervisor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for curriculum
-- ----------------------------
CREATE TABLE `curriculum` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `branch` varchar(10) NOT NULL,
  `description` varchar(200) NOT NULL,
  `name` varchar(20) NOT NULL,
  `published` bit(1) NOT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3ew3mxf1e969ll21wwjf5e639` (`source_id`),
  CONSTRAINT `FK3ew3mxf1e969ll21wwjf5e639` FOREIGN KEY (`source_id`) REFERENCES `curriculum` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for lesson
-- ----------------------------
CREATE TABLE `lesson` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `end_of_applicable_days` int(11) NOT NULL,
  `modules` json NOT NULL,
  `name` varchar(20) NOT NULL,
  `number` varchar(20) NOT NULL,
  `questionnaire_address` varchar(100) DEFAULT NULL,
  `sms_questionnaire_address` varchar(100) DEFAULT NULL,
  `stage` varchar(10) NOT NULL,
  `start_of_applicable_days` int(11) NOT NULL,
  `curriculum_id` bigint(20) DEFAULT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb2m3bre39wl0j8bf0aiapuqgi` (`curriculum_id`),
  KEY `FKjkbkfiipill91ovdg4w61qwy0` (`source_id`),
  CONSTRAINT `FKb2m3bre39wl0j8bf0aiapuqgi` FOREIGN KEY (`curriculum_id`) REFERENCES `curriculum` (`id`),
  CONSTRAINT `FKjkbkfiipill91ovdg4w61qwy0` FOREIGN KEY (`source_id`) REFERENCES `lesson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for lesson_schedule
-- ----------------------------
CREATE TABLE `lesson_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `end_of_applicable_months` int(11) NOT NULL,
  `lessons` json NOT NULL,
  `name` varchar(20) NOT NULL,
  `stage` varchar(10) NOT NULL,
  `start_of_applicable_months` int(11) NOT NULL,
  `curriculum_id` bigint(20) DEFAULT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4fk1rjggt3hvfoqgn8ub9pwat` (`curriculum_id`),
  KEY `FKkkx1q5iabfo735g1anx30n8e6` (`source_id`),
  CONSTRAINT `FK4fk1rjggt3hvfoqgn8ub9pwat` FOREIGN KEY (`curriculum_id`) REFERENCES `curriculum` (`id`),
  CONSTRAINT `FKkkx1q5iabfo735g1anx30n8e6` FOREIGN KEY (`source_id`) REFERENCES `lesson_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for module
-- ----------------------------
CREATE TABLE `module` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `branch` varchar(10) NOT NULL,
  `components` json DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `name` varchar(40) NOT NULL,
  `number` varchar(20) NOT NULL,
  `published` bit(1) NOT NULL,
  `topic` int(11) NOT NULL,
  `version_key` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for user
-- ----------------------------
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `last_modified_password_at` datetime DEFAULT NULL,
  `password_hash` varchar(60) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `role` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `chw_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`),
  KEY `FK2s48epbxv6n5pbvgc9dwb6ue0` (`chw_id`),
  CONSTRAINT `FK2s48epbxv6n5pbvgc9dwb6ue0` FOREIGN KEY (`chw_id`) REFERENCES `community_house_worker` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for visit
-- ----------------------------
CREATE TABLE `visit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `complete_time` datetime DEFAULT NULL,
  `day` int(11) NOT NULL,
  `month` int(11) NOT NULL,
  `next_module_index` int(11) NOT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `status` varchar(15) NOT NULL,
  `visit_time` datetime NOT NULL,
  `year` int(11) NOT NULL,
  `baby_id` bigint(20) NOT NULL,
  `chw_id` bigint(20) NOT NULL,
  `lesson_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmumitrl8l8mvbpbgne6s0eyvi` (`baby_id`),
  KEY `FK2j2kupipmgku1fbm89jdh0epw` (`chw_id`),
  KEY `FKpm8tem62xkvub1ulcbqil6qqj` (`lesson_id`),
  CONSTRAINT `FK2j2kupipmgku1fbm89jdh0epw` FOREIGN KEY (`chw_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKmumitrl8l8mvbpbgne6s0eyvi` FOREIGN KEY (`baby_id`) REFERENCES `baby` (`id`),
  CONSTRAINT `FKpm8tem62xkvub1ulcbqil6qqj` FOREIGN KEY (`lesson_id`) REFERENCES `lesson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- -----------------------------
-- init admin user
-- -----------------------------
INSERT INTO `user` VALUES (1, NOW(), 'ANONYMOUS', b'0', NOW(), 'ANONYMOUS', NULL, '$2a$10$pv3kUd1uuwVB4OLflRpwQOPB7FA/7FZra4Xf5ZnrL.uf9SR3STh4m', '18888888888', 'Administrator', 'ROLE_ADMIN', 'admin', NULL);
