package com.god.life.service;

import com.god.life.domain.Member;
import com.god.life.dto.alarm.request.AlarmOnOffRequest;
import com.god.life.dto.member.request.FcmUpdateRequest;
import com.god.life.dto.member.request.ModifyWhoAmIRequest;
import com.god.life.dto.member.request.SignupRequest;
import com.god.life.dto.member.response.LoginInfoResponse;
import com.god.life.dto.member.response.MemberInfoResponse;
import com.god.life.dto.member.response.TokenResponse;
import com.god.life.dto.popular.PopularMemberResponse;
import com.god.life.error.ErrorMessage;
import com.god.life.error.JwtInvalidException;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.MemberRepository;
import com.god.life.service.alarm.FcmAlarmService;
import com.god.life.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    private final FcmAlarmService alarmService;

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

        LoginInfoResponse response = LoginInfoResponse.from(findMember);

        return response;
    }

    // 토큰 재발급 ==> RTT 방식으로 리프레시 토큰도 재발급
    @Transactional
    public TokenResponse reissueToken(String providerId) {
        Member member = memberRepository.findByProviderId(providerId)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_MEMBER_MESSAGE.getErrorMessage()));

        TokenResponse response = jwtUtil.createToken(String.valueOf(member.getId()), member.getNickname());
        redisService.setValue(String.valueOf(member.getId()), response.getRefreshToken(), REFRESH_EXPIRATION_TIME);


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
        redisService.deleteValue(String.valueOf(memberId));
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawalMember(Long memberId) {
        // 멤버 조회
        Member deleteMember = memberRepository.findById(memberId).get();
        //회원 이미지, 게시판 이미지 삭제
        imageService.deleteUserImages(deleteMember);
        commentService.deleteCommentWrittenByMember(deleteMember);
        //좋아요 삭제
        godLifeScoreService.deleteUserLikedHistory(deleteMember);
        //게시판, 댓글, 갓생 점수 기록 삭제
        boardService.deleteBoardWrittenByMember(deleteMember);
        //알람 토큰 삭제
        alarmService.deleteAllAlarm(memberId);
        //멤버 삭제
        memberRepository.delete(deleteMember);
    }

    public List<String> getAllTokens(){
        return memberRepository.findAllFcmToken().orElse(new ArrayList<>());
    }

    // memberId의 유저 정보를 조회함
    public MemberInfoResponse memberInfoResponse(Member member, Long findMemberId) {
        MemberInfoResponse memberInfo = memberRepository.getMemberInfo(findMemberId);
        memberInfo.setOwner(member.getId().equals(findMemberId));

        return memberInfo;
    }

    public List<PopularMemberResponse> searchWeeklyPopularMember() {
        return memberRepository.findWeeklyPopularMember();
    }

    public List<PopularMemberResponse> searchAllTimePopularMember(){
        return memberRepository.findAllTimePopularMember();
    }

    @Transactional
    public void updateFcmToken(FcmUpdateRequest request, Member member) {
        memberRepository.updateFcm(request.getFcmToken(), member.getId());
    }

    @Transactional
    public void updateAlarmOption(Long memberId, AlarmOnOffRequest request) {
        memberRepository.updateAlarmOption(memberId, request.getOnOff());
    }

}
