package com.example.luckydaybackend.auth.config;

import com.example.luckydaybackend.auth.utils.JwtUtil;
import com.example.luckydaybackend.model.UserSession;
import com.example.luckydaybackend.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserSessionRepository userSessionRepository; // ✅ UserSessionRepository 주입

    public JwtFilter(JwtUtil jwtUtil, UserSessionRepository userSessionRepository) {
        this.jwtUtil = jwtUtil;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // 🔥 회원가입 및 로그인 API는 인증 없이 접근 가능
        if (requestPath.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 응답 반환
            response.getWriter().write("토큰이 없습니다.");
            return;
        }

        token = token.substring(7);

        // ✅ 토큰 유효성 검사
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 응답 반환
            response.getWriter().write("토큰이 만료되었거나 유효하지 않습니다.");
            return;
        }

        String email = jwtUtil.extractEmail(token);
        String tokenHash = jwtUtil.hashToken(token); // ✅ 토큰을 해싱하여 저장된 값과 비교

        // ✅ `user_sessions`에서 해당 토큰이 존재하는지 확인
        Optional<UserSession> session = userSessionRepository.findByTokenHash(tokenHash);
        if (session.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 응답 반환
            response.getWriter().write("세션이 만료되었습니다.");
            return;
        }

        System.out.println("✅ JWT 인증 완료 : " + email);
        chain.doFilter(request, response);
    }
}
