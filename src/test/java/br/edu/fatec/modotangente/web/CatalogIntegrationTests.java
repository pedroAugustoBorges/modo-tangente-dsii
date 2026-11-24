package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.domain.Topic;
import br.edu.fatec.modotangente.repository.*;
import br.edu.fatec.modotangente.service.*;
import br.edu.fatec.modotangente.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogIntegrationTests {

    @Test
    void upgradesTheLegacyLocalPositionConstraintWithoutLosingHistory() throws Exception {
        var script = new ClassPathResource("db/local/allow_historical_question_positions.sql");
        String url = "jdbc:h2:mem:legacy_positions;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
        try (var connection = java.sql.DriverManager.getConnection(url);
             var statement = connection.createStatement()) {
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection, script);
            statement.execute("create table quiz_questions(id bigint primary key, quiz_id bigint, position int,"
                    + " unique(quiz_id, position), unique(id, quiz_id))");
            statement.execute("insert into quiz_questions values (1, 10, 1)");
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection, script);
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection, script);
            assertEquals(1, statement.executeUpdate("insert into quiz_questions values (2, 10, 1)"));
            try (var rows = statement.executeQuery("select count(*) from quiz_questions")) {
                assertTrue(rows.next());
                assertEquals(2, rows.getInt(1));
            }
        }
    }

    @Test
    void updatesAndDeactivatesCatalogEntriesAndEditsOptionsInDrafts() {
        topics.save(topicId, new TopicData("Spring", "Novo tópico", 3, true));
        assertEquals("Spring", topics.get(topicId).title());
        quizzes.save(quizId, new QuizData(topicId, "Nova edição", null, "Instruções", 2, true));
        assertEquals("Nova edição", quizzes.get(quizId).title());
        Long id = questions.save(quizId, null, published(1, "Original")).id();
        var draft = new QuestionData(2, "Outro texto", null, BigDecimal.TEN, false,
                List.of(new OptionData(1, "Alternativa em revisão", false)));
        assertEquals(id, questions.save(quizId, id, draft).id());
        assertEquals(List.of("Alternativa em revisão"), questions.listOptions(quizId, id).stream()
                .map(OptionAdminView::text).toList());
        questions.save(quizId, id, published(3, "Publicada"));
        questions.deactivate(quizId, id);
        assertFalse(questions.get(quizId, id).active());
        quizzes.deactivate(quizId);
        assertFalse(quizzes.get(quizId).active());
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AppUserRepository accounts;
    @Autowired TopicCatalogService topics;
    @Autowired QuizCatalogService quizzes;
    @Autowired QuestionCatalogService questions;
    @Autowired QuizQuestionProvider references;
    @Autowired QuizContextProvider context;
    @Autowired QuizAttemptService attempts;
    @Autowired QuestionAttemptRepository questionAttempts;
    @Autowired CatalogImporter importer;
    @Autowired TopicRepository topicRows;

    private AppUser student;
    private AppUser admin;
    private Long topicId;
    private Long quizId;

    @BeforeEach
    void setUp() {
        student = account("STUDENT");
        admin = account("ADMIN");
        topicId = topics.save(null, new TopicData("Java", null, 1, true)).id();
        quizId = quizzes.save(null, new QuizData(topicId, "Quiz", null, "Leia", 1, true)).id();
    }

    private AppUser account(String role) {
        var account = new AppUser();
        account.setEmail(UUID.randomUUID() + "@example.com");
        account.setFullName(role);
        account.setRole(role);
        account.setPasswordHash("unused");
        return accounts.save(account);
    }

    private QuestionData published(int position, String text) {
        return new QuestionData(position, text, "Revisar conceitos", BigDecimal.ONE, true,
                List.of(new OptionData(2, "B", true), new OptionData(1, "A", false)));
    }

    @Test
    void protectsAdministrationAndValidatesRelations() throws Exception {
        String payload = json.writeValueAsString(new QuizData(topicId, "Novo", null, null, 0, true));
        mvc.perform(post("/api/admin/quizzes").with(user(student.getEmail())).with(csrf())
                .contentType("application/json").content(payload)).andExpect(status().isForbidden());
        var authorized = user(admin.getEmail()).roles("ADMIN");
        mvc.perform(post("/api/admin/quizzes").with(authorized).contentType("application/json").content(payload))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/quizzes").with(authorized).with(csrf()).contentType("application/json").content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.topicId").value(topicId));
        mvc.perform(post("/api/admin/quizzes").with(authorized).with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(new QuizData(Long.MAX_VALUE, "X", null, null, 0, true))))
                .andExpect(status().isNotFound());
        Long q = questions.save(quizId, null, published(1, "Original")).id();
        mvc.perform(get("/api/admin/quizzes/{quiz}/questions/{id}", Long.MAX_VALUE, q).with(authorized))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/admin/topics/{id}", topicId).with(authorized).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/topics/{id}/quizzes", topicId).with(user(student.getEmail())))
                .andExpect(status().isNotFound());
        assertThrows(ResponseStatusException.class, () -> references.listActiveForAttempt(quizId));
    }

    @Test
    void validatesPublicationBeforeReplacingTheStoredOptions() throws Exception {
        Long id = questions.save(quizId, null, published(1, "Primeira")).id();
        for (var invalid : List.of(List.of(new OptionData(1, "A", true)),
                List.of(new OptionData(1, "A", false), new OptionData(2, "B", false)),
                List.of(new OptionData(1, "A", true), new OptionData(2, "B", true)),
                List.of(new OptionData(1, "A", true), new OptionData(1, "B", false)))) {
            var change = new QuestionData(1, "Revisada", null, BigDecimal.ONE, true, invalid);
            mvc.perform(put("/api/admin/quizzes/{quiz}/questions/{id}", quizId, id)
                    .with(user(admin.getEmail()).roles("ADMIN")).with(csrf())
                    .contentType("application/json").content(json.writeValueAsBytes(change)))
                    .andExpect(status().isBadRequest());
        }
        assertEquals("Primeira", questions.get(quizId, id).statement());
        assertEquals(2, questions.listOptions(quizId, id).size());
        assertThrows(ResponseStatusException.class, () -> questions.save(quizId, null, published(1, "Conflito")));
    }

    @Test
    void versionsUsedQuestionsAndKeepsOriginalContentAvailableOnlyToTheOwner() throws Exception {
        Long second = questions.save(quizId, null, published(2, "Segunda")).id();
        Long first = questions.save(quizId, null, published(1, "Primeira")).id();
        questions.save(quizId, null, new QuestionData(3, "Rascunho", null, BigDecimal.ZERO, false, List.of()));
        assertEquals(List.of(new QuizQuestionProvider.QuestionReference(first, 1),
                new QuizQuestionProvider.QuestionReference(second, 2)), references.listActiveForAttempt(quizId));
        Long attemptId = attempts.start(student.getId(), quizId).id();
        Long originalOption = questions.listOptions(quizId, first).get(1).id();
        Long replacement = questions.save(quizId, first, published(1, "Revisada")).id();
        assertNotEquals(first, replacement);
        assertTrue(attempts.answer(student.getId(), attemptId, first, originalOption).isCorrect());
        mvc.perform(get("/api/quizzes/{id}/questions", quizId).with(user(student.getEmail())))
                .andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(replacement))
                .andExpect(jsonPath("$[0].options[0].text").value("A"))
                .andExpect(jsonPath("$..correct").doesNotExist()).andExpect(jsonPath("$..is_correct").doesNotExist())
                .andExpect(jsonPath("$..studyGuidance").doesNotExist());
        mvc.perform(get("/api/attempts/{a}/questions/{q}", attemptId, first).with(user(student.getEmail())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statement").value("Primeira"));
        mvc.perform(get("/api/attempts/{a}/questions/{q}", attemptId, first).with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isNotFound());
        Long questionAttemptId = questionAttempts.findByQuizAttemptIdAndQuestionId(attemptId, first)
                .orElseThrow().getId();
        assertEquals(new QuizTutorContext("Java", null, "Quiz", null, "Leia", "Primeira", "Revisar conceitos",
                List.of("A", "B"), "B", true), context.loadForQuestionAttempt(questionAttemptId));
    }

    @Test
    void sendsDatabaseBackedQuizContextAndAnswerResultToOpenRouterWithoutSendingTheAnswerKey() {
        Long questionId = questions.save(quizId, null, published(1,
                "Explique o ciclo de vida de um componente Spring.")).id();
        Long attemptId = attempts.start(student.getId(), quizId).id();
        Long correctOptionId = questions.listOptions(quizId, questionId).stream()
                .filter(OptionAdminView::correct)
                .findFirst()
                .orElseThrow()
                .id();
        assertTrue(attempts.answer(student.getId(), attemptId, questionId, correctOptionId).isCorrect());
        Long questionAttemptId = questionAttempts.findByQuizAttemptIdAndQuestionId(attemptId, questionId)
                .orElseThrow().getId();

        RestClient.Builder clientBuilder = RestClient.builder().baseUrl("https://openrouter.test/api/v1");
        MockRestServiceServer server = MockRestServiceServer.bindTo(clientBuilder).build();
        OpenRouterTutorService tutor = new OpenRouterTutorService(
                clientBuilder.build(), context, "test-secret-never-log", "test/provider-model", 300, 0, 12_000);

        server.expect(requestTo("https://openrouter.test/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockRestRequestMatchers.jsonPath("$.model").value("test/provider-model"))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[0].content")
                        .value(org.hamcrest.Matchers.containsString("Tópico: Java")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[0].content")
                        .value(org.hamcrest.Matchers.containsString("Quiz: Quiz")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[0].content")
                        .value(org.hamcrest.Matchers.containsString(
                                "Questão: Explique o ciclo de vida de um componente Spring.")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[0].content")
                        .value(org.hamcrest.Matchers.containsString("Orientação de estudo: Revisar conceitos")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[0].content")
                        .value(org.hamcrest.Matchers.containsString("1. A")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[0].content")
                        .value(org.hamcrest.Matchers.containsString("2. B")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[0].content")
                        .value(org.hamcrest.Matchers.containsString(
                                "Resposta selecionada pelo aluno: B\nResultado registrado pelo backend: correta.")))
                .andExpect(MockRestRequestMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("\"correct\""))))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[1].role").value("user"))
                .andExpect(MockRestRequestMatchers.jsonPath("$.messages[1].content")
                        .value("Acertei ao escolher essa resposta? Como isso se relaciona com o Spring Boot?"))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"O Spring Boot configura a aplicação e o Spring gerencia os componentes."}}]}
                        """, MediaType.APPLICATION_JSON));

        String reply = tutor.generateReply(questionAttemptId, List.of(
                new TutorService.TutorTurn("STUDENT", "Acertei ao escolher essa resposta? Como isso se relaciona com o Spring Boot?")));

        assertEquals("O Spring Boot configura a aplicação e o Spring gerencia os componentes.", reply);
        server.verify();
    }

    @Test
    void importsOnceAndRollsBackInvalidBatches() throws Exception {
        byte[] seed;
        try (var input = new ClassPathResource("catalog/spring-ioc.json").getInputStream()) {
            seed = input.readAllBytes();
        }
        long before = topicRows.count();
        assertTrue(importer.load(seed));
        assertFalse(importer.load(seed));
        String source = new String(seed, StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> importer.load((source + " ").getBytes(StandardCharsets.UTF_8)));
        var invalid = source.replace("testContext", "invalid").replace("\"correct\": true", "\"correct\": false");
        assertThrows(ResponseStatusException.class, () -> importer.load(invalid.getBytes(StandardCharsets.UTF_8)));
        assertEquals(before + 1, topicRows.count());
    }

    @Test
    void importsSpringBootAndSpringWebAsSeparateTopicsAndServesTheirQuizThroughSpringWeb() throws Exception {
        byte[] baseCatalog;
        byte[] springWebCatalog;
        try (var input = new ClassPathResource("catalog/dsii-fundamentos-v1.json").getInputStream()) {
            baseCatalog = input.readAllBytes();
        }
        try (var input = new ClassPathResource("catalog/spring-web-v1.json").getInputStream()) {
            springWebCatalog = input.readAllBytes();
        }

        long topicsBeforeImport = topicRows.count();
        assertTrue(importer.load(baseCatalog));
        assertTrue(importer.load(springWebCatalog));
        assertFalse(importer.load(baseCatalog));
        assertFalse(importer.load(springWebCatalog));
        assertEquals(topicsBeforeImport + 6, topicRows.count());

        mvc.perform(get("/api/topics").with(user(student.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..title", hasItem("Spring Boot")))
                .andExpect(jsonPath("$..title", hasItem("Spring Web (Spring MVC)")));

        Long springWebTopicId = topicRows.findAll().stream()
                .filter(topic -> "Spring Web (Spring MVC)".equals(topic.getTitle()))
                .map(Topic::getId)
                .findFirst()
                .orElseThrow();
        var quizzesForSpringWeb = json.readTree(mvc.perform(get("/api/topics/{topicId}/quizzes", springWebTopicId)
                        .with(user(student.getEmail())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertEquals(1, quizzesForSpringWeb.size());

        mvc.perform(get("/api/quizzes/{quizId}/questions", quizzesForSpringWeb.get(0).path("id").asLong())
                        .with(user(student.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", org.hamcrest.Matchers.is(5)))
                .andExpect(jsonPath("$..correct").doesNotExist())
                .andExpect(jsonPath("$..is_correct").doesNotExist());
    }
}
