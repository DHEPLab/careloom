ALTER TABLE questionnaire_record ADD COLUMN title_no VARCHAR(50) NOT NULL ;
ALTER TABLE visit DROP record_ids;
ALTER TABLE questionnaire_record ADD COLUMN visit_id BIGINT(20) NOT NULL ;
ALTER TABLE questionnaire_record ADD CONSTRAINT m_qr_o_visit FOREIGN KEY (visit_id) REFERENCES `visit`(`id`);
