package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.*;
import br.edu.fatec.modotangente.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class DatabaseQuizContextProvider implements QuizContextProvider {
    private final QuestionAttemptRepository attempts;
    private final QuizQuestionRepository questions;
    private final QuizQuestionOptionRepository options;

    public DatabaseQuizContextProvider(
            QuestionAttemptRepository attempts,
            QuizQuestionRepository questions,
            QuizQuestionOptionRepository options) {
        this.attempts = attempts;
        this.questions = questions;
        this.options = options;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizTutorContext loadForQuestionAttempt(Long questionAttemptId) {
        QuestionAttempt attempt = attempts.findById(questionAttemptId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        Long questionId = attempt.getQuestionId();
        QuizQuestion question = questions.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        Quiz quiz = question.getQuiz();
        Topic topic = quiz.getTopic();
        String selectedOptionText = attempt.getSelectedOptionId() == null
                ? null
                : options.findByIdAndQuestionId(attempt.getSelectedOptionId(), questionId)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND))
                        .getOptionText();
        return new QuizTutorContext(topic.getTitle(), topic.getDescription(), quiz.getTitle(), quiz.getDescription(),
                quiz.getInstructions(), question.getStatement(), question.getStudyGuidance(),
                options.findByQuestionIdOrderByPositionAsc(questionId).stream()
                        .map(QuizQuestionOption::getOptionText).toList(),
                selectedOptionText,
                selectedOptionText == null ? null : attempt.getCorrect());
    }
}
