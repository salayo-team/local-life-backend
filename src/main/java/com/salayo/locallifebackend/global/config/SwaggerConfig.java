package com.salayo.locallifebackend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(components());
    }

    private Info apiInfo() {
        return new Info()
            .title("Local-Life API 문서")
            .description("2025 KDT 해커톤 - LocalLife 백엔드 API 명세서")
            .version("v1.0.0")
            .contact(new Contact()
                .name("Team Salayo")
                .email("noreply.salayo.locallife@gmail.com"))
            .license(new License()
                .name("Salayo License")
                .url("배포 도메인 반영 예정"));
    }

    private Components components() {
        return new Components()
            .addSecuritySchemes(
                SECURITY_SCHEME_NAME,
                new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            );
    }

}
