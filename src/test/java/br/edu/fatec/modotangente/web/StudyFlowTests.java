package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import br.edu.fatec.modotangente.service.QuestionCatalogService;
import br.edu.fatec.modotangente.service.QuizCatalogService;
import br.edu.fatec.modotangente.service.TopicCatalogService;
import br.edu.fatec.modotangente.service.TutorService;
import br.edu.fatec.modotangente.web.dto.OptionData;
import br.edu.fatec.modotangente.web.dto.QuestionData;
import br.edu.fatec.modotangente.web.dto.QuizData;
import br.edu.fatec.modotangente.web.dto.SendMessageRequest;
import br.edu.fatec.modotangente.web.dto.TopicData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudyFlowTests {

    @Autowired MockMvc mvc;
    @Autowired AppUserRepository accounts;
    @Autowired PasswordEncoder passwords;
    @Autowired TopicCatalogService topics;
    @Autowired QuizCatalogService quizzes;
    @Autowired QuestionCatalogService questions;
    @Autowired ObjectMapper json;
    @MockitoBean TutorService tutor;

    private MockHttpSession student;
    private long topicId;
    private long quizId;

    @BeforeEach
    void prepareStudySession() throws Exception {
        student = login();
        topicId = topics.save(null, new TopicData("Tópico de integração", null, 1, true)).id();
        quizId = quizzes.save(null, new QuizData(topicId, "Quiz de integração", null, "Leia as questões.", 1, true))
                .id();
    }

    @Test
    void showsCatalogAndProtectsStudyPagesAndForms() throws Exception {
        mvc.perform(get("/estudo")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
        mvc.perform(get("/").session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/estudo\"")));
        mvc.perform(get("/estudo").session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("/estudo/topicos/" + topicId)));
        mvc.perform(get("/estudo/topicos/{id}", topicId).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Quiz de integração")));
        mvc.perform(get("/estudo/topicos/{id}", Long.MAX_VALUE).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Este tópico não está disponível.")));
        mvc.perform(get("/estudo/quizzes/{id}", quizId).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Este quiz ainda não tem questões publicadas.")));
        mvc.perform(post("/estudo/quizzes/{id}/tentativas", quizId).session(student))
                .andExpect(status().isForbidden());
    }

    @Test
    void answersAndResumesAttemptsWithoutRevealingTheAnswerKey() throws Exception {
        long first = publishQuestion(1, "Primeira questão");
        long second = publishQuestion(2, "Segunda questão");
        String attempt = startAttempt();
        String question = attempt + "/questoes/" + first;

        mvc.perform(get(attempt).session(student)).andExpect(redirectedUrl(question));
        mvc.perform(get(question).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("0 respondidas")))
                .andExpect(content().string(containsString("Alternativa A")))
                .andExpect(content().string(not(containsString("isCorrect"))))
                .andExpect(content().string(not(containsString("Resposta correta."))));

        var invalid = mvc.perform(post(question + "/resposta").session(student).with(csrf())
                        .param("optionId", Long.toString(Long.MAX_VALUE)))
                .andExpect(redirectedUrl(question)).andReturn();
        mvc.perform(get(question).session(student).flashAttrs(invalid.getFlashMap())).andExpect(status().isOk())
                .andExpect(content().string(containsString("A alternativa não está disponível nesta questão.")));

        long optionId = questions.listOptions(quizId, first).get(1).id();
        mvc.perform(post(question + "/resposta").session(student).with(csrf())
                        .param("optionId", Long.toString(optionId)))
                .andExpect(redirectedUrl(question));
        mvc.perform(get(question).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Resposta correta.")))
                .andExpect(content().string(containsString("1 respondidas")));
        mvc.perform(get(attempt).session(student)).andExpect(redirectedUrl(attempt + "/questoes/" + second));
        mvc.perform(get(question).session(login())).andExpect(status().isNotFound());
    }

    @Test
    void reopensQuestionHistoryAndRetriesWithoutDuplicatingMessages() throws Exception {
        long first = publishQuestion(1, "Primeira questão do chat");
        long second = publishQuestion(2, "Segunda questão do chat");
        String attempt = startAttempt();
        String chat = attempt + "/questoes/" + first + "/chat";
        mvc.perform(post(chat).session(student).with(csrf())).andExpect(redirectedUrl(chat));
        mvc.perform(get(chat).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ainda não há mensagens")))
                .andExpect(content().string(containsString("/js/study-chat.js")));
        mvc.perform(get("/js/study-chat.js").session(student)).andExpect(status().isOk());

        String attemptId = attempt.substring(attempt.lastIndexOf('/') + 1);
        String conversation = mvc.perform(get("/api/attempts/{attempt}/questions/{question}/conversation",
                        attemptId, first).session(student)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long conversationId = json.readTree(conversation).path("id").asLong();
        UUID requestId = UUID.randomUUID();
        when(tutor.generateReply(any(), any()))
                .thenThrow(new IllegalStateException("Falha simulada"))
                .thenReturn("Resposta recuperada");

        mvc.perform(send(conversationId, "Dúvida da primeira questão", requestId))
                .andExpect(status().isBadGateway());
        mvc.perform(get(chat).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("data-retry-id=\"" + requestId + "\"")))
                .andExpect(content().string(containsString("Dúvida da primeira questão")));
        mvc.perform(send(conversationId, "Dúvida da primeira questão", requestId)).andExpect(status().isOk());
        mvc.perform(send(conversationId, "Dúvida da primeira questão", requestId)).andExpect(status().isOk());
        verify(tutor, times(2)).generateReply(any(), any());
        String history = mvc.perform(get(chat).session(student)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Resposta recuperada")))
                .andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals(1, history.split("Resposta recuperada", -1).length - 1);

        String otherChat = attempt + "/questoes/" + second + "/chat";
        mvc.perform(post(otherChat).session(student).with(csrf())).andExpect(redirectedUrl(otherChat));
        mvc.perform(get(otherChat).session(student)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Dúvida da primeira questão"))));
        mvc.perform(get(chat).session(login())).andExpect(status().isNotFound());
    }

    private MockHttpSession login() throws Exception {
        var account = new AppUser();
        account.setEmail(UUID.randomUUID() + "@example.com");
        account.setFullName("Estudante");
        account.setRole("STUDENT");
        account.setPasswordHash(passwords.encode("senha-de-teste"));
        accounts.save(account);
        return (MockHttpSession) mvc.perform(post("/login").with(csrf())
                        .param("email", account.getEmail()).param("password", "senha-de-teste"))
                .andExpect(redirectedUrl("/")).andReturn().getRequest().getSession(false);
    }

    private long publishQuestion(int position, String title) {
        return questions.save(quizId, null, new QuestionData(position, title, null, BigDecimal.ONE, true,
                List.of(new OptionData(1, "Alternativa A", false), new OptionData(2, "Alternativa B", true))))
                .id();
    }

    private String startAttempt() throws Exception {
        return mvc.perform(post("/estudo/quizzes/{id}/tentativas", quizId).session(student).with(csrf()))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
    }

    private MockHttpServletRequestBuilder send(long conversationId, String message, UUID requestId)
            throws Exception {
        return post("/api/conversations/{id}/messages", conversationId).session(student).with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new SendMessageRequest(message, requestId)));
    }
}
