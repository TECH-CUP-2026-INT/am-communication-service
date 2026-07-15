package co.edu.escuelaing.techcup.communications.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI communicationsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechCup Communications Service API")
                        .version("v1")
                        .description("Servicio de comunicaciones: chats, mensajería, soporte y preguntas frecuentes."));
    }
}
