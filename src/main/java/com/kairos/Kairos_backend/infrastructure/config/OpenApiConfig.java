package com.kairos.Kairos_backend.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI.
 * La documentación queda en http://localhost:8080/swagger-ui.html
 * El botón "Authorize" permite pegar el token JWT del login para probar los endpoints protegidos.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI kairosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("KAIRÓS API")
                        .version("1.0")
                        .description("Sistema de inventario y ventas para franquicias de tecnología. "
                                + "Para los endpoints protegidos: haga login en POST /api/usuarios/login, "
                                + "copie el token y péguelo en el botón Authorize."))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
