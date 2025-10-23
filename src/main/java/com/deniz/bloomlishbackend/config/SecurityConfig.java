package com.deniz.bloomlishbackend.config;

import com.deniz.bloomlishbackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // Authentication & Register herkese açık
                        .requestMatchers("/api/auth/**").permitAll()

                        // Postları listeleme & görüntüleme herkese açık
                        .requestMatchers(HttpMethod.GET, "/api/posts/get-all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/get/**").permitAll()

                        // Yorumları listeleme & görüntüleme herkese açık
                        .requestMatchers(HttpMethod.GET, "/api/comments/get-all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/get/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/get/post/**").permitAll()

                        // Post oluşturma, beğenme, yorum ekleme → sadece login
                        .requestMatchers(HttpMethod.POST, "/api/posts/create").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/posts/*/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/comment").authenticated()

                        // Yorum silme & güncelleme → sadece login
                        .requestMatchers(HttpMethod.DELETE,"/api/comments/delete/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/api/comments/update/**").authenticated()

                        // Günlük işlemleri → sadece login
                        .requestMatchers("/api/notes/**").authenticated()

                        // Diğer her şey login ister
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
