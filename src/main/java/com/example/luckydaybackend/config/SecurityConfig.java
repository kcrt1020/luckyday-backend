package com.example.luckydaybackend.config;  // ✅ 패키지 경로 확인!

import com.example.luckydaybackend.auth.config.JwtFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration  // ✅ 반드시 추가!
@EnableWebSecurity  // ✅ 반드시 추가!
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
        logger.info("🚀 SecurityConfig 로드됨!");  // ✅ 로그 추가
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("🔥 Security Filter Chain 설정 중...");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()  // ✅ 회원가입 & 로그인 API는 인증 없이 허용
                        .requestMatchers("/actuator/**").permitAll()  // ✅ Actuator 엔드포인트 인증 없이 허용
                        .anyRequest().authenticated()  // 🔒 나머지 API는 인증 필요 (기존 `permitAll()` 문제 해결)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);  // ✅ JWT 필터 추가

        logger.info("✅ Security 설정 완료!");
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();  // 🔥 (임시) 유저 데이터 대신 DB 기반 UserDetailsService로 변경 필요!
    }
}
