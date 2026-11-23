package br.edu.fatec.modotangente.service;

/**
 * Backend contract for resolving a question's topic, quiz, statement and alternatives.
 * The browser never supplies this context. The catalog module owns the production adapter.
 */
public interface QuizContextProvider {
    QuizTutorContext loadForQuestion(Long questionId);
}
