package com.example.backend.util.security;

import com.example.backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "Jwt Authorization")
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl serviceImpl;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl serviceImpl) {
        this.jwtUtil = jwtUtil;
        this.serviceImpl = serviceImpl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws
            ServletException,
            IOException {
        String token = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(token)) {
            if (token.startsWith(JwtUtil.BEARER_PREFIX)) {
                token = jwtUtil.substringToken(token);
            }

            if (!jwtUtil.validateToken(token)) {
                log.error("유효성 검증에 실패했습니다.");
                return;
            }

            Claims claims = jwtUtil.getUserInfoFromToken(token);
            String email = claims.getSubject();
            log.info(email);
            try {
                setAuthentication(email);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = createAuthentication(email);
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(String email) {
        UserDetails userDetails = serviceImpl.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
