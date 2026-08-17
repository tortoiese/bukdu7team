package io.entry.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * entry.cors.origins 프로퍼티로 허용 오리진을 주입한다. 배포 환경별로 값이 다르다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${entry.cors.origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Entry-Session")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
