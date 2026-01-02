package io.github.matheuscavalari.authorizationservice.config;

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
                        .title("Authorization Service API")
                        .version("0.0.1")
                        .description(
                                "APIs do authorization-service responsáveis pela autorização de transações financeiras " +
                                        "de crédito e débito, orquestrando a comunicação com o account-service."
                        )
                        .contact(new Contact()
                                .name("Matheus Cavalari")
                                .email("matheuscavbarbosa@hotmail.com")
                        )
                );
    }
}
