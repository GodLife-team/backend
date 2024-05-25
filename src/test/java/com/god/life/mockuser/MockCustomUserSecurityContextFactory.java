package com.god.life.mockuser;

import com.god.life.domain.Member;
import com.god.life.domain.ProviderType;
import com.god.life.domain.Sex;
import com.god.life.token.JwtAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class MockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<MockUserCustom> {

    @Override
    public SecurityContext createSecurityContext(MockUserCustom annotation) {

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        Member member = Member.builder()
                .id(1L)
                .nickname("TESTER")
                .email("TEST@NAVER.COM")
                .godLifePoint(0)
                .whoAmI("Hello WORLD!")
                .providerId("1234")
                .providerName(ProviderType.KAKAO)
                .age(10)
                .sex(Sex.MALE)
                .build();

        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(annotation.role());

        JwtAuthenticationToken token = new JwtAuthenticationToken(member, "", authorities);
        securityContext.setAuthentication(token);
        return securityContext;
    }


}
