ALTER TABLE baby_modify_record DROP COLUMN `baby_json`,
ADD COLUMN `new_baby_json` varchar(4096),
ADD COLUMN `old_baby_json` varchar(4096);

ALTER TABLE carer_modify_record DROP COLUMN `carer_json`,
ADD COLUMN `new_carer_json` varchar(4096),
ADD COLUMN `old_carer_json` varchar(4096);