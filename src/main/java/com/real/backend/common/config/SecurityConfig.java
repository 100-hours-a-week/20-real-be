package com.real.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.real.backend.security.JwtUtils;
import com.real.backend.security.filter.CustomLoginFilter;
import com.real.backend.security.filter.JwtFilter;
import com.real.backend.common.util.CookieUtils;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Bean에 AuthenticationManager 등록
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable); // form 로그인 방식
        http.httpBasic(AbstractHttpConfigurer::disable); // http basic 인증 방식
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource)); // cors 설정


        // 경로별 인가 작업
        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/v1/oauth/**", "/api/v1/news").permitAll()
                .requestMatchers("/api/v1/auth/**", "api/v2/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/healthz").permitAll()    //서버 헬스 체크용 API, Security Filter 우회
                .requestMatchers("/monitoring-prod/health","/monitoring-prod/info","/monitoring-prod/prometheus").permitAll() // 모니터링 API, Security Filter 우회
                .requestMatchers("/monitoring-dev/health", "/monitoring-dev/info", "/monitoring-dev/prometheus").permitAll()
                .requestMatchers("/api/v1/users/enroll", "/api/v1/invited-user").permitAll()
                .anyRequest().authenticated()  // 나머지는 필터 통과
            );

        http
            .addFilterBefore(new JwtFilter(jwtUtils, cookieUtils), CustomLoginFilter.class)
            .addFilterAt(new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtils), UsernamePasswordAuthenticationFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
