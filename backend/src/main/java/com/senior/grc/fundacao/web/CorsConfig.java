package com.senior.grc.fundacao.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para o frontend (Angular) chamar a API de outra origem (Vercel). Na demo o conjunto de
 * origens e configuravel por ambiente ({@code GRC_CORS_ORIGINS}); o default libera qualquer origem,
 * aceitavel porque os dados sao ficticios e o acesso usa o cabecalho de tenant (nao ha segredo).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${GRC_CORS_ORIGINS:*}")
    private String origens;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origens.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
