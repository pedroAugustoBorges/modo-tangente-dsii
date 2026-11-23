package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.*;
import br.edu.fatec.modotangente.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class DatabaseQuizContextProvider implements QuizContextProvider {
    private final QuizQuestionRepository questions;
    private final QuizQuestionOptionRepository options;

    public DatabaseQuizContextProvider(QuizQuestionRepository questions, QuizQuestionOptionRepository options) {
        this.questions = questions;
        this.options = options;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizTutorContext loadForQuestion(Long questionId) {
        QuizQuestion question = questions.findById(questionId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        Quiz quiz = question.getQuiz();
        Topic topic = quiz.getTopic();
        return new QuizTutorContext(topic.getTitle(), topic.getDescription(), quiz.getTitle(), quiz.getDescription(),
                quiz.getInstructions(), question.getStatement(), question.getStudyGuidance(),
                options.findByQuestionIdOrderByPositionAsc(questionId).stream()
                        .map(QuizQuestionOption::getOptionText).toList());
    }
}
