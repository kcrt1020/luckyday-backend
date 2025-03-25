package com.example.luckydaybackend.auth;

import com.example.luckydaybackend.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // ✅ UserDetailsService 주입 (유저 정보 가져오기)

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("토큰이 없습니다.");
            return;
        }

        token = token.substring(7);

        // ✅ 토큰 유효성 검사
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("토큰이 만료되었거나 유효하지 않습니다.");
            return;
        }

        String email = jwtUtil.extractEmail(token);
//        System.out.println("✅ JWT 인증 완료 : " + email);

        // ✅ UserDetails 가져오기
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//        System.out.println("🔍 UserDetails 로드 완료: " + userDetails.getUsername());

        // ✅ Authentication 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // ✅ SecurityContextHolder에 인증 정보 저장
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
//        System.out.println("✅ SecurityContextHolder에 인증 정보 설정 완료!");

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/uploads/"); // ✅ 이미지 요청 필터링 제외
    }
}
