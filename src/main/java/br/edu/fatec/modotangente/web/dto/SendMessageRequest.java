package br.edu.fatec.modotangente.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendMessageRequest(
        @NotBlank String content,
        @NotNull UUID requestId) {
}
