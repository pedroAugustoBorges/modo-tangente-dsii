package br.edu.fatec.modotangente.config;

import br.edu.fatec.modotangente.service.CatalogImporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CatalogImportConfigurationTests {
    @Test
    void importsConfiguredResourcesInOrder() throws Exception {
        var environment = new MockEnvironment().withProperty("catalog.import-resources",
                "classpath:catalog/dsii-fundamentos-v1.json, classpath:catalog/spring-web-v1.json");
        var importer = mock(CatalogImporter.class);
        var runner = new CatalogImportConfiguration().importCatalog(
                environment, new DefaultResourceLoader(), importer);

        runner.run(new DefaultApplicationArguments(new String[0]));

        var payloads = ArgumentCaptor.forClass(byte[].class);
        verify(importer, times(2)).load(payloads.capture());
        var mapper = new ObjectMapper();
        assertEquals(List.of("modo-tangente-dsii@catalogo-dsii-fundamentos-v1",
                        "modo-tangente-dsii@catalogo-spring-web-v1"),
                payloads.getAllValues().stream()
                        .map(payload -> {
                            try {
                                return mapper.readTree(payload).path("source").asText();
                            } catch (Exception exception) {
                                throw new AssertionError(exception);
                            }
                        })
                        .toList());
    }

    @Test
    void continuesToAcceptLegacySingleResourceProperty() throws Exception {
        var environment = new MockEnvironment().withProperty("catalog.import-resource",
                "classpath:catalog/spring-web-v1.json");
        var importer = mock(CatalogImporter.class);
        var runner = new CatalogImportConfiguration().importCatalog(
                environment, new DefaultResourceLoader(), importer);

        runner.run(new DefaultApplicationArguments(new String[0]));

        var payload = ArgumentCaptor.forClass(byte[].class);
        verify(importer).load(payload.capture());
        assertEquals("modo-tangente-dsii@catalogo-spring-web-v1",
                new ObjectMapper().readTree(payload.getValue()).path("source").asText());
    }
}
