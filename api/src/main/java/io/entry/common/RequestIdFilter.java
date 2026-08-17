package io.entry.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 매 요청마다 requestId를 발급해 RequestContext에 보관한다. 응답 meta.requestId로 노출된다.
 */
@Component
@Order(1)
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            RequestContext.setRequestId(UUID.randomUUID().toString());
            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}
