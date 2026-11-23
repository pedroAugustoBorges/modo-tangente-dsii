package br.edu.fatec.modotangente.web.dto;

import jakarta.validation.constraints.*;

public record TopicData(
        @NotBlank @Size(max = 160) String title,
         String description,
        @NotNull @PositiveOrZero Integer displayOrder,
        @NotNull Boolean active) { }
