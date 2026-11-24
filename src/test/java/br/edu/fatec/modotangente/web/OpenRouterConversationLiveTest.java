package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import br.edu.fatec.modotangente.service.QuestionCatalogService;
import br.edu.fatec.modotangente.service.QuizCatalogService;
import br.edu.fatec.modotangente.service.TopicCatalogService;
import br.edu.fatec.modotangente.web.dto.OptionAdminView;
import br.edu.fatec.modotangente.web.dto.OptionData;
import br.edu.fatec.modotangente.web.dto.QuestionData;
import br.edu.fatec.modotangente.web.dto.QuizData;
import br.edu.fatec.modotangente.web.dto.SendMessageRequest;
import br.edu.fatec.modotangente.web.dto.TopicData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Explicit live check: set OPENROUTER_LIVE_TEST=true and OPENROUTER_API_KEY to run.
 * It exercises the authenticated HTTP API against isolated H2 data and makes one real provider request.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "OPENROUTER_LIVE_TEST", matches = "true")
class OpenRouterConversationLiveTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Environment environment;
    @Autowired AppUserRepository accounts;
    @Autowired TopicCatalogService topics;
    @Autowired QuizCatalogService quizzes;
    @Autowired QuestionCatalogService questions;

    @Test
    void answersAQuestionWithItsPersistedCorrectnessAndReturnsTheLiveTutorReply() throws Exception {
        assertTrue(environment.getProperty("openrouter.api-key", "").length() > 0,
                "O teste live precisa de uma chave OpenRouter configurada, sem imprimi-la.");

        String email = "openrouter-live-" + UUID.randomUUID() + "@example.com";
        AppUser student = new AppUser();
        student.setEmail(email);
        student.setFullName("Aluno de teste OpenRouter");
        student.setPasswordHash("unused-in-mock-auth");
        student.setRole("STUDENT");
        student.setActive(true);
        accounts.saveAndFlush(student);

        Long topicId = topics.save(null, new TopicData(
                "Spring Boot — teste OpenRouter", "Injeção de dependência e IoC", 0, true)).id();
        Long quizId = quizzes.save(null, new QuizData(
                topicId, "Quiz de injeção de dependência", "Questão de validação",
                "Responda e depois converse com o tutor sobre a questão.", 0, true)).id();
        Long questionId = questions.save(quizId, null, new QuestionData(
                1,
                "No Spring Boot, qual mecanismo permite que o container forneça uma dependência a um componente sem que ele a instancie manualmente?",
                "Relacione IoC com a colaboração entre serviço e repositório.",
                BigDecimal.ONE,
                true,
                List.of(
                        new OptionData(1, "Injeção de dependências", true),
                        new OptionData(2, "Herança de classe", false)))).id();
        Long correctOptionId = questions.listOptions(quizId, questionId).stream()
                .filter(OptionAdminView::correct)
                .findFirst()
                .orElseThrow()
                .id();

        String studentAuth = email;
        JsonNode attempt = json.readTree(mvc.perform(post("/api/quizzes/{quizId}/attempts", quizId)
                        .with(user(studentAuth)).with(csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        long attemptId = attempt.path("id").asLong();

        mvc.perform(post("/api/attempts/{attemptId}/questions/{questionId}/answer", attemptId, questionId)
                        .with(user(studentAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedOptionId\":" + correctOptionId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true));

        JsonNode conversation = json.readTree(mvc.perform(post(
                                "/api/attempts/{attemptId}/questions/{questionId}/conversation", attemptId, questionId)
                        .with(user(studentAuth)).with(csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        long conversationId = conversation.path("id").asLong();

        String question = "Acertei ao selecionar Injeção de dependências? Explique como o Spring Boot entrega um repositório a um serviço, em até três frases.";
        JsonNode exchange = json.readTree(mvc.perform(post(
                                "/api/conversations/{conversationId}/messages", conversationId)
                        .with(user(studentAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SendMessageRequest(question, UUID.randomUUID()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentMessage.llmStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.tutorMessage.senderRole").value("TUTOR"))
                .andReturn().getResponse().getContentAsString());

        String reply = exchange.path("tutorMessage").path("content").asText();
        assertFalse(reply.isBlank(), "O OpenRouter deve retornar uma resposta textual.");
        System.out.println("Resposta real do tutor OpenRouter:\n" + reply);
    }
}
