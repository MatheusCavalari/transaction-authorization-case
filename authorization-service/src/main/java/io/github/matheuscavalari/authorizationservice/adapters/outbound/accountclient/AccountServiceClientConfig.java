package io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AccountServiceClientConfig {

    @Bean
    RestClient accountServiceRestClient(@Value("${account-service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}

