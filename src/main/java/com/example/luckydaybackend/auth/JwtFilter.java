package com.example.luckydaybackend.auth;

import com.example.luckydaybackend.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        // ✅ 토큰 유효성 검사 (user_sessions 조회 X)
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 응답 반환
            response.getWriter().write("토큰이 만료되었거나 유효하지 않습니다.");
            return;
        }

        String email = jwtUtil.extractEmail(token);
        System.out.println("✅ JWT 인증 완료 : " + email);

        // 🔥 accessToken은 DB 조회 필요 없음! 그대로 인증 처리 진행
        chain.doFilter(request, response);
    }
}
