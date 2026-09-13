package me.dhanur.tasks.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RobotsHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/api-docs") || uri.startsWith("/actuator")) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow");
        }
        filterChain.doFilter(request, response);
    }
}
