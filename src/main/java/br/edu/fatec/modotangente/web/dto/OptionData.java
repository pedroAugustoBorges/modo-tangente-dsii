package br.edu.fatec.modotangente.web.dto;

import jakarta.validation.constraints.*;

public record OptionData(@NotNull @Positive Integer position, @NotBlank String text, @NotNull Boolean correct) { }
