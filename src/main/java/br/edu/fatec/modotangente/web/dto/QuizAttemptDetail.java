package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.QuestionAttempt;
import br.edu.fatec.modotangente.domain.QuizAttempt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record QuizAttemptDetail(
        Long id,
        Long quizId,
        String status,
        BigDecimal score,
        Instant startedAt,
        Instant finishedAt,
        List<QuestionAttemptProgress> questionAttempts) {

    public static QuizAttemptDetail from(QuizAttempt attempt, List<QuestionAttempt> questionAttempts) {
        return new QuizAttemptDetail(
                attempt.getId(),
                attempt.getQuizId(),
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getStartedAt(),
                attempt.getFinishedAt(),
                questionAttempts.stream().map(QuestionAttemptProgress::from).toList());
    }
}
