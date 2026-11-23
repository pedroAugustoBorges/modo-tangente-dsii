-- Drafts and retired versions retain their original position in the historical quiz.
ALTER TABLE public.quiz_questions DROP CONSTRAINT quiz_questions_quiz_id_position_key;
CREATE UNIQUE INDEX ux_published_question_position
    ON public.quiz_questions (quiz_id, position) WHERE active;
