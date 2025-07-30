package com.vn.caro_game.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for static resource handling.
 *
 * <p>This configuration enables serving uploaded files (like avatars) as static resources
 * that can be accessed via HTTP URLs. Follows Spring Boot best practices for file serving.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded avatar files
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + avatarUploadDir + "/")
                .setCachePeriod(3600); // Cache for 1 hour

        // Serve other static resources if needed
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31536000); // Cache for 1 year
    }
}
