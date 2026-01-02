package io.github.matheuscavalari.accountservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .version("0.0.1")
                        .description("APIs do account-service: operações de contas e saldos")
                        .contact(new Contact().name("Matheus Cavalari").email("matheuscavbarbosa@hotmail.com"))
                );
    }
}

