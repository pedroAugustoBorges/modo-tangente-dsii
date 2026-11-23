package br.edu.fatec.modotangente.service;

import java.util.List;

/** Didactic context resolved by the backend for the question linked to a conversation. */
public record QuizTutorContext(
        String topicTitle,
        String topicDescription,
        String quizTitle,
        String quizDescription,
        String quizInstructions,
        String questionStatement,
        String studyGuidance,
        List<String> options,
        String selectedOptionText,
        Boolean answerCorrect) {

    public QuizTutorContext {
        options = options == null ? List.of() : List.copyOf(options);
        if ((selectedOptionText == null) != (answerCorrect == null)) {
            throw new IllegalArgumentException("A resposta e seu resultado devem estar ambos presentes ou ausentes.");
        }
    }
}
