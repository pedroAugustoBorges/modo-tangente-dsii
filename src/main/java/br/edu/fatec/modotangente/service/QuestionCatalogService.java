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

    private final QuizQuestionOptionRepository options;

    public QuestionCatalogService(QuizRepository quizzes, QuizQuestionRepository questions,
                                  QuestionAttemptRepository attempts, QuizQuestionOptionRepository options) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.attempts = attempts;
        this.options = options;
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
        validateOptions(data);
        if (data.active() && questions.findByQuizIdAndPositionAndActiveTrue(quizId, data.position())
                .filter(existing -> !existing.getId().equals(id)).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Já existe uma questão publicada nesta posição.");
        }
        QuizQuestion question = id == null ? new QuizQuestion() : requireQuestion(quizId, id);
        if (id != null && attempts.existsByQuestionId(id)) {
            question.setActive(false);
            questions.flush();
            question = new QuizQuestion();
        } else if (id != null) {
            options.deleteAll(options.findByQuestionIdOrderByPositionAsc(id));
            options.flush();
        }
        question.setQuizId(quizId);
        question.setPosition(data.position());
        question.setStatement(data.statement());
        question.setStudyGuidance(data.studyGuidance());
        question.setPoints(data.points());
        question.setActive(data.active());
        QuizQuestion saved = questions.save(question);
        for (OptionData input : data.options()) {
            QuizQuestionOption option = new QuizQuestionOption();
            option.setQuestionId(saved.getId());
            option.setPosition(input.position());
            option.setOptionText(input.text());
            option.setCorrect(input.correct());
            options.save(option);
        }
        return QuestionView.from(saved);
    }

    public List<OptionAdminView> listOptions(Long quizId, Long id) {
        requireQuestion(quizId, id);
        return options.findByQuestionIdOrderByPositionAsc(id).stream().map(OptionAdminView::from).toList();
    }

    private void validateOptions(QuestionData data) {
        long positions = data.options().stream().map(OptionData::position).distinct().count();
        long correct = data.options().stream().filter(OptionData::correct).count();
        if (positions != data.options().size() || correct > 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Use posições distintas e no máximo uma correta.");
        }
        if (data.active() && (positions < 2 || correct != 1)) {
            throw new ResponseStatusException(BAD_REQUEST, "Publicação exige duas alternativas ou mais e uma correta.");
        }
    }

    @Transactional
    public void deactivate(Long quizId, Long id) {
        lockQuiz(quizId);
        requireQuestion(quizId, id).setActive(false);
    }
}
