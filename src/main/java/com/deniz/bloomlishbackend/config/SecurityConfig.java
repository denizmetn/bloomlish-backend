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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth

                        // WebSocket → HERKESE AÇIK OLMALI
                        .requestMatchers("/socket/**").permitAll()
                        .requestMatchers("/socket").permitAll()

                        // Authentication & Register herkese açık
                        .requestMatchers("/api/auth/**").permitAll()



                        .requestMatchers(HttpMethod.POST, "/api/messages/send").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/messages/get/**").hasRole("STUDENT")

                        //chat ve admin kısmı için liste erişimi
                        .requestMatchers("/api/users/all").hasRole("ADMIN")
                        .requestMatchers("/api/users/students").hasAnyRole("STUDENT", "ADMIN")


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


                        .requestMatchers(HttpMethod.GET, "/api/quiz/start").authenticated()
                        .requestMatchers("api/results/**").authenticated()
                        .requestMatchers("api/quiz/start").authenticated()
                        .requestMatchers("api/quiz/submit").authenticated()
                        .requestMatchers("/audio/**").permitAll()
                        .requestMatchers("api/results/summary/me").authenticated()


                        .requestMatchers(HttpMethod.POST, "/api/lessons/create").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/lessons/**").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/lessons/**").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll()


                        //ödeme
                        .requestMatchers(HttpMethod.POST, "/api/payments/lesson-callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/lesson/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-lessons").authenticated()
                        // ÖDEME (abonelik / billing)
                        .requestMatchers(HttpMethod.POST, "/api/billing/checkout").authenticated()
                        .requestMatchers("/api/billing/checkout-callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/billing/start-trial").authenticated()



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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Frontend (Vite) için CORS
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        //  İyzico callback'leri için full serbest CORS
        CorsConfiguration callbackConfig = new CorsConfiguration();
        callbackConfig.setAllowedOriginPatterns(List.of("*"));          // Hangi domainten gelirse gelsin
        callbackConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        callbackConfig.setAllowedHeaders(List.of("*"));
        callbackConfig.setAllowCredentials(false);                      // server-to-server, cookie yok

        //  WebSocket için CORS
        CorsConfiguration wsConfig = new CorsConfiguration();
        wsConfig.addAllowedOriginPattern("*");
        wsConfig.addAllowedMethod("*");
        wsConfig.addAllowedHeader("*");
        wsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        //  WEBSOCKET
        source.registerCorsConfiguration("/socket/**", wsConfig);
        source.registerCorsConfiguration("/socket", wsConfig);

        // DERS ÖDEMESİ CALLBACK
        source.registerCorsConfiguration("/api/payments/lesson-callback", callbackConfig);

        // ABONELİK (IYZICO) CALLBACK – BURASI EKSİKTİ
        source.registerCorsConfiguration("/api/billing/checkout-callback", callbackConfig);
        // istersen şöyle de yazabilirsin:
        // source.registerCorsConfiguration("/api/billing/checkout-callback/**", callbackConfig);

        // GENEL API → FRONTEND
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}