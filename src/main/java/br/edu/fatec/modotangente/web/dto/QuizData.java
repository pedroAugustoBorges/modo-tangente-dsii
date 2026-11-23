package br.edu.fatec.modotangente.web.dto;

import jakarta.validation.constraints.*;

public record QuizData(
        @NotNull @Positive Long topicId,
        @NotBlank @Size(max = 180) String title,
        String description,
        String instructions,
        @NotNull @PositiveOrZero Integer displayOrder,
        @NotNull Boolean active) { }
