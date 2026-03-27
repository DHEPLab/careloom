ALTER TABLE `carer_modify_record` DROP `carer_json`,
CHANGE `change_column` `changed_column` VARCHAR(255),
ADD `new_carer_json` VARCHAR(4096),
ADD `old_carer_json` VARCHAR(4096);