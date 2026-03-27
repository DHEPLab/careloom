ALTER TABLE baby MODIFY COLUMN feeding_pattern VARCHAR(50);
UPDATE baby
SET feeding_pattern =
        CASE
            WHEN feeding_pattern = 'BREAST_MILK' THEN 'EXCLUSIVE_BREASTFEEDING'
            WHEN feeding_pattern = 'MILK_POWDER' THEN 'FORMULA_FEEDING'
            WHEN feeding_pattern = 'MIXED' THEN 'MIXED_BREAST_FORMULA_FEEDING'
            WHEN feeding_pattern = 'TERMINATED' THEN 'NO_BREAST_FORMULA_FEEDING'
            ELSE feeding_pattern
            END;
