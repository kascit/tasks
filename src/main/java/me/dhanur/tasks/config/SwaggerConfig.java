package me.dhanur.tasks.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Configuration
public class SwaggerConfig {

    @Bean
    public FilterRegistrationBean<SwaggerBadgeFilter> swaggerBadgeFilter() {
        FilterRegistrationBean<SwaggerBadgeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SwaggerBadgeFilter());
        registrationBean.addUrlPatterns("/swagger-ui/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    public static class SwaggerBadgeFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (httpRequest.getRequestURI().endsWith("index.html")) {
                ResponseWrapper wrapper = new ResponseWrapper(httpResponse);
                chain.doFilter(request, wrapper);

                String content = wrapper.toString();
                String badgeScript = "<script src=\"https://dhanur.me/motherland.js\" data-mode=\"long\" data-theme=\"dark\" data-position=\"top-left\" data-size=\"40px\" data-gap=\"0.7rem\" data-animate=\"true\"></script>";

                content = content.replace("</body>", badgeScript + "\n</body>");

                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                httpResponse.setContentLength(bytes.length);
                httpResponse.getOutputStream().write(bytes);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    static class ResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private final ByteArrayOutputStream capture;
        private ServletOutputStream output;
        private PrintWriter writer;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
            capture = new ByteArrayOutputStream(response.getBufferSize());
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called on this response.");
            }
            if (output == null) {
                output = new ServletOutputStream() {
                    @Override
                    public void write(int b) {
                        capture.write(b);
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {
                    }
                };
            }
            return output;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (output != null) {
                throw new IllegalStateException("getOutputStream() has already been called on this response.");
            }
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(capture, getCharacterEncoding()));
            }
            return writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            super.flushBuffer();
            if (writer != null) {
                writer.flush();
            } else if (output != null) {
                output.flush();
            }
        }

        @Override
        public String toString() {
            try {
                if (writer != null) {
                    writer.flush();
                }
                return capture.toString(getCharacterEncoding());
            } catch (Exception e) {
                return "";
            }
        }
    }
}
