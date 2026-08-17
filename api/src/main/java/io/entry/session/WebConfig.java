package io.entry.session;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SessionInterceptor는 세션 발급 엔드포인트 자체와 헬스체크·문서 경로는 제외한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionInterceptor sessionInterceptor;

    public WebConfig(SessionInterceptor sessionInterceptor) {
        this.sessionInterceptor = sessionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/health",
                        "/api/v1/sessions"
                );
    }
}
