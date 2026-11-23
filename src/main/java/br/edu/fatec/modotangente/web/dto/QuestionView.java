package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.QuizQuestion;
import java.math.BigDecimal;

public record QuestionView(Long id, Long quizId, Integer position, String statement,
                           String studyGuidance, BigDecimal points, boolean active) {
    public static QuestionView from(QuizQuestion question) {
        return new QuestionView(question.getId(), question.getQuizId(), question.getPosition(),
                question.getStatement(), question.getStudyGuidance(), question.getPoints(), question.getActive());
    }
}
