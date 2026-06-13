package com.example.travellingapp.config;

import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.security.filter.TokenFilter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.Objects;
import static com.example.travellingapp.util.Common.getNonAuthenticatedUrls;

@Log4j2
@Configuration
public class SecurityConfig {
    private final ConfigurationRepository configurationRepository;
    public SecurityConfig(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenFilter tokenFilter
    ) throws Exception {
        String[] publicUrls = Arrays.stream(getNonAuthenticatedUrls(configurationRepository))
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(url -> url.replace("{", "").replace("}", ""))
                .filter(url -> !url.isBlank())
                .toArray(String[]::new);
        Arrays.stream(publicUrls)
                .forEach(url -> log.info("Permitting public URL: {}", url));
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Allow unauthenticated access to the health endpoint and any additional public URLs
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/v1/health").permitAll();
                    if (publicUrls.length > 0) {
                        auth.requestMatchers(publicUrls).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public SessionRegistryImpl sessionRegistry() {
        return new SessionRegistryImpl();
    }
}