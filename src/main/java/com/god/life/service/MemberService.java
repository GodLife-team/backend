package com.god.life.service;

import com.god.life.domain.Image;
import com.god.life.domain.Member;
import com.god.life.dto.*;
import com.god.life.error.ErrorMessage;
import com.god.life.error.JwtInvalidException;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.MemberRepository;
import com.god.life.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final BoardService boardService;
    private final CommentService commentService;
    private final GodLifeScoreService godLifeScoreService;
    private final ImageService imageService;
    private final RedisService redisService;

    @Value("${jwt.secret.expire.refresh}")
    private int REFRESH_EXPIRATION_TIME;

    @Transactional
    public TokenResponse signUp(SignupRequest signUpRequest) {
        Member member = SignupRequest.toMember(signUpRequest);
        Member savedMember = memberRepository.save(member);

        TokenResponse response = jwtUtil.createToken(String.valueOf(savedMember.getId()), savedMember.getNickname());
        //redis에 refresh token 저장
        redisService.setValue(String.valueOf(savedMember.getId()), response.getRefreshToken(),
                REFRESH_EXPIRATION_TIME);
        savedMember.updateRefreshToken(response.getRefreshToken()); // refresh 토큰 업데이트

        return response;
    }

    public Member loadByUsername(Long id) {
       return memberRepository.findById(id)
               .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_MEMBER_MESSAGE.getErrorMessage()));
    }

    @Transactional
    public TokenResponse updateRefreshToken(String jwt) {
        String id = jwtUtil.getId(jwt);
        String refreshInRedis = redisService.getValues(id);

        //만약 해커가 refresh 토큰으로 access token으로 재발급 받음
        //근데 추후 유저가 refresh 토큰으로 access token으로 재발급 시도시
        // redis에 있는 두개의 값이 다름 ==> 해킹이라 판단 후 재 로그인
        if (refreshInRedis.equals(RedisService.NO_VALUE) || !jwt.equals(refreshInRedis)) {
            redisService.deleteValue(id);
            throw new JwtInvalidException("재 로그인 해주세요.");
        }

        //Token 생성
        TokenResponse token = jwtUtil.createToken(String.valueOf(id), "nickname");
        redisService.setValue(String.valueOf(id), token.getRefreshToken(),
                REFRESH_EXPIRATION_TIME);

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

    public LoginInfoResponse getUserInfo(Long loginMember) {
        Member findMember = memberRepository.findByIdWithImage(loginMember)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_MEMBER_MESSAGE.getErrorMessage()));

        LoginInfoResponse response = LoginInfoResponse.builder()
                .age(findMember.getAge())
                .sex(findMember.getSex().getSex())
                .nickname(findMember.getNickname())
                .godLifeScore((int) findMember.getGodLifePoint())
                .backgroundImage(findMember.getBackgroundName() == null ? "" : findMember.getBackgroundName())
                .whoAmI(findMember.getWhoAmI())
                .memberId(findMember.getId())
                .fcm(findMember.getFcmToken())
                .profileImage(findMember.getProfileName() == null ? "" : findMember.getProfileName()).build();

        return response;
    }

    // 토큰 재발급 ==> RTT 방식으로 리프레시 토큰도 재발급
    @Transactional
    public TokenResponse reissueToken(String providerId) {
        Member member = memberRepository.findByProviderId(providerId)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_MEMBER_MESSAGE.getErrorMessage()));

        TokenResponse response = jwtUtil.createToken(String.valueOf(member.getId()), member.getNickname());
        redisService.setValue(String.valueOf(member.getId()), response.getRefreshToken(), REFRESH_EXPIRATION_TIME);
        //member.updateRefreshToken(response.getRefreshToken()); // refresh 토큰 업데이트


        return response;
    }

    // 자기소개 업데이트
    @Transactional
    public Boolean updateWhoAmI(Long memberId, ModifyWhoAmIRequest modifyWhoAmIRequest) {
        Member member = memberRepository.findById(memberId).get();
        member.updateWhoAmI(modifyWhoAmIRequest.getWhoAmI());
        return true;
    }

    // 로그 아웃
    @Transactional
    public void removeRefreshToken(Long memberId) {
        Member member = memberRepository.findById(memberId).get();
        member.inValidateRefreshToken();
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawalMember(Long memberId) {
        // 멤버 조회
        Member deleteMember = memberRepository.findById(memberId).get();
        //회원 이미지, 게시판 이미지 삭제
        imageService.deleteUserImages(deleteMember);
        //댓글 삭제
        commentService.deleteCommentWrittenByMember(deleteMember);
        //좋아요 삭제
        godLifeScoreService.deleteUserLikedHistory(deleteMember);
        //게시판 삭제
        boardService.deleteBoardWrittenByMember(deleteMember);
        //멤버 삭제
        memberRepository.delete(deleteMember);
    }

    // memberId의 유저 정보를 조회함
    public MemberInfoResponse memberInfoResponse(Member member, Long findMemberId) {
        MemberInfoResponse memberInfo = memberRepository.getMemberInfo(findMemberId);

//        MemberInfoResponse memberInfo = memberRepository.getMemberTotalInfo(findMemberId)
//                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_MEMBER_MESSAGE.getErrorMessage()));

        memberInfo.setOwner(member.getId().equals(findMemberId));

        return memberInfo;
    }


    public List<PopularMemberResponse> searchWeeklyPopularMember() {
        return memberRepository.findWeeklyPopularMember();
    }

    public List<PopularMemberResponse> searchAllTimePopularMember(){
        return memberRepository.findAllTimePopularMember();
    }



}
