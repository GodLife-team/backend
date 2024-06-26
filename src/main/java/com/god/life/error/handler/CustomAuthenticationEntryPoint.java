package com.god.life.error.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.god.life.dto.common.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;


// Authentication 인증 예외
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        CommonResponse<String> res = new CommonResponse<>(HttpStatus.UNAUTHORIZED, "", "로그인하지 않았습니다.");
        String responseMessage = objectMapper.writeValueAsString(res);
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(responseMessage);
        writer.close();
    }
}
