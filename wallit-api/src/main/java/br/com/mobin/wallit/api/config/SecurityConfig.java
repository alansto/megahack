package br.com.mobin.wallit.api.config;

import br.com.mobin.wallit.api.security.JWTAuthenticationFilter;
import br.com.mobin.wallit.api.security.SecurityHelper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class SecurityConfig implements WebFluxConfigurer {

    @Getter
    @Value("${security.secret-key}")
    private String jwtSecretKey;
    @Getter
    @Value("${security.encrypt-key}")
    private String encryptKey;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .maxAge(3600);
    }

    @Bean
    JWTAuthenticationFilter authenticationFilter(SecurityHelper securityHelper) {

        return new JWTAuthenticationFilter(serverWebExchange -> {

            String url = serverWebExchange.getRequest().getPath().value();
            boolean openAccess = url.contains("/v1/signin") || url.contains("/v1/signup");

            return !openAccess;
        }, securityHelper);
    }
}