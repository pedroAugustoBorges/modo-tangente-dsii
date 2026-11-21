package br.edu.fatec.modotangente.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AnswerQuestionRequest(
        @NotNull @Positive Long selectedOptionId) {
}
