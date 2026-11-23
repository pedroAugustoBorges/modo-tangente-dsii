package br.edu.fatec.modotangente.config;

import br.edu.fatec.modotangente.service.CatalogImporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;

@Configuration
@ConditionalOnProperty(name = "catalog.import-resource")
public class CatalogImportConfiguration {
    @Bean
    ApplicationRunner importCatalog(@Value("${catalog.import-resource}") Resource resource, CatalogImporter importer) {
        return args -> {
            try (var input = resource.getInputStream()) {
                importer.load(input.readAllBytes());
            }
        };
    }
}
