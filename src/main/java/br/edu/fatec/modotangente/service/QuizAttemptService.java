package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.QuestionAttempt;
import br.edu.fatec.modotangente.domain.QuizAttempt;
import br.edu.fatec.modotangente.domain.QuizQuestionOption;
import br.edu.fatec.modotangente.repository.QuestionAttemptRepository;
import br.edu.fatec.modotangente.repository.QuizAttemptRepository;
import br.edu.fatec.modotangente.repository.QuizQuestionOptionRepository;
import br.edu.fatec.modotangente.repository.QuizRepository;
import br.edu.fatec.modotangente.web.dto.QuestionAttemptProgress;
import br.edu.fatec.modotangente.web.dto.QuizAttemptDetail;
import br.edu.fatec.modotangente.web.dto.QuizAttemptSummary;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final QuizQuestionOptionRepository optionRepository;
    private final QuizQuestionProvider questionProvider;

    public QuizAttemptService(
            QuizAttemptRepository attemptRepository,
            QuizRepository quizRepository,
            QuestionAttemptRepository questionAttemptRepository,
            QuizQuestionOptionRepository optionRepository,
            QuizQuestionProvider questionProvider) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.optionRepository = optionRepository;
        this.questionProvider = questionProvider;
    }

    @Transactional
    public QuizAttemptSummary start(Long userId, Long quizId) {
        quizRepository.findByIdAndActiveTrue(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz não encontrado."));
        return createAttempt(userId, quizId);
    }

    @Transactional
    public QuizAttemptSummary resumeOrStart(Long userId, Long topicId, Long quizId) {
        // Serialize lookup and creation with concurrent openings of the same quiz.
        quizRepository.lockForCatalog(quizId)
                .filter(quiz -> topicId.equals(quiz.getTopicId()) && Boolean.TRUE.equals(quiz.getActive())
                        && Boolean.TRUE.equals(quiz.getTopic().getActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz não encontrado."));

        return attemptRepository.findResumable(userId, quizId, PageRequest.of(0, 1)).stream()
                .findFirst().map(QuizAttemptSummary::from)
                .orElseGet(() -> createAttempt(userId, quizId));
    }

    private QuizAttemptSummary createAttempt(Long userId, Long quizId) {
        List<QuizQuestionProvider.QuestionReference> questions = questionProvider.listActiveForAttempt(quizId);
        if (questions == null) {
            throw new IllegalStateException("O catálogo retornou uma lista nula de questões.");
        }
        validateQuestionReferences(questions);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizId(quizId);
        attempt.setStatus("IN_PROGRESS");
        QuizAttempt savedAttempt = attemptRepository.save(attempt);

        List<QuestionAttempt> progress = questions.stream()
                .sorted(java.util.Comparator.comparing(QuizQuestionProvider.QuestionReference::position))
                .map(reference -> {
                    QuestionAttempt questionAttempt = new QuestionAttempt();
                    questionAttempt.setQuizAttemptId(savedAttempt.getId());
                    questionAttempt.setQuizId(quizId);
                    questionAttempt.setQuestionId(reference.questionId());
                    return questionAttempt;
                })
                .toList();
        questionAttemptRepository.saveAll(progress);

        return QuizAttemptSummary.from(savedAttempt);
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptSummary> listForUser(Long userId) {
        return attemptRepository.findAllByUserIdOrderByStartedAtDesc(userId).stream()
                .map(QuizAttemptSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizAttemptDetail findOwned(Long userId, Long attemptId) {
        QuizAttempt attempt = attemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada."));
        return QuizAttemptDetail.from(
                attempt,
                questionAttemptRepository.findAllForAttemptOrdered(attempt.getId()));
    }

    @Transactional
    public QuestionAttemptProgress answer(
            Long userId,
            Long attemptId,
            Long questionId,
            Long selectedOptionId) {
        QuizAttempt attempt = attemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada."));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tentativa não está aberta para respostas.");
        }

        QuestionAttempt questionAttempt = questionAttemptRepository
                .findForUpdate(attempt.getId(), questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada nesta tentativa."));

        if (questionAttempt.getAnsweredAt() != null) {
            if (selectedOptionId.equals(questionAttempt.getSelectedOptionId())) {
                return QuestionAttemptProgress.from(questionAttempt);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta questão já foi respondida nesta tentativa.");
        }

        QuizQuestionOption selectedOption = optionRepository.findByIdAndQuestionId(selectedOptionId, questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alternativa não encontrada para esta questão."));

        questionAttempt.setSelectedOptionId(selectedOption.getId());
        questionAttempt.setCorrect(Boolean.TRUE.equals(selectedOption.getCorrect()));
        questionAttempt.setAnsweredAt(Instant.now());

        return QuestionAttemptProgress.from(questionAttemptRepository.save(questionAttempt));
    }

    private void validateQuestionReferences(List<QuizQuestionProvider.QuestionReference> questions) {
        Set<Long> ids = new HashSet<>();
        Set<Integer> positions = new HashSet<>();
        for (QuizQuestionProvider.QuestionReference question : questions) {
            if (question == null
                    || !ids.add(question.questionId())
                    || !positions.add(question.position())) {
                throw new IllegalStateException("O catálogo retornou referências de questões duplicadas ou inválidas.");
            }
        }
    }
}
