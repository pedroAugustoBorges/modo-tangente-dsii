ALTER TABLE messages
    ADD COLUMN processing_started_at TIMESTAMPTZ;

UPDATE messages
SET processing_started_at = created_at
WHERE sender_role = 'STUDENT'
  AND llm_status = 'PROCESSING';
