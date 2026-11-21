package br.edu.fatec.modotangente.service;

import java.util.List;

/**
 * Read-only contract used when a quiz attempt snapshots its active questions.
 * The catalog owner can implement this contract without coupling attempt logic
 * to catalog CRUD.
 */
public interface QuizQuestionProvider {

    List<QuestionReference> listActiveForAttempt(Long quizId);

    record QuestionReference(Long questionId, Integer position) {
        public QuestionReference {
            if (questionId == null || questionId <= 0) {
                throw new IllegalArgumentException("questionId precisa ser positivo.");
            }
            if (position == null || position <= 0) {
                throw new IllegalArgumentException("position precisa ser positivo.");
            }
        }
    }
}
