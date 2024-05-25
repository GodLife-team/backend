package com.god.life.token;

import com.god.life.domain.Member;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String jwt;
    private Member principal;
    private Object credentials;


    // 인증 전
    public JwtAuthenticationToken(String jwt) {
        super(null);
        this.jwt = jwt;
        super.setAuthenticated(false);
    }

    // 인증 후
    public JwtAuthenticationToken(Member principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }


    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getJwt() {
        return jwt;
    }
}
