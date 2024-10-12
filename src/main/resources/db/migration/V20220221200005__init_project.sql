INSERT INTO project (name, code, created_at, created_by, last_modified_at, last_modified_by, deleted) VALUES('健康未来', 'health_future', '2022-02-21 10:12:23', 'admin', NULL, NULL, 0);

ALTER TABLE baby ADD project_id BIGINT UNSIGNED DEFAULT 0 NULL;

ALTER TABLE baby_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE carer ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE carer_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE community_house_worker ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE community_house_worker_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE curriculum ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE curriculum_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE lesson ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE lesson_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE questionnaire ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE questionnaire_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE module ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE module_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE user ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE user_history ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE visit ADD project_id BIGINT UNSIGNED NULL;

ALTER TABLE visit_history ADD project_id BIGINT UNSIGNED NULL;

