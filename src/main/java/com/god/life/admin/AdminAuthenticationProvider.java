package com.god.life.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 관리자 전용 HttpBasic 인증 로그인
 */
@Component
public class AdminAuthenticationProvider implements AuthenticationProvider {

    private final String adminId;
    private final String adminPassword;

    public AdminAuthenticationProvider(@Value("${admin.id}") String adminId,
                                       @Value("${admin.password}") String adminPassword) {
        this.adminId = adminId;
        this.adminPassword = adminPassword;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getPrincipal();

        //지정된 관리자 아이디/비번이 아니면 모두 에러
        if (!username.equals(adminId) && !password.equals(adminPassword)) {
            throw new AuthenticationCredentialsNotFoundException("인증 오류!");
        }

        return new UsernamePasswordAuthenticationToken(username, password, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
