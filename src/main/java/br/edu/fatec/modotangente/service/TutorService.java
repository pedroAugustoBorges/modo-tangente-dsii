package br.edu.fatec.modotangente.service;

import java.util.List;

/** Backend boundary for generating a tutor reply; the browser never supplies context. */
public interface TutorService {

    String generateReply(Long questionId, List<TutorTurn> history);

    record TutorTurn(String senderRole, String content) {
    }
}
