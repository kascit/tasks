package me.dhanur.tasks.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final String AUTH_SERVICE_URL =
            System.getenv().getOrDefault("AUTH_SERVICE_URL", "https://auth.dhanur.me").replaceAll("/$", "");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/v1/tasks");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        AuthStatus status = verifyWithAuthService(request);
        if (!status.authenticated) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required for tasks API");
            return;
        }

        request.setAttribute("auth.role", status.role);

        boolean shouldCharge = shouldChargeCredits(request.getMethod());
        boolean charged = false;

        if (shouldCharge) {
            CreditUseResult charge = debitCredits(request, 1);
            if (!charge.success) {
                writeJsonError(response, HttpServletResponse.SC_PAYMENT_REQUIRED,
                        charge.error != null ? charge.error : "Insufficient credits");
                return;
            }
            charged = true;
        }

        filterChain.doFilter(request, response);

        if (charged && response.getStatus() >= 400) {
            refundCredits(request, 1);
        }
    }

    private boolean shouldChargeCredits(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private AuthStatus verifyWithAuthService(HttpServletRequest request) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_SERVICE_URL + "/api/status"))
                    .timeout(Duration.ofSeconds(3))
                    .GET();

            String cookie = request.getHeader("Cookie");
            if (cookie != null && !cookie.isBlank()) {
                builder.header("Cookie", cookie);
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isBlank()) {
                builder.header("Authorization", authHeader);
            }

            HttpResponse<String> resp = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return OBJECT_MAPPER.readValue(resp.body(), AuthStatus.class);
            }
        } catch (Exception ignored) {
        }

        return AuthStatus.guest();
    }

    private CreditUseResult debitCredits(HttpServletRequest request, int amount) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("service", "tasks");
            payload.put("amount", amount);
            payload.put("description", "task_api_operation_" + request.getMethod().toLowerCase());

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_SERVICE_URL + "/api/credits/use"))
                    .timeout(Duration.ofSeconds(4))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload)));

            String cookie = request.getHeader("Cookie");
            if (cookie != null && !cookie.isBlank()) {
                builder.header("Cookie", cookie);
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isBlank()) {
                builder.header("Authorization", authHeader);
            }

            HttpResponse<String> resp = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            CreditUseResult result = OBJECT_MAPPER.readValue(resp.body(), CreditUseResult.class);

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                result.success = true;
                return result;
            }

            if (result.error == null || result.error.isBlank()) {
                result.error = "Credit debit failed";
            }
            result.success = false;
            return result;
        } catch (Exception ex) {
            CreditUseResult fallback = new CreditUseResult();
            fallback.success = false;
            fallback.error = "Could not validate credits";
            return fallback;
        }
    }

    private void refundCredits(HttpServletRequest request, int amount) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("service", "tasks");
            payload.put("amount", amount);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_SERVICE_URL + "/api/credits/refund"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload)));

            String cookie = request.getHeader("Cookie");
            if (cookie != null && !cookie.isBlank()) {
                builder.header("Cookie", cookie);
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isBlank()) {
                builder.header("Authorization", authHeader);
            }

            HTTP.send(builder.build(), HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getWriter(), Map.of("error", message));
    }

    private static class AuthStatus {
        public boolean authenticated;
        public String role;

        static AuthStatus guest() {
            AuthStatus status = new AuthStatus();
            status.authenticated = false;
            status.role = "guest";
            return status;
        }
    }

    private static class CreditUseResult {
        public boolean success;
        public String error;
    }
}
