package net.greeta.book.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests

                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()

                        .requestMatchers(
                                "/admin/secure/**",
                                "/messages/secure/admin/message").hasRole(BOOK_MANAGER)

                        .requestMatchers(
                                "/secure/**",
                                "/reviews/secure/**",
                                "/messages/secure/**").hasAnyRole(BOOK_MANAGER, BOOK_USER)

                        .requestMatchers(
                                "/**",
                                "/reviews/**",
                                "/messages/**",
                                "/admin/**").permitAll()

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .build();
    }

    public static final String BOOK_MANAGER = "BOOK_MANAGER";
    public static final String BOOK_USER = "BOOK_USER";
}