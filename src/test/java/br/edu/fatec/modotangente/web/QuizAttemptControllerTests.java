package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.domain.Quiz;
import br.edu.fatec.modotangente.domain.QuizAttempt;
import br.edu.fatec.modotangente.domain.Topic;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import br.edu.fatec.modotangente.repository.QuizAttemptRepository;
import br.edu.fatec.modotangente.repository.QuizRepository;
import br.edu.fatec.modotangente.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizAttemptControllerTests {

    private static final String STUDENT_EMAIL = "pedro@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private TopicRepository topicRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private QuizAttemptRepository attemptRepository;

    private Long quizId;

    @BeforeEach
    void createFixture() {
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

        mockMvc.perform(post("/api/quizzes/{quizId}/attempts", quizId).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.quizId").value(quizId.intValue()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.userId").doesNotExist());

        mockMvc.perform(get("/api/attempts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..quizId", hasItem(quizId.intValue())))
                .andExpect(jsonPath("$.length()").value(1));
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
}
