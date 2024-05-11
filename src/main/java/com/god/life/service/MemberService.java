package com.god.life.service;

import com.god.life.domain.Member;
import com.god.life.domain.ProviderType;
import com.god.life.domain.Sex;
import com.god.life.dto.SignupRequest;
import com.god.life.dto.SignupResponse;
import com.god.life.repository.MemberRepository;
import com.god.life.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Transactional
    public SignupResponse signUp(SignupRequest signUpRequest) {
        Member member = createMember(signUpRequest);
        Member savedMember = memberRepository.save(member);

        String accessToken = jwtUtil.createAccessToken(String.valueOf(savedMember.getId()), member.getNickname());
        String refreshToken = jwtUtil.createRefreshToken();

        savedMember.updateRefreshToken(refreshToken); // refresh 토큰 업데이트
        return new SignupResponse(accessToken, refreshToken);
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


}
