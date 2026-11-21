package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.QuestionAttempt;

import java.time.Instant;

public record QuestionAttemptProgress(
        Long questionId,
        Integer position,
        Long selectedOptionId,
        boolean answered,
        Boolean isCorrect,
        Instant answeredAt) {

    public static QuestionAttemptProgress from(QuestionAttempt attempt) {
        return new QuestionAttemptProgress(
                attempt.getQuestionId(),
                attempt.getQuestion().getPosition(),
                attempt.getSelectedOptionId(),
                attempt.getAnsweredAt() != null,
                attempt.getCorrect(),
                attempt.getAnsweredAt());
    }
}
