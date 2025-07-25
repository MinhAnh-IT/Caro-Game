package com.vn.caro_game.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Caro Game API Documentation}")
    private String appDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Caro Game API")
                        .version(appVersion)
                        .description("""
                            ## Caro Game REST API Documentation
                            
                            This API provides comprehensive authentication and game management services for the Caro Game application.
                            
                            ### Features:
                            - **JWT Authentication**: Secure token-based authentication with access and refresh tokens
                            - **Password Management**: Secure password reset with OTP verification
                            - **Account Security**: Protected endpoints with role-based access control
                            - **Rate Limiting**: Built-in protection against abuse with request limits
                            
                            ### Authentication:
                            Most endpoints require JWT authentication. Include the access token in the Authorization header:
                            ```
                            Authorization: Bearer <your_access_token>
                            ```
                            
                            ### Error Handling:
                            All endpoints return standardized error responses with appropriate HTTP status codes.
                            
                            ### Rate Limits:
                            - OTP requests: 3 requests per 15 minutes
                            - Login attempts: 5 attempts per 15 minutes
                            - Password reset: 3 attempts per hour
                            
                            For support, please contact our development team.
                            """)
                        .termsOfService("https://carogame.com/terms")
                        .contact(new Contact()
                                .name("Caro Game Development Team")
                                .email("dev@carogame.com")
                                .url("https://github.com/MinhAnh-IT/Caro-Game"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api-dev.carogame.com")
                                .description("Development Server"),
                        new Server()
                                .url("https://api-staging.carogame.com")
                                .description("Staging Server"),
                        new Server()
                                .url("https://api.carogame.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("JWT Authorization header using the Bearer scheme. " +
                                                   "Enter 'Bearer' [space] and then your token in the text input below. " +
                                                   "Example: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'")))
                .externalDocs(new ExternalDocumentation()
                        .description("Caro Game GitHub Repository - Complete API Documentation and Examples")
                        .url("https://github.com/MinhAnh-IT/Caro-Game"));
    }
}
