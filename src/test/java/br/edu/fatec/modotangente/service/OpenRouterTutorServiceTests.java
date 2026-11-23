package br.edu.fatec.modotangente.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenRouterTutorServiceTests {

    private static final String API_KEY = "test-secret-never-log";
    private static final Long QUESTION_ID = 42L;

    private QuizContextProvider contextProvider;
    private MockRestServiceServer server;
    private OpenRouterTutorService service;

    @BeforeEach
    void setUp() {
        contextProvider = mock(QuizContextProvider.class);
        when(contextProvider.loadForQuestion(QUESTION_ID)).thenReturn(testContext());

        RestClient.Builder builder = RestClient.builder().baseUrl("https://openrouter.test/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new OpenRouterTutorService(
                builder.build(), contextProvider, API_KEY, "test/provider-model", 600, 8, 12_000);
    }

    @AfterEach
    void verifyRequests() {
        server.verify();
    }

    @Test
    void sendsBackendContextAndHistoryWithTheConfiguredAuthorizationAndLimits() {
        server.expect(requestTo("https://openrouter.test/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("test/provider-model"))
                .andExpect(jsonPath("$.max_tokens").value(600))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[0].content").value(org.hamcrest.Matchers.containsString("Spring Boot")))
                .andExpect(jsonPath("$.messages[0].content").value(org.hamcrest.Matchers.containsString("Alternativas (sem indicação de correção)")))
                .andExpect(jsonPath("$.messages[0].content").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("1. Alternativa correta"))))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(jsonPath("$.messages[2].role").value("assistant"))
                .andExpect(jsonPath("$.messages[3].role").value("user"))
                .andExpect(jsonPath("$.messages[3].content").value("Explique injeção de dependência."))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"É o Spring entregando a dependência pronta."}}]}
                        """, MediaType.APPLICATION_JSON));

        String reply = service.generateReply(QUESTION_ID, List.of(
                new TutorService.TutorTurn("STUDENT", "O que é um bean?"),
                new TutorService.TutorTurn("TUTOR", "Um objeto gerenciado pelo Spring."),
                new TutorService.TutorTurn("STUDENT", "Explique injeção de dependência.")));

        assertEquals("É o Spring entregando a dependência pronta.", reply);
        verify(contextProvider).loadForQuestion(QUESTION_ID);
    }

    @Test
    void sendsOnlyTheLastEightCompleteExchangesAndKeepsTheCurrentQuestion() {
        List<TutorService.TutorTurn> history = new ArrayList<>();
        for (int exchange = 0; exchange < 10; exchange++) {
            history.add(new TutorService.TutorTurn("STUDENT", "pergunta-antiga-" + exchange));
            history.add(new TutorService.TutorTurn("TUTOR", "resposta-antiga-" + exchange));
        }
        history.add(new TutorService.TutorTurn("STUDENT", "pergunta-atual"));

        server.expect(requestTo("https://openrouter.test/api/v1/chat/completions"))
                .andExpect(jsonPath("$.messages[*].content", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("pergunta-antiga-0"))))
                .andExpect(jsonPath("$.messages[*].content", org.hamcrest.Matchers.hasItem("pergunta-antiga-2")))
                .andExpect(jsonPath("$.messages[*].content", org.hamcrest.Matchers.hasItem("resposta-antiga-9")))
                .andExpect(jsonPath("$.messages[*].content", org.hamcrest.Matchers.hasItem("pergunta-atual")))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"Resposta"}}]}
                        """, MediaType.APPLICATION_JSON));

        service.generateReply(QUESTION_ID, history);
    }

    @Test
    void rejectsEmptyProviderContentWithoutReturningAnInvalidReply() {
        server.expect(requestTo("https://openrouter.test/api/v1/chat/completions"))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"  "}}]}
                        """, MediaType.APPLICATION_JSON));

        OpenRouterTutorService.TutorProviderException exception = assertThrows(
                OpenRouterTutorService.TutorProviderException.class,
                () -> service.generateReply(QUESTION_ID, currentQuestion()));

        assertTrue(exception.getMessage().contains("resposta vazia"));
        assertFalse(exception.getMessage().contains(API_KEY));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 429, 503})
    void sanitizesProviderStatusErrors(int status) {
        server.expect(requestTo("https://openrouter.test/api/v1/chat/completions"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.valueOf(status))
                        .body("provider error containing " + API_KEY));

        OpenRouterTutorService.TutorProviderException exception = assertThrows(
                OpenRouterTutorService.TutorProviderException.class,
                () -> service.generateReply(QUESTION_ID, currentQuestion()));

        assertFalse(exception.getMessage().contains(API_KEY));
        assertFalse(exception.getMessage().contains("provider error"));
    }

    @Test
    void sanitizesMalformedProviderJson() {
        server.expect(requestTo("https://openrouter.test/api/v1/chat/completions"))
                .andRespond(withSuccess("not-json", MediaType.APPLICATION_JSON));

        OpenRouterTutorService.TutorProviderException exception = assertThrows(
                OpenRouterTutorService.TutorProviderException.class,
                () -> service.generateReply(QUESTION_ID, currentQuestion()));

        assertTrue(exception.getMessage().contains("resposta inválida"));
        assertFalse(exception.getMessage().contains("not-json"));
    }

    @Test
    void sanitizesTransportTimeouts() {
        server.expect(requestTo("https://openrouter.test/api/v1/chat/completions"))
                .andRespond(withException(new IOException("timeout " + API_KEY)));

        OpenRouterTutorService.TutorProviderException exception = assertThrows(
                OpenRouterTutorService.TutorProviderException.class,
                () -> service.generateReply(QUESTION_ID, currentQuestion()));

        assertFalse(exception.getMessage().contains(API_KEY));
        assertFalse(exception.getMessage().contains("timeout"));
    }

    @Test
    void sanitizesTimeoutAndNeverCallsProviderWhenCredentialIsMissing() {
        OpenRouterTutorService withoutKey = new OpenRouterTutorService(
                RestClient.builder().baseUrl("https://openrouter.test/api/v1").build(),
                contextProvider, "", "test/provider-model", 600, 8, 12_000);
        OpenRouterTutorService.TutorProviderException missingKey = assertThrows(
                OpenRouterTutorService.TutorProviderException.class,
                () -> withoutKey.generateReply(QUESTION_ID, currentQuestion()));
        assertTrue(missingKey.getMessage().contains("não está configurada"));
    }

    @Test
    void rejectsAnInputThatCannotFitWithoutDroppingQuestionOrDidacticContext() {
        OpenRouterTutorService smallBudget = new OpenRouterTutorService(
                RestClient.builder().baseUrl("https://openrouter.test/api/v1").build(),
                contextProvider, API_KEY, "test/provider-model", 600, 8, 100);

        OpenRouterTutorService.TutorProviderException exception = assertThrows(
                OpenRouterTutorService.TutorProviderException.class,
                () -> smallBudget.generateReply(QUESTION_ID, currentQuestion()));

        assertTrue(exception.getMessage().contains("excedem o limite"));
    }

    private static List<TutorService.TutorTurn> currentQuestion() {
        return List.of(new TutorService.TutorTurn("STUDENT", "Explique injeção de dependência."));
    }

    private static QuizTutorContext testContext() {
        return new QuizTutorContext(
                "Spring Boot", "Framework para aplicações Java", "Quiz de Spring", "Quiz introdutório",
                "Considere o contexto da aplicação", "O que é injeção de dependência?", "Revise IoC e beans",
                List.of("Instanciar tudo manualmente", "Receber dependências do container"));
    }
}
