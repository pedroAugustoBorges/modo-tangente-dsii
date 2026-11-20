package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.Quiz;
import br.edu.fatec.modotangente.domain.QuizAttempt;
import br.edu.fatec.modotangente.repository.QuizAttemptRepository;
import br.edu.fatec.modotangente.repository.QuizRepository;
import br.edu.fatec.modotangente.web.dto.QuizAttemptSummary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;

    public QuizAttemptService(QuizAttemptRepository attemptRepository, QuizRepository quizRepository) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
    }

    @Transactional
    public QuizAttemptSummary start(Long userId, Long quizId) {
        Quiz quiz = quizRepository.findByIdAndActiveTrue(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz não encontrado."));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizId(quiz.getId());
        attempt.setStatus("IN_PROGRESS");
        return QuizAttemptSummary.from(attemptRepository.save(attempt));
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptSummary> listForUser(Long userId) {
        return attemptRepository.findAllByUserIdOrderByStartedAtDesc(userId).stream()
                .map(QuizAttemptSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizAttemptSummary findOwned(Long userId, Long attemptId) {
        return attemptRepository.findByIdAndUserId(attemptId, userId)
                .map(QuizAttemptSummary::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada."));
    }
}
