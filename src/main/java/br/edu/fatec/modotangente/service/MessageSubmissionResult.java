package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.web.dto.MessageExchangeResponse;
import org.springframework.http.HttpStatus;

public record MessageSubmissionResult(HttpStatus status, MessageExchangeResponse body) {
}
