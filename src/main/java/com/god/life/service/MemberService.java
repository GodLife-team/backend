package com.god.life.service;

import com.god.life.domain.Image;
import com.god.life.domain.Member;
import com.god.life.domain.ProviderType;
import com.god.life.domain.Sex;
import com.god.life.dto.LoginInfoResponse;
import com.god.life.dto.SignupRequest;
import com.god.life.dto.TokenResponse;
import com.god.life.exception.JwtInvalidException;
import com.god.life.repository.MemberRepository;
import com.god.life.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Transactional
    public TokenResponse signUp(SignupRequest signUpRequest) {
        Member member = SignupRequest.toMember(signUpRequest);
        Member savedMember = memberRepository.save(member);

        TokenResponse response = jwtUtil.createToken(String.valueOf(savedMember.getId()), savedMember.getNickname());
        savedMember.updateRefreshToken(response.getRefreshToken()); // refresh 토큰 업데이트

        return response;
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

    public boolean checkAlreadySignup(String id) {
        return memberRepository.existsByProviderId(id);
    }

    // jwt에서 DB 정보 불러올때 사진도 같이?? 아니면 유저정보가 필요할 때만???
    // 일단 DB에서 조회하는 것오르 하자.
    public LoginInfoResponse getUserInfo(Member loginMember) {
        Member findMember = memberRepository.findByIdWithImage(loginMember.getId());

        LoginInfoResponse response = LoginInfoResponse.builder()
                .age(findMember.getAge())
                .sex(findMember.getSex().getSex())
                .nickname(findMember.getNickname())
                .godLifeScore((int) findMember.getGodLifePoint())
                .backgroundImage("")
                .profileImage("").build();

        List<Image> memberImages = findMember.getImages();
        for (Image image : memberImages) {
            if (image.getServerName().equals("profile")) {
                response.setProfileImage(image.getServerName());
            } else if (image.getServerName().equals("background")) {
                response.setBackgroundImage(image.getServerName());
            }
        }

        return response;
    }
}
