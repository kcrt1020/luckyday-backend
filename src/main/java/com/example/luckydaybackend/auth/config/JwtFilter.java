package com.example.luckydaybackend.auth.config;

import com.example.luckydaybackend.auth.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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
        if (token != null && token.startsWith("Bearer ")) {
            token=token.substring(7);
            if(jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                System.out.println("JWT 인증 완료 : " + username);
            }
        }

        chain.doFilter(request, response);
    }
}
