package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.Quiz;

public record QuizView(Long id, Long topicId, String title, String description, String instructions, Integer displayOrder, Boolean active) {
    public static QuizView from(Quiz value) {
        return new QuizView(value.getId(), value.getTopicId(), value.getTitle(), value.getDescription(), value.getInstructions(), value.getDisplayOrder(), value.getActive());
    }
}
