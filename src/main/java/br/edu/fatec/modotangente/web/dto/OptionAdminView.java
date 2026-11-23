package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.QuizQuestionOption;

public record OptionAdminView(Long id, Integer position, String text, boolean correct) {
    public static OptionAdminView from(QuizQuestionOption option) {
        return new OptionAdminView(option.getId(), option.getPosition(), option.getOptionText(), option.getCorrect());
    }
}
