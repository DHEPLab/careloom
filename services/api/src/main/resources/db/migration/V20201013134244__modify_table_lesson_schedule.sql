ALTER TABLE `lesson_schedule`
ADD COLUMN `start_of_applicable_days` int(11) NOT NULL,
ADD COLUMN `end_of_applicable_days` int(11) NOT NULL,
DROP COLUMN `start_of_applicable_months`,
DROP COLUMN `end_of_applicable_months`;