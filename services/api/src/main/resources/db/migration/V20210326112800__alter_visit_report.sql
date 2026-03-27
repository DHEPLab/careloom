ALTER TABLE visit_report ADD COLUMN visit_id BIGINT(20) NOT NULL;
ALTER TABLE visit_report ADD CONSTRAINT v_r_visit FOREIGN KEY (visit_id) REFERENCES visit(id);