UPDATE carer
SET family_ties =
        CASE
            WHEN family_ties = 'GRANDMOTHER' THEN 'PATERNAL_GRANDMOTHER'
            WHEN family_ties = 'GRANDFATHER' THEN 'MATERNAL_GRANDMOTHER'
            WHEN family_ties = 'GRANDMA' THEN 'PATERNAL_GRANDFATHER'
            WHEN family_ties = 'GRANDPA' THEN 'MATERNAL_GRANDFATHER'
            ELSE family_ties
            END;
