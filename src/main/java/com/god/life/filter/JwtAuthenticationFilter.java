package com.god.life.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.god.life.dto.common.CommonResponse;
import com.god.life.token.JwtAuthenticationToken;
import com.god.life.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

import static com.god.life.util.JwtUtil.parseJwt;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final AuthenticationManager manager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().contains("reissue") || request.getRequestURI().contains("check")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorize Access Token 검사
        String authorizeHeader;
        if ((authorizeHeader = request.getHeader(JwtUtil.AUTHORIZE_HEADER)) != null) {
            String jwt = parseJwt(authorizeHeader);
            if(jwt == null) {
                setErrorResponse(response, "Access Token이 없습니다.");
                return;
            }
            try {
                Authentication jwtToken = new JwtAuthenticationToken(jwt);
                Authentication authenticateToken = manager.authenticate(jwtToken);
                SecurityContextHolder.getContext().setAuthentication(authenticateToken);
            } catch (ExpiredJwtException exception) { // Access Token 만료
                setErrorResponse(response, "토큰이 만료되었습니다.");
                return;
            } catch (Exception exception) { // 그 외의 Token Error
                setErrorResponse(response, "잘못된 토큰입니다.");
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        CommonResponse<String> res = new CommonResponse<>(HttpStatus.UNAUTHORIZED, "", errorMessage);
        String responseMessage = objectMapper.writeValueAsString(res);
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(responseMessage);
        writer.close();
        SecurityContextHolder.clearContext();
    }

}
