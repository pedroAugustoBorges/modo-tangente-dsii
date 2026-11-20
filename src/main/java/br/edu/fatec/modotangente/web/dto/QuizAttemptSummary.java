package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.QuizAttempt;

import java.math.BigDecimal;
import java.time.Instant;

public record QuizAttemptSummary(
        Long id,
        Long quizId,
        String status,
        BigDecimal score,
        Instant startedAt,
        Instant finishedAt) {

    public static QuizAttemptSummary from(QuizAttempt attempt) {
        return new QuizAttemptSummary(
                attempt.getId(),
                attempt.getQuizId(),
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getStartedAt(),
                attempt.getFinishedAt());
    }
}
