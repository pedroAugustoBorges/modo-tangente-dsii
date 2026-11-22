package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.web.dto.ConversationSummary;

public record ConversationAccessResult(ConversationSummary conversation, boolean created) {
}
