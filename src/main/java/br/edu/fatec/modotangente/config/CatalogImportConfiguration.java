package br.edu.fatec.modotangente.config;

import br.edu.fatec.modotangente.service.CatalogImporter;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CatalogImportConfiguration {
    @Bean
    ApplicationRunner importCatalog(Environment environment, ResourceLoader resourceLoader, CatalogImporter importer) {
        String configuredResources = environment.getProperty("catalog.import-resources");
        if (configuredResources == null || configuredResources.isBlank()) {
            configuredResources = environment.getProperty("catalog.import-resource", "");
        }

        List<String> locations = Arrays.stream(configuredResources.split(","))
                .map(String::trim)
                .filter(location -> !location.isEmpty())
                .toList();

        return args -> {
            for (String location : locations) {
                Resource resource = resourceLoader.getResource(location);
                try (var input = resource.getInputStream()) {
                    importer.load(input.readAllBytes());
                }
            }
        };
    }
}
