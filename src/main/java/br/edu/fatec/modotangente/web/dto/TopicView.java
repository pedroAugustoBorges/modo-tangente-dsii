package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.Topic;

public record TopicView(Long id, String title, String description, Integer displayOrder, Boolean active) {
    public static TopicView from(Topic value) {
        return new TopicView(value.getId(), value.getTitle(), value.getDescription(), value.getDisplayOrder(), value.getActive());
    }
}
