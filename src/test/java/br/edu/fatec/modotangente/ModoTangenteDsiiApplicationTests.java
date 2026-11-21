package br.edu.fatec.modotangente;

import br.edu.fatec.modotangente.service.QuizQuestionProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ModoTangenteDsiiApplicationTests {

	@MockitoBean
	private QuizQuestionProvider questionProvider;

	@Test
	void contextLoads() {
	}

}
