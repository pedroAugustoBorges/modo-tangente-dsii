package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import br.edu.fatec.modotangente.service.QuestionCatalogService;
import br.edu.fatec.modotangente.service.QuizAttemptService;
import br.edu.fatec.modotangente.service.QuizCatalogService;
import br.edu.fatec.modotangente.service.TopicCatalogService;
import br.edu.fatec.modotangente.web.dto.OptionAdminView;
import br.edu.fatec.modotangente.web.dto.OptionData;
import br.edu.fatec.modotangente.web.dto.QuestionData;
import br.edu.fatec.modotangente.web.dto.QuizData;
import br.edu.fatec.modotangente.web.dto.TopicData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminCatalogPagesTests {

    private static final String PASSWORD = "senha-de-teste";
    private static final BCryptPasswordEncoder FIXTURE_ENCODER = new BCryptPasswordEncoder(4);

    @Autowired private MockMvc mvc;
    @Autowired private AppUserRepository accounts;
    @Autowired private TopicCatalogService topics;
    @Autowired private QuizCatalogService quizzes;
    @Autowired private QuestionCatalogService questions;
    @Autowired private QuizAttemptService attempts;
    @Autowired private EntityManager persistence;

    private MockHttpSession admin;

    @BeforeEach
    void signInAsAdmin() throws Exception {
        AppUser account = createAccount("ADMIN");
        var login = mvc.perform(formLogin().user("email", account.getEmail()).password(PASSWORD))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andReturn();
        admin = (MockHttpSession) login.getRequest().getSession(false);
    }

    @Test
    void createsEditsAndDeactivatesATopicFromThePanel() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
        mvc.perform(get("/admin/topicos/novo").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/admin/topicos/novo").session(admin)
                        .param("title", "Tópico do painel").param("displayOrder", "3").param("active", "true"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/topicos/novo").session(admin).with(csrf())
                        .param("title", "Tópico do painel").param("displayOrder", "3").param("active", "true"))
                .andExpect(redirectedUrl("/admin"));
        long topicId = topics.list().stream().filter(topic -> topic.title().equals("Tópico do painel"))
                .findFirst().orElseThrow().id();

        mvc.perform(get("/admin").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/admin/topicos/" + topicId)));
        mvc.perform(get("/admin/topicos/{id}", topicId).session(admin))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(HtmlUtils.htmlUnescape(result.getResponse().getContentAsString()))
                        .contains("value=\"Tópico do painel\""));
        mvc.perform(post("/admin/topicos/{id}", topicId).session(admin).with(csrf())
                        .param("title", "Tópico atualizado").param("displayOrder", "5").param("active", "true"))
                .andExpect(redirectedUrl("/admin"));
        mvc.perform(post("/admin/topicos/{id}/desativar", topicId).session(admin).with(csrf()))
                .andExpect(redirectedUrl("/admin"));

        assertThat(topics.get(topicId).title()).isEqualTo("Tópico atualizado");
        assertThat(topics.get(topicId).displayOrder()).isEqualTo(5);
        assertThat(topics.get(topicId).active()).isFalse();
    }

    @Test
    void editsAQuizAndKeepsItsQuestionManagementAccessible() throws Exception {
        long topicId = createTopic();
        mvc.perform(get("/admin/quizzes/novo").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tópico de administração")));
        mvc.perform(post("/admin/quizzes/novo").session(admin).with(csrf())
                        .param("topicId", Long.toString(topicId)).param("title", "Quiz do painel")
                        .param("displayOrder", "1").param("active", "true"))
                .andExpect(redirectedUrl("/admin"));
        long quizId = quizzes.list().stream()
                .filter(quiz -> quiz.topicId().equals(topicId)).findFirst().orElseThrow().id();
        mvc.perform(get("/admin/quizzes/{id}", quizId).session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Quiz do painel\"")));
        mvc.perform(post("/admin/quizzes/{id}", quizId).session(admin).with(csrf())
                        .param("topicId", Long.toString(topicId)).param("title", "Quiz atualizado")
                        .param("instructions", "Leia as questões").param("displayOrder", "2").param("active", "true"))
                .andExpect(redirectedUrl("/admin"));
        mvc.perform(post("/admin/quizzes/{id}/desativar", quizId).session(admin).with(csrf()))
                .andExpect(redirectedUrl("/admin"));
        mvc.perform(get(questionsPath(quizId)).session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Quiz atualizado")));
        assertThat(quizzes.get(quizId).instructions()).isEqualTo("Leia as questões");
        assertThat(quizzes.get(quizId).active()).isFalse();
    }

    @Test
    void rejectsInvalidFieldsAndKeepsTheQuizFormInput() throws Exception {
        long topicId = createTopic();
        mvc.perform(post("/admin/quizzes/novo").session(admin).with(csrf())
                        .param("topicId", Long.toString(topicId)).param("title", "Quiz em edição")
                        .param("displayOrder", "inválida").param("active", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(HtmlUtils.htmlUnescape(result.getResponse().getContentAsString()))
                        .contains("value=\"Quiz em edição\"", "Informe um número inteiro a partir de 0."));
        assertThat(quizzes.list()).noneMatch(quiz -> quiz.topicId().equals(topicId));
    }

    @Test
    void savesADraftWithoutOptionsAndPublishesItWithOptions() throws Exception {
        long quizId = createQuiz();
        String path = questionsPath(quizId);
        mvc.perform(get(path + "/nova").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Adicionar alternativa")));
        mvc.perform(questionForm(path + "/nova", "Em elaboração", false).param("_options", "on"))
                .andExpect(status().is3xxRedirection());
        var draft = questions.list(quizId).get(0);
        assertThat(draft.active()).isFalse();
        assertThat(questions.listOptions(quizId, draft.id())).isEmpty();

        mvc.perform(withTwoOptions(questionForm(path + "/" + draft.id(), "Questão publicada", true)))
                .andExpect(redirectedUrl(path + "/" + draft.id()));
        mvc.perform(get(path + "/" + draft.id()).session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alternativa correta")));
        assertThat(questions.get(quizId, draft.id()).active()).isTrue();
        assertThat(questions.listOptions(quizId, draft.id())).extracting(OptionAdminView::correct)
                .containsExactly(false, true);
    }

    @Test
    void keepsTheEnteredOptionsWhenPublicationFails() throws Exception {
        long quizId = createQuiz();
        String path = questionsPath(quizId);
        var request = questionForm(path + "/nova", "Publicação incompleta", true)
                .param("options[0].position", "1")
                .param("options[0].text", "Alternativa em revisão")
                .param("options[0].correct", "true");

        mvc.perform(request).andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Publicação exige duas alternativas ou mais")))
                .andExpect(content().string(containsString("Alternativa em revisão")));
        assertThat(questions.list(quizId)).isEmpty();
    }

    @Test
    void keepsOptionTextWhenAnOptionPositionIsInvalid() throws Exception {
        long quizId = createQuiz();
        var request = questionForm(questionsPath(quizId) + "/nova", "Questão em revisão", false)
                .param("options[0].position", "inválida")
                .param("options[0].text", "\nTexto já preenchido")
                .param("_options[0].correct", "on");

        mvc.perform(request).andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\n\nTexto já preenchido</textarea>")))
                .andExpect(content().string(containsString("Informe uma posição inteira a partir de 1.")));
        assertThat(questions.list(quizId)).isEmpty();
    }

    @Test
    void savesAllAlternativesFromALargeExistingDraft() throws Exception {
        long quizId = createQuiz();
        List<OptionData> options = IntStream.rangeClosed(1, 257)
                .mapToObj(position -> new OptionData(position, "Alternativa " + position, false))
                .toList();
        long questionId = questions.save(quizId, null,
                new QuestionData(1, "Questão importada", null, BigDecimal.ONE, false, options)).id();
        String path = questionsPath(quizId) + "/" + questionId;
        var form = questionForm(path, "Questão importada e revisada", false);
        for (int i = 0; i < options.size(); i++) {
            String field = "options[" + i + "].";
            form.param(field + "position", Integer.toString(i + 1))
                    .param(field + "text", "Alternativa " + (i + 1))
                    .param("_" + field + "correct", "on");
        }

        mvc.perform(form).andExpect(redirectedUrl(path));
        assertThat(questions.get(quizId, questionId).statement()).isEqualTo("Questão importada e revisada");
        assertThat(questions.listOptions(quizId, questionId)).hasSize(257);
    }

    @Test
    void redirectsToTheNewVersionAndPreservesTheOriginalAttempt() throws Exception {
        long quizId = createQuiz();
        long questionId = questions.save(quizId, null, new QuestionData(1, "Questão original", null,
                BigDecimal.ONE, true, List.of(new OptionData(1, "Errada", false),
                new OptionData(2, "Correta original", true)))).id();
        long studentId = createAccount("STUDENT").getId();
        long attemptId = attempts.start(studentId, quizId).id();
        long originalOption = questions.listOptions(quizId, questionId).get(1).id();
        persistence.flush();
        persistence.clear();
        String path = questionsPath(quizId);

        var response = mvc.perform(withTwoOptions(questionForm(path + "/" + questionId, "Questão revisada", true)))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse();
        long replacement = questions.list(quizId).stream().filter(question -> question.active())
                .findFirst().orElseThrow().id();

        assertThat(replacement).isNotEqualTo(questionId);
        assertThat(response.getRedirectedUrl()).isEqualTo(path + "/" + replacement);
        assertThat(questions.get(quizId, questionId).statement()).isEqualTo("Questão original");
        assertThat(attempts.answer(studentId, attemptId, questionId, originalOption).isCorrect()).isTrue();

        mvc.perform(post(path + "/" + replacement + "/desativar").session(admin).with(csrf()))
                .andExpect(redirectedUrl(path));
        assertThat(questions.get(quizId, replacement).active()).isFalse();
    }

    private AppUser createAccount(String role) {
        var account = new AppUser();
        account.setEmail(UUID.randomUUID() + "@example.com");
        account.setFullName(role);
        account.setRole(role);
        account.setPasswordHash(FIXTURE_ENCODER.encode(PASSWORD));
        return accounts.save(account);
    }

    private long createTopic() {
        return topics.save(null, new TopicData("Tópico de administração", null, 0, true)).id();
    }

    private long createQuiz() {
        return quizzes.save(null, new QuizData(createTopic(), "Quiz de administração", null, null, 0, true)).id();
    }

    private String questionsPath(long quizId) {
        return "/admin/quizzes/" + quizId + "/questoes";
    }

    private MockHttpServletRequestBuilder questionForm(String path, String statement, boolean active) {
        return post(path).session(admin).with(csrf()).param("position", "1").param("points", "1.25")
                .param("statement", statement).param("active", Boolean.toString(active));
    }

    private MockHttpServletRequestBuilder withTwoOptions(MockHttpServletRequestBuilder form) {
        return form.param("options[0].position", "1").param("options[0].text", "Outra alternativa")
                .param("_options[0].correct", "on")
                .param("options[1].position", "2").param("options[1].text", "Alternativa correta")
                .param("options[1].correct", "true");
    }
}
