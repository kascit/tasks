package me.dhanur.tasks.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Bean
        public OpenAPI customOpenAPI() {
                Server localServer = new Server();
                localServer.setUrl("http://localhost:" + serverPort);
                localServer.setDescription("Local development server");

                Server productionServer = new Server();
                productionServer.setUrl("https://tasks.dhanur.me");
                productionServer.setDescription("Production server");

                return new OpenAPI()
                                .servers(List.of(localServer, productionServer))
                                .info(new Info()
                                                .title("Task Manager API")
                                                .version("1.0.0")
                                                .description("""
                                                                A comprehensive RESTful API for managing tasks with CRUD operations,
                                                                pagination, filtering, and validation.

                                                                ## Features
                                                                - Create, read, update, and delete tasks
                                                                - Status management (TODO, IN_PROGRESS, DONE)
                                                                - Pagination and sorting
                                                                - Input validation
                                                                - Comprehensive error handling
                                                                """)
                                                .contact(new Contact()
                                                                .name("kascit")
                                                                .email("contact@dhanur.me")
                                                                .url("https://github.com/kascit/tasks"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")));
        }
}
