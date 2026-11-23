-- Older file-backed H2 databases were initialized by Hibernate with UNIQUE (quiz_id, position).
-- Hibernate update retains removed constraints. Locate its generated name before updating the mapping.
EXECUTE IMMEDIATE COALESCE((
    SELECT 'ALTER TABLE public.quiz_questions DROP CONSTRAINT "' || REPLACE(c.constraint_name, '"', '""') || '"'
    FROM information_schema.table_constraints c
    JOIN information_schema.key_column_usage k
      ON k.constraint_schema = c.constraint_schema AND k.constraint_name = c.constraint_name
    WHERE LOWER(c.table_schema) = 'public' AND LOWER(c.table_name) = 'quiz_questions'
      AND c.constraint_type = 'UNIQUE'
    GROUP BY c.constraint_name
    HAVING LISTAGG(LOWER(k.column_name), ',') WITHIN GROUP (ORDER BY k.ordinal_position) = 'quiz_id,position'
), 'SET @catalog_schema_current = TRUE');
