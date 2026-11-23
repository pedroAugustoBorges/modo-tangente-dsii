package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import br.edu.fatec.modotangente.service.QuizContextProvider;
import br.edu.fatec.modotangente.service.QuizQuestionProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:modo_tangente_auth;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({SessionControllerTests.AdminRoutes.class, SessionControllerTests.StudyPages.class})
class SessionControllerTests {

    private static final String PASSWORD = "senha-de-teste";
    private static final BCryptPasswordEncoder FIXTURE_ENCODER = new BCryptPasswordEncoder(4);

    @Autowired private MockMvc mockMvc;
    @Autowired private AppUserRepository appUserRepository;
    @MockitoBean private QuizQuestionProvider questionProvider;
    @MockitoBean private QuizContextProvider contextProvider;

    @Test
    void redirectsGuestsAndRendersTheEmailLoginForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"email\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void authenticatesAnAppUserAndSharesTheNormalizedIdentityWithTheQuizApi() throws Exception {
        AppUser student = createUser("Aluno@Example.com", "STUDENT", true);

        MvcResult login = login("  ALUNO@EXAMPLE.COM  ", PASSWORD)
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("aluno@example.com").withRoles("STUDENT"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-label=\"Navegação principal\"")))
                .andExpect(content().string(containsString("Conta de Teste")));

        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId().intValue()))
                .andExpect(jsonPath("$.fullName").value("Conta de Teste"))
                .andExpect(jsonPath("$.email").value("aluno@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/attempts").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsIncorrectPasswords() throws Exception {
        AppUser student = createUser("senha@example.com", "STUDENT", true);

        login(student.getEmail(), "senha-incorreta")
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void rejectsInactiveAccountsEvenWithTheCorrectPassword() throws Exception {
        AppUser inactiveUser = createUser("inativo@example.com", "STUDENT", false);

        login(inactiveUser.getEmail(), PASSWORD)
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void deniesStudentsAccessToAdminPagesAndTheAdminApi() throws Exception {
        AppUser student = createUser("estudante@example.com", "STUDENT", true);
        MockHttpSession session = loginSession(student);

        mockMvc.perform(get("/admin").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/topics").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void revokesAdminAccessWhenAnAccountIsDeactivatedDuringASession() throws Exception {
        AppUser admin = createUser("admin@example.com", "ADMIN", true);
        MockHttpSession session = loginSession(admin);

        mockMvc.perform(get("/admin").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/topics").session(session)).andExpect(status().isOk());

        admin.setActive(false);
        appUserRepository.saveAndFlush(admin);

        mockMvc.perform(get("/admin").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/topics").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRequiresCsrfAndInvalidatesTheSession() throws Exception {
        AppUser student = createUser("sair@example.com", "STUDENT", true);
        MockHttpSession session = loginSession(student);

        mockMvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void guestsCanLoadTheSharedStylesAndSeeOnlyTheLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/css/app.css\"")))
                .andExpect(content().string(not(containsString("aria-label=\"Navegação principal\""))));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @Test
    void rendersTheStudyCatalogWithTheAuthenticatedNavigation() throws Exception {
        MockHttpSession session = loginSession(createUser("catalogo@example.com", "STUDENT", true));

        mockMvc.perform(get("/__test/estudo/catalogo").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-label=\"Navegação principal\"")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("Tópicos de estudo")))
                .andExpect(content().string(containsString("Spring Boot")));
    }

    @Test
    void preservesTheOpenAndAnsweredQuestionStatesInTheSharedLayout() throws Exception {
        MockHttpSession session = loginSession(createUser("questao@example.com", "STUDENT", true));

        mockMvc.perform(get("/__test/estudo/questao").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-label=\"Navegação principal\"")))
                .andExpect(content().string(containsString("Questão 2 de 4")))
                .andExpect(content().string(containsString("O que é injeção de dependências?")))
                .andExpect(content().string(not(containsString("disabled=\"disabled\""))));

        String answeredPage = mockMvc.perform(get("/__test/estudo/questao").param("respondida", "true").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Questão respondida.")))
                .andExpect(content().string(containsString("disabled=\"disabled\"")))
                .andReturn().getResponse().getContentAsString();

        assertThat(answeredPage).containsPattern("value=\"22\"\\s+checked=\"checked\"");
    }

    private AppUser createUser(String email, String role, boolean active) {
        AppUser user = new AppUser();
        user.setFullName("Conta de Teste");
        user.setEmail(email);
        user.setPasswordHash(FIXTURE_ENCODER.encode(PASSWORD));
        user.setRole(role);
        user.setActive(active);
        return appUserRepository.saveAndFlush(user);
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(formLogin().user("email", email).password(password));
    }

    private MockHttpSession loginSession(AppUser user) throws Exception {
        MvcResult result = login(user.getEmail(), PASSWORD)
                .andExpect(authenticated().withRoles(user.getRole()))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @TestComponent
    @RestController
    static class AdminRoutes {
        @GetMapping({"/admin", "/api/admin/topics"})
        String adminEndpoint() {
            return "OK";
        }
    }

    @TestComponent
    @Controller
    static class StudyPages {
        @GetMapping("/__test/estudo/catalogo")
        String catalog(Model model) {
            model.addAttribute("titulo", "Tópicos de estudo");
            model.addAttribute("itens", List.of(Map.of("titulo", "Spring Boot", "url", "/")));
            return "estudo/catalogo";
        }

        @GetMapping("/__test/estudo/questao")
        String question(@RequestParam(defaultValue = "false") boolean respondida, Model model) {
            model.addAttribute("titulo", "Quiz de Spring");
            model.addAttribute("enunciado", "O que é injeção de dependências?");
            model.addAttribute("posicao", 2);
            model.addAttribute("totalQuestoes", 4);
            model.addAttribute("alternativas", List.of(
                    Map.of("id", 21L, "texto", "Um tipo de banco de dados"),
                    Map.of("id", 22L, "texto", "Fornecer dependências a um objeto")));
            model.addAttribute("respondida", respondida);
            model.addAttribute("alternativaSelecionadaId", respondida ? 22L : null);
            return "estudo/questao";
        }
    }
}
