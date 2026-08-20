package io.entry.adminauth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * D1/D2 API(/admin/**, /personas/**)만 AdminAuthInterceptor로 지킨다.
 * 로그인 엔드포인트 자체(/admin/login)는 제외한다.
 */
@Configuration
public class AdminWebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    public AdminWebConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/v1/admin/**", "/api/v1/personas/**")
                .excludePathPatterns("/api/v1/admin/login");
    }
}
