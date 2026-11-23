package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.*;
import br.edu.fatec.modotangente.repository.*;
import br.edu.fatec.modotangente.web.dto.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional(readOnly = true)
public class StudentCatalogService {
    private final TopicRepository topics;
    private final QuizRepository quizzes;
    private final QuizQuestionRepository questions;
    private final QuizQuestionOptionRepository options;
    private final QuestionAttemptRepository progress;

    public StudentCatalogService(TopicRepository topics, QuizRepository quizzes, QuizQuestionRepository questions,
                                 QuizQuestionOptionRepository options, QuestionAttemptRepository progress) {
        this.topics = topics;
        this.quizzes = quizzes;
        this.questions = questions;
        this.options = options;
        this.progress = progress;
    }

    public List<QuizView> quizzes(Long topicId) {
        topics.findByIdAndActiveTrue(topicId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        return quizzes.findByTopicIdAndActiveTrueOrderByDisplayOrderAscIdAsc(topicId).stream()
                .map(QuizView::from).toList();
    }

    private List<QuizQuestion> publishedQuestions(Long quizId) {
        quizzes.findByIdAndActiveTrueAndTopicActiveTrue(quizId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        return questions.findByQuizIdAndActiveTrueOrderByPositionAsc(quizId);
    }

    public List<StudentQuestion> questions(Long quizId) {
        List<QuizQuestion> published = publishedQuestions(quizId);
        if (published.isEmpty()) return List.of();
        List<Long> ids = published.stream().map(QuizQuestion::getId).toList();
        Map<Long, List<QuizQuestionOption>> byQuestion = options.findByQuestionIdInOrderByPositionAsc(ids).stream()
                .collect(Collectors.groupingBy(QuizQuestionOption::getQuestionId));
        return published.stream().map(q -> StudentQuestion.from(q, byQuestion.getOrDefault(q.getId(), List.of())))
                .toList();
    }

    public StudentQuestion historical(Long userId, Long attemptId, Long questionId) {
        QuestionAttempt entry = progress.findByQuizAttemptIdAndQuestionIdAndQuizAttemptUserId(attemptId, questionId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        return StudentQuestion.from(entry.getQuestion(), options.findByQuestionIdOrderByPositionAsc(questionId));
    }

    @Transactional
    public List<QuizQuestionProvider.QuestionReference> listActiveForAttempt(Long quizId) {
        // Hold the same lock as the editor until the enclosing attempt transaction commits its references.
        quizzes.lockForCatalog(quizId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        List<QuizQuestion> published = publishedQuestions(quizId);
        if (published.isEmpty()) throw new ResponseStatusException(CONFLICT, "Quiz sem questões publicadas.");
        return published.stream().map(q -> new QuizQuestionProvider.QuestionReference(q.getId(), q.getPosition())).toList();
    }
}
