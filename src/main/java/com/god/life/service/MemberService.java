package com.god.life.service;

import com.god.life.domain.Member;
import com.god.life.domain.ProviderType;
import com.god.life.domain.Sex;
import com.god.life.dto.SignupRequest;
import com.god.life.dto.TokenResponse;
import com.god.life.exception.JwtInvalidException;
import com.god.life.repository.MemberRepository;
import com.god.life.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Transactional
    public TokenResponse signUp(SignupRequest signUpRequest) {
        Member member = createMember(signUpRequest);
        Member savedMember = memberRepository.save(member);

        TokenResponse response = jwtUtil.createToken(String.valueOf(savedMember.getId()), savedMember.getNickname());
        savedMember.updateRefreshToken(response.getRefreshToken()); // refresh 토큰 업데이트

        return response;
    }

    private Member createMember(SignupRequest signUpRequest) {
        return Member.builder()
                .age(signUpRequest.getAge())
                .sex(Sex.findSex(signUpRequest.getSex()))
                .email(signUpRequest.getEmail())
                .godLifePoint(0L)
                .providerName(ProviderType.KAKAO)
                .providerToken(signUpRequest.getProviderToken())
                .nickname(signUpRequest.getNickname()).build();
    }

    @Transactional(readOnly = true)
    public Member loadByUsername(Long id) {
       return memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다.."));
    }

    @Transactional
    public TokenResponse updateRefreshToken(String jwt) {
        // jwt 조회
        Optional<Member> findMember = memberRepository.findByRefreshToken(jwt);
        if (findMember.isEmpty()) {
            throw new JwtInvalidException("잘못된 토큰입니다.");
        }
        Member member = findMember.get();
        TokenResponse token = jwtUtil.createToken(String.valueOf(member.getId()), member.getNickname());
        member.updateRefreshToken(token.getRefreshToken());
        return token;
    }

    public boolean checkDuplicateNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean checkDuplicateEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

}
