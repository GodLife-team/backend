package com.god.life.service;

import com.god.life.domain.Member;
import com.god.life.token.JwtAuthenticationToken;
import com.god.life.util.JwtUtil;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.util.List;


@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String jwt = ((JwtAuthenticationToken) authentication).getJwt();
        return processMemberAuthentication(jwt);
    }

    // JWT 이용한 DB 조회
    private Authentication processMemberAuthentication(String jwt) {
        Long memberId = Long.valueOf(jwtUtil.getId(jwt));
        Member member = memberService.loadByUsername(memberId);
        //log.info("{} Login!", member);
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(jwtUtil.getRole(jwt));
        return new JwtAuthenticationToken(member, "", authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
