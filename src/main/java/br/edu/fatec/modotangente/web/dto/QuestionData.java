package br.edu.fatec.modotangente.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record QuestionData(
        @NotNull @Positive Integer position,
        @NotBlank String statement,
        String studyGuidance,
        @NotNull @PositiveOrZero @Digits(integer = 5, fraction = 2) BigDecimal points,
        @NotNull Boolean active,
        @NotNull java.util.List<@NotNull @jakarta.validation.Valid OptionData> options) { }
