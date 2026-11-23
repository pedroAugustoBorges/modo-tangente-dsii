package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.*;
import br.edu.fatec.modotangente.repository.*;
import br.edu.fatec.modotangente.web.dto.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional(readOnly = true)
public class QuestionCatalogService {
    private final QuizRepository quizzes;
    private final QuizQuestionRepository questions;
    private final QuestionAttemptRepository attempts;

    public QuestionCatalogService(QuizRepository quizzes, QuizQuestionRepository questions,
                                  QuestionAttemptRepository attempts) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.attempts = attempts;
    }

    public List<QuestionView> list(Long quizId) {
        if (!quizzes.existsById(quizId)) throw new ResponseStatusException(NOT_FOUND);
        return questions.findByQuizIdOrderByPositionAscIdAsc(quizId).stream().map(QuestionView::from).toList();
    }

    public QuestionView get(Long quizId, Long id) {
        return QuestionView.from(requireQuestion(quizId, id));
    }

    private QuizQuestion requireQuestion(Long quizId, Long id) {
        return questions.findByIdAndQuizId(id, quizId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    private void lockQuiz(Long quizId) {
        quizzes.lockForCatalog(quizId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @Transactional
    public QuestionView save(Long quizId, Long id, QuestionData data) {
        lockQuiz(quizId);
        QuizQuestion question = id == null ? new QuizQuestion() : requireQuestion(quizId, id);
        if (id != null && attempts.existsByQuestionId(id)) {
            question.setActive(false);
            questions.flush();
            question = new QuizQuestion();
        }
        question.setQuizId(quizId);
        question.setPosition(data.position());
        question.setStatement(data.statement());
        question.setStudyGuidance(data.studyGuidance());
        question.setPoints(data.points());
        question.setActive(false);
        return QuestionView.from(questions.save(question));
    }

    @Transactional
    public void deactivate(Long quizId, Long id) {
        lockQuiz(quizId);
        requireQuestion(quizId, id).setActive(false);
    }
}
