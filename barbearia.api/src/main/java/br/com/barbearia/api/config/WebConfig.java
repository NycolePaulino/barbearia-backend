package br.com.barbearia.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // permite CORS para todas as URLs que começam com /api/
                .allowedOrigins("http://localhost:3000") // requisições no front dessa URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS"); // métodos HTTP permitidos
    }
}