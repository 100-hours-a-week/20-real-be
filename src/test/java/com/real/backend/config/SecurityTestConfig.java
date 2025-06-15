package com.real.backend.config;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import com.real.backend.common.util.CookieUtils;
import com.real.backend.security.JwtUtils;

@TestConfiguration
@EnableWebSecurity
public class SecurityTestConfig {

    @Bean
    public JwtUtils jwtUtils() {
        return mock(JwtUtils.class); // 필요하다면 stub 설정
    }

    @Bean
    public CookieUtils cookieUtils() {
        return mock(CookieUtils.class);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers("/auth/**", "/login").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
