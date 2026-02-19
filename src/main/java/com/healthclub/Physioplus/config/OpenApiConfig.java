package com.healthclub.Physioplus.config;

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
    public OpenAPI physioplusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PhysioPlus API")
                        .description("REST API for PhysioPlus - Healthcare Appointment & Consultation Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PhysioPlus Team")
                                .email("support@physioplus.com"))
                        .license(new License()
                                .name("Private License")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }
}
