package com.god.life.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.god.life.dto.CommonResponse;
import com.god.life.token.JwtAuthenticationToken;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private static final String AUTHORIZE_HEADER = "Authorization";
    private static final String AUTHORIZE_HEADER_PREFIX = "Bearer ";

    private final AuthenticationManager manager;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Authorize Access Token 검사
        String authorizeHeader;
        if ((authorizeHeader = request.getHeader(AUTHORIZE_HEADER)) != null) {
            String jwt = parseJwt(authorizeHeader);
            if(jwt == null) {
                setErrorResponse(response, "Acecss Token이 없습니다.");
                return;
            }

            try {
                Authentication jwtToken = new JwtAuthenticationToken(jwt);
                Authentication authenticateToken = manager.authenticate(jwtToken);
                SecurityContextHolder.getContext().setAuthentication(authenticateToken);
            } catch (ExpiredJwtException exception) { // Access Token 만료
                setErrorResponse(response, "Access Token가 만료되었습니다.");
                return;
            } catch (Exception exception) { // 그 외의 Token Error
                setErrorResponse(response, "잘못된 Access Token 입니다. ");
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

    private String parseJwt(String authorizeHeader) {
        if (StringUtils.hasText(authorizeHeader) &&
                authorizeHeader.startsWith(AUTHORIZE_HEADER_PREFIX)) {
            return authorizeHeader.substring(AUTHORIZE_HEADER_PREFIX.length());
        }

        return null;
    }
}
