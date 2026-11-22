package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.domain.Conversation;
import br.edu.fatec.modotangente.domain.Message;
import br.edu.fatec.modotangente.domain.Quiz;
import br.edu.fatec.modotangente.domain.QuizAttempt;
import br.edu.fatec.modotangente.domain.QuestionAttempt;
import br.edu.fatec.modotangente.domain.QuizQuestion;
import br.edu.fatec.modotangente.domain.QuizQuestionOption;
import br.edu.fatec.modotangente.domain.Topic;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import br.edu.fatec.modotangente.repository.ConversationRepository;
import br.edu.fatec.modotangente.repository.MessageRepository;
import br.edu.fatec.modotangente.repository.QuestionAttemptRepository;
import br.edu.fatec.modotangente.repository.QuizAttemptRepository;
import br.edu.fatec.modotangente.repository.QuizQuestionOptionRepository;
import br.edu.fatec.modotangente.repository.QuizQuestionRepository;
import br.edu.fatec.modotangente.repository.QuizRepository;
import br.edu.fatec.modotangente.repository.TopicRepository;
import br.edu.fatec.modotangente.service.QuizQuestionProvider;
import br.edu.fatec.modotangente.service.TutorService;
import br.edu.fatec.modotangente.web.dto.SendMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizAttemptControllerTests {

    private static final String STUDENT_EMAIL = "pedro@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private ConversationRepository conversationRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private TopicRepository topicRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private QuizAttemptRepository attemptRepository;
    @Autowired private QuestionAttemptRepository questionAttemptRepository;
    @Autowired private QuizQuestionRepository questionRepository;
    @Autowired private QuizQuestionOptionRepository optionRepository;
    @MockitoBean private QuizQuestionProvider questionProvider;
    @MockitoBean private TutorService tutorService;

    private Long quizId;
    private Long firstQuestionId;
    private Long secondQuestionId;
    private Long firstCorrectOptionId;
    private Long firstIncorrectOptionId;
    private Long secondCorrectOptionId;

    @BeforeEach
    void createFixture() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        questionAttemptRepository.deleteAll();
        attemptRepository.deleteAll();

        AppUser student = appUserRepository.findByEmailIgnoreCase(STUDENT_EMAIL).orElseGet(() -> {
            AppUser user = new AppUser();
            user.setFullName("Pedro de Teste");
            user.setEmail(STUDENT_EMAIL);
            user.setPasswordHash("not-used-in-mock-auth");
            user.setRole("STUDENT");
            user.setActive(true);
            return appUserRepository.saveAndFlush(user);
        });
        Topic topic = new Topic();
        topic.setTitle("Spring Boot");
        topic.setDescription("Tópico de teste");
        topic.setDisplayOrder(0);
        topic.setActive(true);
        topic = topicRepository.saveAndFlush(topic);

        Quiz quiz = new Quiz();
        quiz.setTopicId(topic.getId());
        quiz.setTitle("Quiz de teste");
        quiz.setDisplayOrder(0);
        quiz.setActive(true);
        quizId = quizRepository.saveAndFlush(quiz).getId();

        QuizQuestion firstQuestion = createQuestion(quizId, 1, "Questão de teste 1");
        QuizQuestion secondQuestion = createQuestion(quizId, 2, "Questão de teste 2");
        firstQuestionId = firstQuestion.getId();
        secondQuestionId = secondQuestion.getId();

        firstCorrectOptionId = createOption(firstQuestionId, 1, "Alternativa correta 1", true).getId();
        firstIncorrectOptionId = createOption(firstQuestionId, 2, "Alternativa incorreta 1", false).getId();
        secondCorrectOptionId = createOption(secondQuestionId, 1, "Alternativa correta 2", true).getId();

        when(questionProvider.listActiveForAttempt(quizId)).thenReturn(List.of(
                new QuizQuestionProvider.QuestionReference(firstQuestionId, 1),
                new QuizQuestionProvider.QuestionReference(secondQuestionId, 2)));
        when(tutorService.generateReply(anyLong(), anyList())).thenAnswer(invocation -> {
            List<TutorService.TutorTurn> history = invocation.getArgument(1);
            return "Resposta de teste: " + history.get(history.size() - 1).content();
        });
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void createsAndListsOnlyTheAuthenticatedStudentsAttempt() throws Exception {
        AppUser otherStudent = new AppUser();
        otherStudent.setFullName("Outro estudante da lista");
        otherStudent.setEmail("lista-outro@example.com");
        otherStudent.setPasswordHash("not-used-in-mock-auth");
        otherStudent.setRole("STUDENT");
        otherStudent.setActive(true);
        otherStudent = appUserRepository.saveAndFlush(otherStudent);

        QuizAttempt otherAttempt = new QuizAttempt();
        otherAttempt.setUserId(otherStudent.getId());
        otherAttempt.setQuizId(quizId);
        otherAttempt.setStatus("IN_PROGRESS");
        attemptRepository.saveAndFlush(otherAttempt);

        MvcResult result = mockMvc.perform(post("/api/quizzes/{quizId}/attempts", quizId).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.quizId").value(quizId.intValue()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andReturn();

        Long attemptId = attemptId(result);
        mockMvc.perform(get("/api/attempts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..quizId", hasItem(quizId.intValue())))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/attempts/{attemptId}", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionAttempts.length()").value(2));
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void initializesOrderedProgressAndRecordsOneFinalAnswer() throws Exception {
        Long attemptId = startAttempt();

        mockMvc.perform(get("/api/attempts/{attemptId}", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionAttempts.length()").value(2))
                .andExpect(jsonPath("$.questionAttempts[0].questionId").value(firstQuestionId.intValue()))
                .andExpect(jsonPath("$.questionAttempts[0].answered").value(false))
                .andExpect(jsonPath("$.questionAttempts[1].questionId").value(secondQuestionId.intValue()))
                .andExpect(jsonPath("$.questionAttempts[0].correctOptionId").doesNotExist());

        mockMvc.perform(post(
                        "/api/attempts/{attemptId}/questions/{questionId}/answer",
                        attemptId,
                        firstQuestionId)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"selectedOptionId\":" + firstCorrectOptionId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(firstQuestionId.intValue()))
                .andExpect(jsonPath("$.selectedOptionId").value(firstCorrectOptionId.intValue()))
                .andExpect(jsonPath("$.answered").value(true))
                .andExpect(jsonPath("$.isCorrect").value(true));

        mockMvc.perform(post(
                        "/api/attempts/{attemptId}/questions/{questionId}/answer",
                        attemptId,
                        firstQuestionId)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"selectedOptionId\":" + firstCorrectOptionId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true));

        mockMvc.perform(post(
                        "/api/attempts/{attemptId}/questions/{questionId}/answer",
                        attemptId,
                        firstQuestionId)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"selectedOptionId\":" + firstIncorrectOptionId + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void rejectsAnOptionThatBelongsToAnotherQuestion() throws Exception {
        Long attemptId = startAttempt();

        mockMvc.perform(post(
                        "/api/attempts/{attemptId}/questions/{questionId}/answer",
                        attemptId,
                        firstQuestionId)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"selectedOptionId\":" + secondCorrectOptionId + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void lazilyCreatesOneConversationForEachQuestionAttempt() throws Exception {
        Long attemptId = startAttempt();
        org.junit.jupiter.api.Assertions.assertEquals(0, conversationRepository.count());

        String path = "/api/attempts/{attemptId}/questions/{questionId}/conversation";
        mockMvc.perform(get(path, attemptId, firstQuestionId))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(path, attemptId, Long.MAX_VALUE).with(csrf()))
                .andExpect(status().isNotFound());

        MvcResult created = mockMvc.perform(post(path, attemptId, firstQuestionId).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionAttemptId").isNumber())
                .andReturn();
        Long firstConversationId = conversationId(created);

        mockMvc.perform(post(path, attemptId, firstQuestionId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstConversationId.intValue()));

        mockMvc.perform(get(path, attemptId, firstQuestionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstConversationId.intValue()));

        Long secondAttemptId = startAttempt();
        mockMvc.perform(post(path, secondAttemptId, firstQuestionId).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.not(firstConversationId.intValue())));

        org.junit.jupiter.api.Assertions.assertEquals(2, conversationRepository.count());
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void doesNotRevealAConversationOwnedByAnotherStudent() throws Exception {
        AppUser otherStudent = new AppUser();
        otherStudent.setFullName("Outro aluno");
        otherStudent.setEmail("outro@example.com");
        otherStudent.setPasswordHash("not-used-in-mock-auth");
        otherStudent.setRole("STUDENT");
        otherStudent.setActive(true);
        otherStudent = appUserRepository.saveAndFlush(otherStudent);

        QuizAttempt otherAttempt = new QuizAttempt();
        otherAttempt.setUserId(otherStudent.getId());
        otherAttempt.setQuizId(quizId);
        otherAttempt.setStatus("IN_PROGRESS");
        otherAttempt = attemptRepository.saveAndFlush(otherAttempt);

        QuestionAttempt otherQuestionAttempt = new QuestionAttempt();
        otherQuestionAttempt.setQuizAttemptId(otherAttempt.getId());
        otherQuestionAttempt.setQuizId(quizId);
        otherQuestionAttempt.setQuestionId(firstQuestionId);
        otherQuestionAttempt = questionAttemptRepository.saveAndFlush(otherQuestionAttempt);

        Conversation otherConversation = new Conversation();
        otherConversation.setQuestionAttemptId(otherQuestionAttempt.getId());
        conversationRepository.saveAndFlush(otherConversation);

        String path = "/api/attempts/{attemptId}/questions/{questionId}/conversation";
        mockMvc.perform(get(path, otherAttempt.getId(), firstQuestionId))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(path, otherAttempt.getId(), firstQuestionId).with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/conversations/{conversationId}/messages", otherConversation.getId()))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/conversations/{conversationId}/messages", otherConversation.getId())
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new SendMessageRequest("Não deve ser salva", UUID.randomUUID()))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void sendsMessagesPersistsRepliesAndReusesTheSameRequestId() throws Exception {
        Long attemptId = startAttempt();
        Long conversationId = startConversation(attemptId, firstQuestionId);
        UUID requestId = UUID.randomUUID();
        String path = "/api/conversations/{conversationId}/messages";
        String requestBody = objectMapper.writeValueAsString(new SendMessageRequest("O que é Spring Boot?", requestId));

        MvcResult created = mockMvc.perform(post(path, conversationId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentMessage.senderRole").value("STUDENT"))
                .andExpect(jsonPath("$.studentMessage.llmStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.tutorMessage.senderRole").value("TUTOR"))
                .andExpect(jsonPath("$.tutorMessage.content").value("Resposta de teste: O que é Spring Boot?"))
                .andReturn();
        JsonNode firstResponse = objectMapper.readTree(created.getResponse().getContentAsString());
        long studentMessageId = firstResponse.path("studentMessage").path("id").asLong();
        long tutorMessageId = firstResponse.path("tutorMessage").path("id").asLong();

        mockMvc.perform(post(path, conversationId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentMessage.id").value(studentMessageId))
                .andExpect(jsonPath("$.tutorMessage.id").value(tutorMessageId));

        mockMvc.perform(post(path, conversationId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new SendMessageRequest("Conteúdo diferente", requestId))))
                .andExpect(status().isConflict());

        verify(tutorService, times(1)).generateReply(eq(conversationId), anyList());
        mockMvc.perform(get(path, conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senderRole").value("STUDENT"))
                .andExpect(jsonPath("$[1].senderRole").value("TUTOR"))
                .andExpect(jsonPath("$[1].replyToMessageId").value(studentMessageId));
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void returnsMessagesInStableOrder() throws Exception {
        Long attemptId = startAttempt();
        Long conversationId = startConversation(attemptId, firstQuestionId);
        String path = "/api/conversations/{conversationId}/messages";

        mockMvc.perform(post(path, conversationId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new SendMessageRequest("Primeira pergunta", UUID.randomUUID()))))
                .andExpect(status().isCreated());
        mockMvc.perform(post(path, conversationId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new SendMessageRequest("Segunda pergunta", UUID.randomUUID()))))
                .andExpect(status().isCreated());

        mockMvc.perform(get(path, conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].content").value("Primeira pergunta"))
                .andExpect(jsonPath("$[1].content").value("Resposta de teste: Primeira pergunta"))
                .andExpect(jsonPath("$[2].content").value("Segunda pergunta"))
                .andExpect(jsonPath("$[3].content").value("Resposta de teste: Segunda pergunta"));
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void reportsAnExistingProcessingRequestWithoutCallingTheTutorAgain() throws Exception {
        Long attemptId = startAttempt();
        Long conversationId = startConversation(attemptId, firstQuestionId);
        UUID requestId = UUID.randomUUID();
        Message pending = new Message();
        pending.setConversationId(conversationId);
        pending.setSenderRole("STUDENT");
        pending.setContent("Pergunta em andamento");
        pending.setRequestId(requestId);
        pending.setLlmStatus("PROCESSING");
        pending = messageRepository.saveAndFlush(pending);

        mockMvc.perform(post("/api/conversations/{conversationId}/messages", conversationId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new SendMessageRequest(pending.getContent(), requestId))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.studentMessage.id").value(pending.getId()))
                .andExpect(jsonPath("$.studentMessage.llmStatus").value("PROCESSING"))
                .andExpect(jsonPath("$.tutorMessage").doesNotExist());

        verifyNoInteractions(tutorService);
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void marksFailedTutorRequestsAndAllowsRetryWithTheSameRequestId() throws Exception {
        Long attemptId = startAttempt();
        Long conversationId = startConversation(attemptId, firstQuestionId);
        UUID requestId = UUID.randomUUID();
        String path = "/api/conversations/{conversationId}/messages";
        String body = objectMapper.writeValueAsString(new SendMessageRequest("Pergunta recuperável", requestId));
        doThrow(new IllegalStateException("falha simulada"))
                .doReturn("Resposta após retry")
                .when(tutorService).generateReply(anyLong(), anyList());

        mockMvc.perform(post(path, conversationId).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isBadGateway());
        org.junit.jupiter.api.Assertions.assertEquals(
                "FAILED", messageRepository.findByConversationIdAndRequestId(conversationId, requestId)
                        .orElseThrow().getLlmStatus());

        mockMvc.perform(post(path, conversationId).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentMessage.llmStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.tutorMessage.content").value("Resposta após retry"));

        org.junit.jupiter.api.Assertions.assertEquals(2, messageRepository.count());
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void doesNotRevealAnAttemptOwnedByAnotherStudent() throws Exception {
        AppUser otherStudent = new AppUser();
        otherStudent.setFullName("Outro estudante");
        otherStudent.setEmail("outro@example.com");
        otherStudent.setPasswordHash("not-used-in-mock-auth");
        otherStudent.setRole("STUDENT");
        otherStudent.setActive(true);
        otherStudent = appUserRepository.saveAndFlush(otherStudent);

        QuizAttempt otherAttempt = new QuizAttempt();
        otherAttempt.setUserId(otherStudent.getId());
        otherAttempt.setQuizId(quizId);
        otherAttempt.setStatus("IN_PROGRESS");
        otherAttempt = attemptRepository.saveAndFlush(otherAttempt);

        mockMvc.perform(get("/api/attempts/{attemptId}", otherAttempt.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = STUDENT_EMAIL)
    void rejectsAttemptsForInactiveQuizzes() throws Exception {
        Quiz inactiveQuiz = quizRepository.findById(quizId).orElseThrow();
        inactiveQuiz.setActive(false);
        inactiveQuiz = quizRepository.saveAndFlush(inactiveQuiz);

        mockMvc.perform(post("/api/quizzes/{quizId}/attempts", inactiveQuiz.getId()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "inativo@example.com")
    void rejectsAUserWhoseAccountIsInactive() throws Exception {
        AppUser inactiveStudent = new AppUser();
        inactiveStudent.setFullName("Conta inativa");
        inactiveStudent.setEmail("inativo@example.com");
        inactiveStudent.setPasswordHash("not-used-in-mock-auth");
        inactiveStudent.setRole("STUDENT");
        inactiveStudent.setActive(false);
        appUserRepository.saveAndFlush(inactiveStudent);

        mockMvc.perform(get("/api/attempts"))
                .andExpect(status().isUnauthorized());
    }

    private Long startAttempt() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/quizzes/{quizId}/attempts", quizId).with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        return attemptId(result);
    }

    private Long attemptId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private Long conversationId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private Long startConversation(Long attemptId, Long questionId) throws Exception {
        String path = "/api/attempts/{attemptId}/questions/{questionId}/conversation";
        MvcResult result = mockMvc.perform(post(path, attemptId, questionId).with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        return conversationId(result);
    }

    private QuizQuestion createQuestion(Long parentQuizId, int position, String statement) {
        QuizQuestion question = new QuizQuestion();
        question.setQuizId(parentQuizId);
        question.setPosition(position);
        question.setStatement(statement);
        question.setPoints(java.math.BigDecimal.ONE);
        question.setActive(true);
        return questionRepository.saveAndFlush(question);
    }

    private QuizQuestionOption createOption(
            Long parentQuestionId,
            int position,
            String text,
            boolean correct) {
        QuizQuestionOption option = new QuizQuestionOption();
        option.setQuestionId(parentQuestionId);
        option.setPosition(position);
        option.setOptionText(text);
        option.setCorrect(correct);
        return optionRepository.saveAndFlush(option);
    }
}
