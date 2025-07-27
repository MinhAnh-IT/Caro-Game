package com.vn.caro_game.configs;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SwaggerConfig.
 * Tests the OpenAPI configuration and documentation setup.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SwaggerConfigTest {

    @InjectMocks
    private SwaggerConfig swaggerConfig;

    @Test
    void customOpenAPI_ShouldReturnValidConfiguration() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectTitle() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertEquals("Caro Game API", openAPI.getInfo().getTitle());
    }

    @Test
    void customOpenAPI_ShouldHaveConfiguredVersion() {
        // Given
        String testVersion = "2.1.0";
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", testVersion);
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertEquals(testVersion, openAPI.getInfo().getVersion());
    }

    @Test
    void customOpenAPI_ShouldHaveDescription() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo().getDescription());
        assertFalse(openAPI.getInfo().getDescription().isEmpty());
    }

    @Test
    void customOpenAPI_ShouldHaveSecurityConfiguration() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearerAuth"));
    }

    @Test
    void customOpenAPI_ShouldHaveJWTSecurityScheme() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(securityScheme);
        assertEquals("http", securityScheme.getType().toString().toLowerCase());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }

    @Test
    void customOpenAPI_ShouldHaveContactInformation() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo().getContact());
        assertNotNull(openAPI.getInfo().getContact().getName());
        assertNotNull(openAPI.getInfo().getContact().getEmail());
    }

    @Test
    void customOpenAPI_ShouldHaveServers() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(swaggerConfig, "appDescription", "Test API Description");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
    }
}
