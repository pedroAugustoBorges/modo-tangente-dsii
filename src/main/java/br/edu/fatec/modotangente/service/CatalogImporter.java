package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.CatalogImport;
import br.edu.fatec.modotangente.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogImporter {
    public record Batch(@NotBlank @Size(max = 500) String source, @NotEmpty List<@NotNull @Valid Section> topics) {}
    public record Section(@NotNull @Valid TopicData topic, @NotEmpty List<@NotNull @Valid Lesson> quizzes) {}
    public record Lesson(@NotBlank @Size(max = 180) String title, String description, String instructions,
                         @NotNull @PositiveOrZero Integer displayOrder,
                         @NotEmpty List<@NotNull @Valid QuestionData> questions) {}

    private final ObjectMapper json;
    private final Validator validator;
    private final EntityManager entities;
    private final TopicCatalogService topics;
    private final QuizCatalogService quizzes;
    private final QuestionCatalogService questions;

    public CatalogImporter(ObjectMapper json, Validator validator, EntityManager entities, TopicCatalogService topics,
                           QuizCatalogService quizzes, QuestionCatalogService questions) {
        this.json = json;
        this.validator = validator;
        this.entities = entities;
        this.topics = topics;
        this.quizzes = quizzes;
        this.questions = questions;
    }

    @Transactional
    public boolean load(byte[] content) throws IOException, NoSuchAlgorithmException {
        Batch batch = json.readValue(content, Batch.class);
        var errors = validator.validate(batch);
        if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
        String checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        CatalogImport prior = entities.find(CatalogImport.class, batch.source());
        if (prior != null) {
            if (checksum.equals(prior.getSha256())) return false;
            throw new IllegalArgumentException("Origem já importada com outro conteúdo: " + batch.source());
        }
        entities.persist(new CatalogImport(batch.source(), checksum));
        entities.flush();
        for (Section section : batch.topics()) {
            Long topicId = topics.save(null, section.topic()).id();
            for (Lesson lesson : section.quizzes()) {
                Long quizId = quizzes.save(null, new QuizData(topicId, lesson.title(), lesson.description(),
                        lesson.instructions(), lesson.displayOrder(), true)).id();
                for (QuestionData question : lesson.questions()) questions.save(quizId, null, question);
            }
        }
        return true;
    }
}
