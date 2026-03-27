UPDATE baby
SET stage =
    CASE
        WHEN stage = 'EDC' THEN 'UNBORN'
        WHEN stage = 'BIRTH' THEN 'BORN'
        ELSE stage
        END;
