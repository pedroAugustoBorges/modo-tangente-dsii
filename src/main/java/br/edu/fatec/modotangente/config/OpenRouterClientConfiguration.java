package br.edu.fatec.modotangente.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class OpenRouterClientConfiguration {

    @Bean("openRouterRestClient")
    RestClient openRouterRestClient(
            @Value("${openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl,
            @Value("${openrouter.connect-timeout:PT3S}") Duration connectTimeout,
            @Value("${openrouter.read-timeout:PT30S}") Duration readTimeout) {
        if (connectTimeout.isZero() || connectTimeout.isNegative()
                || readTimeout.isZero() || readTimeout.isNegative()) {
            throw new IllegalArgumentException("Os timeouts do OpenRouter precisam ser positivos.");
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl.replaceAll("/+$", ""))
                .requestFactory(requestFactory)
                .build();
    }
}
