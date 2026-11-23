package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.QuizQuestion;
import br.edu.fatec.modotangente.domain.QuizQuestionOption;
import java.math.BigDecimal;
import java.util.List;

public record StudentQuestion(Long id, Long quizId, Integer position, String statement,
                              BigDecimal points, List<Option> options) {
    public record Option(Long id, Integer position, String text) { }

    public static StudentQuestion from(QuizQuestion question, List<QuizQuestionOption> options) {
        return new StudentQuestion(question.getId(), question.getQuizId(), question.getPosition(),
                question.getStatement(), question.getPoints(), options.stream()
                .map(option -> new Option(option.getId(), option.getPosition(), option.getOptionText())).toList());
    }
}
