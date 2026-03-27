ALTER TABLE lesson ADD COLUMN questionnaire_id BIGINT(20);
ALTER TABLE lesson ADD CONSTRAINT m_lesson_o_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES `questionnaire`(`id`) ;
