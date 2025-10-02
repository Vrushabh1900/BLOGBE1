package newblogproject.example.newproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Value("${frontend.url}")
            public String frontendurl;
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
//                        .allowedOrigins("https://urban-enigma-4jwg5xv4gwgxh76xg-5173.app.github.dev/")
//                        .allowedOrigins("http://127.0.0.1:5173")
                        .allowedMethods("*")
//                        .allowedOrigins("http://localhost:5173/")
//                        .allowedOrigins("http://localhost:9002")
                        .allowedOrigins("http://127.0.0.1:8080/api")
//                        .allowedOrigins("https://studio.firebase.google.com/studio-3434890361")
                        .allowedHeaders("*")
                        .allowCredentials(true);


            }
        };
    }

}
