package br.edu.fatec.modotangente.service;

/** Backend contract for resolving the persisted context of a question attempt. */
public interface QuizContextProvider {
    QuizTutorContext loadForQuestionAttempt(Long questionAttemptId);
}
